package DevFlow.OpenCloset_Back.Board.Repository;

import DevFlow.OpenCloset_Back.Board.entity.Board;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BoardListRepository {
    private final EntityManager entityManager;

    public List<Board> findWindow(String address, LocalDateTime modifiedAt, Long beforeId, int limit) {
        // Fetch only to-one relations here so SQL LIMIT applies to products, not image rows.
        String jpql = "select b from Board b join fetch b.seller s left join fetch b.buyer "
                + "where " + (address == null ? "s.address is null" : "s.address = :address");
        if (beforeId != null) {
            jpql += modifiedAt == null
                    ? " and b.modifiedAt is null and b.id < :beforeId"
                    : " and (b.modifiedAt < :modifiedAt or (b.modifiedAt = :modifiedAt and b.id < :beforeId) or b.modifiedAt is null)";
        }
        jpql += " order by b.modifiedAt desc, b.id desc";
        var query = entityManager.createQuery(jpql, Board.class).setMaxResults(limit);
        if (address != null) query.setParameter("address", address);
        if (beforeId != null) {
            query.setParameter("beforeId", beforeId);
            if (modifiedAt != null) query.setParameter("modifiedAt", modifiedAt);
        }
        return query.getResultList();
    }
}
