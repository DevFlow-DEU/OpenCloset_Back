package DevFlow.OpenCloset_Back.Login.Kakao.controller;

import DevFlow.OpenCloset_Back.Login.Dto.res.KakaoUserInfoDto;
import DevFlow.OpenCloset_Back.Login.Dto.res.LoginResponseDto;
import DevFlow.OpenCloset_Back.Login.Kakao.service.KakaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth/kakao")
@RequiredArgsConstructor
@Tag(name = "카카오 로그인", description = "카카오 소셜 로그인 API")
public class KakaoLoginController {

    private final KakaoService kakaoService;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Operation(summary = "카카오 로그인 URL 조회", description = "프론트에서 이 URL로 리다이렉트하면 카카오 로그인 페이지가 열립니다.")
    @ApiResponse(responseCode = "200", description = "카카오 로그인 URL 반환 성공")
    @GetMapping("/login-url")
    public ResponseEntity<String> getKakaoLoginUrl() {
        String kakaoLoginUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code";
        return ResponseEntity.ok(kakaoLoginUrl);
    }

    @Operation(summary = "카카오 로그인 콜백", description = "카카오에서 인가 코드를 받아 로그인 처리 후 JWT 토큰을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카카오 로그인 성공, JWT 토큰 반환"),
            @ApiResponse(responseCode = "500", description = "카카오 로그인 처리 중 오류 발생")
    })
    @GetMapping("/callback")
    public ResponseEntity<LoginResponseDto> kakaoCallback(
            @Parameter(description = "카카오에서 전달받은 인가 코드") @RequestParam("code") String code) {

        log.info("카카오 로그인 콜백 - 인가 코드 수신");

        String accessToken = kakaoService.getAccessToken(code);
        KakaoUserInfoDto userInfo = kakaoService.getUserInfo(accessToken);
        LoginResponseDto responseDto = kakaoService.loginOrRegister(userInfo);

        return ResponseEntity.ok(responseDto);
    }
}
