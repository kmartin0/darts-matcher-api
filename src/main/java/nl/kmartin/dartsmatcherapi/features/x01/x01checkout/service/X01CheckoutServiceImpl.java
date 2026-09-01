package nl.kmartin.dartsmatcherapi.features.x01.x01checkout.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.kmartin.dartsmatcherapi.error.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapi.error.response.TargetError;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.DartboardSectionArea;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01Checkout;
import nl.kmartin.dartsmatcherapi.i18n.MessageKeys;
import nl.kmartin.dartsmatcherapi.i18n.MessageResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Provides checkout information and validation for X01 matches.
 *
 * Loads the configured checkout table and provides operations for retrieving checkouts
 * and determining whether scores or remaining points are valid checkout states.
 */
@Service
public class X01CheckoutServiceImpl implements IX01CheckoutService {
    private static final Set<Integer> INVALID_CHECKOUTS = Set.of(169, 168, 166, 165, 163, 162, 159);

    private final MessageResolver messageResolver;
    private final Map<Integer, X01Checkout> checkoutsMap;

    public X01CheckoutServiceImpl(@Value("classpath:data/checkouts.json") Resource checkoutsResourceFile,
                                  MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
        this.checkoutsMap = createCheckoutMap(checkoutsResourceFile);
    }

    /**
     * Loads the checkout data from the resource file and maps it by checkout score.
     *
     * @param checkoutsResourceFile the checkout data resource
     * @return an unmodifiable map of checkouts by score
     * @throws IllegalStateException if the checkout data cannot be loaded or is empty
     */
    private static Map<Integer, X01Checkout> createCheckoutMap(Resource checkoutsResourceFile) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Read the JSON file to a list of X01Checkout objects
            List<X01Checkout> checkoutsList = mapper.readValue(
                    checkoutsResourceFile.getInputStream(),
                    new TypeReference<>() {
                    }
            );

            if (checkoutsList == null || checkoutsList.isEmpty()) {
                throw new IllegalStateException("Checkouts not found or empty");
            }

            return checkoutsList.stream().collect(Collectors.toUnmodifiableMap(X01Checkout::checkout, Function.identity()));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize X01CheckoutService due to IO error", e);
        }
    }

    /**
     * Returns all available checkouts as a list.
     *
     * @return all available checkouts
     */
    @Override
    public List<X01Checkout> getCheckoutsAsList() {
        return checkoutsMap.values().stream().toList();
    }

    /**
     * Gets the checkout for a remaining score.
     *
     * @param remaining the remaining score
     * @return the checkout, or empty if no checkout is available
     */
    @Override
    public Optional<X01Checkout> getCheckout(int remaining) {
        return Optional.ofNullable(checkoutsMap.get(remaining));
    }


    /**
     * Determines whether a score can be checked out.
     *
     * @param score the score to check
     * @return whether the score can be checked out
     */
    @Override
    public boolean isScoreCheckout(int score) {
        return score >= X01Checkout.MINIMUM_CHECKOUT
                && score <= X01Checkout.MAXIMUM_CHECKOUT
                && !INVALID_CHECKOUTS.contains(score);
    }

    /**
     * Determines whether a score can be checked out with the given number of darts.
     *
     * @param score     the score to check
     * @param dartsUsed the number of darts used
     * @return whether the score can be checked out
     * @throws InvalidArgumentsException if the checkout requires more darts than were used
     */
    @Override
    public boolean isScoreCheckout(int score, int dartsUsed) {
        if (!isScoreCheckout(score)) return false;
        Optional<X01Checkout> checkout = getCheckout(score);

        if (checkout.isEmpty()) return false;

        if (!isEnoughDartsUsedForCheckout(checkout.get(), dartsUsed)) {
            throw new InvalidArgumentsException(
                    new TargetError(
                            "dartsUsed",
                            messageResolver.getMessage(MessageKeys.MESSAGE_IMPOSSIBLE_CHECKOUT_MIN_DARTS, score, dartsUsed)
                    )
            );
        }

        return true;
    }

    /**
     * Determines whether the remaining score is zero or a bust.
     *
     * @param remaining the remaining score
     * @return whether the remaining score is zero or a bust
     */
    @Override
    public boolean isRemainingZeroOrBust(int remaining) {
        return isRemainingZero(remaining) || isRemainingBust(remaining);
    }

    /**
     * Determines whether the remaining score is a bust.
     *
     * @param remaining the remaining score
     * @return whether the remaining score is a bust
     */
    @Override
    public boolean isRemainingBust(int remaining) {
        return remaining < X01Checkout.MINIMUM_CHECKOUT && !isRemainingZero(remaining);
    }

    /**
     * Determines whether the remaining score is zero.
     *
     * @param remaining the remaining score
     * @return whether the remaining score is zero
     */
    @Override
    public boolean isRemainingZero(int remaining) {
        return remaining == 0;
    }

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
    @Override
    public boolean isValidCheckout(int remaining, Dart lastDart) {
        DartboardSectionArea lastDartArea = lastDart.area();

        return isRemainingZero(remaining) && lastDartArea.isDouble();
    }

    /**
     * Determines whether enough darts were used for the checkout.
     *
     * @param checkout  the checkout
     * @param dartsUsed the number of darts used
     * @return whether enough darts were used
     */
    private boolean isEnoughDartsUsedForCheckout(X01Checkout checkout, int dartsUsed) {
        return dartsUsed >= checkout.minDarts();
    }
}