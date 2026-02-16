package DevFlow.OpenCloset_Back.Login.RefreshToken;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    // 저장 또는 갱신
    public void saveRefreshToken(String username, String refreshToken) {
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUsername(username);

        if (existingToken.isPresent()) {
            RefreshToken token = existingToken.get();
            token.setRefreshToken(refreshToken);
            refreshTokenRepository.save(token);
        } else {
            RefreshToken token = RefreshToken.builder()
                    .username(username)
                    .refreshToken(refreshToken)
                    .build();
            refreshTokenRepository.save(token);
        }
    }

    // 유효한 토큰인지 확인
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByRefreshToken(token);
    }

    // 삭제
    public void deleteByUsername(String username) {
        refreshTokenRepository.deleteByUsername(username);
    }
}
