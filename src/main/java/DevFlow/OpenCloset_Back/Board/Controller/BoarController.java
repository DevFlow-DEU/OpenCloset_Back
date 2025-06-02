package DevFlow.OpenCloset_Back.Board.Controller;

import DevFlow.OpenCloset_Back.Board.Service.BoardService;
import DevFlow.OpenCloset_Back.Board.dto.req.BoardCreateRequestDto;
import DevFlow.OpenCloset_Back.Board.dto.res.BoardCreateResponsetDto;
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
public class BoarController {
    private final BoardService boardService;
    private final UserRepository userRepository;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(BoarController.class);


    @GetMapping("/board/All")
    public List<BoardCreateResponsetDto> getPosts(
            @AuthenticationPrincipal UserDetails userDetails) {
        // 1. 로그인 유저의 username 추출
        String username = userDetails.getUsername();

        // 2. username으로 User 엔티티 조회 (UserService 활용)
        User loginUser = userService.findByUsername(username);

        // 3. 해당 유저의 address로 게시글 조회
        return boardService.getPostsByAddress(loginUser.getAddress());
    }

    @PostMapping("/board/create")
    public BoardCreateResponsetDto createBoard(@RequestBody BoardCreateRequestDto requestDto, @AuthenticationPrincipal UserDetails userDetails) {
       // User loginUser = userRepository.findByUsername(userDetails.getUsername())
               // .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        //User loginUser = userService.findByUsername(userDetails.getUsername());

        String username = userDetails.getUsername();
        logger.info("인증된 유저명: {}", username);

        User loginUser = userService.findByUsername(username);
        logger.info("조회된 유저 ID: {}", loginUser.getId());

        return boardService.createBoard(requestDto,loginUser);
    }

    @GetMapping("/board/{id}")  //게시물 지정 조회
    public BoardCreateResponsetDto getPost(@PathVariable Long id) {
        return boardService.getPost(id);
    }
}
