package nl.kmartin.dartsmatcherapi.features.x01.x01match.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapi.validator.validdartscore.ValidDartScore;

/**
 * Represents a single turn played in an X01 match.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class X01Turn {

    public static final String FIELD_SCORE = "score";

    @ValidDartScore
    private int score;

    @Positive
    @Max(X01LegRoundScore.MAXIMUM_DARTS_PER_ROUND)
    private Integer checkoutDartsUsed;

    @PositiveOrZero
    @Max(X01LegRoundScore.MAXIMUM_DARTS_PER_ROUND)
    private Integer doublesMissed;
}