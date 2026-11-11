package DevFlow.OpenCloset_Back.Board;

import DevFlow.OpenCloset_Back.Board.Service.BoardListCursor;
import DevFlow.OpenCloset_Back.Login.Jwt_Util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("board-pagination-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(BoardPaginationIntegrationTest.SqlCaptureConfiguration.class)
class BoardPaginationIntegrationTest extends BoardPaginationTestDatabase {
    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;
    @Autowired EntityManagerFactory emf;
    @Autowired SqlCapture capture;

    @TestConfiguration
    static class SqlCaptureConfiguration {
        @Bean SqlCapture sqlCapture() { return new SqlCapture(); }

        @Bean HibernatePropertiesCustomizer statementInspector(SqlCapture capture) {
            return properties -> properties.put("hibernate.session_factory.statement_inspector", capture);
        }
    }

    static class SqlCapture implements StatementInspector {
        final Queue<String> statements = new ConcurrentLinkedQueue<>();

        @Override public String inspect(String sql) {
            statements.add(sql);
            return sql;
        }
    }

    @BeforeEach void seed() { BoardListTestData.seed(jdbc, 135); }
    @AfterEach void cleanUp() { BoardListTestData.clear(jdbc); }

    @Test
    void defaultFirstWindowUsesSqlLimitAndBoundedAssociationQueries() throws Exception {
        var statistics = emf.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        capture.statements.clear();
        JsonNode page = ok(null, null, BoardListTestData.EMAIL);
        assertThat(page.path("items").size()).isEqualTo(20);
        assertThat(page.path("hasNext").asBoolean()).isTrue();
        assertThat(page.path("nextCursor").asText()).isNotBlank();
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(5);
        assertThat(capture.statements).anyMatch(sql -> sql.contains("from board ") && sql.contains(" limit ?"));
        assertThat(capture.statements).noneMatch(sql -> sql.toLowerCase().contains("count("));
        assertThat(capture.statements.stream().filter(sql -> sql.contains("join board_images")).count()).isEqualTo(1);
        for (JsonNode item : page.path("items")) assertThat(item.path("images").size()).isEqualTo(2);
    }

    @Test
    void allWindowsMatchDatabaseOrderWithNoMissingOrDuplicateTiedRows() throws Exception {
        List<Long> expected = expectedIds(BoardListTestData.MAIN_ADDRESS);
        List<Long> actual = new ArrayList<>();
        String cursor = null;
        boolean sawWished = false;
        boolean sawOwner = false;
        boolean sawBuyer = false;
        int pages = 0;
        do {
            JsonNode page = ok(cursor, "20", BoardListTestData.EMAIL);
            pages++;
            assertThat(pages).isLessThan(20);
            for (JsonNode item : page.path("items")) {
                long id = item.path("id").asLong();
                actual.add(id);
                assertThat(item.path("sellerNickname").asText()).isEqualTo("page-user-" + item.path("sellerId").asLong());
                assertThat(item.path("wished").asBoolean()).isEqualTo(id % 10 == 1);
                assertThat(item.path("owner").asBoolean()).isEqualTo(item.path("sellerId").asLong() == 1);
                sawWished |= item.path("wished").asBoolean();
                sawOwner |= item.path("owner").asBoolean();
                sawBuyer |= !item.path("buyerId").isNull();
            }
            if (page.path("hasNext").asBoolean()) {
                assertThat(page.path("items").size()).isEqualTo(20);
                cursor = page.path("nextCursor").asText();
            } else {
                assertThat(page.path("nextCursor").isNull()).isTrue();
                assertThat(page.path("items").size()).isEqualTo(expected.size() % 20);
                cursor = null;
            }
        } while (cursor != null);
        assertThat(actual).containsExactlyElementsOf(expected).doesNotHaveDuplicates();
        assertThat(sawWished).isTrue();
        assertThat(sawOwner).isTrue();
        assertThat(sawBuyer).isTrue();
    }

    @Test
    void emptyAddressResultHasNoCursorOrAssociationQueries() throws Exception {
        var statistics = emf.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        JsonNode page = ok(null, null, "board-page-121@example.test");
        assertThat(page.path("items").isEmpty()).isTrue();
        assertThat(page.path("hasNext").asBoolean()).isFalse();
        assertThat(page.path("nextCursor").isNull()).isTrue();
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(3);
    }

    @Test
    void exactlyFullLastWindowDoesNotAdvertiseAnotherWindow() throws Exception {
        BoardListTestData.seed(jdbc, 20);
        JsonNode page = ok(null, "20", BoardListTestData.EMAIL);
        assertThat(page.path("items").size()).isEqualTo(20);
        assertThat(page.path("hasNext").asBoolean()).isFalse();
        assertThat(page.path("nextCursor").isNull()).isTrue();
    }

    @Test
    void requestAfterLastRowReturnsEmptyWindow() throws Exception {
        String cursor = new BoardListCursor(BoardListTestData.BASE_TIME, 1).encode();
        JsonNode page = ok(cursor, "20", BoardListTestData.EMAIL);
        assertThat(page.path("items").isEmpty()).isTrue();
        assertThat(page.path("hasNext").asBoolean()).isFalse();
        assertThat(page.path("nextCursor").isNull()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "100"})
    void acceptsSizeBoundsWithConstantSqlCount(String size) throws Exception {
        var statistics = emf.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        JsonNode page = ok(null, size, BoardListTestData.EMAIL);
        assertThat(page.path("items").size()).isEqualTo(Integer.parseInt(size));
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(5);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "101", "2147483647", "2147483648", "abc", "1.5"})
    void invalidSizesAreRejectedThroughHttp(String size) throws Exception {
        ResponseEntity<byte[]> response = get(null, size, BoardListTestData.EMAIL);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(mapper.readTree(response.getBody()).path("status").asInt()).isEqualTo(400);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid!", "e30"})
    void invalidCursorsUseExistingErrorBody(String cursor) throws Exception {
        ResponseEntity<byte[]> response = get(cursor, "20", BoardListTestData.EMAIL);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        JsonNode body = mapper.readTree(response.getBody());
        assertThat(body.path("message").asText()).isEqualTo("올바르지 않은 상품 목록 커서입니다.");
        assertThat(body.path("error").asText()).isEqualTo("Bad Request");
    }

    @Test
    void nullModifiedDatesRemainReachableAcrossCursorBoundaries() throws Exception {
        BoardListTestData.seed(jdbc, 40);
        jdbc.update("update board set modified_at=null where id<=25");
        List<Long> actual = new ArrayList<>();
        String cursor = null;
        do {
            JsonNode page = ok(cursor, "10", BoardListTestData.EMAIL);
            page.path("items").forEach(item -> actual.add(item.path("id").asLong()));
            cursor = page.path("hasNext").asBoolean() ? page.path("nextCursor").asText() : null;
        } while (cursor != null);
        assertThat(actual).containsExactlyElementsOf(expectedIds(BoardListTestData.MAIN_ADDRESS)).doesNotHaveDuplicates();
    }

    @Test
    void missingProfileAddressRetainsPreviousIsNullFiltering() throws Exception {
        jdbc.update("update board set seller_id=122 where id=1");
        JsonNode page = ok(null, null, "board-page-122@example.test");
        assertThat(page.path("items").size()).isEqualTo(1);
        assertThat(page.path("items").get(0).path("id").asLong()).isEqualTo(1);
    }

    @Test
    void aCursorNeverBypassesCurrentUsersAddressFilter() throws Exception {
        String cursor = ok(null, "1", BoardListTestData.EMAIL).path("nextCursor").asText();
        JsonNode page = ok(cursor, "100", "board-page-81@example.test");
        List<Long> ids = new ArrayList<>();
        page.path("items").forEach(item -> ids.add(item.path("id").asLong()));
        assertThat(ids).containsExactlyElementsOf(expectedIds(BoardListTestData.OTHER_ADDRESS));
    }

    @Test
    void deletingCursorAnchorDoesNotBreakFollowingWindow() throws Exception {
        JsonNode first = ok(null, "20", BoardListTestData.EMAIL);
        String cursor = first.path("nextCursor").asText();
        long anchor = Objects.requireNonNull(BoardListCursor.decode(cursor)).id();
        List<Long> expected = expectedIds(BoardListTestData.MAIN_ADDRESS).subList(20, 40);
        jdbc.update("delete from wishlist where board_id=?", anchor);
        jdbc.update("delete from top where board_id=?", anchor);
        jdbc.update("delete from board_images where board_id=?", anchor);
        jdbc.update("delete from board where id=?", anchor);
        List<Long> actual = new ArrayList<>();
        ok(cursor, "20", BoardListTestData.EMAIL).path("items").forEach(item -> actual.add(item.path("id").asLong()));
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    void swaggerDocumentsNumericLimitsAndNullableNextCursor() throws Exception {
        ResponseEntity<byte[]> response = rest.getForEntity("/v3/api-docs", byte[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode document = mapper.readTree(response.getBody());
        JsonNode operation = document.path("paths").path("/board/All").path("get");
        assertThat(operation.path("responses").path("200").path("content").path("application/json")
                .path("schema").path("$ref").asText()).isEqualTo("#/components/schemas/BoardCursorResponseDto");
        JsonNode size = java.util.stream.StreamSupport.stream(operation.path("parameters").spliterator(), false)
                .filter(parameter -> parameter.path("name").asText().equals("size")).findFirst().orElseThrow().path("schema");
        assertThat(size.path("type").asText()).isEqualTo("integer");
        assertThat(size.path("default").asInt()).isEqualTo(20);
        assertThat(size.path("minimum").asInt()).isEqualTo(1);
        assertThat(size.path("maximum").asInt()).isEqualTo(100);
        JsonNode cursorType = document.path("components").path("schemas").path("BoardCursorResponseDto")
                .path("properties").path("nextCursor").path("type");
        assertThat(cursorType.isArray()).isTrue();
        List<String> types = new ArrayList<>();
        cursorType.forEach(type -> types.add(type.asText()));
        assertThat(types).containsExactlyInAnyOrder("string", "null");
    }

    @Test
    void authenticationRemainsRequired() {
        assertThat(rest.getForEntity("/board/All", byte[].class).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid-token");
        assertThat(rest.exchange("/board/All", HttpMethod.GET, new HttpEntity<>(headers), byte[].class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private JsonNode ok(String cursor, String size, String email) throws Exception {
        ResponseEntity<byte[]> response = get(cursor, size, email);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return mapper.readTree(response.getBody());
    }

    private ResponseEntity<byte[]> get(String cursor, String size, String email) {
        var builder = UriComponentsBuilder.fromUriString("http://127.0.0.1:" + port + "/board/All");
        if (cursor != null) builder.queryParam("cursor", cursor);
        if (size != null) builder.queryParam("size", size);
        URI uri = builder.build().encode().toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(JwtUtil.generateToken(email, "ROLE_USER"));
        return rest.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
    }

    private List<Long> expectedIds(String address) {
        return jdbc.queryForList("""
                select b.id from board b join users s on s.id=b.seller_id
                where s.address=? order by b.modified_at desc,b.id desc
                """, Long.class, address);
    }
}
