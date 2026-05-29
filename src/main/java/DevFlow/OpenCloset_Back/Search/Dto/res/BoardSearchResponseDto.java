package DevFlow.OpenCloset_Back.Search.Dto.res;

import DevFlow.OpenCloset_Back.Board.entity.Board;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Getter
@Setter
@Schema(description = "검색 결과 개별 항목 DTO")
public class BoardSearchResponseDto {

    @Schema(description = "게시물 ID", example = "1")
    private Long id;

    @Schema(description = "게시물 제목", example = "나이키 바람막이 L사이즈")
    private String name;

    @Schema(description = "대여 기간 (일 수)", example = "7")
    private long rentalPeriod;

    @Schema(description = "대여 가격 (원)", example = "15000")
    private int rentalCost;

    @Schema(description = "거래 장소 위도", example = "35.1796")
    private Double latitude;

    @Schema(description = "거래 장소 경도", example = "129.0756")
    private Double longitude;

    @Schema(description = "이미지 URL 목록")
    private List<String> imageUrls;

    @Schema(description = "대여 시작일", example = "2026-05-01")
    private LocalDate startDate;

    @Schema(description = "대여 종료일", example = "2026-05-07")
    private LocalDate endDate;

    @Schema(description = "게시물 생성일")
    private LocalDateTime createdAt;

    @Schema(description = "상품 상태 (대여가능 / 대여중 / 반납완료)", example = "대여가능")
    private String status;

    @Schema(description = "카테고리", example = "outer")
    private String category;

    @Schema(description = "성별 (M / W / 공용)", example = "공용")
    private String sex;

    @Schema(description = "사이즈", example = "L")
    private String size;

    @Schema(description = "내가 찜한 상품인지 여부", example = "false")
    private boolean isWished;

    public BoardSearchResponseDto(Board board) {
        this.id = board.getId();
        this.name = board.getTitle();
        this.rentalPeriod = (board.getStartDate() != null && board.getEndDate() != null)
                ? ChronoUnit.DAYS.between(board.getStartDate(), board.getEndDate()) : 0;
        this.rentalCost = board.getPrice() != null ? board.getPrice().intValue() : 0;
        this.latitude = board.getLatitude();
        this.longitude = board.getLongitude();
        this.startDate = board.getStartDate();
        this.endDate = board.getEndDate();
        this.imageUrls = board.getFullImageUrls();
        this.createdAt = board.getCreatedAt();
        this.status = board.getStatus();
        this.category = board.getCategory();
        this.sex = board.getSex();
        this.size = board.getSize();
        this.isWished = false; // TODO: 찜 로직 구현 시 매핑
    }
}
