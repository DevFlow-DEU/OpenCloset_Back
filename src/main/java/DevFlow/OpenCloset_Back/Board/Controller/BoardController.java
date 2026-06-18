package DevFlow.OpenCloset_Back.Board.Controller;

import DevFlow.OpenCloset_Back.Board.Service.BoardService;
import DevFlow.OpenCloset_Back.Board.dto.req.BoardCreateRequestDto;
import DevFlow.OpenCloset_Back.Board.dto.res.*;
import DevFlow.OpenCloset_Back.Security.CustomUserDetailsService;
import DevFlow.OpenCloset_Back.User.User_Repository.UserRepository;
import DevFlow.OpenCloset_Back.User.User_Service.UserService;
import DevFlow.OpenCloset_Back.User.entity.User;
import DevFlow.OpenCloset_Back.Wishlist.Service.WishlistService;
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
import java.util.Set;

@Tag(name = "Board API", description = "게시물 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final WishlistService wishlistService;
    private static final Logger logger = LoggerFactory.getLogger(BoardController.class);
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * DTO 리스트에 로그인 유저의 찜 여부를 일괄 매핑하는 헬퍼 메서드
     */
    private void applyWishedStatus(List<BoardCreateResponsetDto> dtos, UserDetails userDetails) {
        if (userDetails == null || dtos.isEmpty())
            return;
        User user = userService.findByEmail(userDetails.getUsername());
        Set<Long> wishedIds = wishlistService.getWishedBoardIds(user.getId());
        dtos.forEach(dto -> dto.setIsWished(wishedIds.contains(dto.getId())));
    }

    @Operation(summary = "전체 게시물 목록 조회", description = "모든 게시물을 최신순으로 조회합니다. 로그인 상태이면 같은 주소(address) 기반으로 필터링된 게시물을 반환하며, 찜 여부(isWished)가 매핑됩니다. 비로그인 시 전체 게시물을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "게시물 목록 조회 성공")
    @GetMapping("/All")
    public List<BoardCreateResponsetDto> getPosts(@AuthenticationPrincipal UserDetails userDetails) {
        List<BoardCreateResponsetDto> posts;
        if (userDetails != null) {
            String email = userDetails.getUsername();
            User loginUser = userService.findByEmail(email);
            posts = boardService.getPostsByAddress(loginUser.getAddress());
        } else {
            posts = boardService.getPosts();
        }
        applyWishedStatus(posts, userDetails);
        return posts;
    }

    @Operation(summary = "새 게시물 생성", description = "새로운 옷 대여 게시물을 작성. 작성자가 seller(판매자)로 자동 등록됨. (토큰 인증 필수)")
    @ApiResponse(responseCode = "200", description = "게시물 생성 성공")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BoardCreateResponsetDto createBoard(@ModelAttribute BoardCreateRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        String email = userDetails.getUsername();
        logger.info("인증된 유저 이메일: {}", email);

        User seller = userService.findByEmail(email);
        logger.info("조회된 판매자(seller) ID: {}", seller.getId());

        return boardService.createBoard(requestDto, seller);
    }

    @Operation(summary = "특정 게시물 상세 조회", description = "게시물 ID로 상세 정보를 조회합니다. seller/buyer 정보, 상태, 좌표, 대여기간, 이미지 목록, 찜 여부가 포함됩니다.")
    @ApiResponse(responseCode = "200", description = "게시물 조회 성공")
    @GetMapping("/{id}")
    public BoardCreateResponsetDto getPost(
            @io.swagger.v3.oas.annotations.Parameter(description = "게시물 ID", example = "1") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        BoardCreateResponsetDto dto = boardService.getPost(id);
        if (userDetails != null) {
            User user = userService.findByEmail(userDetails.getUsername());
            dto.setIsWished(wishlistService.isWished(user.getId(), id));
        }
        return dto;
    }

    @Operation(summary = "상의(top) 카테고리 목록 조회")
    @GetMapping("/top")
    public List<TopsResponseDto> getTops() {
        return boardService.getTops();
    }

    @Operation(summary = "하의(bottom) 카테고리 목록 조회")
    @GetMapping("/bottom")
    public List<BottomsReponseDto> getBottoms() {
        return boardService.getBottoms();
    }

    @Operation(summary = "아우터(outer) 카테고리 목록 조회")
    @GetMapping("/outher")
    public List<OutherResponseDto> getOuter() {
        return boardService.getOuters();
    }

    @Operation(summary = "원피스(onepiece) 카테고리 목록 조회")
    @GetMapping("/onepiece")
    public List<One_pieceResponseDto> getOnePieces() {
        return boardService.getOnePieces();
    }

    @Operation(summary = "주얼리(jewelry) 카테고리 목록 조회")
    @GetMapping("/jewelry")
    public List<JewelryResponseDto> getJewelry() {
        return boardService.getJewelry();
    }

    @Operation(summary = "신발(shoes) 카테고리 목록 조회")
    @GetMapping("/shoes")
    public List<ShoesResponseDto> getShoes() {
        return boardService.getShoes();
    }

    @Operation(summary = "가방(bag) 카테고리 목록 조회")
    @GetMapping("/bag")
    public List<BagsResponseDto> getBags() {
        return boardService.getBags();
    }

    // ============================================
    // 상태관리 API
    // ============================================

    @Operation(summary = "게시물 상태 변경", description = "게시물의 대여 상태를 변경합니다. "
            + "본인이 올린 게시물(seller)만 변경 가능합니다. "
            + "상태값: 대여가능 / 대여중 / 반납완료 (토큰 인증 필수)")
    @ApiResponse(responseCode = "200", description = "상태 변경 성공")
    @PatchMapping("/{id}/status")
    public BoardCreateResponsetDto updateStatus(
            @io.swagger.v3.oas.annotations.Parameter(description = "게시물 ID", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.Parameter(description = "변경할 상태값 (대여가능 / 대여중 / 반납완료)", example = "대여중") @RequestParam String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);
        return boardService.updateStatus(id, status, user);
    }

    @Operation(summary = "내 상품 상태별 필터링 조회", description = "로그인한 유저(seller)가 등록한 상품을 상태별로 필터링하여 조회합니다. "
            + "status 파라미터를 안 보내면 전체 조회. 찜 여부(isWished)가 매핑됩니다. (토큰 인증 필수)")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/my")
    public List<BoardCreateResponsetDto> getMyBoardsByStatus(
            @io.swagger.v3.oas.annotations.Parameter(description = "필터링할 상태값 (대여가능 / 대여중 / 반납완료). 비워두면 전체 조회", example = "대여중") @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);
        List<BoardCreateResponsetDto> boards = boardService.getMyBoards(user, status);
        applyWishedStatus(boards, userDetails);
        return boards;
    }

    // ============================================
    // Owner / Renter 필터 API
    // ============================================

    @Operation(summary = "Owner(빌려준) 상품 목록 조회 (로직 구현됨 ✅)", description = "로그인한 유저가 '빌려준' 상품(seller이면서 buyer가 존재하는 게시물) 목록을 조회합니다. "
            + "status 파라미터로 상태 필터링 가능. 찜 여부(isWished)가 매핑됩니다. (토큰 인증 필수)")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/my/owner")
    public List<BoardCreateResponsetDto> getOwnerBoards(
            @io.swagger.v3.oas.annotations.Parameter(description = "상태 필터 (대여가능 / 대여중 / 반납완료). 비워두면 전체", example = "대여중") @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);
        List<BoardCreateResponsetDto> boards = boardService.getOwnerBoards(user, status);
        applyWishedStatus(boards, userDetails);
        return boards;
    }

    @Operation(summary = "Renter(빌린) 상품 목록 조회 (로직 구현됨 ✅)", description = "로그인한 유저가 '빌린' 상품(buyer인 게시물) 목록을 조회합니다. "
            + "status 파라미터로 상태 필터링 가능. 찜 여부(isWished)가 매핑됩니다. (토큰 인증 필수)")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/my/renter")
    public List<BoardCreateResponsetDto> getRenterBoards(
            @io.swagger.v3.oas.annotations.Parameter(description = "상태 필터 (대여가능 / 대여중 / 반납완료). 비워두면 전체", example = "대여중") @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);
        List<BoardCreateResponsetDto> boards = boardService.getRenterBoards(user, status);
        applyWishedStatus(boards, userDetails);
        return boards;
    }
}
