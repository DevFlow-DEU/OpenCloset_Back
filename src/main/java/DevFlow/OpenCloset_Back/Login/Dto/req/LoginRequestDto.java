package DevFlow.OpenCloset_Back.Login.Dto.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
    private String email;
    private String password;
}
