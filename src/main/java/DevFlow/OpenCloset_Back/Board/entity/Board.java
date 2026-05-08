package DevFlow.OpenCloset_Back.Board.entity;

import DevFlow.OpenCloset_Back.Board.dto.req.BoardCreateRequestDto;
import DevFlow.OpenCloset_Back.User.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class Board implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // 제목

    @Column(nullable = false)
    private String description; // 의류에 관한 설명

    // 다중 이미지 지원 - board_images 테이블이 자동 생성됨
    @ElementCollection
    @CollectionTable(name = "board_images", joinColumns = @JoinColumn(name = "board_id"))
    @Column(name = "image_path")
    private List<String> images = new ArrayList<>(); // 의류 사진 (여러 장)

    @Column(nullable = false)
    private String size; // 의류 사이즈

    @Column(nullable = false)
    private String sex; // 해당 의류 성별

    @Column(nullable = false)
    private Double latitude; // 위도

    @Column(nullable = false)
    private Double longitude; // 경도

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private LocalDate startDate; // 대여 시작일

    @Column(nullable = false)
    private LocalDate endDate; // 대여 종료일

    @Column(nullable = false)
    private String category;

    // 상품 상태 - default: 대여가능
    @Column(nullable = false)
    private String status; // 대여가능 / 대여중 / 반납완료

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime modifiedAt;

    // seller (판매자 = 게시물 올리는 사람 = 옷 주인)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    // buyer (구매자 = 옷을 빌리는 사람, 대여 전에는 null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = true)
    private User buyer;

    public Board(BoardCreateRequestDto req, List<String> imagePaths, User seller) {
        this.title = req.getTitle();
        this.description = req.getDescription();
        this.sex = req.getSex();
        this.images = imagePaths != null ? imagePaths : new ArrayList<>();
        this.size = req.getSize();
        this.latitude = req.getLatitude();
        this.longitude = req.getLongitude();
        this.price = req.getPrice();
        this.startDate = req.getStartDate();
        this.endDate = req.getEndDate();
        this.category = req.getCategory();
        this.status = "대여가능"; // 신규 게시물은 무조건 '대여가능' 상태
        this.seller = seller;
        this.buyer = null; // 아직 빌리는 사람 없음
    }

    // 이미지 경로에 도메인 URL을 붙여서 전체 URL 리스트로 반환
    public List<String> getFullImageUrls() {
        if (this.images == null || this.images.isEmpty()) {
            return List.of("https://opencloset.jihongeek.com/images/default_board.png");
        }
        return this.images.stream()
                .map(img -> img.startsWith("http") ? img : "https://opencloset.jihongeek.com" + img)
                .collect(Collectors.toList());
    }
}