package DevFlow.OpenCloset_Back.Chat.Controller;

import DevFlow.OpenCloset_Back.Chat.dto.req.ChatRoomCreateRequestDto;
import DevFlow.OpenCloset_Back.Chat.dto.res.ChatMessageResponseDto;
import DevFlow.OpenCloset_Back.Chat.dto.res.ChatRoomResponseDto;
import DevFlow.OpenCloset_Back.Chat.dto.res.WearerListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Chat API", description = "채팅 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    // ============================================
    // 채팅방 관련 API
    // ============================================

    @Operation(
            summary = "채팅방 생성 (또는 기존 방 반환)",
            description = "특정 게시물에 대해 채팅방을 생성합니다. "
                    + "이미 해당 게시물에 대한 채팅방이 존재하면 기존 방을 반환합니다. "
                    + "seller는 게시물 작성자, wearer는 로그인한 유저로 자동 지정됩니다. (토큰 인증 필수)"
    )
    @ApiResponse(responseCode = "200", description = "채팅방 생성/조회 성공")
    @PostMapping("/room")
    public ChatRoomResponseDto createRoom(
            @RequestBody ChatRoomCreateRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 로직 구현 예정
        return null;
    }

    @Operation(
            summary = "내 채팅방 목록 조회",
            description = "로그인한 유저가 참여한 모든 채팅방 목록을 조회합니다. "
                    + "마지막 메시지, 안 읽은 메시지 수 포함. (토큰 인증 필수)"
    )
    @ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공")
    @GetMapping("/rooms")
    public List<ChatRoomResponseDto> getMyRooms(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 로직 구현 예정
        return null;
    }

    // ============================================
    // 채팅 메시지 관련 API
    // ============================================

    @Operation(
            summary = "채팅 메시지 내역 조회",
            description = "특정 채팅방의 과거 대화 내역을 조회합니다. (토큰 인증 필수)"
    )
    @ApiResponse(responseCode = "200", description = "메시지 내역 조회 성공")
    @GetMapping("/room/{roomId}/messages")
    public List<ChatMessageResponseDto> getMessages(
            @Parameter(description = "채팅방 ID", example = "1")
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 로직 구현 예정
        return null;
    }

    @Operation(
            summary = "메시지 읽음 처리",
            description = "특정 채팅방의 안 읽은 메시지를 모두 읽음 처리합니다. (토큰 인증 필수)"
    )
    @ApiResponse(responseCode = "200", description = "읽음 처리 성공")
    @PatchMapping("/room/{roomId}/read")
    public void markAsRead(
            @Parameter(description = "채팅방 ID", example = "1")
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 로직 구현 예정
    }

    // ============================================
    // 거래 확정 관련 API
    // ============================================

    @Operation(
            summary = "게시물별 채팅 유저(wearer) 리스트 조회",
            description = "seller(옷 주인)가 자신의 게시물에 대해 채팅을 건 wearer(빌리려는 사람) 목록을 조회합니다. "
                    + "이 목록에서 buyer를 선택하여 거래를 확정할 수 있습니다. (토큰 인증 필수)"
    )
    @ApiResponse(responseCode = "200", description = "wearer 리스트 조회 성공")
    @GetMapping("/board/{boardId}/wearers")
    public List<WearerListResponseDto> getWearerList(
            @Parameter(description = "게시물 ID", example = "5")
            @PathVariable Long boardId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 로직 구현 예정
        return null;
    }

    @Operation(
            summary = "거래 확정 (buyer 지정)",
            description = "seller가 특정 wearer를 buyer로 확정합니다. "
                    + "확정 시 게시물 상태가 '대여가능' → '대여중'으로 변경됩니다. (토큰 인증 필수)"
    )
    @ApiResponse(responseCode = "200", description = "거래 확정 성공")
    @PatchMapping("/board/{boardId}/confirm/{wearerId}")
    public void confirmDeal(
            @Parameter(description = "게시물 ID", example = "5")
            @PathVariable Long boardId,
            @Parameter(description = "buyer로 지정할 wearer의 유저 ID", example = "7")
            @PathVariable Long wearerId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 로직 구현 예정
    }
}
