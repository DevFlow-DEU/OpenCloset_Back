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

        @Operation(summary = "프로필 통합 수정", description = "기존에 분리되어 있던 이미지, 닉네임, 주소 변경을 한 번에 처리하는 API입니다. 변경을 원하는 필드 값(FormData 형식)만 채워서 보내면 됩니다. 변경하지 않을 값은 빈 값으로 두거나 안 보내도 됩니다. JWT 토큰이 필수입니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "프로필 수정 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"프로필 정보가 성공적으로 수정되었습니다.\"}"))),
                        @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 닉네임 중복", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"이미 사용 중인 닉네임입니다.\"}"))),
                        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Unauthorized\"}")))
        })
        @PostMapping(value = "/edit", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<Map<String, String>> editProfile(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @RequestParam(value = "profileImage", required = false) MultipartFile file,
                        @RequestParam(value = "nickname", required = false) String nickname,
                        @RequestParam(value = "address", required = false) String address) {
                String email = userDetails.getUsername();
                try {
                        userService.updateProfile(email, file, nickname, address);
                        return ResponseEntity.ok(Map.of("message", "프로필 정보가 성공적으로 수정되었습니다."));
                } catch (IOException e) {
                        e.printStackTrace();
                        return ResponseEntity.status(500).body(Map.of("message", "파일 업로드 오류: " + e.getMessage()));
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
                }
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