package DevFlow.OpenCloset_Back.Chat.ChatRoom.Entity;

import DevFlow.OpenCloset_Back.Board.entity.Board;
import DevFlow.OpenCloset_Back.User.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 게시물에 대한 채팅인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    // seller = 옷 주인 (게시물 작성자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    // wearer = 빌리려는 사람 (채팅을 건 사람)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wearer_id", nullable = false)
    private User wearer;

    private LocalDateTime createdAt;

    public ChatRoom(Board board, User seller, User wearer) {
        this.board = board;
        this.seller = seller;
        this.wearer = wearer;
        this.createdAt = LocalDateTime.now();
    }
}
