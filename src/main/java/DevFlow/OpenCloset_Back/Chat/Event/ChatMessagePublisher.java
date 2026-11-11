package DevFlow.OpenCloset_Back.Chat.Event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessagePublisher {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(ChatMessageSentEvent event) {
        try {
            redisTemplate.convertAndSend("chat:room:" + event.roomId(),
                    objectMapper.writeValueAsString(event.message()));
        } catch (JsonProcessingException | RuntimeException ex) {
            // Persistence already committed. History remains available if live delivery fails.
            log.error("저장된 채팅 메시지 실시간 전달 실패 - roomId: {}, messageId: {}",
                    event.roomId(), event.message().getMessageId(), ex);
        }
    }
}
