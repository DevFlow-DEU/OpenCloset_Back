package DevFlow.OpenCloset_Back.Login.Kakao.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "kakao_user")
public class KakaoUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String kakaoId; // 카카오 고유 사용자 ID

    @Column(nullable = true)
    private String email; // 카카오 이메일 (동의 안 할 수도 있음)

    @Column(nullable = false)
    private String nickname; // 카카오 닉네임
}
