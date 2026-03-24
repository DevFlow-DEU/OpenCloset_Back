package DevFlow.OpenCloset_Back.Board.Repository;

import DevFlow.OpenCloset_Back.Board.entity.Board;
import DevFlow.OpenCloset_Back.Board.entity.One_Piece;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface One_pieceRepository extends JpaRepository<One_Piece, Long> {
    void deleteByBoardIn(List<Board> boards);
}
