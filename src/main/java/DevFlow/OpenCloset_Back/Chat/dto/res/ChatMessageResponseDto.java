package DevFlow.OpenCloset_Back.Chat.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "REST 대화 내역의 항목 및 /topic/chat/{roomId}로 수신하는 실시간 메시지. messageId로 중복 제거합니다.")
public class ChatMessageResponseDto {
    @Schema(description = "저장된 메시지 고유 ID. 내역 조회와 실시간 수신을 합칠 때 중복 제거 기준", example = "10")
    private Long messageId;

    @Schema(description = "JWT로 인증한 실제 발신자 ID. 요청의 senderId는 사용하지 않습니다.", example = "7")
    private Long senderId;

    @Schema(description = "보낸 사람 닉네임", example = "홍길동")
    private String senderNickname;

    @Schema(description = "보낸 사람 프로필 이미지", example = "https://opencloset.jihongeek.com/images/default_profile.png", nullable = true)
    private String senderProfileImage;

    @Schema(description = "메시지 내용", example = "이 옷 사이즈 어떻게 되나요?")
    private String message;

    @Schema(description = "상대방의 읽음 여부. PATCH 읽음 처리 후 내역 재조회로 갱신하며 실시간 읽음 알림은 없습니다.", example = "false")
    private Boolean isRead;

    @Schema(description = "전송 시각 (시간대 오프셋 없는 서버 LocalDateTime)", example = "2026-11-11T10:01:00")
    private LocalDateTime sentAt;
}
