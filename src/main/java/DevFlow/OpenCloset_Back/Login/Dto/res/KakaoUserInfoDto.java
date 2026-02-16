package DevFlow.OpenCloset_Back.Login.Dto.res;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KakaoUserInfoDto {

    private Long id; // 카카오 고유 사용자 ID

    private KakaoAccount kakao_account;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class KakaoAccount {
        private String email;
        private Profile profile;

        @Getter
        @Setter
        @NoArgsConstructor
        public static class Profile {
            private String nickname;
        }
    }

    // 편의 메서드
    public String getEmail() {
        if (kakao_account != null) {
            return kakao_account.getEmail();
        }
        return null;
    }

    public String getNickname() {
        if (kakao_account != null && kakao_account.getProfile() != null) {
            return kakao_account.getProfile().getNickname();
        }
        return null;
    }
}
