package DevFlow.OpenCloset_Back.Wishlist;

import DevFlow.OpenCloset_Back.Board.dto.res.BoardCreateResponsetDto;
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

@Tag(name = "Wishlist API", description = "찜 기능 관련 API (현재 스웨거 명세용 초안)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/wishlist")
public class WishlistController {

    @Operation(summary = "게시물 찜하기/취소 (토글)", description = "[명세 초안] 특정 게시물을 찜하거나 찜 취소합니다. 토큰 인증이 필요합니다.")
    @ApiResponse(responseCode = "200", description = "찜 등록/취소 성공")
    @PostMapping("/{boardId}")
    public ResponseEntity<Map<String, Object>> toggleWishlist(
            @Parameter(description = "찜할 게시물의 ID", example = "1")
            @PathVariable Long boardId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // TODO: 실제 찜 로직 구현 (현재는 명세용 더미 응답 반환)
        return ResponseEntity.ok(Map.of(
                "message", "찜이 등록되었습니다. (더미)",
                "isWished", true
        ));
    }

    @Operation(summary = "내 찜 목록 조회", description = "[명세 초안] 로그인한 사용자가 찜한 모든 게시물 목록을 조회합니다. 토큰 인증이 필요합니다.")
    @ApiResponse(responseCode = "200", description = "찜 목록 조회 성공")
    @GetMapping("/my")
    public ResponseEntity<List<BoardCreateResponsetDto>> getMyWishlist(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // TODO: 실제 찜 목록 조회 로직 구현 (현재는 명세용 빈 리스트 반환)
        return ResponseEntity.ok(List.of());
    }
}
