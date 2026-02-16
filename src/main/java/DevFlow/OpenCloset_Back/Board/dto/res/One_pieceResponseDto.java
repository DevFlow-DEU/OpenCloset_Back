package DevFlow.OpenCloset_Back.Board.dto.res;

import DevFlow.OpenCloset_Back.Board.entity.One_Piece;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class One_pieceResponseDto {
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

    public One_pieceResponseDto(One_Piece one_piece){
        this.id = one_piece.getBoard().getId();
        this.title = one_piece.getBoard().getTitle();
        this.description = one_piece.getBoard().getDescription();
        this.image = one_piece.getBoard().getImage();
        this.size = one_piece.getBoard().getSize();
        this.sex = one_piece.getBoard().getSex();
        this.place = one_piece.getBoard().getPlace();
        this.category =one_piece.getBoard().getCategory();
        this.date = one_piece.getBoard().getDate();
        this.price = one_piece.getBoard().getPrice();
        this.createAt = one_piece.getBoard().getCreatedAt();
    }
}
