package DevFlow.OpenCloset_Back.Chat;

import DevFlow.OpenCloset_Back.Chat.Security.ChatChannelInterceptor;
import DevFlow.OpenCloset_Back.Chat.Service.ChatAccessService;
import DevFlow.OpenCloset_Back.Login.Jwt_Util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.nio.charset.StandardCharsets;
import java.security.Principal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatChannelInterceptorTest {
    private final ChatAccessService access = mock(ChatAccessService.class);
    private final UserDetailsService users = mock(UserDetailsService.class);
    @SuppressWarnings("unchecked")
    private final ObjectProvider<MessageChannel> outbound = mock(ObjectProvider.class);
    private final ChatChannelInterceptor interceptor = new ChatChannelInterceptor(users, access, new ObjectMapper(), outbound);
    private String token;

    @BeforeEach
    void setUp() {
        token = JwtUtil.generateToken("wearer@example.test", "ROLE_USER");
        lenient().when(users.loadUserByUsername("wearer@example.test"))
                .thenReturn(User.withUsername("wearer@example.test").password("unused").roles("USER").build());
    }

    @Test
    void connectRequiresValidBearerToken() {
        var headers = StompHeaderAccessor.create(StompCommand.CONNECT);
        Message<?> missing = frame(headers, "");
        assertThatThrownBy(() -> interceptor.preSend(missing, null))
                .isInstanceOf(BadCredentialsException.class);
        headers = StompHeaderAccessor.create(StompCommand.CONNECT);
        headers.setNativeHeader("Authorization", "Bearer invalid");
        Message<?> invalid = frame(headers, "");
        assertThatThrownBy(() -> interceptor.preSend(invalid, null)).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void validConnectBindsAuthenticatedPrincipal() {
        Principal principal = connect();
        assertThat(principal.getName()).isEqualTo("wearer@example.test");
        assertThat(((Authentication) principal).isAuthenticated()).isTrue();
    }

    @Test
    void participantCanSubscribeAndSendButCannotPublishDirectlyToBroker() {
        Principal principal = connect();
        var subscribe = authenticated(StompCommand.SUBSCRIBE, "/topic/chat/7", principal);
        interceptor.preSend(frame(subscribe, ""), null);
        var send = authenticated(StompCommand.SEND, "/app/chat/send", principal);
        interceptor.preSend(frame(send, "{\"roomId\":7,\"senderId\":999,\"message\":\"hello\"}"), null);
        verify(access, times(2)).requireParticipant(7L, "wearer@example.test");

        var direct = authenticated(StompCommand.SEND, "/topic/chat/7", principal);
        assertThatThrownBy(() -> interceptor.preSend(frame(direct, "{}"), null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void outsiderAndWildcardSubscriptionsAreRejected() {
        Principal principal = connect();
        when(access.requireParticipant(7L, "wearer@example.test")).thenThrow(new AccessDeniedException("forbidden"));
        var outside = authenticated(StompCommand.SUBSCRIBE, "/topic/chat/7", principal);
        assertThatThrownBy(() -> interceptor.preSend(frame(outside, ""), null)).isInstanceOf(AccessDeniedException.class);
        for (String topic : new String[]{"/topic/chat/*", "/topic/**", "/queue/chat/errors", "/topic/chat/0"}) {
            var wildcard = authenticated(StompCommand.SUBSCRIBE, topic, principal);
            assertThatThrownBy(() -> interceptor.preSend(frame(wildcard, ""), null))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    void rejectedSocketFrameGetsAnErrorEvenWithOrderedInboundDelivery() {
        MessageChannel channel = mock(MessageChannel.class);
        when(outbound.getObject()).thenReturn(channel);
        var headers = authenticated(StompCommand.SUBSCRIBE, "/topic/**", connect());
        headers.setSessionId("rejected-session");
        assertThatThrownBy(() -> interceptor.preSend(frame(headers, ""), null)).isInstanceOf(AccessDeniedException.class);
        var sent = org.mockito.ArgumentCaptor.forClass(Message.class);
        verify(channel).send(sent.capture());
        var error = StompHeaderAccessor.wrap(sent.getValue());
        assertThat(error.getCommand()).isEqualTo(StompCommand.ERROR);
        assertThat(error.getSessionId()).isEqualTo("rejected-session");
        assertThat(error.getMessage()).isEqualTo("FORBIDDEN");
    }

    @Test
    void laterFramesRecheckTheSessionToken() {
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                "wearer@example.test", "invalid-or-expired-token", java.util.List.of());
        var headers = authenticated(StompCommand.SUBSCRIBE, "/topic/chat/7", authentication);
        assertThatThrownBy(() -> interceptor.preSend(frame(headers, ""), null))
                .isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(access);
    }

    @Test
    void unauthenticatedFramesAndMalformedRoomIdsAreRejected() {
        var anonymous = StompHeaderAccessor.create(StompCommand.SEND);
        anonymous.setDestination("/app/chat/send");
        assertThatThrownBy(() -> interceptor.preSend(frame(anonymous, "{}"), null)).isInstanceOf(BadCredentialsException.class);
        Principal principal = connect();
        for (String json : new String[]{"{}", "null", "not-json", "{\"roomId\":1.5}", "{\"roomId\":-1}"}) {
            var send = authenticated(StompCommand.SEND, "/app/chat/send", principal);
            assertThatThrownBy(() -> interceptor.preSend(frame(send, json), null)).isInstanceOf(IllegalArgumentException.class);
        }
        verifyNoInteractions(access);
    }

    private Principal connect() {
        var headers = StompHeaderAccessor.create(StompCommand.CONNECT);
        headers.setNativeHeader("Authorization", "Bearer " + token);
        interceptor.preSend(frame(headers, ""), null);
        return headers.getUser();
    }

    private StompHeaderAccessor authenticated(StompCommand command, String destination, Principal principal) {
        var headers = StompHeaderAccessor.create(command);
        headers.setDestination(destination);
        headers.setUser(principal);
        return headers;
    }

    private Message<byte[]> frame(StompHeaderAccessor headers, String json) {
        headers.setLeaveMutable(true);
        return MessageBuilder.createMessage(json.getBytes(StandardCharsets.UTF_8), headers.getMessageHeaders());
    }
}
