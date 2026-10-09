package nl.kmartin.dartsmatcher.api.features.x01.x01dartbot.service;

import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcher.api.features.dartboard.model.DartThrow;
import nl.kmartin.dartsmatcher.api.features.x01.x01dartbot.model.X01DartBotTurnSnapshot;

import java.util.List;

public interface IX01DartBotThrowSimulator {

    /**
     * Generates the next dart throws for the current dart bot leg state.
     *
     * @param dartBotTurnSnapshot the current dart bot turn snapshot
     * @return the generated dart throws
     */
    List<DartThrow> getNextDartThrows(@NotNull X01DartBotTurnSnapshot dartBotTurnSnapshot);
}