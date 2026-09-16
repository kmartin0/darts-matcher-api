package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model.X01DartBotTurn;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;

public interface IX01DartBotService {

    /**
     * Creates a dart bot turn for the supplied player and leg.
     *
     * @param dartBotPlayer the dart bot player
     * @param leg           the leg in which the dart bot is throwing
     * @param x01           the starting score for the leg
     * @param trackDoubles  whether missed doubles are tracked
     * @return the generated dart bot turn
     */
    X01DartBotTurn createDartBotTurn(
            @NotNull @Valid X01MatchPlayer dartBotPlayer,
            @NotNull @Valid X01Leg leg,
            int x01,
            boolean trackDoubles
    );
}