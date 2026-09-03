package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model.X01DartBotLegState;

public interface IX01DartBotCheckoutPolicy {

    /**
     * Determines whether the target number of darts has been reached.
     *
     * @param dartsThrown      the number of darts thrown
     * @param targetNumOfDarts the target number of darts
     * @return whether the target number of darts has been reached
     */
    boolean isTargetNumOfDartsReached(int dartsThrown, int targetNumOfDarts);

    /**
     * Determines whether a simulated dart result is valid for the current leg state.
     *
     * @param result          the dart result
     * @param dartBotLegState the current dart bot leg state
     * @return whether the dart result is valid
     */
    boolean isDartResultValid(@NotNull @Valid Dart result, @NotNull X01DartBotLegState dartBotLegState);
}