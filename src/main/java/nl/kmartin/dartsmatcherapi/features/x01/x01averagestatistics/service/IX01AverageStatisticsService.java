package nl.kmartin.dartsmatcherapi.features.x01.x01averagestatistics.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01averagestatistics.model.X01AverageStatistics;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;

public interface IX01AverageStatisticsService {
    void updateAverageStats(X01AverageStatistics playerAverageStats, X01LegRoundScore playerScore, int roundNumber, Integer checkoutDartsUsed);
}
