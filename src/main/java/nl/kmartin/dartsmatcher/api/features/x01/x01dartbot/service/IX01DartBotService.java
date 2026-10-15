package nl.kmartin.dartsmatcher.api.features.x01.x01dartbot.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcher.api.features.x01.x01dartbot.model.X01DartBotTurn;
import nl.kmartin.dartsmatcher.api.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01MatchSettings;

public interface IX01DartBotService {

    /**
     * Creates a dart bot turn for the supplied player and leg.
     *
     * @param dartBotPlayer the dart bot player
     * @param leg           the leg in which the dart bot is throwing
     * @param matchSettings the match settings defining the starting score, double tracking and format
     * @return the generated dart bot turn
     */
    X01DartBotTurn createDartBotTurn(
            @NotNull @Valid X01MatchPlayer dartBotPlayer,
            @NotNull @Valid X01Leg leg,
            @NotNull @Valid X01MatchSettings matchSettings
    );
}