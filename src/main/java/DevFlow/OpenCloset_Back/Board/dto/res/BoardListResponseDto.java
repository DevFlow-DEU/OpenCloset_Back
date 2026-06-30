package DevFlow.OpenCloset_Back.Board.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "상품 목록 래퍼 DTO (총 개수 + 상품 리스트)")
public class BoardListResponseDto {

    @Schema(description = "상품 목록")
    private List<BoardCreateResponsetDto> items;

    @Schema(description = "총 상품 수", example = "22")
    private long totalCount;
}
