package DevFlow.OpenCloset_Back.Chat;

import DevFlow.OpenCloset_Back.Board.Repository.BoardRepository;
import DevFlow.OpenCloset_Back.Board.entity.Board;
import DevFlow.OpenCloset_Back.Chat.ChatMessage.Entity.ChatMessage;
import DevFlow.OpenCloset_Back.Chat.ChatMessage.Repository.ChatMessageRepository;
import DevFlow.OpenCloset_Back.Chat.ChatRoom.Entity.ChatRoom;
import DevFlow.OpenCloset_Back.Chat.ChatRoom.Repository.ChatRoomRepository;
import DevFlow.OpenCloset_Back.Chat.Controller.ChatMessageHandler;
import DevFlow.OpenCloset_Back.Chat.Event.ChatMessageSentEvent;
import DevFlow.OpenCloset_Back.Chat.Service.ChatAccessService;
import DevFlow.OpenCloset_Back.Chat.Service.ChatService;
import DevFlow.OpenCloset_Back.Chat.dto.req.ChatSendRequestDto;
import DevFlow.OpenCloset_Back.User.User_Repository.UserRepository;
import DevFlow.OpenCloset_Back.User.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChatServiceTest {
    private final ChatRoomRepository rooms = mock(ChatRoomRepository.class);
    private final ChatMessageRepository messages = mock(ChatMessageRepository.class);
    private final BoardRepository boards = mock(BoardRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final ChatAccessService access = mock(ChatAccessService.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final ChatService service = new ChatService(rooms, messages, boards, users, access, events);

    @Test
    @SuppressWarnings("deprecation")
    void handlerIgnoresForgedSenderIdAndUsesAuthenticatedIdentity() {
        ChatService delegate = mock(ChatService.class);
        ChatSendRequestDto request = new ChatSendRequestDto();
        request.setRoomId(7L);
        request.setSenderId(999L);
        request.setMessage("hello");
        new ChatMessageHandler(delegate).handleMessage(request, () -> "wearer@example.test");
        verify(delegate).sendMessage(7L, "wearer@example.test", "hello");
    }

    @Test
    void savedMessageUsesParticipantAndPublishesCommitEvent() {
        User seller = user(1L, "seller@example.test");
        User wearer = user(2L, "wearer@example.test");
        ChatRoom room = new ChatRoom(7L, new Board(), seller, wearer, LocalDateTime.now());
        when(access.requireParticipant(7L, wearer.getEmail())).thenReturn(room);
        when(messages.save(any())).thenAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            message.setId(10L);
            return message;
        });
        var response = service.sendMessage(7L, wearer.getEmail(), "안녕하세요 😀");
        assertThat(response.getSenderId()).isEqualTo(2L);
        assertThat(response.getMessageId()).isEqualTo(10L);
        assertThat(response.getIsRead()).isFalse();
        verify(events).publishEvent(new ChatMessageSentEvent(7L, response));
    }

    @Test
    void invalidContentOrOutsiderDoesNotSaveOrPublish() {
        for (String content : new String[]{null, "", " \n\t", "a".repeat(1001)}) {
            assertThatThrownBy(() -> service.sendMessage(7L, "user@example.test", content))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        when(access.requireParticipant(7L, "outsider@example.test")).thenThrow(new AccessDeniedException("forbidden"));
        assertThatThrownBy(() -> service.sendMessage(7L, "outsider@example.test", "hello"))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(messages, events);
    }

    @Test
    void sellerCannotConfirmSomeoneWithoutAChatRoom() {
        User seller = user(1L, "seller@example.test");
        Board board = new Board();
        board.setSeller(seller);
        board.setStatus("대여가능");
        when(boards.findByIdForChatUpdate(3L)).thenReturn(Optional.of(board));
        when(rooms.existsByBoardIdAndWearerId(3L, 9L)).thenReturn(false);
        assertThatThrownBy(() -> service.confirmDeal(3L, 9L, seller)).isInstanceOf(IllegalArgumentException.class);
        assertThat(board.getBuyer()).isNull();
        assertThat(board.getStatus()).isEqualTo("대여가능");
        verify(boards, never()).save(any());
    }

    private User user(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setNickname(email);
        return user;
    }
}
