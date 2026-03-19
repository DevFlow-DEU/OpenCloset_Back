package DevFlow.OpenCloset_Back.User.User_Controller;

import DevFlow.OpenCloset_Back.Login.Login_Service.LoginService;
import DevFlow.OpenCloset_Back.User.dto.req.AddressChangeRequestDto;
import DevFlow.OpenCloset_Back.User.dto.res.MyPageProfileResponseDto;
import DevFlow.OpenCloset_Back.User.dto.res.MyProductResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
@Tag(name = "마이페이지", description = "마이페이지 관련 API")
public class MyPageController {

        private final LoginService loginService;

        @Operation(summary = "마이페이지 프로필 조회", description = "현재 로그인된 사용자의 프로필 정보를 조회합니다. (닉네임, 주소, 이메일) JWT 토큰이 필요합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "프로필 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MyPageProfileResponseDto.class))),
                        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (토큰 없음 또는 만료)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Unauthorized\"}")))
        })
        @GetMapping("/profile")
        public ResponseEntity<MyPageProfileResponseDto> getMyProfile(
                        @AuthenticationPrincipal UserDetails userDetails) {
                return ResponseEntity.ok(new MyPageProfileResponseDto("UserName", "부산 진구 가야동", "user@example.com"));
        }

        @Operation(summary = "주소 변경", description = "현재 로그인된 사용자의 주소를 변경합니다. JWT 토큰이 필요합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "주소 변경 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"주소가 성공적으로 변경되었습니다.\"}"))),
                        @ApiResponse(responseCode = "400", description = "잘못된 요청 (주소 값 누락)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"status\": 400, \"error\": \"Bad Request\", \"message\": \"변경할 주소를 입력해주세요.\"}"))),
                        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (토큰 없음 또는 만료)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Unauthorized\"}")))
        })
        @PutMapping("/address")
        public ResponseEntity<Map<String, String>> changeAddress(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @RequestBody AddressChangeRequestDto requestDto) {
                return ResponseEntity.ok(Map.of("message", "주소가 성공적으로 변경되었습니다."));
        }

        @Operation(summary = "내 상품 목록 조회", description = "현재 로그인된 사용자가 등록한 상품 목록을 조회합니다. JWT 토큰이 필요합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "상품 목록 조회 성공", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MyProductResponseDto.class)))),
                        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (토큰 없음 또는 만료)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Unauthorized\"}")))
        })
        @GetMapping("/products")
        public ResponseEntity<List<MyProductResponseDto>> getMyProducts(
                        @AuthenticationPrincipal UserDetails userDetails) {
                List<MyProductResponseDto> dummyProducts = List.of(
                                new MyProductResponseDto(1L, "나이키 에어맥스 90", 50000, "판매중",
                                                "https://example.com/image1.jpg", "2026-03-19T10:00:00"),
                                new MyProductResponseDto(2L, "아디다스 후드티", 30000, "예약중", "https://example.com/image2.jpg",
                                                "2026-03-18T14:30:00"));
                return ResponseEntity.ok(dummyProducts);
        }

        @Operation(summary = "로그아웃", description = "현재 로그인된 사용자를 로그아웃합니다. 세션을 무효화합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "로그아웃 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"로그아웃 성공!\"}"))),
                        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (토큰 없음 또는 만료)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Unauthorized\"}")))
        })
        @PostMapping("/logout")
        public ResponseEntity<String> logout(HttpSession session) {
                loginService.logoutUser(session);
                return ResponseEntity.ok("로그아웃 성공!");
        }
}