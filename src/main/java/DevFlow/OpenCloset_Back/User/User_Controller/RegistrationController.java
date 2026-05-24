package DevFlow.OpenCloset_Back.User.User_Controller;

import DevFlow.OpenCloset_Back.User.User_Service.UserService;
import DevFlow.OpenCloset_Back.User.dto.req.UserCreateRequestDto;
import DevFlow.OpenCloset_Back.User.dto.res.UserResponeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "회원가입", description = "사용자 회원가입 관련 API")
public class RegistrationController {

        private final UserService userService;

        @Operation(summary = "회원가입", description = "신규 사용자를 등록. 이메일, 닉네임, 비밀번호, 주소 정보가 필요.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "회원가입 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponeDto.class))),
                        @ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 필드 누락 또는 유효하지 않은 데이터)", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "필수 필드 누락", value = "{\"status\": 400, \"message\": \"이메일은 필수 입력 항목입니다.\", \"timestamp\": \"2026-02-04T15:40:00\"}"))),
                        @ApiResponse(responseCode = "400", description = "이미 존재하는 이메일 또는 닉네임", content = @Content(mediaType = "application/json", examples = {
                                        @ExampleObject(name = "이메일 중복", value = "{\"status\": 400, \"error\": \"Bad Request\", \"message\": \"이미 사용 중인 이메일입니다.\", \"timestamp\": \"2026-02-04T15:40:00\"}"),
                                        @ExampleObject(name = "닉네임 중복", value = "{\"status\": 400, \"error\": \"Bad Request\", \"message\": \"이미 사용 중인 닉네임입니다.\", \"timestamp\": \"2026-02-04T15:40:00\"}")
                        }))
        })
        @PostMapping("/register")
        public ResponseEntity<UserResponeDto> registerUser(@RequestBody UserCreateRequestDto requestDto) {
                UserResponeDto responseDto = userService.registerUser(requestDto);
                return ResponseEntity.ok(responseDto);
        }

        @Operation(summary = "닉네임 중복 확인", description = "회원가입 또는 프로필 수정 전 닉네임이 이미 사용 중인지 확인합니다. "
                        + "사용 가능하면 available: true, 이미 사용 중이면 available: false를 반환합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "중복 확인 완료", content = @Content(mediaType = "application/json", examples = {
                                        @ExampleObject(name = "사용 가능", value = "{\"available\": true, \"message\": \"사용 가능한 닉네임입니다.\"}"),
                                        @ExampleObject(name = "중복됨", value = "{\"available\": false, \"message\": \"이미 사용 중인 닉네임입니다.\"}")
                        })),
                        @ApiResponse(responseCode = "400", description = "닉네임 파라미터 누락", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"available\": false, \"message\": \"닉네임을 입력해주세요.\"}")))
        })
        @GetMapping("/check_nickname")
        public ResponseEntity<Map<String, Object>> checkNickname(
                        @io.swagger.v3.oas.annotations.Parameter(description = "중복 확인할 닉네임", example = "홍길동") @RequestParam String nickname) {

                if (nickname == null || nickname.trim().isEmpty()) {
                        return ResponseEntity.badRequest().body(Map.of(
                                        "available", false,
                                        "message", "닉네임을 입력해주세요."));
                }

                boolean isDuplicate = userService.isNicknameDuplicate(nickname);

                if (isDuplicate) {
                        return ResponseEntity.ok(Map.of(
                                        "available", false,
                                        "message", "이미 사용 중인 닉네임입니다."));
                } else {
                        return ResponseEntity.ok(Map.of(
                                        "available", true,
                                        "message", "사용 가능한 닉네임입니다."));
                }
        }
}