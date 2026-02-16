package DevFlow.OpenCloset_Back.User.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "에러 응답 DTO")
public class ErrorResponseDto {

    @Schema(description = "HTTP 상태 코드", example = "400")
    private int status;

    @Schema(description = "에러 메시지", example = "잘못된 요청입니다.")
    private String message;

    @Schema(description = "에러 발생 시간", example = "2026-02-04T15:40:00")
    private String timestamp;
}
