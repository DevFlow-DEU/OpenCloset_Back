package DevFlow.OpenCloset_Back.Chat.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "채팅방 생성 요청 DTO")
public class ChatRoomCreateRequestDto {
    @Schema(description = "채팅을 걸 게시물 ID", example = "1")
    private Long boardId;
}
