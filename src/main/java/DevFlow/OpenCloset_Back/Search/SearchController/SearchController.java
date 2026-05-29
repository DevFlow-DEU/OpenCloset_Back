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
            summary = "게시물 검색 (기존 - 로직 구현됨 ✅)",
            description = "키워드, 성별, 사이즈 등 기본 조건으로 검색합니다. "
                    + "로그인한 유저의 주소 기반으로 필터링됩니다. (토큰 인증 필수)"
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
            summary = "게시물 고급 검색 (명세 초안 - 로직 미구현 ❌)",
            description = "[명세 초안] 키워드 + 필터(성별, 가격범위, 사이즈, 카테고리) + 정렬(최신순/비싼순/싼순) 기능이 추가된 검색 API입니다. "
                    + "응답에 총 검색 결과 갯수(totalCount)가 포함됩니다. (토큰 인증 필수)"
    )
    @ApiResponse(responseCode = "200", description = "검색 성공")
    @PostMapping("/advanced")
    public ResponseEntity<SearchResultResponseDto> advancedSearch(
            @RequestBody BoardSearchRequestDto request,
            @Parameter(description = "정렬 기준 (latest: 최신순, expensive: 비싼순, cheap: 싼순)", example = "latest")
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @AuthenticationPrincipal UserDetails userDetails) {

        // TODO: 고급 검색 로직 구현 예정
        return ResponseEntity.ok(new SearchResultResponseDto(List.of(), 0));
    }
}
