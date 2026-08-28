package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Turn;

public interface IX01DartBotService {
    X01Turn createDartBotTurn(@NotNull @Valid X01Match match);
}