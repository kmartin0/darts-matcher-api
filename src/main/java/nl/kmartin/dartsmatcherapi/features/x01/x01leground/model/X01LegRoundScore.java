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
    public static final int MINIMUM_SCORE = 0;
    public static final int MAXIMUM_SCORE = 180;
    public static final int MINIMUM_REMAINING = 0;

    @Min(X01Turn.MINIMUM_DOUBLES_MISSED)
    @Max(X01Turn.MAXIMUM_DOUBLES_MISSED)
    private Integer doublesMissed;

    @Min(MINIMUM_SCORE)
    @Max(MAXIMUM_SCORE)
    private int score;

    @Min(MINIMUM_REMAINING)
    private int remaining;

    public X01LegRoundScore(X01Turn turn, boolean trackDoubles) {
        this.doublesMissed = trackDoubles
                ? (turn.getDoublesMissed() != null ? turn.getDoublesMissed() : 0)
                : null;
        this.score = turn.getScore();
    }
}
