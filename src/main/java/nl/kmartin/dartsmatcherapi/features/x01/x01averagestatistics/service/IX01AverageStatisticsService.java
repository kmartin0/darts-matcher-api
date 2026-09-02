package nl.kmartin.dartsmatcherapi.features.x01.x01averagestatistics.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import nl.kmartin.dartsmatcherapi.features.x01.x01averagestatistics.model.X01AverageStatistics;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;

public interface IX01AverageStatisticsService {

    void updateAverageStats(
            @NotNull @Valid X01AverageStatistics playerAverageStats,
            @NotNull @Valid X01LegRoundScore playerScore,
            @Positive int roundNumber,
            @Min(X01Leg.MINIMUM_CHECKOUT_DARTS_USED) @Max(X01Leg.MAXIMUM_CHECKOUT_DARTS_USED) Integer checkoutDartsUsed
    );
}