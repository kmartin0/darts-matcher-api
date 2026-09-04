package nl.kmartin.dartsmatcherapi.features.x01.x01match.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01Turn;
import nl.kmartin.dartsmatcherapi.validator.validdartscore.ValidDartScore;

/**
 * Represents a request to create a turn for the current thrower in an X01 match.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class X01CreateTurnRequest {

    public static final String FIELD_SCORE = "score";

    @ValidDartScore
    private int score;

    @Positive
    @Max(X01Turn.MAXIMUM_DARTS_PER_TURN)
    private Integer checkoutDartsUsed;

    @PositiveOrZero
    @Max(X01Turn.MAXIMUM_DARTS_PER_TURN)
    private Integer doublesMissed;
}
