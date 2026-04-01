package DevFlow.OpenCloset_Back.User.User_Controller;

import DevFlow.OpenCloset_Back.Login.Login_Service.LoginService;
import DevFlow.OpenCloset_Back.User.User_Service.UserService;
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
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
@Tag(name = "마이페이지", description = "마이페이지 관련 API")
public class MyPageController {

        private final LoginService loginService;
        private final UserService userService;

        @Operation(summary = "마이페이지 프로필 조회", description = "현재 로그인된 사용자의 프로필 정보를 조회합니다. (닉네임, 주소, 이메일) JWT 토큰이 필요합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "프로필 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MyPageProfileResponseDto.class))),
                        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (토큰 없음 또는 만료)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Unauthorized\"}")))
        })
        @GetMapping("/profile")
        public ResponseEntity<MyPageProfileResponseDto> getMyProfile(
                        @AuthenticationPrincipal UserDetails userDetails) {
                String email = userDetails.getUsername();
                MyPageProfileResponseDto profile = userService.getProfile(email);
                return ResponseEntity.ok(profile);
        }

        @Operation(summary = "프로필 이미지 업로드", description = "현재 로그인된 사용자의 프로필 이미지를 업로드합니다. JWT 토큰이 필요합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "프로필 이미지 업로드 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"프로필 이미지가 성공적으로 업로드되었습니다.\"}"))),
                        @ApiResponse(responseCode = "400", description = "잘못된 요청 (파일 누락 등)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"status\": 400, \"error\": \"Bad Request\", \"message\": \"업로드할 파일이 비어 있습니다.\"}"))),
                        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (토큰 없음 또는 만료)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Unauthorized\"}")))
        })
        @PostMapping(value = "/profile/image", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<Map<String, String>> uploadProfileImage(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @io.swagger.v3.oas.annotations.Parameter(description = "업로드할 사진 파일 (10MB 이하)") @RequestParam("profileImage") MultipartFile file) {
                String email = userDetails.getUsername();
                try {
                        userService.uploadProfileImage(email, file);
                        return ResponseEntity.ok(Map.of("message", "프로필 이미지가 성공적으로 업로드되었습니다."));
                } catch (IOException e) {
                        e.printStackTrace();
                        return ResponseEntity.status(500).body(Map.of("message", "파일 업로드 오류: " + e.getMessage()));
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
                }
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
                String email = userDetails.getUsername();
                userService.changeAddress(email, requestDto.getNewAddress());
                return ResponseEntity.ok(Map.of("message", "주소가 성공적으로 변경되었습니다."));
        }

        @Operation(summary = "내 상품 목록 조회", description = "현재 로그인된 유저가 업로드(등록)한 모든 상품(옷) 목록을 불러옴. \n\n 이메일(JWT 토큰)을 기반으로 본인이 올린 게시물만 출력하며, 상태(판매중 등)와 사진 정보가 포함.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "내 상품 목록 조회 완벽히 성공!", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MyProductResponseDto.class)), examples = @ExampleObject(value = "[\n  {\n    \"productId\": 25,\n    \"title\": \"나이키 바람막이 L사이즈 빌려드려요!\",\n    \"price\": 15000,\n    \"status\": \"판매중\",\n    \"imageUrl\": \"https://opencloset.jihongeek.com/uploads/boards/17150123984_nike_windbreaker.jpg\",\n    \"createdAt\": \"2026-03-24T18:30:11.123\"\n  },\n  {\n    \"productId\": 11,\n    \"title\": \"아디다스 츄리닝 바지\",\n    \"price\": 8000,\n    \"status\": \"예약중\",\n    \"imageUrl\": \"https://opencloset.jihongeek.com/images/default_board.png\",\n    \"createdAt\": \"2026-03-10T12:00:00.000\"\n  }\n]"))),
                        @ApiResponse(responseCode = "401", description = "로그인하지 않았거나 토큰이 만료됨", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Unauthorized - 유효하지 않은 토큰입니다.\"}")))
        })
        @GetMapping("/products")
        public ResponseEntity<List<MyProductResponseDto>> getMyProducts(
                        @AuthenticationPrincipal UserDetails userDetails) {
                String email = userDetails.getUsername();
                List<MyProductResponseDto> myProducts = userService.getMyProducts(email);
                return ResponseEntity.ok(myProducts);
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