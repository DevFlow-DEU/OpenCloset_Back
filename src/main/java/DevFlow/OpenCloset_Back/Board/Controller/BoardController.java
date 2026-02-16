package DevFlow.OpenCloset_Back.Board.Controller;

import DevFlow.OpenCloset_Back.Board.Service.BoardService;
import DevFlow.OpenCloset_Back.Board.dto.req.BoardCreateRequestDto;
import DevFlow.OpenCloset_Back.Board.dto.res.*;
import DevFlow.OpenCloset_Back.Security.CustomUserDetailsService;
import DevFlow.OpenCloset_Back.User.User_Repository.UserRepository;
import DevFlow.OpenCloset_Back.User.User_Service.UserService;
import DevFlow.OpenCloset_Back.User.entity.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService;
    private final UserRepository userRepository;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(BoardController.class);
    private final CustomUserDetailsService customUserDetailsService;

    @GetMapping("/All") // 모든 게시물 조회
    public List<BoardCreateResponsetDto> getPosts(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            String email = userDetails.getUsername();
            User loginUser = userService.findByEmail(email);
            return boardService.getPostsByAddress(loginUser.getAddress());
        } else {
            // 로그인 안 한 경우 전체 게시글 반환 (원하면 아래 주석 해제)
            return boardService.getPosts();
            // throw new IllegalArgumentException("로그인이 필요합니다.");
        }
    }

    @PostMapping("/create")
    public BoardCreateResponsetDto createBoard(@RequestBody BoardCreateRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        logger.info("인증된 유저 이메일: {}", email);

        User loginUser = userService.findByEmail(email);
        logger.info("조회된 유저 ID: {}", loginUser.getId());

        return boardService.createBoard(requestDto, loginUser);
    }

    @GetMapping("/{id}") // 게시물 지정 조회
    public BoardCreateResponsetDto getPost(@PathVariable Long id) {
        return boardService.getPost(id);
    }

    @GetMapping("/top")
    public List<TopsResponseDto> getTops() {
        return boardService.getTops();
    }

    @GetMapping("/bottom")
    public List<BottomsReponseDto> getBottoms() {
        return boardService.getBottoms();
    }

    @GetMapping("/outher")
    public List<OutherResponseDto> getOuter() {
        return boardService.getOuters();
    }

    @GetMapping("/onepiece")
    public List<One_pieceResponseDto> getOnePieces() {
        return boardService.getOnePieces();
    }

    @GetMapping("/jewelry")
    public List<JewelryResponseDto> getJewelry() {
        return boardService.getJewelry();
    }

    @GetMapping("/shoes")
    public List<ShoesResponseDto> getShoes() {
        return boardService.getShoes();
    }
}
