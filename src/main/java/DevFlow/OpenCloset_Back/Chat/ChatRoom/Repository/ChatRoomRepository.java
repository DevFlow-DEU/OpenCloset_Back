package DevFlow.OpenCloset_Back.Chat.ChatRoom.Repository;

import DevFlow.OpenCloset_Back.Chat.ChatRoom.Entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByUser1IdOrUser2Id(Long user1Id, Long user2Id);

    void deleteByUser1IdOrUser2Id(Long user1Id, Long user2Id);
}
