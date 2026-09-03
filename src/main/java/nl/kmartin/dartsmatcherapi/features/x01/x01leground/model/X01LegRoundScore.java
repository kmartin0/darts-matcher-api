package nl.kmartin.dartsmatcherapi.features.x01.x01leground.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Turn;

/**
 * Stores a player's score for a round within an X01 leg.
 *
 * Contains the scored points, remaining points after the turn and optionally
 * the number of doubles missed.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class X01LegRoundScore {
    public static final int MAXIMUM_DARTS_PER_ROUND = 3;
    public static final int MAXIMUM_SCORE = 180;

    @PositiveOrZero
    @Max(MAXIMUM_DARTS_PER_ROUND)
    private Integer doublesMissed;

    @PositiveOrZero
    @Max(MAXIMUM_SCORE)
    private int score;

    @PositiveOrZero
    private int remaining;

    /**
     * Creates a round score from an X01 turn.
     *
     * Missed doubles are stored only when double tracking is enabled. Remaining
     * points are calculated separately while processing the leg.
     *
     * @param turn the turn to convert
     * @param trackDoubles whether missed doubles should be tracked
     */
    public X01LegRoundScore(X01Turn turn, boolean trackDoubles) {
        this.doublesMissed = trackDoubles
                ? (turn.getDoublesMissed() != null ? turn.getDoublesMissed() : 0)
                : null;
        this.score = turn.getScore();
    }
}