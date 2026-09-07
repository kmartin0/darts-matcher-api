package nl.kmartin.dartsmatcherapi.features.x01.x01checkout.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01Checkout;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01CheckoutInsufficientDartsException;

import java.util.List;
import java.util.Optional;

public interface IX01CheckoutService {

    /**
     * Returns all available checkouts as a list.
     *
     * @return all available checkouts
     */
    List<X01Checkout> getCheckoutsAsList();

    /**
     * Gets the checkout for a remaining score.
     *
     * @param remaining the remaining score
     * @return the checkout, or empty if no checkout is available
     */
    Optional<X01Checkout> getCheckout(int remaining);

    /**
     * Determines whether a score can be checked out.
     *
     * @param score the score to check
     * @return whether the score can be checked out
     */
    boolean isScoreCheckout(int score);

    /**
     * Determines whether a score can be checked out with the given number of darts.
     *
     * @param score     the score to check
     * @param dartsUsed the number of darts used
     * @return whether the score can be checked out
     * @throws X01CheckoutInsufficientDartsException when the checkout requires more darts than were used
     */
    boolean isScoreCheckout(int score, int dartsUsed);

    /**
     * Determines whether a checkout is valid.
     *
     * A checkout is valid when the remaining score is zero and the final dart
     * lands in a double area.
     *
     * @param remaining the remaining score
     * @param lastDart  the final dart thrown
     * @return whether the checkout is valid
     */
    boolean isValidCheckout(int remaining, @NotNull @Valid Dart lastDart);

    /**
     * Determines whether the remaining score is zero or a bust.
     *
     * @param remaining the remaining score
     * @return whether the remaining score is zero or a bust
     */
    boolean isRemainingZeroOrBust(int remaining);

    /**
     * Determines whether the remaining score is a bust.
     *
     * @param remaining the remaining score
     * @return whether the remaining score is a bust
     */
    boolean isRemainingBust(int remaining);

    /**
     * Determines whether the remaining score is zero.
     *
     * @param remaining the remaining score
     * @return whether the remaining score is zero
     */
    boolean isRemainingZero(int remaining);

    /**
     * Determines whether a turn produces a legal X01 result.
     *
     * @param score             the points scored in the turn
     * @param remaining         the remaining score after the turn
     * @param checkoutDartsUsed the number of darts used for the checkout
     * @return whether the turn result is legal
     * @throws X01CheckoutInsufficientDartsException when the checkout requires more darts than were used
     */
    boolean isTurnResultLegal(int score, int remaining, Integer checkoutDartsUsed);
}