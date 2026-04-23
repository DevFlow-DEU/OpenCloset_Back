package DevFlow.OpenCloset_Back.Board.dto.res;

import DevFlow.OpenCloset_Back.Board.entity.Bottom;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class BottomsReponseDto {
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

    public BottomsReponseDto(Bottom bottom) {
        this.id = bottom.getBoard().getId();
        this.title = bottom.getBoard().getTitle();
        this.description = bottom.getBoard().getDescription();
        this.image = bottom.getBoard().getImage();
        this.size = bottom.getBoard().getSize();
        this.sex = bottom.getBoard().getSex();
        this.latitude = bottom.getBoard().getLatitude();
        this.longitude = bottom.getBoard().getLongitude();
        this.category = bottom.getBoard().getCategory();
        this.startDate = bottom.getBoard().getStartDate();
        this.endDate = bottom.getBoard().getEndDate();
        this.price = bottom.getBoard().getPrice();
        this.status = bottom.getBoard().getStatus();
        this.createAt = bottom.getBoard().getCreatedAt();
    }
}
