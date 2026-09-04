package nl.kmartin.dartsmatcherapi.features.x01.x01scorestatistics.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01Turn;
import nl.kmartin.dartsmatcherapi.features.x01.x01scorestatistics.model.X01ScoreStatistics;

public interface IX01ScoreStatisticsService {

    /**
     * Updates a player's score statistics from a processed turn.
     *
     * @param scoreStatistics the score statistics to update
     * @param playerTurn      the processed turn
     */
    void updateScoreStatistics(
            @NotNull @Valid X01ScoreStatistics scoreStatistics,
            @NotNull @Valid X01Turn playerTurn
    );
}