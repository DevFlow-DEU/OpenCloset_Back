package DevFlow.OpenCloset_Back.Board.Repository;

import DevFlow.OpenCloset_Back.Board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findAllByOrderByModifiedAtDesc();
    List<Board> findByUser_Address(String address);
    List<Board> findByUser_AddressOrderByModifiedAtDesc(String address);

    @Query("SELECT b FROM Board b " +
            "WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:description IS NULL OR LOWER(b.description) LIKE LOWER(CONCAT('%', :description, '%'))) " +
            "AND (:size IS NULL OR b.size = :size) " +
            "AND (:place IS NULL OR LOWER(b.place) LIKE LOWER(CONCAT('%', :place, '%'))) " +
            "AND b.user.address = :address " + // 주소 조건 추가
            "ORDER BY b.createdAt DESC")
    List<Board> searchBoards(
            @Param("title") String title,
            @Param("description") String description,
            @Param("size") String size,
            @Param("place") String place,
            @Param("address") String address // 주소 파라미터 추가
    );

}