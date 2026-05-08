package DevFlow.OpenCloset_Back.Board.dto.res;

import DevFlow.OpenCloset_Back.Board.entity.Jewelry;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class JewelryResponseDto {
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

    public JewelryResponseDto(Jewelry jewelry) {
        this.id = jewelry.getBoard().getId();
        this.title = jewelry.getBoard().getTitle();
        this.description = jewelry.getBoard().getDescription();
        this.images = jewelry.getBoard().getFullImageUrls();
        this.size = jewelry.getBoard().getSize();
        this.sex = jewelry.getBoard().getSex();
        this.latitude = jewelry.getBoard().getLatitude();
        this.longitude = jewelry.getBoard().getLongitude();
        this.category = jewelry.getBoard().getCategory();
        this.startDate = jewelry.getBoard().getStartDate();
        this.endDate = jewelry.getBoard().getEndDate();
        this.price = jewelry.getBoard().getPrice();
        this.status = jewelry.getBoard().getStatus();
        this.createAt = jewelry.getBoard().getCreatedAt();
    }
}
