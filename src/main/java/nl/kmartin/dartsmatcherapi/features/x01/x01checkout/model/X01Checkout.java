package nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model;

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
        int checkout,
        int minDarts,
        List<Dart> suggested
) {
    public static final int MINIMUM_CHECKOUT = 2;
    public static final int MAXIMUM_CHECKOUT = 170;
}