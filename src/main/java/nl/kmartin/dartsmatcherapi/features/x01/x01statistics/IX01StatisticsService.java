package nl.kmartin.dartsmatcherapi.features.x01.x01statistics;

import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;

public interface IX01StatisticsService {
    void updatePlayerStatistics(X01Match match);
}
