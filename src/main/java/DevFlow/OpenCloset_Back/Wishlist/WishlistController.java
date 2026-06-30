package DevFlow.OpenCloset_Back.Wishlist;

import DevFlow.OpenCloset_Back.Board.dto.res.BoardCreateResponsetDto;
import DevFlow.OpenCloset_Back.Board.dto.res.BoardListResponseDto;
import DevFlow.OpenCloset_Back.User.User_Service.UserService;
import DevFlow.OpenCloset_Back.User.entity.User;
import DevFlow.OpenCloset_Back.Wishlist.Service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Wishlist API", description = "찜 기능 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserService userService;

    @Operation(summary = "게시물 찜하기/취소 (토글) (로직 구현됨 ✅)", description = "특정 게시물을 찜하거나 찜 취소합니다. "
            + "이미 찜한 상태이면 찜 취소, 찜하지 않은 상태이면 찜 등록. (토큰 인증 필수)")
    @ApiResponse(responseCode = "200", description = "찜 등록/취소 성공")
    @PostMapping("/{boardId}")
    public ResponseEntity<Map<String, Object>> toggleWishlist(
            @Parameter(description = "찜할 게시물의 ID", example = "1")
            @PathVariable Long boardId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByEmail(userDetails.getUsername());
        boolean isWished = wishlistService.toggleWishlist(user, boardId);

        String message = isWished ? "찜이 등록되었습니다." : "찜이 취소되었습니다.";
        return ResponseEntity.ok(Map.of(
                "message", message,
                "isWished", isWished
        ));
    }

    @Operation(summary = "내 찜 목록 조회 (로직 구현됨 ✅)", description = "로그인한 사용자가 찜한 게시물 목록을 최신순으로 조회합니다. "
            + "status 파라미터로 대여 상태 필터링 가능 (대여가능 / 예약중 / 대여중 / 대여완료), "
            + "category 파라미터로 의류 카테고리 필터링 가능 (top / bottom / outer / one piece / jewelry / shoes / bag). "
            + "응답에 총 상품 수(totalCount)가 포함됩니다. (토큰 인증 필수)")
    @ApiResponse(responseCode = "200", description = "찜 목록 조회 성공")
    @GetMapping("/my")
    public ResponseEntity<BoardListResponseDto> getMyWishlist(
            @Parameter(description = "상태 필터 (대여가능 / 예약중 / 대여중 / 대여완료). 비워두면 전체 조회", example = "대여가능")
            @RequestParam(required = false) String status,
            @Parameter(description = "카테고리 필터 (top / bottom / outer / one piece / jewelry / shoes / bag). 비워두면 전체 조회", example = "outer")
            @RequestParam(required = false) String category,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByEmail(userDetails.getUsername());
        List<BoardCreateResponsetDto> wishlist = wishlistService.getMyWishlist(user, status, category);
        return ResponseEntity.ok(new BoardListResponseDto(wishlist, wishlist.size()));
    }
}
