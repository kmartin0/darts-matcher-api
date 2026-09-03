package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Turn;

public interface IX01DartBotService {

    /**
     * Creates the next turn for the current dart bot player.
     *
     * @param match the match in which the dart bot is throwing
     * @return the generated dart bot turn
     * @throws IllegalStateException when the current thrower is not a configured dart bot or the current leg cannot be resolved
     */
    X01Turn createDartBotTurn(@NotNull @Valid X01Match match);
}