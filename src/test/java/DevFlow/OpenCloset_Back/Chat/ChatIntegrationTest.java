package DevFlow.OpenCloset_Back.Chat;

import DevFlow.OpenCloset_Back.Board.Repository.BoardRepository;
import DevFlow.OpenCloset_Back.Board.entity.Board;
import DevFlow.OpenCloset_Back.Chat.ChatMessage.Repository.ChatMessageRepository;
import DevFlow.OpenCloset_Back.Chat.ChatRoom.Repository.ChatRoomRepository;
import DevFlow.OpenCloset_Back.Chat.Controller.ChatMessageHandler;
import DevFlow.OpenCloset_Back.Chat.Service.ChatService;
import DevFlow.OpenCloset_Back.Chat.dto.res.ChatMessageResponseDto;
import DevFlow.OpenCloset_Back.Chat.dto.res.ChatRoomResponseDto;
import DevFlow.OpenCloset_Back.Login.Jwt_Util.JwtUtil;
import DevFlow.OpenCloset_Back.User.User_Repository.UserRepository;
import DevFlow.OpenCloset_Back.User.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.messaging.*;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

// Explicit loopback addresses prevent these fixture tests from writing to a remote DB/Redis.
@ActiveProfiles("local")
@Import(ChatIntegrationTest.SubscriptionTestConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:mariadb://127.0.0.1:${DB_PORT:13306}/OpenCloset?sslMode=disable",
        "spring.data.redis.host=127.0.0.1"
})
class ChatIntegrationTest {
    @LocalServerPort int port;
    @Autowired UserRepository users;
    @Autowired BoardRepository boards;
    @Autowired ChatRoomRepository rooms;
    @Autowired ChatMessageRepository messages;
    @Autowired ChatService chat;
    @Autowired ObjectMapper mapper;
    @Autowired TestRestTemplate rest;
    @Autowired PlatformTransactionManager transactions;
    @Autowired SubscriptionBarrier subscriptionBarrier;
    private final List<WebSocketStompClient> clients = new ArrayList<>();
    private final List<StompSession> sessions = new ArrayList<>();
    private User seller;
    private User wearer;
    private User outsider;
    private Board board;
    private Long roomId;
    static class SubscriptionBarrier implements ExecutorChannelInterceptor {
        final Map<String, CompletableFuture<Void>> subscriptions = new ConcurrentHashMap<>();

        @Override
        public void afterMessageHandled(Message<?> message, MessageChannel channel, MessageHandler handler, Exception ex) {
            var headers = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            if (handler instanceof SimpleBrokerMessageHandler && headers != null
                    && headers.getCommand() == StompCommand.SUBSCRIBE) {
                var ready = subscriptions.get(headers.getSubscriptionId());
                if (ready != null) {
                    if (ex == null) ready.complete(null);
                    else ready.completeExceptionally(ex);
                }
            }
        }
    }

    @TestConfiguration
    static class SubscriptionTestConfiguration implements WebSocketMessageBrokerConfigurer {
        @Bean
        SubscriptionBarrier subscriptionBarrier() { return new SubscriptionBarrier(); }

        @Override
        public void configureClientInboundChannel(ChannelRegistration registration) {
            registration.interceptors(subscriptionBarrier());
        }
    }

    @BeforeEach
    void setUp() {
        subscriptionBarrier.subscriptions.clear();
        String suffix = UUID.randomUUID().toString();
        seller = createUser("seller", suffix);
        wearer = createUser("wearer", suffix);
        outsider = createUser("outsider", suffix);
        board = new Board();
        board.setSeller(seller);
        board.setTitle("chat-test-" + suffix);
        board.setDescription("local integration fixture");
        board.setSize("M");
        board.setSex("공용");
        board.setLatitude(37.0);
        board.setLongitude(127.0);
        board.setPrice(1000L);
        board.setStartDate(LocalDate.now());
        board.setEndDate(LocalDate.now().plusDays(1));
        board.setCategory("top");
        board.setStatus("대여가능");
        board = boards.saveAndFlush(board);
        roomId = chat.createRoom(board.getId(), wearer).getRoomId();
    }

