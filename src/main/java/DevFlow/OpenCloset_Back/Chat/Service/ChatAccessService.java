package DevFlow.OpenCloset_Back.Chat.Service;

import DevFlow.OpenCloset_Back.Chat.ChatRoom.Entity.ChatRoom;
import DevFlow.OpenCloset_Back.Chat.ChatRoom.Repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ChatAccessService {
    private final ChatRoomRepository chatRoomRepository;

    @Transactional(readOnly = true)
    public ChatRoom requireParticipant(Long roomId, String email) {
        if (roomId == null || roomId <= 0) {
            throw new IllegalArgumentException("유효한 채팅방 ID가 필요합니다.");
        }
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));
        if (email == null || (!email.equals(room.getSeller().getEmail())
                && !email.equals(room.getWearer().getEmail()))) {
            throw new AccessDeniedException("본인이 참여한 채팅방만 접근할 수 있습니다.");
        }
        return room;
    }
}
