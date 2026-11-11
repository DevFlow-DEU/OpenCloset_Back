package DevFlow.OpenCloset_Back.Chat.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "채팅 메시지 전송 요청 DTO (WebSocket)")
public class ChatSendRequestDto {
    @Schema(description = "채팅방 ID", example = "1")
    private Long roomId;

    @Deprecated
    @Schema(description = "호환용 필드. 무시하며 JWT의 로그인 사용자를 발신자로 사용합니다.", deprecated = true)
    private Long senderId;

    @Schema(description = "메시지 내용 (1~1000자, 공백만 있는 메시지는 불가)", maxLength = 1000, example = "이 옷 사이즈 어떻게 되나요?")
    private String message;
}
