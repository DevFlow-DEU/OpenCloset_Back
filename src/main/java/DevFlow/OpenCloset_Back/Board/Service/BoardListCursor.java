package DevFlow.OpenCloset_Back.Board.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

/** Opaque, versioned keyset position. A null modifiedAt sorts after dated rows. */
public record BoardListCursor(LocalDateTime modifiedAt, long id) {
    private static final String INVALID_CURSOR = "올바르지 않은 상품 목록 커서입니다.";

    public String encode() {
        String value = "1|" + (modifiedAt == null ? "null" : modifiedAt) + "|" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public static BoardListCursor decode(String value) {
        if (value == null) return null;
        if (value.isBlank() || value.length() > 128) throw new IllegalArgumentException(INVALID_CURSOR);
        try {
            String[] parts = new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8).split("\\|", -1);
            if (parts.length != 3 || !parts[0].equals("1")) throw new IllegalArgumentException();
            LocalDateTime modifiedAt = parts[1].equals("null") ? null : LocalDateTime.parse(parts[1]);
            long id = Long.parseLong(parts[2]);
            if (id <= 0 || (modifiedAt != null && (modifiedAt.getYear() < 1000
                    || modifiedAt.getYear() > 9999 || modifiedAt.getNano() % 1000 != 0))) {
                throw new IllegalArgumentException();
            }
            BoardListCursor cursor = new BoardListCursor(modifiedAt, id);
            if (!cursor.encode().equals(value)) throw new IllegalArgumentException();
            return cursor;
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(INVALID_CURSOR);
        }
    }
}
