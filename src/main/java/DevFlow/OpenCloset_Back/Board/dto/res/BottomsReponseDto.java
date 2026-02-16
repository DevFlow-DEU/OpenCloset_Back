package DevFlow.OpenCloset_Back.Board.dto.res;

import DevFlow.OpenCloset_Back.Board.entity.Bottom;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BottomsReponseDto {
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
    
    public BottomsReponseDto(Bottom bottom){
        this.id = bottom.getBoard().getId();
        this.title = bottom.getBoard().getTitle();
        this.description = bottom.getBoard().getDescription();
        this.image = bottom.getBoard().getImage();
        this.size = bottom.getBoard().getSize();
        this.sex = bottom.getBoard().getSex();
        this.place = bottom.getBoard().getPlace();
        this.category =bottom.getBoard().getCategory();
        this.date = bottom.getBoard().getDate();
        this.price = bottom.getBoard().getPrice();
        this.createAt = bottom.getBoard().getCreatedAt();
    }
}
