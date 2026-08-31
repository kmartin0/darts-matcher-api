package nl.kmartin.dartsmatcherapi.features.x01.x01match.model;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.kmartin.dartsmatcherapi.validators.validdartscore.ValidDartScore;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01Turn {
    public static final int MINIMUM_CHECKOUT_DARTS_USED = 1;
    public static final int MAXIMUM_CHECKOUT_DARTS_USED = 3;
    public static final int MINIMUM_DOUBLES_MISSED = 0;
    public static final int MAXIMUM_DOUBLES_MISSED = 3;

    @ValidDartScore
    int score;

    @Min(MINIMUM_CHECKOUT_DARTS_USED)
    @Max(MAXIMUM_CHECKOUT_DARTS_USED)

    Integer checkoutDartsUsed;

    @Min(MINIMUM_DOUBLES_MISSED)
    @Max(MAXIMUM_DOUBLES_MISSED)
    Integer doublesMissed;
}