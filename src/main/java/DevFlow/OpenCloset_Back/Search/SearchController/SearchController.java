package DevFlow.OpenCloset_Back.Search.SearchController;

import DevFlow.OpenCloset_Back.Search.Dto.req.BoardSearchRequestDto;
import DevFlow.OpenCloset_Back.Search.Dto.res.BoardSearchResponseDto;
import DevFlow.OpenCloset_Back.Search.Dto.res.SearchResultResponseDto;
import DevFlow.OpenCloset_Back.Search.SearchService.SearchService;
import DevFlow.OpenCloset_Back.User.User_Service.UserService;
import DevFlow.OpenCloset_Back.User.entity.User;
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

@Tag(name = "Search API", description = "게시물 검색 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {
    private final SearchService searchService;
    private final UserService userService;

    @Operation(
            summary = "게시물 기본 검색 (로직 구현됨 ✅)",
            description = "키워드(제목/설명) + 사이즈 조건으로 검색합니다. "
                    + "필터(성별, 가격, 카테고리)와 정렬 기능이 필요하면 /search/advanced API를 사용하세요. "
                    + "로그인한 유저의 주소 기반으로 필터링됩니다. 정렬: 최신순 고정. (토큰 인증 필수)"
    )
    @ApiResponse(responseCode = "200", description = "검색 성공")
    @PostMapping
    public ResponseEntity<List<BoardSearchResponseDto>> searchBoards(@RequestBody BoardSearchRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("SearchController 호출됨: " + request.getTitle());

        User loginUser = userService.findByEmail(userDetails.getUsername());
        String address = loginUser.getAddress();
        List<BoardSearchResponseDto> result = searchService.searchBoards(request, address);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "게시물 고급 검색 — 필터 + 정렬 (로직 구현됨 ✅)",
            description = "앱 검색 결과 화면에서 사용하는 API입니다. "
                    + "[필터] 성별(M/W/공용), 가격범위(minPrice~maxPrice), 사이즈(S/M/L/XL/FREE), "
                    + "카테고리(top/bottom/outer/one piece/jewelry/shoes/bag). "
                    + "[정렬] sort 파라미터 — latest(최신순), expensive(비싼순), cheap(싼순). "
                    + "[응답] 총 검색 결과 수(totalCount) + 상품 목록(items) + 찜 여부(isWished) 매핑. "
                    + "모든 필터는 선택사항이며, 보내지 않으면 해당 조건은 무시됩니다. "
                    + "로그인한 유저의 주소 기반으로 필터링됩니다. (토큰 인증 필수)"
    )
    @ApiResponse(responseCode = "200", description = "검색 성공")
    @PostMapping("/advanced")
    public ResponseEntity<SearchResultResponseDto> advancedSearch(
            @RequestBody BoardSearchRequestDto request,
            @Parameter(description = "정렬 기준 (latest: 최신순, expensive: 비싼순, cheap: 싼순)", example = "latest")
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @AuthenticationPrincipal UserDetails userDetails) {

        User loginUser = userService.findByEmail(userDetails.getUsername());
        String address = loginUser.getAddress();
        SearchResultResponseDto result = searchService.advancedSearch(request, sort, address, loginUser.getId());
        return ResponseEntity.ok(result);
    }
}
