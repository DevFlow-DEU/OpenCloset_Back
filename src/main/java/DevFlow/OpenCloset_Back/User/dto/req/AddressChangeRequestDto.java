package DevFlow.OpenCloset_Back.User.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "주소 변경 요청 DTO")
public class AddressChangeRequestDto {
    @Schema(description = "변경할 새 주소", example = "서울시 강남구 역삼동", requiredMode = RequiredMode.REQUIRED)
    private String newAddress;
}
