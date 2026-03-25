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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.List;

@Tag(name = "Board API", description = "게시물 관련 API")
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
            return boardService.getPosts();
        }
    }

    @Operation(summary = "새 게시물 생성", description = "새로운 옷 대여 게시물을 작성. (토큰 인증 필수)")
    @ApiResponse(responseCode = "200", description = "게시물 생성 성공")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BoardCreateResponsetDto createBoard(@ModelAttribute BoardCreateRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

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
