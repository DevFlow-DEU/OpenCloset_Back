package DevFlow.OpenCloset_Back.Search.Dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "게시물 검색 요청 DTO")
public class BoardSearchRequestDto {

    @Schema(description = "검색 키워드 (제목에서 검색)", example = "나이키")
    private String title;

    @Schema(description = "검색 키워드 (설명에서 검색)", example = "바람막이")
    private String description;

    @Schema(description = "성별 필터 (M / W / 공용)", example = "M")
    private String sex;

    @Schema(description = "사이즈 필터 (S / M / L / XL / FREE 등)", example = "L")
    private String size;

    @Schema(description = "카테고리 필터 (top / bottom / outer / one piece / jewelry / shoes / bag)", example = "outer")
    private String category;

    @Schema(description = "최소 가격 필터 (원)", example = "5000")
    private Long minPrice;

    @Schema(description = "최대 가격 필터 (원)", example = "30000")
    private Long maxPrice;
}
