package DevFlow.OpenCloset_Back.Search.SearchController;

import DevFlow.OpenCloset_Back.Search.Dto.req.BoardSearchRequestDto;
import DevFlow.OpenCloset_Back.Search.Dto.res.BoardSearchResponseDto;
import DevFlow.OpenCloset_Back.Search.SearchService.SearchService;
import DevFlow.OpenCloset_Back.User.User_Service.UserService;
import DevFlow.OpenCloset_Back.User.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {
    private final SearchService searchService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<List<BoardSearchResponseDto>> searchBoards(@RequestBody BoardSearchRequestDto request, @AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("SearchController 호출됨: " + request.getTitle());

        User loginUser = userService.findByUsername(userDetails.getUsername());
        String address = loginUser.getAddress();
        List<BoardSearchResponseDto> result = searchService.searchBoards(request,address);
        return ResponseEntity.ok(result);
    }


}
