package DevFlow.OpenCloset_Back.Chat.Security;

import DevFlow.OpenCloset_Back.Chat.Service.ChatAccessService;
import DevFlow.OpenCloset_Back.Login.Jwt_Util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ChatChannelInterceptor implements ChannelInterceptor {
    private static final Pattern ROOM_TOPIC = Pattern.compile("^/topic/chat/([1-9][0-9]*)$");
    private final UserDetailsService userDetailsService;
    private final ChatAccessService chatAccessService;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<MessageChannel> outboundChannel;

    public ChatChannelInterceptor(UserDetailsService userDetailsService, ChatAccessService chatAccessService,
                                  ObjectMapper objectMapper,
                                  @Qualifier("clientOutboundChannel") ObjectProvider<MessageChannel> outboundChannel) {
        this.userDetailsService = userDetailsService;
        this.chatAccessService = chatAccessService;
        this.objectMapper = objectMapper;
        this.outboundChannel = outboundChannel;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headers = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (headers == null || headers.getCommand() == null) {
            return message; // Heartbeat, not a client application frame.
        }
        try {
            return authorize(message, headers);
        } catch (RuntimeException exception) {
            sendError(headers, exception);
            throw exception;
        }
    }

    private Message<?> authorize(Message<?> message, StompHeaderAccessor headers) {
        StompCommand command = headers.getCommand();
        if (command == StompCommand.CONNECT) {
            String authorization = headers.getFirstNativeHeader("Authorization");
            if (authorization == null) {
                authorization = headers.getFirstNativeHeader("authorization");
            }
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new BadCredentialsException("STOMP CONNECT에 Bearer 토큰이 필요합니다.");
            }
            String token = authorization.substring(7);
            if (!JwtUtil.validateToken(token)) {
                throw new BadCredentialsException("유효하지 않거나 만료된 토큰입니다.");
            }
            var user = userDetailsService.loadUserByUsername(JwtUtil.extractUsername(token));
            headers.setUser(UsernamePasswordAuthenticationToken.authenticated(user, token, user.getAuthorities()));
            return message;
        }
        if (command == StompCommand.DISCONNECT || command == StompCommand.UNSUBSCRIBE) {
            return message; // Allow session cleanup even after token expiry.
        }
        if (!(headers.getUser() instanceof Authentication authentication)
                || !authentication.isAuthenticated()
                || !(authentication.getCredentials() instanceof String token)
                || !JwtUtil.validateToken(token)) {
            throw new BadCredentialsException("인증이 필요하거나 토큰이 만료되었습니다.");
        }
        if (command == StompCommand.SUBSCRIBE) {
            if ("/user/queue/chat/errors".equals(headers.getDestination())) {
                return message;
            }
            var matcher = ROOM_TOPIC.matcher(headers.getDestination() == null ? "" : headers.getDestination());
            if (!matcher.matches()) {
                throw new AccessDeniedException("채팅방 단위로만 구독할 수 있습니다.");
            }
            try {
                chatAccessService.requireParticipant(Long.valueOf(matcher.group(1)), authentication.getName());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("유효한 채팅방 ID가 필요합니다.");
            }
        } else if (command == StompCommand.SEND) {
            if (!"/app/chat/send".equals(headers.getDestination())) {
                throw new AccessDeniedException("허용되지 않은 메시지 전송 경로입니다.");
            }
            chatAccessService.requireParticipant(readRoomId(message), authentication.getName());
        } else {
            throw new AccessDeniedException("지원하지 않는 STOMP 명령입니다.");
        }
        return message;
    }

    private void sendError(StompHeaderAccessor request, RuntimeException exception) {
        if (request.getSessionId() == null) {
            return;
        }
        String code = exception instanceof AuthenticationException ? "UNAUTHORIZED"
                : exception instanceof AccessDeniedException ? "FORBIDDEN"
                : exception instanceof IllegalArgumentException ? "INVALID_REQUEST" : "CHAT_UNAVAILABLE";
        var error = StompHeaderAccessor.create(StompCommand.ERROR);
        error.setSessionId(request.getSessionId());
        error.setMessage(code);
        error.setContentType(MediaType.TEXT_PLAIN);
        try {
            // Ordered inbound channels don't propagate failures to the WebSocket error handler.
            // Sending an ERROR on the outbound channel also closes the offending socket.
            outboundChannel.getObject().send(MessageBuilder.createMessage(
                    code.getBytes(StandardCharsets.UTF_8), error.getMessageHeaders()));
        } catch (RuntimeException sendFailure) {
            log.debug("채팅 오류 응답 전송 실패 - sessionId: {}", request.getSessionId(), sendFailure);
        }
    }

    private Long readRoomId(Message<?> message) {
        if (!(message.getPayload() instanceof byte[] payload) || payload.length > 16 * 1024) {
            throw new IllegalArgumentException("유효한 JSON 메시지가 필요합니다.");
        }
        try {
            JsonNode json = objectMapper.readTree(payload);
            JsonNode roomId = json == null ? null : json.get("roomId");
            if (roomId == null || !roomId.isIntegralNumber() || !roomId.canConvertToLong() || roomId.longValue() <= 0) {
                throw new IllegalArgumentException("유효한 채팅방 ID가 필요합니다.");
            }
            return roomId.longValue();
        } catch (IOException ex) {
            throw new IllegalArgumentException("유효한 JSON 메시지가 필요합니다.");
        }
    }
}
