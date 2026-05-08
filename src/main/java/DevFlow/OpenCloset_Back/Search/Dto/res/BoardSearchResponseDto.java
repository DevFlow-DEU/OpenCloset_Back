package DevFlow.OpenCloset_Back.Search.Dto.res;

import DevFlow.OpenCloset_Back.Board.entity.Board;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Getter
@Setter
public class BoardSearchResponseDto {
    private Long id;
    private String name;
    private long rentalPeriod; // 대여 기간 (일 수)
    private int rentalCost;
    private Double latitude;
    private Double longitude;
    private List<String> imageUrls;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;

    public BoardSearchResponseDto(Board board) {
        this.id = board.getId();
        this.name = board.getTitle();
        this.rentalPeriod = (board.getStartDate() != null && board.getEndDate() != null)
                ? ChronoUnit.DAYS.between(board.getStartDate(), board.getEndDate()) : 0;
        this.rentalCost = board.getPrice() != null ? board.getPrice().intValue() : 0;
        this.latitude = board.getLatitude();
        this.longitude = board.getLongitude();
        this.startDate = board.getStartDate();
        this.endDate = board.getEndDate();
        this.imageUrls = board.getFullImageUrls();
        this.createdAt = board.getCreatedAt();
    }
}
