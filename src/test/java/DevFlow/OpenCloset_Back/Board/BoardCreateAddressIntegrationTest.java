package DevFlow.OpenCloset_Back.Board;

import DevFlow.OpenCloset_Back.Board.Repository.BoardRepository;
import DevFlow.OpenCloset_Back.Board.Repository.TopRepository;
import DevFlow.OpenCloset_Back.Login.Jwt_Util.JwtUtil;
import DevFlow.OpenCloset_Back.User.User_Repository.UserRepository;
import DevFlow.OpenCloset_Back.User.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("local")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:mariadb://127.0.0.1:${DB_PORT:13306}/OpenCloset?sslMode=disable",
        "spring.data.redis.host=127.0.0.1"
})
class BoardCreateAddressIntegrationTest {
    private static final String ERROR_MESSAGE = "상품을 등록하려면 먼저 내 동네를 설정해 주세요.";
    private static final String TRADE_ADDRESS = "부산역 1번 출구 거래 장소";
    private static final double LATITUDE = 35.115;
    private static final double LONGITUDE = 129.042;
    private static final Path UPLOAD_ROOT = Path.of("uploads").toAbsolutePath().normalize();
    private static final Path UPLOAD_DIRECTORY = UPLOAD_ROOT.resolve("boards");
    private static final byte[] PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aZfkAAAAASUVORK5CYII=");

    @Autowired TestRestTemplate rest;
    @Autowired UserRepository users;
    @Autowired BoardRepository boards;
    @Autowired TopRepository tops;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;
    private User seller;
    private String token;
    private String imageName;
    private boolean uploadRootExisted;
    private boolean uploadDirectoryExisted;

    @BeforeEach
    void setUp() {
        uploadRootExisted = Files.exists(UPLOAD_ROOT);
        uploadDirectoryExisted = Files.exists(UPLOAD_DIRECTORY);
        String suffix = UUID.randomUUID().toString();
        imageName = "board-address-test-" + suffix + ".png";
        seller = new User();
        seller.setEmail("board-address-" + suffix + "@example.test");
        seller.setNickname("board-address-" + suffix);
        seller.setPassword("unused-test-password");
        seller.setAddress("주소 미입력");
        seller = users.saveAndFlush(seller);
        token = JwtUtil.generateToken(seller.getEmail(), "ROLE_USER");
    }

