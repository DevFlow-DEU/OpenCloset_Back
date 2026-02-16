package DevFlow.OpenCloset_Back.Board.dto.res;

import DevFlow.OpenCloset_Back.Board.entity.Shoes;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ShoesResponseDto {
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

    public ShoesResponseDto(Shoes shoes){
        this.id = shoes.getBoard().getId();
        this.title = shoes.getBoard().getTitle();
        this.description = shoes.getBoard().getDescription();
        this.image = shoes.getBoard().getImage();
        this.size = shoes.getBoard().getSize();
        this.sex = shoes.getBoard().getSex();
        this.place = shoes.getBoard().getPlace();
        this.category =shoes.getBoard().getCategory();
        this.date = shoes.getBoard().getDate();
        this.price = shoes.getBoard().getPrice();
        this.createAt = shoes.getBoard().getCreatedAt();
    }
}
