package DevFlow.OpenCloset_Back.Login.Kakao.service;

import DevFlow.OpenCloset_Back.Login.Dto.res.KakaoTokenResponseDto;
import DevFlow.OpenCloset_Back.Login.Dto.res.KakaoUserInfoDto;
import DevFlow.OpenCloset_Back.Login.Dto.res.LoginResponseDto;
import DevFlow.OpenCloset_Back.Login.Jwt_Util.JwtUtil;
import DevFlow.OpenCloset_Back.User.User_Repository.UserRepository;
import DevFlow.OpenCloset_Back.User.entity.User;
import DevFlow.OpenCloset_Back.Login.RefreshToken.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private final WebClient webClient = WebClient.builder().build();

    public String getAccessToken(String code) {
        KakaoTokenResponseDto tokenResponse = webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=authorization_code"
                        + "&client_id=" + clientId
                        + "&client_secret=" + clientSecret
                        + "&redirect_uri=" + redirectUri
                        + "&code=" + code)
                .retrieve()
                .bodyToMono(KakaoTokenResponseDto.class)
                .block();

        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            throw new RuntimeException("카카오 액세스 토큰을 가져오는데 실패했습니다.");
        }

        log.info("카카오 액세스 토큰 발급 성공");
        return tokenResponse.getAccessToken();
    }

    public KakaoUserInfoDto getUserInfo(String accessToken) {
        KakaoUserInfoDto userInfo = webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfoDto.class)
                .block();

        if (userInfo == null) {
            throw new RuntimeException("카카오 사용자 정보를 가져오는데 실패했습니다.");
        }

        log.info("카카오 사용자 정보 조회 성공 - kakaoId: {}", userInfo.getId());
        return userInfo;
    }

    public LoginResponseDto loginOrRegister(KakaoUserInfoDto kakaoUserInfo) {
        String kakaoId = String.valueOf(kakaoUserInfo.getId());
        String email = kakaoUserInfo.getEmail();

        if (email == null || email.isEmpty()) {
            email = "kakao_" + kakaoId + "@kakao.com";
        }

        String nickname = kakaoUserInfo.getNickname();
        if (nickname == null || nickname.isEmpty()) {
            nickname = "카카오유저_" + kakaoId;
        }

        Optional<User> existingUser = userRepository.findByEmail(email);

        User curUser;
        if (existingUser.isPresent()) {
            curUser = existingUser.get();
            log.info("기존 카카오 사용자 로그인 - email: {}", email);
        } else {
            curUser = new User();
            curUser.setEmail(email);

            // 닉네임 중복 방지 확인
            if (userRepository.findByNickname(nickname).isPresent()) {
                nickname = nickname + "_" + kakaoId.substring(0, 4);
            }
            curUser.setNickname(nickname);

            // 카카오 유저는 비밀번호가 필요 없으므로 랜덤으로 암호화하여 채움 (필수값)
            curUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            curUser.setAddress("주소 미입력"); // 필수값
            curUser.setProfileImage("/images/default_profile.png");

            userRepository.save(curUser);
            log.info("신규 카카오 사용자 통합 등록 - email: {}", email);
        }

        // ROLE_USER로 일반 로그인과 완전히 동일한 권한 부여
        String accessToken = JwtUtil.generateToken(curUser.getEmail(), "ROLE_USER");
        String refreshToken = UUID.randomUUID().toString();

        refreshTokenService.saveRefreshToken(curUser.getEmail(), refreshToken);

        return new LoginResponseDto(
                curUser.getEmail(),
                curUser.getNickname(),
                "카카오 로그인 성공",
                accessToken,
                refreshToken,
                curUser.getProfileImage());
    }
}
