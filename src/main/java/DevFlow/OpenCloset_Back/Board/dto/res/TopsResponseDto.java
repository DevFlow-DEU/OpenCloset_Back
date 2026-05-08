package DevFlow.OpenCloset_Back.Board.dto.res;

import DevFlow.OpenCloset_Back.Board.entity.Top;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class TopsResponseDto {
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

    public TopsResponseDto(Top top) {
        this.id = top.getBoard().getId();
        this.title = top.getBoard().getTitle();
        this.description = top.getBoard().getDescription();
        this.images = top.getBoard().getFullImageUrls();
        this.size = top.getBoard().getSize();
        this.sex = top.getBoard().getSex();
        this.latitude = top.getBoard().getLatitude();
        this.longitude = top.getBoard().getLongitude();
        this.category = top.getBoard().getCategory();
        this.startDate = top.getBoard().getStartDate();
        this.endDate = top.getBoard().getEndDate();
        this.price = top.getBoard().getPrice();
        this.status = top.getBoard().getStatus();
        this.createAt = top.getBoard().getCreatedAt();
    }
}
