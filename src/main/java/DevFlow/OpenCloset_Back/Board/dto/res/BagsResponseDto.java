package DevFlow.OpenCloset_Back.Board.dto.res;

import DevFlow.OpenCloset_Back.Board.entity.Bag;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BagsResponseDto {
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

    public BagsResponseDto(Bag bag) {
        this.id = bag.getBoard().getId();
        this.title = bag.getBoard().getTitle();
        this.description = bag.getBoard().getDescription();
        this.image = bag.getBoard().getImage();
        this.size = bag.getBoard().getSize();
        this.sex = bag.getBoard().getSex();
        this.place = bag.getBoard().getPlace();
        this.category = bag.getBoard().getCategory();
        this.date = bag.getBoard().getDate();
        this.price = bag.getBoard().getPrice();
        this.createAt = bag.getBoard().getCreatedAt();
    }
}
