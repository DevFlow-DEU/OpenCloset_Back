package DevFlow.OpenCloset_Back.Board.dto.res;

import DevFlow.OpenCloset_Back.Board.entity.Board;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class BoardCreateResponsetDto {
    private Long id;
    private String title;
    private String description;
    private List<String> images;
    private String size;
    private String sex;
    private Double latitude;
    private Double longitude;
    private LocalDate startDate;
    private LocalDate endDate;
    private String category;
    private Long price;
    private String status;
    private Long sellerId;
    private String sellerNickname;
    private Long buyerId;
    private String buyerNickname;
    private LocalDateTime createAt;

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
    }
}
