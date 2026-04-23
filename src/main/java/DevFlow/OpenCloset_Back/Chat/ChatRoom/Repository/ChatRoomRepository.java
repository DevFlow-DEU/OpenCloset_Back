package DevFlow.OpenCloset_Back.Chat.ChatRoom.Repository;

import DevFlow.OpenCloset_Back.Chat.ChatRoom.Entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 내가 참여한 모든 채팅방 (seller이든 wearer이든)
    List<ChatRoom> findBySellerIdOrWearerId(Long sellerId, Long wearerId);

    // 특정 게시물에 대한 모든 채팅방 (= 해당 상품에 관심있는 wearer 리스트)
    List<ChatRoom> findByBoardId(Long boardId);

    // 회원 탈퇴 시 채팅방 삭제
    void deleteBySellerIdOrWearerId(Long sellerId, Long wearerId);
}
