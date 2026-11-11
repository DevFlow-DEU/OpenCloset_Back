package DevFlow.OpenCloset_Back.Config;

import DevFlow.OpenCloset_Back.Chat.Security.ChatChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final ChatChannelInterceptor chatChannelInterceptor;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(chatChannelInterceptor);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(16 * 1024);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 구독 경로: 클라이언트가 /topic/chat/{roomId} 를 구독
        config.enableSimpleBroker("/topic", "/queue");
        config.setPreservePublishOrder(true);
        // 전송 경로: 클라이언트가 /app/chat/send 로 메시지를 보냄
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.setPreserveReceiveOrder(true);
        // WebSocket 핸드셰이크 엔드포인트
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // SockJS 없이 순수 WebSocket 연결도 지원
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*");
    }
}
