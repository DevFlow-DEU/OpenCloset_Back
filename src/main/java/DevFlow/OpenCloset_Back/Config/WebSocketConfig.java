package DevFlow.OpenCloset_Back.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 구독 경로: 클라이언트가 /topic/chat/{roomId} 를 구독
        config.enableSimpleBroker("/topic");
        // 전송 경로: 클라이언트가 /app/chat/send 로 메시지를 보냄
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 핸드셰이크 엔드포인트
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // SockJS 없이 순수 WebSocket 연결도 지원
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*");
    }
}
