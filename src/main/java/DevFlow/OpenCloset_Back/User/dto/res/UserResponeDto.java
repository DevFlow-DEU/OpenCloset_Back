package DevFlow.OpenCloset_Back.User.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponeDto {
    private String email;
    private String nickname;
    private String address;
    private String age;

    public UserResponeDto(String email, String address, String nickname, String age) {
        this.email = email;
        this.address = address;
        this.nickname = nickname;
        this.age = age;
    }
}
