package DevFlow.OpenCloset_Back.Chat.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "채팅방 생성 요청 DTO")
public class ChatRoomCreateRequestDto {
    @Schema(description = "문의할 상품 ID. 상품 작성자와 로그인 사용자의 채팅방을 생성하거나 기존 방을 반환합니다.",
            example = "5", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long boardId;
}
