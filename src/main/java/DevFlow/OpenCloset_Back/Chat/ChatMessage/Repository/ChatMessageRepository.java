package DevFlow.OpenCloset_Back.Chat.ChatMessage.Repository;

import DevFlow.OpenCloset_Back.Chat.ChatMessage.Entity.ChatMessage;
import DevFlow.OpenCloset_Back.Chat.ChatRoom.Entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    void deleteByChatRoomIn(List<ChatRoom> chatRooms);
}
