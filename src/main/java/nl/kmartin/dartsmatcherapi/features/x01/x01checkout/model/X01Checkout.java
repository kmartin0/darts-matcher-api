package nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;

import java.util.List;

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

        @Min(MINIMUM_CHECKOUT_DARTS)
        @Max(MAXIMUM_CHECKOUT_DARTS)
        int minDarts,

        @NotNull
        List<@Valid Dart> suggested
) {
    public static final int MINIMUM_CHECKOUT = 2;
    public static final int MAXIMUM_CHECKOUT = 170;

    public static final int MINIMUM_CHECKOUT_DARTS = 1;
    public static final int MAXIMUM_CHECKOUT_DARTS = 3;
}