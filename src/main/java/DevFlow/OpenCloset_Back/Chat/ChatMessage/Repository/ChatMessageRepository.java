package DevFlow.OpenCloset_Back.Chat.ChatMessage.Repository;

import DevFlow.OpenCloset_Back.Chat.ChatMessage.Entity.ChatMessage;
import DevFlow.OpenCloset_Back.Chat.ChatRoom.Entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 회원 탈퇴 시 채팅 메시지 일괄 삭제
    void deleteByChatRoomIn(List<ChatRoom> chatRooms);

    // 채팅방의 메시지 내역 (시간순)
    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(Long chatRoomId);

    // 채팅방의 안 읽은 메시지 수 (상대방이 보낸 것만)
    long countByChatRoomIdAndSenderIdNotAndIsReadFalse(Long chatRoomId, Long myUserId);

    // 채팅방의 가장 최근 메시지
    Optional<ChatMessage> findTopByChatRoomIdOrderBySentAtDesc(Long chatRoomId);

    // 안 읽은 메시지 읽음 처리 (상대방이 보낸 것만)
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.chatRoom.id = :roomId AND m.sender.id <> :userId AND m.isRead = false")
    void markAllAsRead(@Param("roomId") Long roomId, @Param("userId") Long userId);
}
