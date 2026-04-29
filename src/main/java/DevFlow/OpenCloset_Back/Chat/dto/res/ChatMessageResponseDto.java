package DevFlow.OpenCloset_Back.Chat.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "채팅 메시지 응답 DTO")
public class ChatMessageResponseDto {
    @Schema(description = "메시지 ID", example = "1")
    private Long messageId;

    @Schema(description = "보낸 사람 ID", example = "3")
    private Long senderId;

    @Schema(description = "보낸 사람 닉네임", example = "홍길동")
    private String senderNickname;

    @Schema(description = "보낸 사람 프로필 이미지")
    private String senderProfileImage;

    @Schema(description = "메시지 내용", example = "이 옷 사이즈 어떻게 되나요?")
    private String message;

    @Schema(description = "읽음 여부", example = "false")
    private Boolean isRead;

    @Schema(description = "전송 시간")
    private LocalDateTime sentAt;
}
