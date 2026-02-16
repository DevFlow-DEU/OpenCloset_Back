package DevFlow.OpenCloset_Back.Chat.ChatMessage.Entity;

import DevFlow.OpenCloset_Back.Chat.ChatRoom.Entity.ChatRoom;
import DevFlow.OpenCloset_Back.User.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ChatMessage {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private ChatRoom chatRoom;

    @ManyToOne
    private User sneder;

    private String message;
    private LocalDateTime sentAt;
}
