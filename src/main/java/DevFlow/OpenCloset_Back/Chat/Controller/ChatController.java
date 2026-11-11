package DevFlow.OpenCloset_Back.Chat.Controller;

import DevFlow.OpenCloset_Back.Chat.Service.ChatService;
import DevFlow.OpenCloset_Back.Chat.dto.req.ChatRoomCreateRequestDto;
import DevFlow.OpenCloset_Back.Chat.dto.res.ChatMessageResponseDto;
import DevFlow.OpenCloset_Back.Chat.dto.res.ChatRoomResponseDto;
import DevFlow.OpenCloset_Back.Chat.dto.res.WearerListResponseDto;
import DevFlow.OpenCloset_Back.User.User_Service.UserService;
import DevFlow.OpenCloset_Back.User.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Chat API", description = "상품별 1:1 채팅 관련 API")
@SecurityRequirement(name = "JWT")
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;
    private final UserService userService;

    private static final String ROOM_EXAMPLE = """
            {"roomId":1,"boardId":5,"boardTitle":"나이키 바람막이 빌려드려요",
             "boardImage":"https://opencloset.jihongeek.com/images/default_board.png",
             "opponentNickname":"홍길동","opponentProfileImage":"https://opencloset.jihongeek.com/images/default_profile.png",
             "lastMessage":null,"unreadCount":0,"createdAt":"2026-11-11T10:00:00"}
            """;
    private static final String MESSAGE_EXAMPLE = """
            {"messageId":10,"senderId":7,"senderNickname":"김철수",
             "senderProfileImage":"https://opencloset.jihongeek.com/images/default_profile.png",
             "message":"이 옷 사이즈 어떻게 되나요?","isRead":false,"sentAt":"2026-11-11T10:01:00"}
            """;
    private static final String UNAUTHORIZED_EXAMPLE = "Unauthorized: Full authentication is required to access this resource";
    private static final String FORBIDDEN_EXAMPLE = "Access Denied: 본인이 참여한 채팅방만 접근할 수 있습니다.";
    private static final String BAD_ROOM_EXAMPLE = """
            {"status":400,"error":"Bad Request","message":"유효한 채팅방 ID가 필요합니다.","timestamp":"2026-11-11T10:00:00"}
            """;
    private static final String NOT_FOUND_EXAMPLE = """
            {"timestamp":"2026-11-11T01:00:00.000+00:00","status":404,"error":"Not Found","path":"/chat/room/999/messages"}
            """;

    @Operation(summary = "채팅방 생성 (또는 기존 방 반환) (로직 구현됨 ✅)",
            description = """
                    특정 상품의 옷 주인과 로그인한 문의자의 1:1 채팅방을 생성합니다.
                    같은 boardId + 문의자 조합의 방이 있으면 기존 방을 반환합니다.
                    seller는 상품 작성자, wearer는 로그인 사용자로 지정되며 본인 상품에는 문의할 수 없습니다.
                    응답의 roomId를 대화 조회와 실시간 구독에 사용합니다. JWT 토큰이 필요합니다.

                    #### 채팅방 생성 후 실시간 연결
                    Swagger의 Try it out에서는 위 REST 요청을 실행할 수 있습니다. 메시지 전송·수신은 별도의 STOMP 클라이언트를 사용합니다.

                    1. WebSocket `/ws/chat`에 연결합니다. 운영 주소는 `wss://opencloset.jihongeek.com/ws/chat`, 로컬 주소는 `ws://localhost:8080/ws/chat`입니다.
                       SockJS 클라이언트는 같은 경로의 `https://` 또는 `http://` 주소를 사용합니다.
                    2. **STOMP CONNECT 헤더**에 `Authorization: Bearer <accessToken>`을 설정합니다. Swagger의 Authorize는 REST 요청에만 적용됩니다.
                    3. `/topic/chat/{roomId}`와 개인 오류 채널 `/user/queue/chat/errors`를 구독한 뒤 메시지 내역 조회 API를 호출합니다.
                    4. `/app/chat/send`에 아래 JSON을 `content-type: application/json`으로 전송합니다.

                    ```json
                    {"roomId": 1, "message": "이 옷 사이즈 어떻게 되나요?"}
                    ```

                    전송 본문은 `ChatSendRequestDto`, 수신 본문은 `ChatMessageResponseDto` 스키마를 참고합니다.
                    발신자는 JWT 사용자로 결정하며 senderId는 보내지 않아도 됩니다. 메시지는 공백이 아닌 1~1000자여야 합니다.
                    방 참여자만 전송·구독할 수 있으며 와일드카드 구독과 `/topic/...` 직접 전송은 허용하지 않습니다.

                    내용 검증 실패는 개인 오류 채널에 `ChatError` (`code: INVALID_MESSAGE`)로 전달하며 연결을 유지합니다.
                    인증·권한·요청 오류는 STOMP ERROR의 message 헤더와 본문에 `UNAUTHORIZED` (토큰 오류), `FORBIDDEN` (접근 금지),
                    `INVALID_REQUEST` (요청 오류), `CHAT_UNAVAILABLE` (방 없음·처리 실패)를 전달한 뒤 연결을 종료합니다.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 생성 또는 기존 방 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatRoomResponseDto.class),
                            examples = @ExampleObject(name = "채팅방", value = ROOM_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "유효하지 않거나 없는 상품, 본인 상품에 대한 문의",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "상품 ID 오류", value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"유효한 게시물 ID가 필요합니다.\",\"timestamp\":\"2026-11-11T10:00:00\"}"),
                            @ExampleObject(name = "없는 상품", value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"게시물을 찾을 수 없습니다. id=999\",\"timestamp\":\"2026-11-11T10:00:00\"}"),
                            @ExampleObject(name = "본인 상품", value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"본인의 게시물에는 채팅을 걸 수 없습니다.\",\"timestamp\":\"2026-11-11T10:00:00\"}") })),
            @ApiResponse(responseCode = "401", description = "토큰 누락·오류·만료",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"), examples = @ExampleObject(value = UNAUTHORIZED_EXAMPLE)))
    })
    @PostMapping("/room")
    public ChatRoomResponseDto createRoom(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "문의할 상품 ID",
                    content = @Content(schema = @Schema(implementation = ChatRoomCreateRequestDto.class),
                            examples = @ExampleObject(value = "{\"boardId\":5}")))
            @RequestBody ChatRoomCreateRequestDto requestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        return chatService.createRoom(requestDto.getBoardId(), user);
    }

    @Operation(summary = "내 채팅방 목록 조회 (로직 구현됨 ✅)",
            description = "로그인 사용자가 옷 주인 또는 문의자로 참여한 방을 조회합니다. "
                    + "상대방 정보, 상품 정보, 마지막 메시지와 상대방이 보낸 안 읽은 메시지 수를 반환합니다. "
                    + "대화가 없으면 lastMessage는 null이며, 참여한 방이 없으면 빈 배열입니다. JWT 토큰이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내 채팅방 목록 조회 성공",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ChatRoomResponseDto.class)),
                            examples = { @ExampleObject(name = "방 목록", value = "[" + ROOM_EXAMPLE + "]"),
                                    @ExampleObject(name = "참여한 방 없음", value = "[]") })),
            @ApiResponse(responseCode = "401", description = "토큰 누락·오류·만료",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"), examples = @ExampleObject(value = UNAUTHORIZED_EXAMPLE)))
    })
    @GetMapping("/rooms")
    public List<ChatRoomResponseDto> getMyRooms(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        return chatService.getMyRooms(user);
    }

    @Operation(summary = "채팅 메시지 내역 조회 (로직 구현됨 ✅)",
            description = "참여한 방의 대화 전체를 전송 시각 오름차순으로 반환하며, 시각이 같으면 messageId 순으로 정렬합니다. "
                    + "대화가 없으면 빈 배열입니다. 조회만으로 읽음 처리되지 않으며 별도의 PATCH 읽음 처리 API를 호출해야 합니다. "
                    + "실시간 수신 메시지와 같은 응답 형식입니다. 재연결 시 방을 다시 구독한 뒤 이 API로 누락된 대화를 조회하고 "
                    + "messageId 기준으로 중복을 제거합니다. 실시간 전달에 실패해도 DB에 저장된 메시지는 조회할 수 있습니다. JWT 토큰이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메시지 내역 조회 성공",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ChatMessageResponseDto.class)),
                            examples = { @ExampleObject(name = "대화 내역", value = "[" + MESSAGE_EXAMPLE + "]"),
                                    @ExampleObject(name = "대화 없음", value = "[]") })),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 방 ID",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BAD_ROOM_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "토큰 누락·오류·만료",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"), examples = @ExampleObject(value = UNAUTHORIZED_EXAMPLE))),
            @ApiResponse(responseCode = "403", description = "해당 방의 참여자가 아님",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"), examples = @ExampleObject(value = FORBIDDEN_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 채팅방",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = NOT_FOUND_EXAMPLE)))
    })
    @GetMapping("/room/{roomId}/messages")
    public List<ChatMessageResponseDto> getMessages(
            @Parameter(description = "본인이 참여한 채팅방 ID", example = "1", schema = @Schema(minimum = "1")) @PathVariable Long roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        return chatService.getMessages(roomId, user);
    }

    @Operation(summary = "메시지 읽음 처리 (로직 구현됨 ✅)",
            description = "해당 방에서 상대방이 보낸 안 읽은 메시지를 모두 읽음 처리합니다. 본인이 보낸 메시지는 변경하지 않습니다. "
                    + "사용자가 현재 대화를 실제로 보고 있을 때 호출합니다. 성공 시 응답 본문은 없으며 반복 호출할 수 있습니다. "
                    + "읽음 여부와 안 읽은 수는 대화 내역·방 목록 재조회로 갱신하며 실시간 읽음 알림은 보내지 않습니다. JWT 토큰이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "읽음 처리 성공 (응답 본문 없음)", content = @Content),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 방 ID",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BAD_ROOM_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "토큰 누락·오류·만료",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"), examples = @ExampleObject(value = UNAUTHORIZED_EXAMPLE))),
            @ApiResponse(responseCode = "403", description = "해당 방의 참여자가 아님",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"), examples = @ExampleObject(value = FORBIDDEN_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 채팅방",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"timestamp\":\"2026-11-11T01:00:00.000+00:00\",\"status\":404,\"error\":\"Not Found\",\"path\":\"/chat/room/999/read\"}")))
    })
    @PatchMapping("/room/{roomId}/read")
    public void markAsRead(
            @Parameter(description = "본인이 참여한 채팅방 ID", example = "1", schema = @Schema(minimum = "1")) @PathVariable Long roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        chatService.markAsRead(roomId, user);
    }

    @Operation(summary = "게시물별 채팅 유저(wearer) 리스트 조회 (로직 구현됨 ✅)",
            description = "상품 작성자가 해당 상품에 문의한 사람들의 채팅방 ID와 사용자 정보를 조회합니다. "
                    + "이 응답의 wearerId를 거래 확정 API에 사용합니다. 문의자가 없으면 빈 배열입니다. "
                    + "기존 구현에 따라 작성자가 아닌 사용자의 요청은 400으로 반환합니다. JWT 토큰이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품별 문의자 목록 조회 성공",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = WearerListResponseDto.class)),
                            examples = { @ExampleObject(name = "문의자 목록", value = "[{\"roomId\":1,\"wearerId\":7,\"wearerNickname\":\"김철수\",\"wearerProfileImage\":\"https://opencloset.jihongeek.com/images/default_profile.png\"}]"),
                                    @ExampleObject(name = "문의자 없음", value = "[]") })),
            @ApiResponse(responseCode = "400", description = "없는 상품 또는 상품 작성자가 아닌 사용자",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "작성자 아님", value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"게시물의 판매자(seller)만 wearer 목록을 조회할 수 있습니다.\",\"timestamp\":\"2026-11-11T10:00:00\"}"),
                            @ExampleObject(name = "없는 상품", value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"게시물을 찾을 수 없습니다. id=999\",\"timestamp\":\"2026-11-11T10:00:00\"}") })),
            @ApiResponse(responseCode = "401", description = "토큰 누락·오류·만료",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"), examples = @ExampleObject(value = UNAUTHORIZED_EXAMPLE)))
    })
    @GetMapping("/board/{boardId}/wearers")
    public List<WearerListResponseDto> getWearerList(
            @Parameter(description = "본인이 등록한 상품 ID", example = "5", schema = @Schema(minimum = "1")) @PathVariable Long boardId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        return chatService.getWearerList(boardId, user);
    }

    @Operation(summary = "거래 확정 — buyer 지정 (로직 구현됨 ✅)",
            description = "상품 작성자가 실제 해당 상품의 채팅에 참여한 문의자를 대여 상대로 확정합니다. "
                    + "상품이 '대여가능' 상태여야 하며, 성공하면 buyer를 지정하고 상태를 '예약중'으로 변경합니다. "
                    + "상품 작성자·상태·문의자 검증 실패는 기존 구현에 따라 400을 반환합니다. 성공 시 응답 본문은 없습니다. JWT 토큰이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "거래 확정 성공 (응답 본문 없음)", content = @Content),
            @ApiResponse(responseCode = "400", description = "ID 오류, 없는 상품, 작성자가 아님, 잘못된 상품 상태 또는 채팅하지 않은 상대",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "ID 오류", value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"유효한 게시물과 대여 상대 ID가 필요합니다.\",\"timestamp\":\"2026-11-11T10:00:00\"}"),
                            @ExampleObject(name = "작성자 아님", value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"게시물의 판매자(seller)만 거래를 확정할 수 있습니다.\",\"timestamp\":\"2026-11-11T10:00:00\"}"),
                            @ExampleObject(name = "채팅하지 않은 상대", value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"해당 상품의 채팅 참여자만 대여 상대로 확정할 수 있습니다.\",\"timestamp\":\"2026-11-11T10:00:00\"}"),
                            @ExampleObject(name = "상품 상태 오류", value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"'대여가능' 상태의 게시물만 거래를 확정할 수 있습니다. 현재 상태: 예약중\",\"timestamp\":\"2026-11-11T10:00:00\"}") })),
            @ApiResponse(responseCode = "401", description = "토큰 누락·오류·만료",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"), examples = @ExampleObject(value = UNAUTHORIZED_EXAMPLE)))
    })
    @PatchMapping("/board/{boardId}/confirm/{wearerId}")
    public void confirmDeal(
            @Parameter(description = "본인이 등록한 상품 ID", example = "5", schema = @Schema(minimum = "1")) @PathVariable Long boardId,
            @Parameter(description = "문의자 목록에서 선택한 wearerId", example = "7", schema = @Schema(minimum = "1")) @PathVariable Long wearerId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        chatService.confirmDeal(boardId, wearerId, user);
    }
}
