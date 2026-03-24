package DevFlow.OpenCloset_Back.User.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "비밀번호 변경 요청 DTO")
public class PasswordChangeRequestDto {

    @Schema(description = "현재 비밀번호", example = "currentPassword123", requiredMode = RequiredMode.REQUIRED)
    private String currentPassword;

    @Schema(description = "새 비밀번호", example = "newPassword456", requiredMode = RequiredMode.REQUIRED)
    private String newPassword;
}
