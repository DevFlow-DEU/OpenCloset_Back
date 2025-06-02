package DevFlow.OpenCloset_Back.Board.Repository;

import DevFlow.OpenCloset_Back.Board.entity.Jewelry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JewelryRepossitory extends JpaRepository<Jewelry,Long> {
}
