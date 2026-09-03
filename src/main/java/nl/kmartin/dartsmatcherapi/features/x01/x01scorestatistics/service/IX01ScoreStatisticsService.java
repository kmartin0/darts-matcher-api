package nl.kmartin.dartsmatcherapi.features.x01.x01scorestatistics.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapi.features.x01.x01scorestatistics.model.X01ScoreStatistics;

public interface IX01ScoreStatisticsService {

    /**
     * Updates a player's score statistics from a processed round.
     *
     * @param scoreStatistics the score statistics to update
     * @param playerScore     the score for the processed round
     */
    void updateScoreStatistics(
            @NotNull @Valid X01ScoreStatistics scoreStatistics,
            @NotNull @Valid X01LegRoundScore playerScore
    );
}