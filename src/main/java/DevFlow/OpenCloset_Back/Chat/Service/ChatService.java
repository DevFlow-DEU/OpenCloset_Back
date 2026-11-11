package DevFlow.OpenCloset_Back.Chat.Service;

import DevFlow.OpenCloset_Back.Board.Repository.BoardRepository;
import DevFlow.OpenCloset_Back.Board.entity.Board;
import DevFlow.OpenCloset_Back.Chat.ChatMessage.Entity.ChatMessage;
import DevFlow.OpenCloset_Back.Chat.ChatMessage.Repository.ChatMessageRepository;
import DevFlow.OpenCloset_Back.Chat.ChatRoom.Entity.ChatRoom;
import DevFlow.OpenCloset_Back.Chat.ChatRoom.Repository.ChatRoomRepository;
import DevFlow.OpenCloset_Back.Chat.dto.res.ChatMessageResponseDto;
import DevFlow.OpenCloset_Back.Chat.dto.res.ChatRoomResponseDto;
import DevFlow.OpenCloset_Back.Chat.dto.res.WearerListResponseDto;
import DevFlow.OpenCloset_Back.Chat.Event.ChatMessageSentEvent;
import DevFlow.OpenCloset_Back.User.User_Repository.UserRepository;
import DevFlow.OpenCloset_Back.User.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final ChatAccessService chatAccessService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 채팅방 생성 (또는 기존 방 반환)
     * 같은 게시물 + 같은 wearer 조합이면 기존 방을 반환
     */
    @Transactional
    public ChatRoomResponseDto createRoom(Long boardId, User wearer) {
        if (boardId == null || boardId <= 0) {
            throw new IllegalArgumentException("유효한 게시물 ID가 필요합니다.");
        }
        Board board = boardRepository.findByIdForChatUpdate(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다. id=" + boardId));

        User seller = board.getSeller();

        // 본인 게시물에 채팅 불가
        if (seller.getId().equals(wearer.getId())) {
            throw new IllegalArgumentException("본인의 게시물에는 채팅을 걸 수 없습니다.");
        }

        // 이미 존재하는 채팅방이면 반환
        Optional<ChatRoom> existing = chatRoomRepository.findByBoardIdAndWearerId(boardId, wearer.getId());
        if (existing.isPresent()) {
            return toRoomResponseDto(existing.get(), wearer);
        }

        // 새 채팅방 생성
        ChatRoom room = new ChatRoom(board, seller, wearer);
        chatRoomRepository.save(room);

        log.info("채팅방 생성 - roomId: {}, boardId: {}, seller: {}, wearer: {}",
                room.getId(), boardId, seller.getNickname(), wearer.getNickname());

        return toRoomResponseDto(room, wearer);
    }

    /**
     * 내 채팅방 목록 조회
     * 마지막 메시지, 안 읽은 메시지 수 포함
     */
    @Transactional(readOnly = true)
    public List<ChatRoomResponseDto> getMyRooms(User user) {
        List<ChatRoom> rooms = chatRoomRepository.findBySellerIdOrWearerId(user.getId(), user.getId());

        return rooms.stream()
                .map(room -> toRoomResponseDto(room, user))
                .toList();
    }

    /**
     * 채팅 메시지 내역 조회
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDto> getMessages(Long roomId, User user) {
        chatAccessService.requireParticipant(roomId, user.getEmail());

        return chatMessageRepository.findByChatRoomIdOrderBySentAtAscIdAsc(roomId).stream()
                .map(this::toMessageResponseDto)
                .toList();
    }

    /**
     * 안 읽은 메시지 읽음 처리
     */
    @Transactional
    public void markAsRead(Long roomId, User user) {
        chatAccessService.requireParticipant(roomId, user.getEmail());

        chatMessageRepository.markAllAsRead(roomId, user.getId());
    }

    /**
     * 게시물별 wearer(빌리려는 사람) 리스트 조회
     * seller만 조회 가능
     */
    @Transactional(readOnly = true)
    public List<WearerListResponseDto> getWearerList(Long boardId, User user) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다. id=" + boardId));

        if (!board.getSeller().getId().equals(user.getId())) {
            throw new IllegalArgumentException("게시물의 판매자(seller)만 wearer 목록을 조회할 수 있습니다.");
        }

        List<ChatRoom> rooms = chatRoomRepository.findByBoardId(boardId);

        return rooms.stream()
                .map(room -> new WearerListResponseDto(
                        room.getId(),
                        room.getWearer().getId(),
                        room.getWearer().getNickname(),
                        room.getWearer().getProfileImage()
                ))
                .toList();
    }

    /**
     * 거래 확정 (buyer 지정)
     * 게시물 상태를 "예약중"으로 변경하고 buyer를 지정
     */
    @Transactional
    public void confirmDeal(Long boardId, Long wearerId, User user) {
        if (boardId == null || boardId <= 0 || wearerId == null || wearerId <= 0) {
            throw new IllegalArgumentException("유효한 게시물과 대여 상대 ID가 필요합니다.");
        }
        Board board = boardRepository.findByIdForChatUpdate(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다. id=" + boardId));

        if (!board.getSeller().getId().equals(user.getId())) {
            throw new IllegalArgumentException("게시물의 판매자(seller)만 거래를 확정할 수 있습니다.");
        }

        if (!"대여가능".equals(board.getStatus())) {
            throw new IllegalArgumentException("'대여가능' 상태의 게시물만 거래를 확정할 수 있습니다. 현재 상태: " + board.getStatus());
        }

        if (!chatRoomRepository.existsByBoardIdAndWearerId(boardId, wearerId)) {
            throw new IllegalArgumentException("해당 상품의 채팅 참여자만 대여 상대로 확정할 수 있습니다.");
        }

        User buyer = userRepository.findById(wearerId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다. id=" + wearerId));

        board.setBuyer(buyer);
        board.setStatus("예약중");
        boardRepository.save(board);

        log.info("거래 확정 - boardId: {}, buyer: {}", boardId, buyer.getNickname());
    }

    /**
     * 메시지 전송 (WebSocket + Redis + MySQL)
     */
    @Transactional
    public ChatMessageResponseDto sendMessage(Long roomId, String senderEmail, String message) {
        if (message == null || message.isBlank() || message.length() > 1000) {
            throw new IllegalArgumentException("메시지는 공백이 아닌 1~1000자여야 합니다.");
        }
        ChatRoom room = chatAccessService.requireParticipant(roomId, senderEmail);
        User sender = room.getSeller().getEmail().equals(senderEmail) ? room.getSeller() : room.getWearer();

        ChatMessage chatMessage = new ChatMessage(room, sender, message);
        chatMessageRepository.save(chatMessage);
        ChatMessageResponseDto responseDto = toMessageResponseDto(chatMessage);
        eventPublisher.publishEvent(new ChatMessageSentEvent(roomId, responseDto));
        log.debug("채팅 메시지 저장 - roomId: {}, messageId: {}", roomId, responseDto.getMessageId());

        return responseDto;
    }

    // ============================================
    // 내부 헬퍼 메서드
    // ============================================

    private ChatRoomResponseDto toRoomResponseDto(ChatRoom room, User currentUser) {
        // 상대방 결정 (내가 seller면 상대방은 wearer, 반대도 마찬가지)
        User opponent = room.getSeller().getId().equals(currentUser.getId())
                ? room.getWearer()
                : room.getSeller();

        // 마지막 메시지
        String lastMessage = chatMessageRepository.findTopByChatRoomIdOrderBySentAtDescIdDesc(room.getId())
                .map(ChatMessage::getMessage)
                .orElse(null);

        // 안 읽은 메시지 수
        int unreadCount = (int) chatMessageRepository
                .countByChatRoomIdAndSenderIdNotAndIsReadFalse(room.getId(), currentUser.getId());

        // 게시물 이미지 (첫 번째)
        List<String> images = room.getBoard().getFullImageUrls();
        String boardImage = (images != null && !images.isEmpty()) ? images.get(0) : null;

        return new ChatRoomResponseDto(
                room.getId(),
                room.getBoard().getId(),
                room.getBoard().getTitle(),
                boardImage,
                opponent.getNickname(),
                opponent.getProfileImage(),
                lastMessage,
                unreadCount,
                room.getCreatedAt()
        );
    }

    private ChatMessageResponseDto toMessageResponseDto(ChatMessage msg) {
        return new ChatMessageResponseDto(
                msg.getId(),
                msg.getSender().getId(),
                msg.getSender().getNickname(),
                msg.getSender().getProfileImage(),
                msg.getMessage(),
                msg.getIsRead(),
                msg.getSentAt()
        );
    }
}
