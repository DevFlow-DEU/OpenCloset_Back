package DevFlow.OpenCloset_Back.Login.Kakao.service;

import DevFlow.OpenCloset_Back.Login.Dto.res.KakaoTokenResponseDto;
import DevFlow.OpenCloset_Back.Login.Dto.res.KakaoUserInfoDto;
import DevFlow.OpenCloset_Back.Login.Dto.res.LoginResponseDto;
import DevFlow.OpenCloset_Back.Login.Jwt_Util.JwtUtil;
import DevFlow.OpenCloset_Back.Login.Kakao.entity.KakaoUser;
import DevFlow.OpenCloset_Back.Login.Kakao.repository.KakaoUserRepository;
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

    private final KakaoUserRepository kakaoUserRepository;
    private final RefreshTokenService refreshTokenService;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private final WebClient webClient = WebClient.builder().build();

    /**
     * 1단계: 인가 코드로 카카오 액세스 토큰 요청
     */
    public String getAccessToken(String code) {
        KakaoTokenResponseDto tokenResponse = webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=authorization_code"
                        + "&client_id=" + clientId
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

    /**
     * 2단계: 액세스 토큰으로 카카오 사용자 정보 조회
     */
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

    /**
     * 3단계: 카카오 사용자 정보로 로그인 또는 자동 회원가입 처리
     */
    public LoginResponseDto loginOrRegister(KakaoUserInfoDto kakaoUserInfo) {
        String kakaoId = String.valueOf(kakaoUserInfo.getId());
        String email = kakaoUserInfo.getEmail();
        String nickname = kakaoUserInfo.getNickname();

        if (nickname == null || nickname.isEmpty()) {
            nickname = "카카오유저_" + kakaoId;
        }

        Optional<KakaoUser> existingUser = kakaoUserRepository.findByKakaoId(kakaoId);

        KakaoUser kakaoUser;
        if (existingUser.isPresent()) {
            kakaoUser = existingUser.get();
            log.info("기존 카카오 사용자 로그인 - kakaoId: {}", kakaoId);
        } else {
            kakaoUser = KakaoUser.builder()
                    .kakaoId(kakaoId)
                    .email(email)
                    .nickname(nickname)
                    .build();
            kakaoUserRepository.save(kakaoUser);
            log.info("신규 카카오 사용자 자동 등록 - kakaoId: {}", kakaoId);
        }

        String subject = (email != null) ? email : "kakao_" + kakaoId;
        String accessToken = JwtUtil.generateToken(subject, "ROLE_KAKAO_USER");
        String refreshToken = UUID.randomUUID().toString();

        refreshTokenService.saveRefreshToken(subject, refreshToken);

        return new LoginResponseDto(
                subject,
                kakaoUser.getNickname(),
                "카카오 로그인 성공",
                accessToken,
                refreshToken);
    }
}
