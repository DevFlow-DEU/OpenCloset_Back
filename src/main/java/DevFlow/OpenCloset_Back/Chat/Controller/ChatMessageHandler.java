package DevFlow.OpenCloset_Back.Chat.Controller;

import DevFlow.OpenCloset_Back.Chat.Service.ChatService;
import DevFlow.OpenCloset_Back.Chat.dto.req.ChatSendRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

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
     * 1. 클라이언트 → STOMP SEND /app/chat/send {roomId, senderId, message}
     * 2. 서버 → MySQL 저장 + Redis 발행 + WebSocket /topic/chat/{roomId} 전달
     */
    @MessageMapping("/chat/send")
    public void handleMessage(ChatSendRequestDto request) {
        log.info("WebSocket 메시지 수신 - roomId: {}, senderId: {}", request.getRoomId(), request.getSenderId());
        chatService.sendMessage(request.getRoomId(), request.getSenderId(), request.getMessage());
    }
}
