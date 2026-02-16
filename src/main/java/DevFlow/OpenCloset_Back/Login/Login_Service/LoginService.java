package DevFlow.OpenCloset_Back.Login.Login_Service;

import DevFlow.OpenCloset_Back.Login.Dto.req.LoginRequestDto;
import DevFlow.OpenCloset_Back.Login.Dto.res.LoginResponseDto;
import DevFlow.OpenCloset_Back.Login.Jwt_Util.JwtUtil;
import DevFlow.OpenCloset_Back.Login.RefreshToken.RefreshTokenService;
import DevFlow.OpenCloset_Back.User.User_Repository.UserRepository;
import DevFlow.OpenCloset_Back.User.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final BCryptPasswordEncoder passwordEncoder;

    public LoginResponseDto loginUser(LoginRequestDto requestDto) {
        Optional<User> optionalUser = userRepository.findByEmail(requestDto.getEmail());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
                // 로그인한 유저에게 role 부여 (현재는 기본적으로 ROLE_USER로 고정)
                String accessToken = JwtUtil.generateToken(user.getEmail(), "ROLE_USER");
                String refreshToken = UUID.randomUUID().toString();

                refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken);

                return new LoginResponseDto(
                        user.getEmail(),
                        user.getNickname(),
                        "Login successful",
                        accessToken,
                        refreshToken);
            }
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    public void logoutUser(HttpSession session) {
        session.invalidate();
    }
}
