package nl.kmartin.dartsmatcher.api.features.x01.x01match.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.kmartin.dartsmatcher.api.features.x01.x01leground.model.X01Turn;
import nl.kmartin.dartsmatcher.api.validator.validdartscore.ValidDartScore;

/**
 * Represents a request to create a turn for the current thrower in an X01 match.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class X01CreateTurnRequest {

    @ValidDartScore
    private int score;

    @PositiveOrZero
    @Max(X01Turn.MAXIMUM_DARTS_PER_TURN)
    private Integer checkoutDoublesMissed;

    @PositiveOrZero
    @Max(X01Turn.MAXIMUM_DARTS_PER_TURN)
    private Integer openingDoublesMissed;

    @Positive
    @Max(X01Turn.MAXIMUM_DARTS_PER_TURN)
    private Integer checkoutDartsUsed;

    @PositiveOrZero
    private Integer matchVersion;
}