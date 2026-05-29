package DevFlow.OpenCloset_Back.Search.Dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "검색 결과 래퍼 DTO (총 갯수 + 결과 리스트)")
public class SearchResultResponseDto {

    @Schema(description = "검색된 상품 목록")
    private List<BoardSearchResponseDto> items;

    @Schema(description = "총 검색 결과 수", example = "42")
    private long totalCount;
}
