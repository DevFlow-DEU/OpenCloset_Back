package DevFlow.OpenCloset_Back.User.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "내 상품 목록 응답 DTO")
public class MyProductResponseDto {

    @Schema(description = "상품 ID", example = "1")
    private Long productId;

    @Schema(description = "상품 제목", example = "나이키 에어맥스 90")
    private String title;

    @Schema(description = "상품 가격", example = "50000")
    private int price;

    @Schema(description = "상품 상태 (판매중, 예약중, 판매완료)", example = "판매중")
    private String status;

    @Schema(description = "상품 이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "등록 일시", example = "2026-03-19T10:00:00")
    private String createdAt;
}
