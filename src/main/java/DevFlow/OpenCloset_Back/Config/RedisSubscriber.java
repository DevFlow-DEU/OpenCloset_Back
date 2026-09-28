package DevFlow.OpenCloset_Back.Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis Pub/Sub 구독자
 * Redis 채널에서 메시지를 수신하면 WebSocket으로 전달
 *
 * 서버가 여러 대(수평 확장)일 때:
 * - 서버 A에서 Redis에 발행 → 서버 B의 이 구독자가 수신 → 서버 B의 WebSocket 클라이언트에 전달
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            // 채널 형식: chat:room:{roomId}
            String roomId = channel.replace("chat:room:", "");

            Object chatMessage = redisTemplate.getValueSerializer().deserialize(message.getBody());

            // WebSocket 구독자에게 전달
            messagingTemplate.convertAndSend("/topic/chat/" + roomId, chatMessage);

            log.debug("Redis → WebSocket 전달 완료 - roomId: {}", roomId);
        } catch (Exception e) {
            log.error("Redis 메시지 처리 실패: {}", e.getMessage(), e);
        }
    }
}
