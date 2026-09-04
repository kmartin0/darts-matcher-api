package nl.kmartin.dartsmatcherapi.features.x01.x01leground.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a player's turn within an X01 leg round.
 *
 * Contains the scored points, remaining points after the turn and optionally the number of doubles missed.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class X01Turn {
    public static final int MAXIMUM_DARTS_PER_TURN = 3;
    public static final int MAXIMUM_SCORE_PER_TURN = 180;

    @PositiveOrZero
    @Max(MAXIMUM_SCORE_PER_TURN)
    private int score;

    @PositiveOrZero
    private int remaining;

    @PositiveOrZero
    @Max(MAXIMUM_DARTS_PER_TURN)
    private Integer doublesMissed;

    /**
     * Creates a processed turn from submitted turn values.
     *
     * Missed doubles are stored only when double tracking is enabled.
     *
     * @param score         the points scored in the turn
     * @param remaining     the remaining score after the turn
     * @param doublesMissed the number of doubles missed
     * @param trackDoubles  whether missed doubles should be tracked
     */
    public X01Turn(int score, int remaining, Integer doublesMissed, boolean trackDoubles) {
        this.score = score;
        this.remaining = remaining;
        this.doublesMissed = trackDoubles
                ? (doublesMissed != null ? doublesMissed : 0)
                : null;
    }
}