    @AfterEach
    void cleanUp() throws IOException {
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            if (seller != null && seller.getId() != null) {
                var createdBoards = boards.findBySellerId(seller.getId());
                tops.deleteByBoardIn(createdBoards);
                tops.flush();
                boards.deleteAll(createdBoards);
                boards.flush();
                users.deleteById(seller.getId());
            }
        });
        // Delete only this test's uniquely named files; retain pre-existing uploads and directories.
        for (Path image : uploadedImages()) {
            Files.deleteIfExists(image);
        }
        if (!uploadDirectoryExisted) deleteIfEmpty(UPLOAD_DIRECTORY);
        if (!uploadRootExisted) deleteIfEmpty(UPLOAD_ROOT);
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" \t\r\n", "주소 미입력", " \t주소 미입력\r\n ", "\u2003", "\u2003주소 미입력\u2003"})
    void unregisteredProfileAddressRejectsDirectMultipartRequestBeforeAnySave(String address) throws IOException {
        seller.setAddress(address);
        users.saveAndFlush(seller);
        long boardCount = boards.count();
        long categoryCount = tops.count();
        boolean directoryExisted = Files.exists(UPLOAD_DIRECTORY);

        // Valid product location and even a forged profile-address form field cannot bypass the DB check.
        var form = productForm();
        form.add("address", "부산시 부산진구");
        var response = createProduct(form);

        assertRejected(response);
        assertThat(boards.findBySellerId(seller.getId())).isEmpty();
        assertThat(boards.count()).isEqualTo(boardCount);
        assertThat(tops.count()).isEqualTo(categoryCount);
        assertThat(uploadedImages()).isEmpty();
        assertThat(Files.exists(UPLOAD_DIRECTORY)).isEqualTo(directoryExisted);
    }

    @ParameterizedTest
    @ValueSource(strings = {"부산시 부산진구", " \t부산시 부산진구\t "})
    void registeredAddressAllowsProductAndKeepsSeparateTradeLocation(String address) throws IOException {
        seller.setAddress(address);
        users.saveAndFlush(seller);

        assertProductCreated(createProduct(productForm()), address);
    }

    @Test
    void sameAccountCanCreateAfterRegisteringProfileAddressWithoutNewToken() throws IOException {
        assertRejected(createProduct(productForm()));
        assertThat(uploadedImages()).isEmpty();
        assertThat(boards.findBySellerId(seller.getId())).isEmpty();

        MultiValueMap<String, Object> profile = new LinkedMultiValueMap<>();
        profile.add("address", "부산시 부산진구");
        var updated = rest.postForEntity("/mypage/edit", new HttpEntity<>(profile, headers()), JsonNode.class);
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Reuse the original JWT: BoardController must read the seller's current address from the DB.
        assertProductCreated(createProduct(productForm()), "부산시 부산진구");
    }

    private void assertRejected(ResponseEntity<JsonNode> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.path("status").asInt()).isEqualTo(400);
        assertThat(body.path("error").asText()).isEqualTo("Bad Request");
        assertThat(body.path("message").asText()).isEqualTo(ERROR_MESSAGE);
        assertThat(body.path("timestamp").asText()).isNotBlank();
    }

    private void assertProductCreated(ResponseEntity<JsonNode> response, String profileAddress) throws IOException {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        long boardId = body.path("id").asLong();
        assertThat(boardId).isPositive();
        assertThat(body.path("detailAddress").asText()).isEqualTo(TRADE_ADDRESS);
        assertThat(body.path("latitude").asDouble()).isEqualTo(LATITUDE);
        assertThat(body.path("longitude").asDouble()).isEqualTo(LONGITUDE);
        assertThat(body.path("owner").asBoolean()).isTrue();
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            var saved = boards.findById(boardId).orElseThrow();
            assertThat(saved.getSeller().getId()).isEqualTo(seller.getId());
            assertThat(saved.getDetailAddress()).isEqualTo(TRADE_ADDRESS);
            assertThat(saved.getLatitude()).isEqualTo(LATITUDE);
            assertThat(saved.getLongitude()).isEqualTo(LONGITUDE);
            assertThat(saved.getImages()).hasSize(1);
            assertThat(users.findById(seller.getId()).orElseThrow().getAddress()).isEqualTo(profileAddress);
        });
        assertThat(jdbc.queryForObject("select count(*) from top where board_id = ?", Long.class, boardId)).isEqualTo(1);
        List<Path> images = uploadedImages();
        assertThat(images).hasSize(1);
        assertThat(Files.readAllBytes(images.get(0))).isEqualTo(PNG);
    }

    private ResponseEntity<JsonNode> createProduct(MultiValueMap<String, Object> form) {
        return rest.postForEntity("/board/create", new HttpEntity<>(form, headers()), JsonNode.class);
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }

    private MultiValueMap<String, Object> productForm() {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("title", imageName);
        form.add("description", "프로필 동네 등록 검사 테스트");
        form.add("size", "M");
        form.add("sex", "공용");
        form.add("latitude", String.valueOf(LATITUDE));
        form.add("longitude", String.valueOf(LONGITUDE));
        form.add("detailAddress", TRADE_ADDRESS);
        form.add("price", "1000");
        form.add("startDate", LocalDate.now().toString());
        form.add("endDate", LocalDate.now().plusDays(1).toString());
        form.add("category", "top");
        ByteArrayResource image = new ByteArrayResource(PNG) {
            @Override public String getFilename() { return imageName; }
        };
        HttpHeaders imageHeaders = new HttpHeaders();
        imageHeaders.setContentType(MediaType.IMAGE_PNG);
        form.add("images", new HttpEntity<>(image, imageHeaders));
        return form;
    }

    private List<Path> uploadedImages() throws IOException {
        if (!Files.isDirectory(UPLOAD_DIRECTORY)) return List.of();
        try (var files = Files.list(UPLOAD_DIRECTORY)) {
            return files.filter(file -> file.getFileName().toString().endsWith(imageName)).toList();
        }
    }

    private void deleteIfEmpty(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) return;
        try (var files = Files.list(directory)) {
            if (files.findAny().isPresent()) return;
        }
        Files.deleteIfExists(directory);
    }
}
