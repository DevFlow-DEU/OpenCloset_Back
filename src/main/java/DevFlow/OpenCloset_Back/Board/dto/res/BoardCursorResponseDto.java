package DevFlow.OpenCloset_Back.Board.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "수정일 내림차순·ID 내림차순 상품 목록. 전체 개수는 집계하지 않습니다.")
public record BoardCursorResponseDto(
        @Schema(description = "현재 목록의 상품 (기본 20개, 최대 100개)") List<BoardCreateResponsetDto> items,
        @Schema(description = "다음 요청의 cursor에 그대로 전달하는 값. 마지막·빈 목록이면 null", types = {"string", "null"}) String nextCursor,
        @Schema(description = "다음 목록 존재 여부", example = "true") boolean hasNext) {
}
