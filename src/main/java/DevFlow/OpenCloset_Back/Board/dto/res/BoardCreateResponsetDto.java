package DevFlow.OpenCloset_Back.Board.dto.res;

import DevFlow.OpenCloset_Back.Board.entity.Board;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardCreateResponsetDto {
    private Long id;
    private String title;
    private String description;
    private String image;
    private String size;
    private Boolean sex;
    private String place;

    public BoardCreateResponsetDto(Board entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.image = entity.getImage();
        this.size = entity.getSize();
        //this.sex = entity.getSex();
        this.place = entity.getPlace();
    }
}
