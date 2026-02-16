package DevFlow.OpenCloset_Back.Board.dto.res;

import DevFlow.OpenCloset_Back.Board.entity.Top;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TopsResponseDto {
    private Long id;
    private String title;
    private String description;
    private String image;
    private String size;
    private String sex;
    private String place;
    private int date;
    private String category;
    private Long price;
    private LocalDateTime createAt;

    public TopsResponseDto(Top top) {
        this.id = top.getBoard().getId();
        this.title = top.getBoard().getTitle();
        this.description = top.getBoard().getDescription();
        this.image = top.getBoard().getImage();
        this.size = top.getBoard().getSize();
        this.sex = top.getBoard().getSex();
        this.place = top.getBoard().getPlace();
        this.category =top.getBoard().getCategory();
        this.date = top.getBoard().getDate();
        this.price = top.getBoard().getPrice();
        this.createAt = top.getBoard().getCreatedAt();
    }
}
