package DevFlow.OpenCloset_Back.Board.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "게시물 생성 요청 DTO")
public class BoardCreateRequestDto {
    @Schema(description = "게시물 제목", example = "나이키 바람막이 L사이즈 빌려드려요!")
    private String title;

    @Schema(description = "게시물 상세 설명", example = "1회 실착 완전 새상품급입니다. 비 오는 날 입기 좋아요.")
    private String description;

    @Schema(description = "게시물 이미지 파일 (첨부 안 할 시 기본 이미지 등록됨)", type = "string", format = "binary")
    private MultipartFile image;

    @Schema(description = "옷 사이즈 (예: S, M, L, XL, Free)", example = "L")
    private String size;

    @Schema(description = "성별 (예: M, W, 공용)", example = "공용")
    private String sex;

    @Schema(description = "거래 장소 위도 (latitude)", example = "35.1796")
    private Double latitude;

    @Schema(description = "거래 장소 경도 (longitude)", example = "129.0756")
    private Double longitude;

    @Schema(description = "대여 가격 (원 단위)", example = "15000")
    private Long price;

    @Schema(description = "대여 시작일 (yyyy-MM-dd)", example = "2026-05-01")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "대여 종료일 (yyyy-MM-dd)", example = "2026-05-07")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "옷 카테고리 (top, bottom, outer, one piece, jewelry, shoes, bag 중 택 1)", example = "outer")
    private String category;
}
