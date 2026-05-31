package DevFlow.OpenCloset_Back.Board.dto.res;

import DevFlow.OpenCloset_Back.Board.entity.Board;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "게시물 응답 DTO")
public class BoardCreateResponsetDto {
    @Schema(description = "게시물 ID", example = "1")
    private Long id;

    @Schema(description = "게시물 제목", example = "나이키 바람막이 L사이즈 빌려드려요!")
    private String title;

    @Schema(description = "게시물 상세 설명", example = "1회 실착 완전 새상품급입니다.")
    private String description;

    @Schema(description = "의류 이미지 URL 목록 (여러 장)")
    private List<String> images;

    @Schema(description = "옷 사이즈", example = "L")
    private String size;

    @Schema(description = "성별 (M / W / 공용)", example = "공용")
    private String sex;

    @Schema(description = "거래 장소 위도", example = "35.1796")
    private Double latitude;

    @Schema(description = "거래 장소 경도", example = "129.0756")
    private Double longitude;

    @Schema(description = "대여 시작일", example = "2026-05-01")
    private LocalDate startDate;

    @Schema(description = "대여 종료일", example = "2026-05-07")
    private LocalDate endDate;

    @Schema(description = "카테고리 (top / bottom / outer / one piece / jewelry / shoes / bag)", example = "outer")
    private String category;

    @Schema(description = "대여 가격 (원)", example = "15000")
    private Long price;

    @Schema(description = "상품 상태 (대여가능 / 대여중 / 반납완료)", example = "대여가능")
    private String status;

    @Schema(description = "판매자(seller) ID", example = "3")
    private Long sellerId;

    @Schema(description = "판매자 닉네임", example = "홍길동")
    private String sellerNickname;

    @Schema(description = "구매자(buyer) ID (대여 전에는 null)", example = "7", nullable = true)
    private Long buyerId;

    @Schema(description = "구매자 닉네임 (대여 전에는 null)", nullable = true)
    private String buyerNickname;

    @Schema(description = "게시물 생성일")
    private LocalDateTime createAt;

    @Schema(description = "내가 찜한 상품인지 여부 (로그인 안 했으면 기본 false)", example = "false")
    private boolean isWished;

    public void setIsWished(boolean isWished) {
        this.isWished = isWished;
    }

    public BoardCreateResponsetDto(Board entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.images = entity.getFullImageUrls();
        this.size = entity.getSize();
        this.sex = entity.getSex();
        this.latitude = entity.getLatitude();
        this.longitude = entity.getLongitude();
        this.category = entity.getCategory();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.price = entity.getPrice();
        this.status = entity.getStatus();
        this.sellerId = entity.getSeller().getId();
        this.sellerNickname = entity.getSeller().getNickname();
        this.buyerId = entity.getBuyer() != null ? entity.getBuyer().getId() : null;
        this.buyerNickname = entity.getBuyer() != null ? entity.getBuyer().getNickname() : null;
        this.createAt = entity.getCreatedAt();
        this.isWished = false; // TODO: 실제 로직 구현 시 유저별 찜 여부 매핑 필요
    }
}
