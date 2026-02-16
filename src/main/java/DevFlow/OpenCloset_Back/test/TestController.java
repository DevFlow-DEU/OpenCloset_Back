package DevFlow.OpenCloset_Back.test;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/protected")
    public ResponseEntity<String> protectedEndpoint() {
        return ResponseEntity.ok(" 인증된 사용자만 이 메시지를 볼 수 있습니다!");
    }
}  // 제거하거나 삭제하면 안됨

