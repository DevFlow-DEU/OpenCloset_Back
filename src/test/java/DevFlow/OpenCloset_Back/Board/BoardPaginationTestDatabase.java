package DevFlow.OpenCloset_Back.Board;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** These values override inherited environment settings before Hibernate initializes. */
abstract class BoardPaginationTestDatabase {
    @DynamicPropertySource
    static void isolatedServices(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", () -> "jdbc:mariadb://127.0.0.1:23306/OpenCloset?sslMode=disable");
        properties.add("spring.datasource.username", () -> "pagination_test");
        properties.add("spring.datasource.password", () -> "pagination-test-only");
        properties.add("spring.data.redis.host", () -> "127.0.0.1");
        properties.add("spring.data.redis.port", () -> 26379);
    }
}
