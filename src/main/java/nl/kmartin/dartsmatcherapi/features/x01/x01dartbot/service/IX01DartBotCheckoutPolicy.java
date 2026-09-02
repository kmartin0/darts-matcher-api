package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model.X01DartBotLegState;

public interface IX01DartBotCheckoutPolicy {
    boolean isTargetNumOfDartsReached(int dartsThrown, int targetNumOfDarts);

    boolean isDartResultValid(
            @NotNull @Valid Dart result,
            @NotNull X01DartBotLegState dartBotLegState
    );
}