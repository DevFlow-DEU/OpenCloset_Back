package DevFlow.OpenCloset_Back.Board.Repository;

import DevFlow.OpenCloset_Back.Board.entity.Board;
import DevFlow.OpenCloset_Back.Board.entity.Shoes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoesRepository extends JpaRepository<Shoes, Long> {
    void deleteByBoardIn(List<Board> boards);
}
