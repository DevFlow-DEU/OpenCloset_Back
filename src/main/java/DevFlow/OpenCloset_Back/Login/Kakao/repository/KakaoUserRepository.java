package DevFlow.OpenCloset_Back.Login.Kakao.repository;

import DevFlow.OpenCloset_Back.Login.Kakao.entity.KakaoUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KakaoUserRepository extends JpaRepository<KakaoUser, Long> {
    Optional<KakaoUser> findByKakaoId(String kakaoId);
}
