package DevFlow.OpenCloset_Back.Chat.Event;

import DevFlow.OpenCloset_Back.Chat.dto.res.ChatMessageResponseDto;

public record ChatMessageSentEvent(Long roomId, ChatMessageResponseDto message) {
}
