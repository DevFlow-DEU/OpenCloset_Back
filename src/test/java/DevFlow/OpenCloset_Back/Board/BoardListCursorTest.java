package DevFlow.OpenCloset_Back.Board;

import DevFlow.OpenCloset_Back.Board.Service.BoardListCursor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class BoardListCursorTest {
    @Test
    void roundTripsDatabaseMicrosecondsAndLargeIds() {
        BoardListCursor cursor = new BoardListCursor(LocalDateTime.of(2026, 1, 1, 12, 34, 56, 123456000), Long.MAX_VALUE);
        assertThat(BoardListCursor.decode(cursor.encode())).isEqualTo(cursor);
    }

    @Test
    void roundTripsLegacyNullTimestamp() {
        BoardListCursor cursor = new BoardListCursor(null, 12);
        assertThat(BoardListCursor.decode(cursor.encode())).isEqualTo(cursor);
    }

    @Test
    void absentCursorStartsFirstWindow() {
        assertThat(BoardListCursor.decode(null)).isNull();
    }

    @ParameterizedTest
    @MethodSource("invalidCursors")
    void rejectsMalformedOrNonCanonicalCursors(String value) {
        assertThatIllegalArgumentException().isThrownBy(() -> BoardListCursor.decode(value))
                .withMessage("올바르지 않은 상품 목록 커서입니다.");
    }

    static Stream<String> invalidCursors() {
        return Stream.concat(Stream.of("", " ", "not-base64!", "x".repeat(129)), Stream.of(
                "{}", "2|2026-01-01T00:00|1", "1|bad-time|1", "1|2026-02-30T00:00|1",
                "1|2026-01-01T00:00|0", "1|2026-01-01T00:00|-1", "1|2026-01-01T00:00|9223372036854775808",
                "1|2026-01-01T00:00|01", "1|null|0", "1|null|1|extra", "1||1",
                "1|0999-01-01T00:00|1", "1|+10000-01-01T00:00|1", "1|2026-01-01T00:00:00.000000001|1")
                .map(value -> Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8))));
    }
}
