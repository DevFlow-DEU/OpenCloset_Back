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

import DevFlow.OpenCloset_Back.Login.Dto.req.KakaoSignupRequestDto;
import DevFlow.OpenCloset_Back.User.dto.res.UserResponeDto;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
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

    @Transactional
    public LoginResponseDto loginOrRegister(KakaoUserInfoDto kakaoUserInfo) {
        if (kakaoUserInfo == null || kakaoUserInfo.getId() == null) {
            throw new IllegalArgumentException("카카오 사용자 ID가 필요합니다.");
        }
        String kakaoId = String.valueOf(kakaoUserInfo.getId());
        String email = kakaoUserInfo.getEmail();

        if (email == null || email.isEmpty()) {
            email = "kakao_" + kakaoId + "@kakao.com";
        }

        String nickname = kakaoUserInfo.getNickname();
        if (nickname == null || nickname.isEmpty()) {
            nickname = "카카오유저_" + kakaoId;
        }

        User curUser = userRepository.findByKakaoId(kakaoId).orElse(null);
        if (curUser == null) {
            if (userRepository.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("이미 등록된 이메일입니다. 기존 계정으로 로그인해주세요.");
            }
            curUser = new User();
            curUser.setEmail(email);
            curUser.setKakaoId(kakaoId);
            curUser.setKakaoSignupCompleted(false);

            // 닉네임 중복 방지 확인
            String baseNickname = nickname;
            while (userRepository.findByNickname(nickname).isPresent()) {
                nickname = baseNickname + "_" + UUID.randomUUID().toString().substring(0, 8);
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

        LoginResponseDto response = new LoginResponseDto(
                curUser.getEmail(),
                curUser.getNickname(),
                "카카오 로그인 성공",
                accessToken,
                refreshToken,
                curUser.getProfileImage());
        response.setSignupRequired(!Boolean.TRUE.equals(curUser.getKakaoSignupCompleted()));
        return response;
    }

    @Transactional
    public UserResponeDto completeSignup(String email, KakaoSignupRequestDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (user.getKakaoId() == null) {
            throw new IllegalArgumentException("카카오 회원만 이용할 수 있습니다.");
        }
        if (Boolean.TRUE.equals(user.getKakaoSignupCompleted())) {
            throw new IllegalArgumentException("이미 추가 정보 등록을 완료했습니다.");
        }
        if (request.getNickname() == null || request.getNickname().isBlank()
                || request.getNickname().trim().length() > 255) {
            throw new IllegalArgumentException("닉네임은 1~255자로 입력해주세요.");
        }
        if (request.getAddress() == null || request.getAddress().isBlank()
                || request.getAddress().trim().length() > 255) {
            throw new IllegalArgumentException("주소는 1~255자로 입력해주세요.");
        }
        if (request.getPassword() == null || request.getPassword().length() < 8
                || request.getPassword().isBlank()
                || request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("비밀번호는 8자 이상, UTF-8 기준 72바이트 이하로 입력해주세요.");
        }
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
        }
        String nickname = request.getNickname().trim();
        userRepository.findByNickname(nickname).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), user.getId())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
        });
        user.setNickname(nickname);
        user.setAddress(request.getAddress().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setKakaoSignupCompleted(true);
        userRepository.save(user);
        return new UserResponeDto(user.getEmail(), user.getAddress(), user.getNickname(), user.getProfileImage());
    }

}
