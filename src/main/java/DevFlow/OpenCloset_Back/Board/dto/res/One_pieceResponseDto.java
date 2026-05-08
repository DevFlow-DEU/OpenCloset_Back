package DevFlow.OpenCloset_Back.Board.dto.res;

import DevFlow.OpenCloset_Back.Board.entity.One_Piece;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class One_pieceResponseDto {
    private Long id;
    private String title;
    private String description;
    private List<String> images;
    private String size;
    private String sex;
    private Double latitude;
    private Double longitude;
    private LocalDate startDate;
    private LocalDate endDate;
    private String category;
    private Long price;
    private String status;
    private LocalDateTime createAt;

    public One_pieceResponseDto(One_Piece onePiece) {
        this.id = onePiece.getBoard().getId();
        this.title = onePiece.getBoard().getTitle();
        this.description = onePiece.getBoard().getDescription();
        this.images = onePiece.getBoard().getFullImageUrls();
        this.size = onePiece.getBoard().getSize();
        this.sex = onePiece.getBoard().getSex();
        this.latitude = onePiece.getBoard().getLatitude();
        this.longitude = onePiece.getBoard().getLongitude();
        this.category = onePiece.getBoard().getCategory();
        this.startDate = onePiece.getBoard().getStartDate();
        this.endDate = onePiece.getBoard().getEndDate();
        this.price = onePiece.getBoard().getPrice();
        this.status = onePiece.getBoard().getStatus();
        this.createAt = onePiece.getBoard().getCreatedAt();
    }
}
