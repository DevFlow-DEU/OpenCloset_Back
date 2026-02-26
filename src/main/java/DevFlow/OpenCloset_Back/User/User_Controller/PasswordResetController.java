package DevFlow.OpenCloset_Back.User.User_Controller;

import DevFlow.OpenCloset_Back.User.User_Service.UserService;
import DevFlow.OpenCloset_Back.User.dto.req.PasswordChangeRequestDto;
import DevFlow.OpenCloset_Back.User.dto.req.PasswordResetRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "비밀번호 관리", description = "임시 비밀번호 발급 및 비밀번호 변경 API")
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

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 확인한 후 새 비밀번호로 변경합니다. JWT 토큰이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"비밀번호가 성공적으로 변경되었습니다.\"}"))),
            @ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"status\": 400, \"error\": \"Bad Request\", \"message\": \"현재 비밀번호가 일치하지 않습니다.\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (토큰 없음 또는 만료)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Unauthorized\"}")))
    })
    @PutMapping("/password-change")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PasswordChangeRequestDto requestDto) {

        String email = userDetails.getUsername();
        userService.changePassword(email, requestDto.getCurrentPassword(), requestDto.getNewPassword());

        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
    }
}