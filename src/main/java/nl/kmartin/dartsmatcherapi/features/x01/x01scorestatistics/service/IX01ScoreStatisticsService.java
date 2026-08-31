package nl.kmartin.dartsmatcherapi.features.x01.x01scorestatistics.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapi.features.x01.x01scorestatistics.model.X01ScoreStatistics;

public interface IX01ScoreStatisticsService {
    void updateScoreStatistics(X01ScoreStatistics playerScoreStats, X01LegRoundScore playerScore);
}
