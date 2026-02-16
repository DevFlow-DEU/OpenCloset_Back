package DevFlow.OpenCloset_Back.Search.Dto.res;

import DevFlow.OpenCloset_Back.Board.entity.Board;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BoardSearchResponseDto {
    private Long id;
    private String name;
    private int rentalPeriod;
    private int rentalCost;
    private String location;
    private String imageUrl;
    private LocalDateTime createdAt;

    public BoardSearchResponseDto(Board board) {
        this.id = board.getId();
        this.name = board.getTitle();
        this.rentalPeriod = board.getDate() != null ? board.getDate().intValue() : 0;
        this.rentalCost = board.getPrice() != null ? board.getPrice().intValue() : 0;
        this.location = board.getPlace();
        this.imageUrl = board.getImage();
        this.createdAt = board.getCreatedAt();
    }
}
