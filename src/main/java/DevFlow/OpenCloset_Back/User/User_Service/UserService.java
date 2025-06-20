package DevFlow.OpenCloset_Back.User.User_Service;

import DevFlow.OpenCloset_Back.Login.Dto.req.LoginRequestDto;
import DevFlow.OpenCloset_Back.Login.Dto.res.LoginResponseDto;
import DevFlow.OpenCloset_Back.Login.Jwt_Util.JwtUtil;
import DevFlow.OpenCloset_Back.User.User_Repository.UserRepository;
import DevFlow.OpenCloset_Back.User.dto.req.UserCreateRequestDto;
import DevFlow.OpenCloset_Back.User.dto.res.UserResponeDto;
import DevFlow.OpenCloset_Back.User.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserResponeDto registerUser(UserCreateRequestDto requestDto) {
        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(requestDto.getPassword());

        User user = new User();
        user.setUsername(requestDto.getUsername());
        user.setName(requestDto.getName());
        user.setPassword(encryptedPassword);
        user.setAddress(requestDto.getAddress());
        user.setAge(requestDto.getAge());

        userRepository.save(user);

        return new UserResponeDto(
                user.getUsername(),
                user.getAddress(),
                user.getName(),
                user.getAge());
    }
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}

