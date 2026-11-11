package DevFlow.OpenCloset_Back.Board;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Deterministic fixtures, restricted to the disposable pagination database. */
final class BoardListTestData {
    static final String MAIN_ADDRESS = "pagination-town-main";
    static final String OTHER_ADDRESS = "pagination-town-other";
    static final String EMAIL = "board-page-1@example.test";
    static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 1, 1, 0, 0);

    private BoardListTestData() { }

    static void assertIsolated(JdbcTemplate jdbc) {
        String url = jdbc.execute((java.sql.Connection connection) -> connection.getMetaData().getURL());
        if (url == null || !url.startsWith("jdbc:mariadb://127.0.0.1:23306/OpenCloset")) {
            throw new IllegalStateException("Fixture writes require the isolated pagination database.");
        }
        if (jdbc.queryForObject("select count(*) from users where email not like 'board-page-%@example.test'", Long.class) != 0
                || jdbc.queryForObject("select count(*) from board where title not like 'page-fixture-%'", Long.class) != 0) {
            throw new IllegalStateException("Refusing to overwrite non-pagination fixture data.");
        }
    }

    static void clear(JdbcTemplate jdbc) {
        assertIsolated(jdbc);
        jdbc.update("delete from wishlist");
        jdbc.update("delete from top");
        jdbc.update("delete from board_images");
        jdbc.update("delete from board");
        jdbc.update("delete from users");
    }

    static void seed(JdbcTemplate jdbc, int count) {
        clear(jdbc);
        List<Object[]> users = new ArrayList<>();
        for (int id = 1; id <= 122; id++) {
            String address = id <= 80 ? MAIN_ADDRESS : OTHER_ADDRESS;
            if (id == 121) address = "pagination-town-empty";
            if (id == 122) address = null;
            users.add(new Object[]{id, "board-page-" + id + "@example.test", "unused-test-password",
                    "page-user-" + id, address, "/images/default_profile.png"});
        }
        jdbc.batchUpdate("insert into users (id,email,password,nickname,address,profile_image) values (?,?,?,?,?,?)", users);
        List<Object[]> boards = new ArrayList<>();
        List<Object[]> images = new ArrayList<>();
        List<Object[]> wishes = new ArrayList<>();
        List<Object[]> categories = new ArrayList<>();
        for (int id = 1; id <= count; id++) {
            int seller = (id - 1) % 100 + 1;
            Integer buyer = id % 5 == 0 ? 101 + id % 20 : null;
            Timestamp timestamp = Timestamp.valueOf(BASE_TIME.plusSeconds((id - 1) / 7));
            boards.add(new Object[]{id, "page-fixture-" + id, "pagination test product",
                    "L", "공용", 35.1796, 129.0756, "fixture meeting place", 3200L,
                    LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 8), "top",
                    buyer == null ? "대여가능" : "예약중", timestamp, timestamp, seller, buyer});
            for (int image = 1; image <= 2; image++) {
                images.add(new Object[]{id, "/uploads/boards/fixture-" + id + "-" + image + ".jpg"});
            }
            categories.add(new Object[]{id});
            if (id % 10 == 1) wishes.add(new Object[]{1, id, timestamp});
        }
        jdbc.batchUpdate("""
                insert into board (id,title,description,size,sex,latitude,longitude,detail_address,price,
                start_date,end_date,category,status,created_at,modified_at,seller_id,buyer_id)
                values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """, boards);
        jdbc.batchUpdate("insert into board_images (board_id,image_path) values (?,?)", images);
        jdbc.batchUpdate("insert into top (board_id) values (?)", categories);
        jdbc.batchUpdate("insert into wishlist (user_id,board_id,created_at) values (?,?,?)", wishes);
    }
}
