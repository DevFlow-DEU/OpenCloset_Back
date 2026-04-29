package DevFlow.OpenCloset_Back.Chat.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "게시물에 채팅 건 wearer(빌리려는 사람) 목록 DTO")
public class WearerListResponseDto {
    @Schema(description = "채팅방 ID", example = "1")
    private Long roomId;

    @Schema(description = "wearer(빌리려는 사람) 유저 ID", example = "7")
    private Long wearerId;

    @Schema(description = "wearer 닉네임", example = "김철수")
    private String wearerNickname;

    @Schema(description = "wearer 프로필 이미지")
    private String wearerProfileImage;
}
