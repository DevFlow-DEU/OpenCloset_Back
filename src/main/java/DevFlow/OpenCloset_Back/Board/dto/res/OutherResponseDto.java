package DevFlow.OpenCloset_Back.Board.dto.res;

import DevFlow.OpenCloset_Back.Board.entity.Outer_;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OutherResponseDto {
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

    public OutherResponseDto(Outer_ outer){
        this.id = outer.getBoard().getId();
        this.title = outer.getBoard().getTitle();
        this.description = outer.getBoard().getDescription();
        this.image = outer.getBoard().getImage();
        this.size = outer.getBoard().getSize();
        this.sex = outer.getBoard().getSex();
        this.place = outer.getBoard().getPlace();
        this.category =outer.getBoard().getCategory();
        this.date = outer.getBoard().getDate();
        this.price = outer.getBoard().getPrice();
        this.createAt = outer.getBoard().getCreatedAt();
    }
}
