package nl.kmartin.dartsmatcherapi.features.x01.x01checkout.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01Checkout;

import java.util.List;
import java.util.Optional;

public interface IX01CheckoutService {
    List<X01Checkout> getCheckoutsAsList();

    Optional<X01Checkout> getCheckout(int remaining);

    boolean isScoreCheckout(int score);

    boolean isScoreCheckout(int score, int dartsUsed);

    boolean isValidCheckout(int remaining, @NotNull @Valid Dart lastDart);

    boolean isRemainingZeroOrBust(int remaining);

    boolean isRemainingBust(int remaining);

    boolean isRemainingZero(int remaining);
}