package DevFlow.OpenCloset_Back.Board.Repository;

import DevFlow.OpenCloset_Back.Board.entity.Bag;
import DevFlow.OpenCloset_Back.Board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BagRepository extends JpaRepository<Bag, Long> {
    void deleteByBoardIn(List<Board> boards);
}
