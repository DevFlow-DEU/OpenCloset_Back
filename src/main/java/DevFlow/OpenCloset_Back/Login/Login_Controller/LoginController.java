package DevFlow.OpenCloset_Back.Login.Login_Controller;

import DevFlow.OpenCloset_Back.Login.Dto.req.LoginRequestDto;
import DevFlow.OpenCloset_Back.Login.Dto.res.LoginResponseDto;
import DevFlow.OpenCloset_Back.Login.Login_Service.LoginService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> loginUser(@RequestBody LoginRequestDto requestDto) {
        LoginResponseDto responseDto = loginService.loginUser(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        loginService.logoutUser(session);
        return ResponseEntity.ok("로그아웃 성공!");
    }
}
