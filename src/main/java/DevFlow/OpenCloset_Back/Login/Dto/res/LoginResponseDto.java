package DevFlow.OpenCloset_Back.Login.Dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDto {
    @Schema(description = "카카오 회원가입 추가 정보 입력 필요 여부. true이면 추가 정보 화면에서 POST /auth/kakao/register 호출. 일반 로그인은 false. 서버 기능 접근 제한을 의미하지 않음", example = "true")
    private boolean signupRequired;
    private String email;
    private String nickname;
    private String message;
    private String accessToken;
    private String refreshToken;
    private String profileImage;

    public LoginResponseDto(String email, String nickname, String message, String accessToken, String refreshToken, String profileImage) {
        this.email = email;
        this.nickname = nickname;
        this.message = message;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.profileImage = profileImage;
    }
}
