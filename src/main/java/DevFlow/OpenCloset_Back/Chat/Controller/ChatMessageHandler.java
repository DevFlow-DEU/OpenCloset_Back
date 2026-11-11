package DevFlow.OpenCloset_Back.Chat.Controller;

import DevFlow.OpenCloset_Back.Chat.Service.ChatService;
import DevFlow.OpenCloset_Back.Chat.dto.req.ChatSendRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import io.swagger.v3.oas.annotations.media.Schema;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageHandler {

    private final ChatService chatService;

    /**
     * WebSocket STOMP 메시지 수신 핸들러
     * 클라이언트가 /app/chat/send 로 메시지를 보내면 이 메서드가 처리
     *
     * 흐름:
     * CONNECT Authorization 헤더로 인증 후 {roomId, message}를 전송합니다.
     * 발신자는 로그인 사용자로 결정하며 저장 커밋 후 Redis를 통해 전달합니다.
     */
    @MessageMapping("/chat/send")
    public void handleMessage(ChatSendRequestDto request, Principal principal) {
        if (principal == null) {
            throw new org.springframework.security.access.AccessDeniedException("로그인이 필요합니다.");
        }
        chatService.sendMessage(request.getRoomId(), principal.getName(), request.getMessage());
    }

    @MessageExceptionHandler(IllegalArgumentException.class)
    @SendToUser(value = "/queue/chat/errors", broadcast = false)
    public ChatError invalidMessage(IllegalArgumentException exception) {
        return new ChatError("INVALID_MESSAGE", exception.getMessage());
    }

    @Schema(description = "메시지 내용 검증 실패 시 /user/queue/chat/errors로 수신하는 개인 오류. 연결은 유지됩니다.")
    public record ChatError(
            @Schema(description = "입력 검증 오류 코드", example = "INVALID_MESSAGE", allowableValues = {"INVALID_MESSAGE"}) String code,
            @Schema(description = "오류 안내", example = "메시지는 공백이 아닌 1~1000자여야 합니다.") String message) {
    }
}
