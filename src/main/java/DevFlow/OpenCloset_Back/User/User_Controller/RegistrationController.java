package DevFlow.OpenCloset_Back.User.User_Controller;

import DevFlow.OpenCloset_Back.User.User_Service.UserService;
import DevFlow.OpenCloset_Back.User.dto.req.UserCreateRequestDto;
import DevFlow.OpenCloset_Back.User.dto.res.UserResponeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "회원가입", description = "사용자 회원가입 관련 API")
public class RegistrationController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "신규 사용자를 등록. 이메일, 닉네임, 비밀번호, 주소, 나이 정보가 필요.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponeDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 필드 누락 또는 유효하지 않은 데이터)", content = @Content),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일 또는 닉네임", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponeDto> registerUser(@RequestBody UserCreateRequestDto requestDto) {
        UserResponeDto responseDto = userService.registerUser(requestDto);
        return ResponseEntity.ok(responseDto);
    }

}