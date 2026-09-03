package nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;

import java.util.List;
import java.util.Set;

/**
 * Represents an X01 checkout and its suggested dart sequence.
 *
 * @param checkout  the remaining score that can be checked out
 * @param minDarts  the minimum number of darts required to complete the checkout
 * @param suggested the suggested dart sequence for completing the checkout
 */
public record X01Checkout(
        @Min(MINIMUM_CHECKOUT)
        @Max(MAXIMUM_CHECKOUT)
        int checkout,

        @Positive
        @Max(X01LegRoundScore.MAXIMUM_DARTS_PER_ROUND)
        int minDarts,

        @NotNull
        List<@Valid Dart> suggested
) {
    public static final int MINIMUM_CHECKOUT = 2;
    public static final int MAXIMUM_CHECKOUT = 170;

    public static final Set<Integer> IMPOSSIBLE_CHECKOUTS = Set.of(169, 168, 166, 165, 163, 162, 159);
}