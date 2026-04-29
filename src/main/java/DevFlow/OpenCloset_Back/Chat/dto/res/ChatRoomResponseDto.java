package DevFlow.OpenCloset_Back.Chat.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "채팅방 목록 응답 DTO")
public class ChatRoomResponseDto {
    @Schema(description = "채팅방 ID", example = "1")
    private Long roomId;

    @Schema(description = "게시물 ID", example = "5")
    private Long boardId;

    @Schema(description = "게시물 제목", example = "나이키 바람막이 빌려드려요")
    private String boardTitle;

    @Schema(description = "게시물 이미지", example = "https://opencloset.jihongeek.com/uploads/boards/1.jpg")
    private String boardImage;

    @Schema(description = "상대방 닉네임", example = "홍길동")
    private String opponentNickname;

    @Schema(description = "상대방 프로필 이미지")
    private String opponentProfileImage;

    @Schema(description = "마지막 메시지 내용", example = "사이즈 어떻게 되나요?")
    private String lastMessage;

    @Schema(description = "안 읽은 메시지 수", example = "3")
    private int unreadCount;

    @Schema(description = "채팅방 생성일")
    private LocalDateTime createdAt;
}
