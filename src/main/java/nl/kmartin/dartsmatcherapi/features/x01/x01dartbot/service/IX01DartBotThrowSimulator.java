package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service;

import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.DartThrow;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model.X01DartBotLegState;

import java.util.List;

public interface IX01DartBotThrowSimulator {

    /**
     * Generates the next dart throws for the current dart bot leg state.
     *
     * @param dartBotLegState the current dart bot leg state
     * @return the generated dart throws
     */
    List<DartThrow> getNextDartThrows(@NotNull X01DartBotLegState dartBotLegState);
}