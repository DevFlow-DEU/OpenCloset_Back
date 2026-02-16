package DevFlow.OpenCloset_Back.Search.Dto.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardSearchRequestDto {
    private String title;
    private String description;
    private String sex;
    private String size;
    private String place;

}
