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

    @Schema(description = "보내는 사람 ID", example = "3")
    private Long senderId;

    @Schema(description = "메시지 내용", example = "이 옷 사이즈 어떻게 되나요?")
    private String message;
}
