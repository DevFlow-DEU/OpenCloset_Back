package DevFlow.OpenCloset_Back.User.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "회원가입 요청 DTO")
public class UserCreateRequestDto {

    @Schema(description = "사용자 이메일 주소(고유해야함)", example = "user@example.com", requiredMode = RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "사용자 닉네임 (고유해야 함)", example = "홍길동", requiredMode = RequiredMode.REQUIRED)
    private String nickname;

    @Schema(description = "사용자 비밀번호", example = "password123", requiredMode = RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "사용자 주소", example = "부산시 부산진구")
    private String address;

    @Schema(description = "사용자 나이", example = "26")
    private String age;
}
