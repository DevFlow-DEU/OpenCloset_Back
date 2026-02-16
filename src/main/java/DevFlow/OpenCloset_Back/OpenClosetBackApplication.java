package DevFlow.OpenCloset_Back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class OpenClosetBackApplication {

	public static void main(String[] args) {

		SpringApplication.run(OpenClosetBackApplication.class, args);
		//다시 테스트
	}

}
