package DevFlow.OpenCloset_Back.Board.dto.res;

import DevFlow.OpenCloset_Back.Board.entity.Jewelry;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class JewelryResponseDto {
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

    public JewelryResponseDto(Jewelry jewelry){
        this.id = jewelry.getBoard().getId();
        this.title = jewelry.getBoard().getTitle();
        this.description = jewelry.getBoard().getDescription();
        this.image = jewelry.getBoard().getImage();
        this.size = jewelry.getBoard().getSize();
        this.sex = jewelry.getBoard().getSex();
        this.place = jewelry.getBoard().getPlace();
        this.category =jewelry.getBoard().getCategory();
        this.date = jewelry.getBoard().getDate();
        this.price = jewelry.getBoard().getPrice();
        this.createAt = jewelry.getBoard().getCreatedAt();
    }
}
