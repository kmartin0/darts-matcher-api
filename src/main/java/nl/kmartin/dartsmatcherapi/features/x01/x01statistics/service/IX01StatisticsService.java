package nl.kmartin.dartsmatcherapi.features.x01.x01statistics.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;

public interface IX01StatisticsService {
    void updatePlayerStatistics(@NotNull @Valid X01Match match);
}