    @AfterEach
    void cleanUp() {
        sessions.forEach(session -> { if (session.isConnected()) session.disconnect(); });
        clients.forEach(WebSocketStompClient::stop);
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            if (board != null && board.getId() != null) {
                var fixtureRooms = rooms.findByBoardId(board.getId());
                messages.deleteByChatRoomIn(fixtureRooms);
                rooms.deleteAll(fixtureRooms);
                rooms.flush();
                boards.deleteById(board.getId());
                boards.flush();
            }
            for (User user : new User[]{seller, wearer, outsider}) {
                if (user != null) users.deleteById(user.getId());
            }
        });
    }

    @Test
    void twoUsersReceiveExactlyOneMessageWithAuthenticatedSenderAndReadStatePersists() throws Exception {
        Connected owner = connect(seller, false);
        Connected renter = connect(wearer, false);
        var ownerMessages = subscribe(owner.session(), topic(), ChatMessageResponseDto.class);
        var renterMessages = subscribe(renter.session(), topic(), ChatMessageResponseDto.class);
        renter.session().send("/app/chat/send", Map.of("roomId", roomId, "senderId", seller.getId(), "message", "안녕하세요 😀"));

        ChatMessageResponseDto received = ownerMessages.poll(5, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.getSenderId()).isEqualTo(wearer.getId());
        assertThat(received.getMessage()).isEqualTo("안녕하세요 😀");
        assertThat(received.getSentAt()).isNotNull();
        assertThat(renterMessages.poll(5, TimeUnit.SECONDS)).extracting(ChatMessageResponseDto::getMessageId)
                .isEqualTo(received.getMessageId());
        assertThat(ownerMessages.poll(500, TimeUnit.MILLISECONDS)).isNull();
        assertThat(renterMessages.poll(500, TimeUnit.MILLISECONDS)).isNull();
        assertThat(chat.getMessages(roomId, wearer)).hasSize(1);
        assertThat(myRoom(seller).getUnreadCount()).isEqualTo(1);
        assertThat(myRoom(wearer).getUnreadCount()).isZero();
        chat.markAsRead(roomId, seller);
        assertThat(myRoom(seller).getUnreadCount()).isZero();
        assertThat(chat.getMessages(roomId, seller).get(0).getIsRead()).isTrue();
    }

    @Test
    void sockJsConnectionAlsoUsesJwtAndReceivesMessages() throws Exception {
        Connected renter = connect(wearer, true);
        var received = subscribe(renter.session(), topic(), ChatMessageResponseDto.class);
        renter.session().send("/app/chat/send", Map.of("roomId", roomId, "message", "SockJS 테스트"));
        assertThat(received.poll(5, TimeUnit.SECONDS)).extracting(ChatMessageResponseDto::getMessage)
                .isEqualTo("SockJS 테스트");
    }

    @Test
    void consecutiveMessagesFromOneSessionKeepTheirOrderAndSupport1000Characters() throws Exception {
        Connected renter = connect(wearer, false);
        var received = subscribe(renter.session(), topic(), ChatMessageResponseDto.class);
        List<String> contents = List.of("첫 번째", "두 번째", "가".repeat(1000));
        for (String content : contents) {
            renter.session().send("/app/chat/send", Map.of("roomId", roomId, "message", content));
        }
        for (String expected : contents) {
            assertThat(received.poll(5, TimeUnit.SECONDS)).extracting(ChatMessageResponseDto::getMessage)
                    .isEqualTo(expected);
        }
        assertThat(chat.getMessages(roomId, wearer)).extracting(ChatMessageResponseDto::getMessage)
                .containsExactlyElementsOf(contents);
    }

    @Test
    void httpHistoryRejectsAnonymousAndNonParticipant() {
        String path = "/chat/room/" + roomId + "/messages";
        assertThat(rest.getForEntity(path, String.class).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(JwtUtil.generateToken(outsider.getEmail(), "ROLE_USER"));
        assertThat(rest.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), String.class).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        headers.setBearerAuth(JwtUtil.generateToken(wearer.getEmail(), "ROLE_USER"));
        assertThat(rest.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void outsiderCannotSubscribeAndParticipantCannotBypassPersistenceThroughBroker() throws Exception {
        Connected stranger = connect(outsider, false);
        stranger.session().subscribe(topic(), new QueueHandler<>(ChatMessageResponseDto.class, new LinkedBlockingQueue<>()));
        assertThat(stranger.errors().poll(5, TimeUnit.SECONDS)).isNotNull();
        Connected renter = connect(wearer, false);
        renter.session().send(topic(), Map.of("message", "bypass attempt"));
        assertThat(renter.errors().poll(5, TimeUnit.SECONDS)).isNotNull();
        assertThat(chat.getMessages(roomId, wearer)).isEmpty();
    }

    @Test
    void blankMessageReturnsPrivateErrorWithoutSaving() throws Exception {
        Connected renter = connect(wearer, false);
        var errors = subscribe(renter.session(), "/user/queue/chat/errors", ChatMessageHandler.ChatError.class);
        renter.session().send("/app/chat/send", Map.of("roomId", roomId, "message", "   "));
        assertThat(errors.poll(5, TimeUnit.SECONDS)).extracting(ChatMessageHandler.ChatError::code)
                .isEqualTo("INVALID_MESSAGE");
        assertThat(chat.getMessages(roomId, wearer)).isEmpty();
    }

    @Test
    void rolledBackMessageIsNeitherPersistedNorBroadcast() throws Exception {
        Connected renter = connect(wearer, false);
        var received = subscribe(renter.session(), topic(), ChatMessageResponseDto.class);
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            chat.sendMessage(roomId, wearer.getEmail(), "rollback");
            status.setRollbackOnly();
        });
        assertThat(chat.getMessages(roomId, wearer)).isEmpty();
        assertThat(received.poll(750, TimeUnit.MILLISECONDS)).isNull();
    }

    @Test
    void concurrentRoomRequestsReturnOneRoomAndOnlyChatParticipantCanBeConfirmed() throws Exception {
        rooms.deleteById(roomId);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Callable<Long> create = () -> { start.await(); return chat.createRoom(board.getId(), wearer).getRoomId(); };
            Future<Long> first = executor.submit(create);
            Future<Long> second = executor.submit(create);
            start.countDown();
            roomId = first.get(10, TimeUnit.SECONDS);
            assertThat(second.get(10, TimeUnit.SECONDS)).isEqualTo(roomId);
            assertThat(rooms.findByBoardId(board.getId())).hasSize(1);
        } finally {
            executor.shutdownNow();
        }
        assertThatThrownBy(() -> chat.confirmDeal(board.getId(), outsider.getId(), seller))
                .isInstanceOf(IllegalArgumentException.class);
        chat.confirmDeal(board.getId(), wearer.getId(), seller);
        var confirmed = boards.findById(board.getId()).orElseThrow();
        assertThat(confirmed.getStatus()).isEqualTo("예약중");
        assertThat(confirmed.getBuyer().getId()).isEqualTo(wearer.getId());
    }

    private User createUser(String role, String suffix) {
        User user = new User();
        user.setEmail(role + "-" + suffix + "@example.test");
        user.setNickname(role + "-" + suffix);
        user.setPassword("unused-test-password");
        user.setAddress("local-test");
        return users.saveAndFlush(user);
    }

    private ChatRoomResponseDto myRoom(User user) {
        return chat.getMyRooms(user).stream().filter(room -> room.getRoomId().equals(roomId)).findFirst().orElseThrow();
    }

    private String topic() { return "/topic/chat/" + roomId; }

    private Connected connect(User user, boolean sockJs) throws Exception {
        var socketClient = sockJs
                ? new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient())))
                : new StandardWebSocketClient();
        var client = new WebSocketStompClient(socketClient);
        var converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(mapper);
        client.setMessageConverter(converter);
        client.setDefaultHeartbeat(new long[]{0, 0});
        clients.add(client);
        var errors = new LinkedBlockingQueue<String>();
        var headers = new StompHeaders();
        headers.set("Authorization", "Bearer " + JwtUtil.generateToken(user.getEmail(), "ROLE_USER"));
        var session = client.connectAsync("ws://127.0.0.1:" + port + "/ws/chat", new WebSocketHttpHeaders(), headers,
                new StompSessionHandlerAdapter() {
                    @Override public Type getPayloadType(StompHeaders frameHeaders) { return byte[].class; }
                    @Override public void handleFrame(StompHeaders frameHeaders, Object payload) {
                        errors.add("STOMP ERROR: " + frameHeaders.getFirst("message"));
                    }
                    @Override public void handleException(StompSession s, StompCommand command, StompHeaders h,
                                                          byte[] payload, Throwable exception) {
                        errors.add(exception.toString());
                    }
                    @Override public void handleTransportError(StompSession s, Throwable exception) {
                        errors.add(exception.toString());
                    }
                }).get(10, TimeUnit.SECONDS);
        sessions.add(session);
        return new Connected(session, errors);
    }

    private <T> BlockingQueue<T> subscribe(StompSession session, String destination, Class<T> type) throws Exception {
        var ready = new CompletableFuture<Void>();
        var headers = new StompHeaders();
        headers.setId(UUID.randomUUID().toString());
        headers.setDestination(destination);
        subscriptionBarrier.subscriptions.put(headers.getId(), ready);
        BlockingQueue<T> received = new LinkedBlockingQueue<>();
        session.subscribe(headers, new QueueHandler<>(type, received));
        ready.get(5, TimeUnit.SECONDS);
        return received;
    }

    private record Connected(StompSession session, BlockingQueue<String> errors) { }
    private record QueueHandler<T>(Class<T> type, BlockingQueue<T> queue) implements StompFrameHandler {
        @Override public Type getPayloadType(StompHeaders headers) { return type; }
        @Override public void handleFrame(StompHeaders headers, Object payload) { queue.add(type.cast(payload)); }
    }
}
