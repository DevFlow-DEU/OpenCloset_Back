package DevFlow.OpenCloset_Back.Search.SearchService;

import DevFlow.OpenCloset_Back.Board.Repository.BoardRepository;
import DevFlow.OpenCloset_Back.Board.entity.Board;
import DevFlow.OpenCloset_Back.Search.Dto.req.BoardSearchRequestDto;
import DevFlow.OpenCloset_Back.Search.Dto.res.BoardSearchResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SearchService {
    private final BoardRepository boardRepository;

    public SearchService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public List<BoardSearchResponseDto> searchBoards(BoardSearchRequestDto req, String address) {
        System.out.println("title: '" + req.getTitle() + "'");
        System.out.println("desc : '" + req.getDescription() + "'");
        System.out.println("size: '" + req.getSize() + "'");

        try {
            List<Board> results = boardRepository.searchBoards(
                    emptyToNull(req.getTitle()),
                    emptyToNull(req.getDescription()),
                    emptyToNull(req.getSize()),
                    address
            );

            System.out.println("검색 결과 개수: " + results.size());

            return results.stream()
                    .map(BoardSearchResponseDto::new)
                    .toList();
        } catch (Exception e) {
            System.out.println("오류 발생: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    private String emptyToNull(String input) {
        if (input == null) return null;
        input = input.trim();
        return (input.isEmpty() || input.equalsIgnoreCase("null")) ? null : input;
    }

}
