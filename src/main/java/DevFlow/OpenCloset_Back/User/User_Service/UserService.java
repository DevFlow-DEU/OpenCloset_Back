package DevFlow.OpenCloset_Back.User.User_Service;

import DevFlow.OpenCloset_Back.Board.Repository.*;
import DevFlow.OpenCloset_Back.Board.entity.Board;
import DevFlow.OpenCloset_Back.Chat.ChatMessage.Repository.ChatMessageRepository;
import DevFlow.OpenCloset_Back.Chat.ChatRoom.Entity.ChatRoom;
import DevFlow.OpenCloset_Back.Chat.ChatRoom.Repository.ChatRoomRepository;
import DevFlow.OpenCloset_Back.Login.RefreshToken.RefreshTokenRepository;
import DevFlow.OpenCloset_Back.User.User_Repository.UserRepository;
import DevFlow.OpenCloset_Back.User.dto.req.UserCreateRequestDto;
import DevFlow.OpenCloset_Back.User.dto.res.UserResponeDto;
import DevFlow.OpenCloset_Back.User.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final BoardRepository boardRepository;
    private final TopRepository topRepository;
    private final BottomRepository bottomRepository;
    private final OuterRepository outerRepository;
    private final JewelryRepossitory jewelryRepossitory;
    private final One_pieceRepository onePieceRepository;
    private final ShoesRepository shoesRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*_";
    private static final String ALL_CHARS = UPPER + LOWER + DIGITS + SPECIAL;
    private static final SecureRandom RANDOM = new SecureRandom();

    public UserResponeDto registerUser(UserCreateRequestDto requestDto) {
        // 이메일 중복 체크
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 닉네임 중복 체크
        if (userRepository.findByNickname(requestDto.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(requestDto.getPassword());

        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setNickname(requestDto.getNickname());
        user.setPassword(encryptedPassword);
        user.setAddress(requestDto.getAddress());
        user.setAge(requestDto.getAge());

        userRepository.save(user);

        return new UserResponeDto(
                user.getEmail(),
                user.getAddress(),
                user.getNickname(),
                user.getAge());
    }

    public void resetPassword(String email) {
        User user = findByEmail(email);

        String tempPassword = generateTempPassword();

        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        emailService.sendTemporaryPassword(email, tempPassword);
    }

    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = findByEmail(email);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String email) {
        User user = findByEmail(email);
        Long userId = user.getId();

        // 1. 채팅 메시지 삭제 (ChatRoom 참조하므로 먼저 삭제)
        List<ChatRoom> chatRooms = chatRoomRepository.findByUser1IdOrUser2Id(userId, userId);
        if (!chatRooms.isEmpty()) {
            chatMessageRepository.deleteByChatRoomIn(chatRooms);
        }

        // 2. 채팅방 삭제
        chatRoomRepository.deleteByUser1IdOrUser2Id(userId, userId);

        // 3. 카테고리 엔티티 삭제 (Board 참조하므로 Board보다 먼저 삭제)
        List<Board> userBoards = boardRepository.findByUserId(userId);
        if (!userBoards.isEmpty()) {
            topRepository.deleteByBoardIn(userBoards);
            bottomRepository.deleteByBoardIn(userBoards);
            outerRepository.deleteByBoardIn(userBoards);
            jewelryRepossitory.deleteByBoardIn(userBoards);
            onePieceRepository.deleteByBoardIn(userBoards);
            shoesRepository.deleteByBoardIn(userBoards);
        }

        // 4. 게시물 삭제
        boardRepository.deleteByUserId(userId);

        // 5. RefreshToken 삭제
        refreshTokenRepository.deleteByUsername(email);

        // 6. 유저 삭제
        userRepository.delete(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private String generateTempPassword() {
        StringBuilder sb = new StringBuilder(10);
        sb.append(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        sb.append(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        sb.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        sb.append(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));

        // 나머지 6자리는 전체 문자 풀에서 랜덤
        for (int i = 4; i < 10; i++) {
            sb.append(ALL_CHARS.charAt(RANDOM.nextInt(ALL_CHARS.length())));
        }

        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}
