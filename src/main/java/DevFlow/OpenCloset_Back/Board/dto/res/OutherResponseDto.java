package DevFlow.OpenCloset_Back.Board.dto.res;

import DevFlow.OpenCloset_Back.Board.entity.Outer_;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class OutherResponseDto {
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

    public OutherResponseDto(Outer_ outer) {
        this.id = outer.getBoard().getId();
        this.title = outer.getBoard().getTitle();
        this.description = outer.getBoard().getDescription();
        this.image = outer.getBoard().getImage();
        this.size = outer.getBoard().getSize();
        this.sex = outer.getBoard().getSex();
        this.latitude = outer.getBoard().getLatitude();
        this.longitude = outer.getBoard().getLongitude();
        this.category = outer.getBoard().getCategory();
        this.startDate = outer.getBoard().getStartDate();
        this.endDate = outer.getBoard().getEndDate();
        this.price = outer.getBoard().getPrice();
        this.status = outer.getBoard().getStatus();
        this.createAt = outer.getBoard().getCreatedAt();
    }
}
