package DevFlow.OpenCloset_Back.Wishlist.Service;

import DevFlow.OpenCloset_Back.Board.Repository.BoardRepository;
import DevFlow.OpenCloset_Back.Board.dto.res.BoardCreateResponsetDto;
import DevFlow.OpenCloset_Back.Board.entity.Board;
import DevFlow.OpenCloset_Back.User.entity.User;
import DevFlow.OpenCloset_Back.Wishlist.Entity.Wishlist;
import DevFlow.OpenCloset_Back.Wishlist.Repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final BoardRepository boardRepository;

    /**
     * 찜 토글 (찜 등록 / 취소)
     * 이미 찜했으면 취소, 안 했으면 등록
     *
     * @return true = 찜 등록됨, false = 찜 취소됨
     */
    @Transactional
    public boolean toggleWishlist(User user, Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다. id=" + boardId));

        Optional<Wishlist> existing = wishlistRepository.findByUserIdAndBoardId(user.getId(), boardId);

        if (existing.isPresent()) {
            // 이미 찜한 상태 → 찜 취소
            wishlistRepository.delete(existing.get());
            return false;
        } else {
            // 찜 안 한 상태 → 찜 등록
            Wishlist wishlist = new Wishlist(user, board);
            wishlistRepository.save(wishlist);
            return true;
        }
    }

    /**
     * 내 찜 목록 조회
     * 찜한 게시물 목록을 BoardCreateResponsetDto로 반환 (isWished = true)
     */
    @Transactional(readOnly = true)
    public List<BoardCreateResponsetDto> getMyWishlist(User user) {
        List<Wishlist> wishlists = wishlistRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return wishlists.stream()
                .map(w -> {
                    BoardCreateResponsetDto dto = new BoardCreateResponsetDto(w.getBoard());
                    dto.setIsWished(true); // 내 찜 목록이므로 전부 true
                    return dto;
                })
                .toList();
    }

    /**
     * 특정 유저가 특정 게시물을 찜했는지 여부
     */
    @Transactional(readOnly = true)
    public boolean isWished(Long userId, Long boardId) {
        return wishlistRepository.existsByUserIdAndBoardId(userId, boardId);
    }

    /**
     * 유저가 찜한 게시물 ID Set 조회 (목록 조회 시 일괄 매핑용)
     */
    @Transactional(readOnly = true)
    public Set<Long> getWishedBoardIds(Long userId) {
        return wishlistRepository.findByUserId(userId).stream()
                .map(w -> w.getBoard().getId())
                .collect(Collectors.toSet());
    }
}
