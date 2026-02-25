package DevFlow.OpenCloset_Back.User.User_Controller;

import DevFlow.OpenCloset_Back.User.User_Service.UserService;
import DevFlow.OpenCloset_Back.User.dto.req.PasswordResetRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "비밀번호 재설정", description = "임시 비밀번호 발급 및 이메일 전송 API")
public class PasswordResetController {

    private final UserService userService;

    @Operation(summary = "임시 비밀번호 발급", description = "등록된 이메일로 임시 비밀번호를 생성하여 전송합니다. 기존 비밀번호는 임시 비밀번호로 변경됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "임시 비밀번호 전송 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"임시 비밀번호가 이메일로 전송되었습니다.\"}"))),
            @ApiResponse(responseCode = "400", description = "등록되지 않은 이메일", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"status\": 400, \"error\": \"Bad Request\", \"message\": \"사용자를 찾을 수 없습니다.\"}")))
    })
    @PostMapping("/password-reset")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody PasswordResetRequestDto requestDto) {
        userService.resetPassword(requestDto.getEmail());
        return ResponseEntity.ok(Map.of("message", "임시 비밀번호가 이메일로 전송되었습니다."));
    }
}
