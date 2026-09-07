package nl.kmartin.dartsmatcherapi.features.x01.x01checkout.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.DartboardSectionArea;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01Checkout;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01CheckoutInsufficientDartsException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01Turn;
import nl.kmartin.dartsmatcherapi.i18n.MessageResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Loads the configured X01 checkout table and implements checkout lookup and validation.
 */
@Service
@Validated
public class X01CheckoutServiceImpl implements IX01CheckoutService {
    private final Map<Integer, X01Checkout> checkoutsMap;

    public X01CheckoutServiceImpl(
            @Value("classpath:data/checkouts.json") Resource checkoutsResourceFile,
            MessageResolver messageResolver
    ) {
        this.checkoutsMap = createCheckoutMap(checkoutsResourceFile);
    }

    @Override
    public List<X01Checkout> getCheckoutsAsList() {
        return checkoutsMap.values().stream().toList();
    }

    @Override
    public Optional<X01Checkout> getCheckout(int remaining) {
        return Optional.ofNullable(checkoutsMap.get(remaining));
    }

    @Override
    public boolean isScoreCheckout(int score) {
        // A checkout must be within the checkout range and not one of the impossible scores within that range.
        return score >= X01Checkout.MINIMUM_CHECKOUT
                && score <= X01Checkout.MAXIMUM_CHECKOUT
                && !X01Checkout.IMPOSSIBLE_CHECKOUTS.contains(score);
    }

    @Override
    public boolean isScoreCheckout(int score, int dartsUsed) {
        // Reject scores that cannot be checked out or invalid dart counts before consulting the checkout table.
        if (!isScoreCheckout(score) || !isValidDartsUsed(dartsUsed)) return false;

        // A valid checkout score must have a configured checkout route.
        Optional<X01Checkout> checkout = getCheckout(score);
        if (checkout.isEmpty()) return false;

        // Reject checkouts that cannot be completed with the supplied number of darts.
        if (!isEnoughDartsUsedForCheckout(checkout.get(), dartsUsed)) {
            throw new X01CheckoutInsufficientDartsException(score, dartsUsed);
        }

        return true;
    }

    @Override
    public boolean isRemainingZeroOrBust(int remaining) {
        return isRemainingZero(remaining) || isRemainingBust(remaining);
    }

    @Override
    public boolean isRemainingBust(int remaining) {
        // Remaining points below the minimum checkout are a bust unless the leg was checked out exactly.
        return remaining < X01Checkout.MINIMUM_CHECKOUT && !isRemainingZero(remaining);
    }

    @Override
    public boolean isRemainingZero(int remaining) {
        return remaining == 0;
    }

    @Override
    public boolean isValidCheckout(int remaining, Dart lastDart) {
        DartboardSectionArea lastDartArea = lastDart.area();

        // A leg is checked out only when exactly zero remains and the final dart lands in a double.
        return isRemainingZero(remaining) && lastDartArea.isDouble();
    }

    @Override
    public boolean isTurnResultLegal(int score, int remaining, Integer checkoutDartsUsed) {
        // A bust can never represent a legal turn result.
        if (isRemainingBust(remaining)) return false;

        // Reaching zero additionally requires a valid checkout with the supplied dart count.
        if (isRemainingZero(remaining)) {
            return checkoutDartsUsed != null
                    && isScoreCheckout(score, checkoutDartsUsed);
        }

        return true;
    }

    /**
     * Loads the checkout data from the resource file and maps it by checkout score.
     *
     * @param checkoutsResourceFile the checkout data resource
     * @return an unmodifiable map of checkouts by score
     * @throws IllegalStateException if the checkout data cannot be loaded or is empty
     */
    private Map<Integer, X01Checkout> createCheckoutMap(Resource checkoutsResourceFile) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Read the JSON file to a list of X01Checkout objects.
            List<X01Checkout> checkoutsList = mapper.readValue(
                    checkoutsResourceFile.getInputStream(),
                    new TypeReference<>() {
                    }
            );

            if (checkoutsList == null || checkoutsList.isEmpty()) {
                throw new IllegalStateException("Checkouts not found or empty");
            }

            return checkoutsList.stream()
                    .collect(Collectors.toUnmodifiableMap(X01Checkout::checkout, Function.identity()));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize X01CheckoutService due to IO error", e);
        }
    }

    /**
     * Determines whether the given number of darts is valid for a checkout attempt.
     *
     * @param dartsUsed the number of darts used
     * @return whether the number of darts used is within the valid checkout range
     */
    private boolean isValidDartsUsed(int dartsUsed) {
        return dartsUsed > 0 && dartsUsed <= X01Turn.MAXIMUM_DARTS_PER_TURN;
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