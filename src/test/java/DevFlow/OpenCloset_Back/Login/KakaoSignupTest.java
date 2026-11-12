package DevFlow.OpenCloset_Back.Login;

import DevFlow.OpenCloset_Back.Login.Dto.req.KakaoSignupRequestDto;
import DevFlow.OpenCloset_Back.Login.Kakao.service.KakaoService;
import DevFlow.OpenCloset_Back.Login.RefreshToken.RefreshTokenService;
import DevFlow.OpenCloset_Back.User.User_Repository.UserRepository;
import DevFlow.OpenCloset_Back.User.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KakaoSignupTest {
    private final UserRepository repository = mock(UserRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final KakaoService service = new KakaoService(repository, mock(RefreshTokenService.class), encoder);
    private User user;
    private KakaoSignupRequestDto request;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setKakaoId("123");
        user.setPassword("old-hash");
        when(repository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        request = new KakaoSignupRequestDto();
        request.setNickname(" 옷친구 ");
        request.setAddress(" 서울시 강남구 ");
        request.setPassword("password123!");
        request.setPasswordConfirm("password123!");
    }

    @Test
    void savesProfileAndHashesPassword() {
        var response = service.completeSignup(user.getEmail(), request);
        assertEquals("옷친구", response.getNickname());
        assertEquals("서울시 강남구", response.getAddress());
        assertTrue(encoder.matches(request.getPassword(), user.getPassword()));
        assertTrue(user.getKakaoSignupCompleted());
        verify(repository).save(user);
    }

    @Test
    void rejectsPasswordMismatchWithoutSaving() {
        request.setPasswordConfirm("different");
        assertThrows(IllegalArgumentException.class, () -> service.completeSignup(user.getEmail(), request));
        assertEquals("old-hash", user.getPassword());
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsOtherUsersNickname() {
        User other = new User();
        other.setId(2L);
        when(repository.findByNickname("옷친구")).thenReturn(Optional.of(other));
        assertThrows(IllegalArgumentException.class, () -> service.completeSignup(user.getEmail(), request));
        verify(repository, never()).save(any());
    }

    @Test
    void allowsOwnNickname() {
        when(repository.findByNickname("옷친구")).thenReturn(Optional.of(user));
        assertDoesNotThrow(() -> service.completeSignup(user.getEmail(), request));
    }

    @Test
    void rejectsCompletedSignupAndNonKakaoAccount() {
        user.setKakaoSignupCompleted(true);
        assertThrows(IllegalArgumentException.class, () -> service.completeSignup(user.getEmail(), request));
        user.setKakaoSignupCompleted(false);
        user.setKakaoId(null);
        assertThrows(IllegalArgumentException.class, () -> service.completeSignup(user.getEmail(), request));
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsMissingFieldsAndOversizePassword() {
        request.setAddress(" ");
        assertThrows(IllegalArgumentException.class, () -> service.completeSignup(user.getEmail(), request));
        request.setAddress("서울");
        request.setPassword("가".repeat(25));
        request.setPasswordConfirm(request.getPassword());
        assertThrows(IllegalArgumentException.class, () -> service.completeSignup(user.getEmail(), request));
        verify(repository, never()).save(any());
    }

    @Test
    void callbackReportsWhetherSignupIsRequired() {
        var info = new DevFlow.OpenCloset_Back.Login.Dto.res.KakaoUserInfoDto();
        info.setId(123L);
        when(repository.findByKakaoId("123")).thenReturn(Optional.of(user));
        assertTrue(service.loginOrRegister(info).isSignupRequired());
        user.setKakaoSignupCompleted(true);
        assertFalse(service.loginOrRegister(info).isSignupRequired());
    }

    @Test
    void callbackDoesNotAutomaticallyLinkAnExistingEmail() {
        var info = new DevFlow.OpenCloset_Back.Login.Dto.res.KakaoUserInfoDto();
        info.setId(456L);
        var account = new DevFlow.OpenCloset_Back.Login.Dto.res.KakaoUserInfoDto.KakaoAccount();
        account.setEmail(user.getEmail());
        info.setKakao_account(account);
        assertThrows(IllegalArgumentException.class, () -> service.loginOrRegister(info));
        verify(repository, never()).save(any());
    }

}
