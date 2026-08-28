package nl.kmartin.dartsmatcherapi.features.x01.x01leground.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Turn;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01LegRoundScore {
    @Min(0)
    @Max(3)
    private Integer doublesMissed;

    @Min(0)
    @Max(180)
    private int score;

    @Min(0)
    private int remaining;

    public X01LegRoundScore(X01Turn turn, boolean trackDoubles) {
        this.doublesMissed = trackDoubles
                ? (turn.getDoublesMissed() != null ? turn.getDoublesMissed() : 0)
                : null;
        this.score = turn.getScore();
    }
}
