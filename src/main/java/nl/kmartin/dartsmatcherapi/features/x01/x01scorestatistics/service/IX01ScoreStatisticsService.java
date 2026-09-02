package nl.kmartin.dartsmatcherapi.features.x01.x01scorestatistics.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapi.features.x01.x01scorestatistics.model.X01ScoreStatistics;

public interface IX01ScoreStatisticsService {
    void updateScoreStatistics(
            @NotNull @Valid X01ScoreStatistics scoreStatistics,
            @NotNull @Valid X01LegRoundScore playerScore
    );
}
