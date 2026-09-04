package nl.kmartin.dartsmatcherapi.features.x01.x01averagestatistics.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import nl.kmartin.dartsmatcherapi.features.x01.x01averagestatistics.model.X01AverageStatistics;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01Turn;

public interface IX01AverageStatisticsService {

    /**
     * Accumulates average statistics from a player's score for one round.
     *
     * The existing statistics are expected to represent all previously processed rounds.
     * Overall statistics are always updated, while first-nine statistics are updated only
     * for rounds belonging to the first nine darts of the leg.
     *
     * @param playerAverageStats the accumulated average statistics to update
     * @param playerScore        the score for the round being processed
     * @param roundNumber        the round number
     * @param checkoutDartsUsed  number of darts used when checking out, or null when no checkout occurred
     */
    void updateAverageStats(
            @NotNull @Valid X01AverageStatistics playerAverageStats,
            @NotNull @Valid X01Turn playerScore,
            @Positive int roundNumber,
            @Positive @Max(X01Turn.MAXIMUM_DARTS_PER_TURN) Integer checkoutDartsUsed
    );
}