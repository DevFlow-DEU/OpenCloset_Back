package DevFlow.OpenCloset_Back.Board.Repository;

import DevFlow.OpenCloset_Back.Board.entity.Shoes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoesRepository extends JpaRepository<Shoes, Long> {
}
