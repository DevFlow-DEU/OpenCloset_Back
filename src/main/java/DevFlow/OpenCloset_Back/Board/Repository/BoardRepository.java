package DevFlow.OpenCloset_Back.Board.Repository;

import DevFlow.OpenCloset_Back.Board.entity.Board;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
        List<Board> findAllByOrderByModifiedAtDesc();

        List<Board> findBySeller_Address(String address);

        List<Board> findBySeller_AddressOrderByModifiedAtDesc(String address);

        List<Board> findBySellerId(Long sellerId);

        void deleteBySellerId(Long sellerId);

        @Query("SELECT b FROM Board b " +
                        "WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
                        "AND (:description IS NULL OR LOWER(b.description) LIKE LOWER(CONCAT('%', :description, '%'))) "
                        +
                        "AND (:size IS NULL OR b.size = :size) " +
                        "AND (:sex IS NULL OR b.sex = :sex) " +
                        "AND (:category IS NULL OR b.category = :category) " +
                        "AND (:minPrice IS NULL OR b.price >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR b.price <= :maxPrice) " +
                        "AND b.seller.address = :address")
        List<Board> searchBoards(
                        @Param("title") String title,
                        @Param("description") String description,
                        @Param("size") String size,
                        @Param("sex") String sex,
                        @Param("category") String category,
                        @Param("minPrice") Long minPrice,
                        @Param("maxPrice") Long maxPrice,
                        @Param("address") String address,
                        Sort sort);

        // 상태관리: seller의 전체 게시물 조회 (최신순)
        List<Board> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

        // 상태관리: seller의 게시물을 상태별로 조회 (최신순)
        List<Board> findBySellerIdAndStatusOrderByCreatedAtDesc(Long sellerId, String status);

        // Owner 필터: seller이면서 buyer가 존재하는 게시물 (빌려준 이력이 있는 것)
        List<Board> findBySellerIdAndBuyerIsNotNullOrderByCreatedAtDesc(Long sellerId);

        // Owner 필터: seller이면서 buyer가 존재 + 상태별 필터
        List<Board> findBySellerIdAndBuyerIsNotNullAndStatusOrderByCreatedAtDesc(Long sellerId, String status);

        // Renter 필터: buyer인 게시물 전체 조회 (빌린 것)
        List<Board> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

        // Renter 필터: buyer인 게시물 상태별 조회
        List<Board> findByBuyerIdAndStatusOrderByCreatedAtDesc(Long buyerId, String status);

}