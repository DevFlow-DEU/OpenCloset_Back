package DevFlow.OpenCloset_Back.User.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "마이페이지 프로필 조회 응답 DTO")
public class MyPageProfileResponseDto {

    @Schema(description = "사용자 닉네임", example = "UserName")
    private String nickname;

    @Schema(description = "사용자 주소", example = "부산 진구 가야동")
    private String address;

    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;
}
