package DevFlow.OpenCloset_Back.Login.Login_Controller;

import DevFlow.OpenCloset_Back.Login.Dto.req.RefreshTokenRequestDto;
import DevFlow.OpenCloset_Back.Login.Jwt_Util.JwtUtil;
import DevFlow.OpenCloset_Back.Login.RefreshToken.RefreshTokenService;
import DevFlow.OpenCloset_Back.Login.Dto.res.TokenRefreshResponseDto;
import DevFlow.OpenCloset_Back.User.User_Repository.UserRepository;
import DevFlow.OpenCloset_Back.User.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class TokenRefreshController {

    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponseDto> refreshAccessToken(@RequestBody RefreshTokenRequestDto requestDto) {
        String refreshToken = requestDto.getRefreshToken();

        return refreshTokenService.findByToken(refreshToken)
                .map(token -> {
                    // email로 유저 찾기
                    User user = userRepository.findByEmail(token.getUsername())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

                    // 새 access token 생성 (role 포함)
                    String newAccessToken = JwtUtil.generateToken(user.getEmail(), "ROLE_USER");

                    return ResponseEntity.ok(new TokenRefreshResponseDto(newAccessToken));
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
    }
}
