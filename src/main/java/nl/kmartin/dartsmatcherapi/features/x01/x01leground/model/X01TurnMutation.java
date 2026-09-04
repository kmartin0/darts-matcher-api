package nl.kmartin.dartsmatcherapi.features.x01.x01leground.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.validator.validdartscore.ValidDartScore;
import org.bson.types.ObjectId;

/**
 * Contains the data required to apply or replace a turn in an X01 leg.
 *
 * @param x01               the starting score for the leg
 * @param leg               the leg to mutate
 * @param roundNumber       the round number
 * @param score             the scored points
 * @param doublesMissed     the number of doubles missed
 * @param checkoutDartsUsed the number of darts used for the checkout
 * @param throwerId         the player that threw the turn
 * @param trackDoubles      whether missed doubles should be tracked
 */
public record X01TurnMutation(
        @Positive int x01,
        @NotNull @Valid X01Leg leg,
        @Positive int roundNumber,
        @ValidDartScore int score,
        @PositiveOrZero @Max(X01Turn.MAXIMUM_DARTS_PER_TURN) Integer doublesMissed,
        @Positive @Max(X01Turn.MAXIMUM_DARTS_PER_TURN) Integer checkoutDartsUsed,
        @NotNull ObjectId throwerId,
        boolean trackDoubles
) {
}
