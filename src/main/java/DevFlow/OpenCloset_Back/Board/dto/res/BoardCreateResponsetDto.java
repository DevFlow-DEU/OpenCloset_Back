package DevFlow.OpenCloset_Back.Board.dto.res;

import DevFlow.OpenCloset_Back.Board.entity.Board;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class BoardCreateResponsetDto {
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

    public BoardCreateResponsetDto(Board entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.image = entity.getImage();
        this.size = entity.getSize();
        this.sex = entity.getSex();
        this.place = entity.getPlace();
        this.category =entity.getCategory();
        this.date = entity.getDate();
        this.price = entity.getPrice();
        this.createAt = entity.getCreatedAt();
    }
}
