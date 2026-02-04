package DevFlow.OpenCloset_Back.User.User_Repository;

import DevFlow.OpenCloset_Back.User.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByAddress(String address);

    Optional<User> findByNickname(String nickname);
}
