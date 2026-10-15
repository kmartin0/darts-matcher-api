package nl.kmartin.dartsmatcher.api.features.x01.x01leground.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01MatchFormat;

/**
 * Represents a player's turn within an X01 leg round.
 *
 * Contains the scored points, remaining points after the turn and optionally the number of doubles missed.
 */
@Getter
@Setter
@ToString
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
    private Integer checkoutDoublesMissed;

    @PositiveOrZero
    @Max(MAXIMUM_DARTS_PER_TURN)
    private Integer openingDoublesMissed;

    /**
     * Creates a processed turn from submitted turn values.
     *
     * Missed doubles are stored only when double tracking is enabled.
     * Opening doubles missed are stored only for DIDO matches.
     * Missing values default to zero when tracked.
     *
     * @param score                 the points scored in the turn
     * @param remaining             the remaining score after the turn
     * @param checkoutDoublesMissed the number of doubles missed when attempting to finish
     * @param openingDoublesMissed  the number of doubles missed when attempting to start scoring
     * @param trackDoubles          whether missed doubles should be tracked
     * @param format                the match format
     */
    public X01Turn(
            int score,
            int remaining,
            Integer checkoutDoublesMissed,
            Integer openingDoublesMissed,
            boolean trackDoubles,
            X01MatchFormat format
    ) {
        this.score = score;
        this.remaining = remaining;
        this.checkoutDoublesMissed = trackDoubles
                ? (checkoutDoublesMissed != null ? checkoutDoublesMissed : 0)
                : null;
        this.openingDoublesMissed = trackDoubles && format == X01MatchFormat.DIDO
                ? (openingDoublesMissed != null ? openingDoublesMissed : 0)
                : null;
    }
}