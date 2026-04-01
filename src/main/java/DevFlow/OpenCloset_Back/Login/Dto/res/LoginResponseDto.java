package DevFlow.OpenCloset_Back.Login.Dto.res;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDto {
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
