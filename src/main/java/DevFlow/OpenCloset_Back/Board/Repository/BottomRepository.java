package DevFlow.OpenCloset_Back.Board.Repository;

import DevFlow.OpenCloset_Back.Board.entity.Bottom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BottomRepository extends JpaRepository<Bottom, Long> {
}
