package DevFlow.OpenCloset_Back.Wishlist.Repository;

import DevFlow.OpenCloset_Back.Wishlist.Entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // 유저 + 게시물로 찜 데이터 조회
    Optional<Wishlist> findByUserIdAndBoardId(Long userId, Long boardId);

    // 유저가 특정 게시물을 찜했는지 여부
    boolean existsByUserIdAndBoardId(Long userId, Long boardId);

    // 유저의 전체 찜 목록 (최신순)
    List<Wishlist> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 특정 게시물의 찜 개수
    long countByBoardId(Long boardId);

    // 유저 ID 기준 전체 삭제 (회원 탈퇴 시)
    void deleteByUserId(Long userId);

    // 게시물 ID 기준 전체 삭제 (게시물 삭제 시)
    void deleteByBoardId(Long boardId);

    // 특정 유저가 찜한 게시물 ID 목록 조회 (성능 최적화용)
    List<Wishlist> findByUserId(Long userId);
}
