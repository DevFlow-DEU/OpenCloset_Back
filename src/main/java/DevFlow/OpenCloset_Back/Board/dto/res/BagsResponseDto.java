package DevFlow.OpenCloset_Back.Board.dto.res;

import DevFlow.OpenCloset_Back.Board.entity.Bag;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class BagsResponseDto {
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
    private LocalDateTime createAt;

    public BagsResponseDto(Bag bag) {
        this.id = bag.getBoard().getId();
        this.title = bag.getBoard().getTitle();
        this.description = bag.getBoard().getDescription();
        this.images = bag.getBoard().getFullImageUrls();
        this.size = bag.getBoard().getSize();
        this.sex = bag.getBoard().getSex();
        this.latitude = bag.getBoard().getLatitude();
        this.longitude = bag.getBoard().getLongitude();
        this.category = bag.getBoard().getCategory();
        this.startDate = bag.getBoard().getStartDate();
        this.endDate = bag.getBoard().getEndDate();
        this.price = bag.getBoard().getPrice();
        this.status = bag.getBoard().getStatus();
        this.createAt = bag.getBoard().getCreatedAt();
    }
}
