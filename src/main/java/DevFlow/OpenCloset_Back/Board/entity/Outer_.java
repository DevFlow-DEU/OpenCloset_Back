package DevFlow.OpenCloset_Back.Board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Outer_ {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "board_id", unique = true)
    private Board board;

    public Outer_(Board board) {
        this.board = board;
    }
}
