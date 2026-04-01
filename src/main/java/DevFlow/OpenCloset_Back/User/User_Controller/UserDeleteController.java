package DevFlow.OpenCloset_Back.User.User_Controller;

import DevFlow.OpenCloset_Back.User.User_Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "회원 탈퇴", description = "회원 탈퇴 API")
public class UserDeleteController {

    private final UserService userService;

    @Operation(summary = "회원 탈퇴", description = "현재 로그인된 사용자의 계정을 삭제합니다. JSON 바디로 'password'를 보내서 비밀번호가 일치해야 탈퇴됩니다. 관련된 게시글 및 데이터도 함께 삭제됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"회원탈퇴가 완료되었습니다.\"}"))),
            @ApiResponse(responseCode = "400", description = "비밀번호 불일치 또는 누락", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"비밀번호가 일치하지 않습니다.\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Unauthorized\"}")))
    })
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(
            Authentication authentication,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "탈퇴를 위한 비밀번호 입력",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"password\": \"mySecret123!\"}"))
            ) 
            @RequestBody Map<String, String> payload) {
        
        String email = authentication.getName();
        String password = payload.get("password");

        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "비밀번호를 입력해주세요."));
        }

        try {
            userService.deleteUser(email, password);
            return ResponseEntity.ok(Map.of("message", "회원탈퇴가 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}