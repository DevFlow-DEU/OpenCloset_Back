package DevFlow.OpenCloset_Back.Login.Dto.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoSignupRequestDto {
    private String nickname;
    private String address;
    private String password;
    private String passwordConfirm;
}
