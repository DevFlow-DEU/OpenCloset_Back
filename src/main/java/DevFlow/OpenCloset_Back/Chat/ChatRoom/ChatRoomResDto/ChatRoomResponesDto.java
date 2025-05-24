package DevFlow.OpenCloset_Back.Chat.ChatRoom.ChatRoomResDto;

import lombok.*;


@Data // Getter + Setter + ToString + EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponesDto {
    private Long roomId;
    private Long user1Id;
    private Long user2Id;
    private String createdAt;


}
