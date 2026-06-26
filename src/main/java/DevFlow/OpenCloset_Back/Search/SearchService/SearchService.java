package DevFlow.OpenCloset_Back.Search.SearchService;

import DevFlow.OpenCloset_Back.Board.Repository.BoardRepository;
import DevFlow.OpenCloset_Back.Board.entity.Board;
import DevFlow.OpenCloset_Back.Search.Dto.req.BoardSearchRequestDto;
import DevFlow.OpenCloset_Back.Search.Dto.res.BoardSearchResponseDto;
import DevFlow.OpenCloset_Back.Search.Dto.res.SearchResultResponseDto;
import DevFlow.OpenCloset_Back.Wishlist.Service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    private final BoardRepository boardRepository;
    private final WishlistService wishlistService;

    /**
     * 기본 검색 (기존 API 유지)
     * 필터: title, description, size + 주소 기반
     * 정렬: 최신순 고정
     */
    public List<BoardSearchResponseDto> searchBoards(BoardSearchRequestDto req, String address) {
        log.info("기본 검색 - title: '{}', desc: '{}', size: '{}'", req.getTitle(), req.getDescription(), req.getSize());

        try {
            List<Board> results = boardRepository.searchBoards(
                    emptyToNull(req.getTitle()),
                    emptyToNull(req.getDescription()),
                    emptyToNull(req.getSize()),
                    null,  // sex — 기본 검색에서는 미사용
                    null,  // category
                    null,  // minPrice
                    null,  // maxPrice
                    address,
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );

            log.info("기본 검색 결과: {}건", results.size());

            return results.stream()
                    .map(BoardSearchResponseDto::new)
                    .toList();
        } catch (Exception e) {
            log.error("기본 검색 오류: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 고급 검색
     * 필터: title, description, size, sex, category, minPrice, maxPrice + 주소 기반
     * 정렬: sort 파라미터에 따라 동적 (latest / expensive / cheap)
     * 찜 여부(isWished) 매핑 포함
     */
    public SearchResultResponseDto advancedSearch(BoardSearchRequestDto req, String sort, String address, Long userId) {
        Sort sortOrder = switch (sort) {
            case "expensive" -> Sort.by(Sort.Direction.DESC, "price");
            case "cheap" -> Sort.by(Sort.Direction.ASC, "price");
            default -> Sort.by(Sort.Direction.DESC, "createdAt"); // latest
        };

        List<Board> results = boardRepository.searchBoards(
                emptyToNull(req.getTitle()),
                emptyToNull(req.getDescription()),
                emptyToNull(req.getSize()),
                emptyToNull(req.getSex()),
                emptyToNull(req.getCategory()),
                req.getMinPrice(),
                req.getMaxPrice(),
                address,
                sortOrder
        );

        // 찜 여부 매핑
        Set<Long> wishedBoardIds = wishlistService.getWishedBoardIds(userId);

        List<BoardSearchResponseDto> items = results.stream()
                .map(board -> {
                    BoardSearchResponseDto dto = new BoardSearchResponseDto(board);
                    dto.setWished(wishedBoardIds.contains(board.getId()));
                    return dto;
                })
                .toList();

        return new SearchResultResponseDto(items, items.size());
    }

    private String emptyToNull(String input) {
        if (input == null) return null;
        input = input.trim();
        return (input.isEmpty() || input.equalsIgnoreCase("null")) ? null : input;
    }

}

