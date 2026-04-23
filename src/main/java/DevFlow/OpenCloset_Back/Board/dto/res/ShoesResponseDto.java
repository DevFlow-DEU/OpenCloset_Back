package DevFlow.OpenCloset_Back.Board.dto.res;

import DevFlow.OpenCloset_Back.Board.entity.Shoes;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class ShoesResponseDto {
    private Long id;
    private String title;
    private String description;
    private String image;
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

    public ShoesResponseDto(Shoes shoes) {
        this.id = shoes.getBoard().getId();
        this.title = shoes.getBoard().getTitle();
        this.description = shoes.getBoard().getDescription();
        this.image = shoes.getBoard().getImage();
        this.size = shoes.getBoard().getSize();
        this.sex = shoes.getBoard().getSex();
        this.latitude = shoes.getBoard().getLatitude();
        this.longitude = shoes.getBoard().getLongitude();
        this.category = shoes.getBoard().getCategory();
        this.startDate = shoes.getBoard().getStartDate();
        this.endDate = shoes.getBoard().getEndDate();
        this.price = shoes.getBoard().getPrice();
        this.status = shoes.getBoard().getStatus();
        this.createAt = shoes.getBoard().getCreatedAt();
    }
}
