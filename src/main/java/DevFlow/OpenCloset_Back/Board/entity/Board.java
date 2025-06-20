package DevFlow.OpenCloset_Back.Board.entity;

import DevFlow.OpenCloset_Back.Board.dto.req.BoardCreateRequestDto;
import DevFlow.OpenCloset_Back.User.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class Board implements Serializable { //게시물 id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;   //제목

    @Column(nullable = false)
    private String description; //의류에 관한 설명

    @Column(nullable = false)
    private String image;   //의류 사진 (url로 단순 db 삽입 예정)

    @Column(nullable = false)
    private String size;    //의류 사이즈

    @Column(nullable = false)
    private String sex;    //해당 의류 성별

    @Column(nullable = false)
    private String place;   //거래 장소

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer date;

    @Column(nullable = false)
    private String category;

    @CreatedDate
    private LocalDateTime createdAt;    //엔티티가 생성되어 저장될 때 시간을 자동 저장

    @LastModifiedDate
    private LocalDateTime modifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Board(BoardCreateRequestDto req, User user){
        this.title = req.getTitle();
        this.description = req.getDescription();
        this.sex = req.getSex();
        this.image = req.getImage();
        this.size = req.getSize();
        this.place = req.getPlace();
        this.price = req.getPrice();
        this.date = req.getDate();
        this.category = req.getCategory();
        this.user = user;
    }
}