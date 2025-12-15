package nl.kmartin.dartsmatcherapi.features.x01.x01scorestatistics;

import nl.kmartin.dartsmatcherapi.features.x01.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapi.features.x01.model.X01ScoreStatistics;

public interface IX01ScoreStatisticsService {
    void updateScoreStatistics(X01ScoreStatistics playerScoreStats, X01LegRoundScore playerScore);
}
