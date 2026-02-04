package DevFlow.OpenCloset_Back.User.dto.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class UserCreateRequestDto {
    private String email;
    private String nickname;
    private String password;
    private String address;
    private String age;
}
