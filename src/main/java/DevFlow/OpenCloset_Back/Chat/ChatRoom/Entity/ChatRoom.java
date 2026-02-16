package DevFlow.OpenCloset_Back.Chat.ChatRoom.Entity;

import DevFlow.OpenCloset_Back.User.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data // Getter + Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자
@AllArgsConstructor // 모든 필드 생성자
public class ChatRoom {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user1;

    @ManyToOne
    private User user2;

    private LocalDateTime createdAt;

    public ChatRoom(User user1, User user2, LocalDateTime createdAt) {
        this.user1 = user1;
        this.user2 = user2;
        this.createdAt = createdAt;
    }

}

