package DevFlow.OpenCloset_Back.Login.Dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "카카오 회원가입 추가 정보 요청")
public class KakaoSignupRequestDto {
    @Schema(description = "닉네임. 앞뒤 공백 제거 후 1~255자, 다른 회원과 중복 불가", example = "옷친구", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 255)
    private String nickname;
    @Schema(description = "프론트에서 현재 위치를 변환한 주소. 앞뒤 공백 제거 후 1~255자", example = "서울특별시 강남구 역삼동", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 255)
    private String address;
    @Schema(description = "비밀번호. 8자 이상, UTF-8 기준 72바이트 이하. BCrypt로 암호화하여 저장", example = "password123!", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8, format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;
    @Schema(description = "비밀번호 확인. password와 동일해야 함", example = "password123!", requiredMode = Schema.RequiredMode.REQUIRED, format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String passwordConfirm;
}
