package DevFlow.OpenCloset_Back.User.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "회원가입 응답 DTO")
public class UserResponeDto {

    @Schema(description = "등록된 사용자 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "등록된 사용자 닉네임", example = "김영진")
    private String nickname;

    @Schema(description = "등록된 사용자 주소", example = "부산시 부산진구")
    private String address;

    @Schema(description = "등록된 사용자 나이", example = "26")
    private String age;

    public UserResponeDto(String email, String address, String nickname, String age) {
        this.email = email;
        this.address = address;
        this.nickname = nickname;
        this.age = age;
    }
}
