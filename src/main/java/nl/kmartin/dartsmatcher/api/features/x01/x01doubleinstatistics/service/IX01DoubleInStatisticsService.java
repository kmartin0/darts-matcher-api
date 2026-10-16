package nl.kmartin.dartsmatcher.api.features.x01.x01doubleinstatistics.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcher.api.features.x01.x01doubleinstatistics.model.X01DoubleInStatistics;
import nl.kmartin.dartsmatcher.api.features.x01.x01leground.model.X01Turn;

public interface IX01DoubleInStatisticsService {

    /**
     * Updates a player's double-in statistics from a processed DIDO turn.
     *
     * @param playerDoubleInStats the double-in statistics to update
     * @param playerTurn          the processed turn
     * @param isOpening           whether this turn first opened the player's leg
     * @param trackDoubles        whether missed doubles are tracked
     */
    void updateDoubleInStatistics(
            @NotNull @Valid X01DoubleInStatistics playerDoubleInStats,
            @NotNull @Valid X01Turn playerTurn,
            boolean isOpening,
            boolean trackDoubles
    );
}