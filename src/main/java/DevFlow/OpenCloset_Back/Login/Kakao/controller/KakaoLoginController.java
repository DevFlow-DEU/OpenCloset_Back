package DevFlow.OpenCloset_Back.Login.Kakao.controller;

import DevFlow.OpenCloset_Back.Login.Dto.res.KakaoUserInfoDto;
import DevFlow.OpenCloset_Back.Login.Dto.res.LoginResponseDto;
import DevFlow.OpenCloset_Back.Login.Kakao.service.KakaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import DevFlow.OpenCloset_Back.Login.Dto.req.KakaoSignupRequestDto;
import DevFlow.OpenCloset_Back.User.dto.res.UserResponeDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @Operation(summary = "카카오 로그인 URL 조회", description = "프론트에서 이 URL로 리다이렉트하면 카카오 로그인 페이지가 열립니다.", security = {})
    @ApiResponse(responseCode = "200", description = "카카오 로그인 URL 반환 성공")
    @GetMapping("/login-url")
    public ResponseEntity<String> getKakaoLoginUrl() {
        String kakaoLoginUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code";
        return ResponseEntity.ok(kakaoLoginUrl);
    }

    @Operation(summary = "카카오 로그인 콜백", description = "카카오 인가 코드로 로그인 후 JWT와 signupRequired를 반환합니다. true이면 추가 정보 입력 화면에서 POST /auth/kakao/register를 호출하고, false이면 로그인 완료입니다. 카카오 고유 ID로 계정을 식별하며 이메일만 같은 기존 계정은 자동 연결하지 않습니다.", security = {})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카카오 로그인 성공, JWT 및 추가 정보 입력 필요 여부 반환",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "인가 코드 누락, 사용자 ID 누락 또는 이미 등록된 이메일"),
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

    @Operation(summary = "카카오 회원가입 추가 정보 등록", description = "콜백에서 signupRequired가 true인 카카오 회원의 닉네임·주소·비밀번호를 한 번 등록합니다. "
            + "Swagger Authorize에 콜백에서 받은 accessToken을 입력하세요. 완료 후 기존 accessToken과 refreshToken을 계속 사용하며 다음 카카오 로그인부터 signupRequired는 false입니다. "
            + "닉네임 확인은 GET /auth/check_nickname을 사용합니다. 최종 저장에서도 중복을 검사하며 본인 닉네임은 허용합니다. "
            + "현재 위치의 주소 변환은 프론트에서 처리한 뒤 주소 문자열을 보내야 합니다.")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추가 정보 등록 완료",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponeDto.class),
                            examples = @ExampleObject(value = "{\"email\":\"user@example.com\",\"nickname\":\"옷친구\",\"address\":\"서울특별시 강남구 역삼동\",\"profileImage\":\"https://opencloset.jihongeek.com/images/default_profile.png\"}"))),
            @ApiResponse(responseCode = "400", description = "필수값 누락, 길이 제한 위반, 닉네임 중복, 비밀번호 확인 불일치, 일반 회원 요청 또는 이미 등록 완료",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "닉네임 중복", value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"이미 사용 중인 닉네임입니다.\",\"timestamp\":\"2026-11-13T12:00:00\"}"),
                            @ExampleObject(name = "비밀번호 확인 불일치", value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"비밀번호 확인이 일치하지 않습니다.\",\"timestamp\":\"2026-11-13T12:00:00\"}")
                    })),
            @ApiResponse(responseCode = "401", description = "JWT 누락 또는 만료·유효하지 않은 토큰",
                    content = @Content(schema = @Schema(type = "string"), examples = @ExampleObject(value = "Unauthorized: Full authentication is required to access this resource")))
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponeDto> completeSignup(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody KakaoSignupRequestDto request) {
        return ResponseEntity.ok(kakaoService.completeSignup(userDetails.getUsername(), request));
    }

}
