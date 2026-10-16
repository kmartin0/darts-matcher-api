package nl.kmartin.dartsmatcher.api.features.x01.x01doubleinstatistics.service;

import nl.kmartin.dartsmatcher.api.features.x01.x01doubleinstatistics.model.X01DoubleInStatistics;
import nl.kmartin.dartsmatcher.api.features.x01.x01leground.model.X01Turn;
import nl.kmartin.dartsmatcher.api.util.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Updates X01 double-in statistics from processed turns.
 */
@Service
@Validated
public class X01DoubleInStatisticsServiceImpl implements IX01DoubleInStatisticsService {

    @Override
    public void updateDoubleInStatistics(
            X01DoubleInStatistics playerDoubleInStats,
            X01Turn playerTurn,
            boolean openedThisTurn,
            boolean trackDoubles
    ) {
        // Count each leg once, on the turn that first opens it.
        if (openedThisTurn) {
            playerDoubleInStats.incrementDoublesHit();
        }

        // Track missed opening attempts and recalculate the percentage when enabled.
        if (trackDoubles) {
            updateDoublesMissed(playerDoubleInStats, playerTurn);
            updateDoublesPercentage(playerDoubleInStats);
        }
    }

    /**
     * Updates the number of missed opening attempts.
     *
     * @param playerDoubleInStats the double-in statistics to update
     * @param playerTurn          the current turn
     */
    private void updateDoublesMissed(X01DoubleInStatistics playerDoubleInStats, X01Turn playerTurn) {
        int openingDoublesMissed = playerTurn.getOpeningDoublesMissed() != null
                ? playerTurn.getOpeningDoublesMissed()
                : 0;

        int doublesMissed = playerDoubleInStats.getDoublesMissed() != null
                ? playerDoubleInStats.getDoublesMissed()
                : 0;

        playerDoubleInStats.setDoublesMissed(doublesMissed + openingDoublesMissed);
    }

    /**
     * Updates the opening percentage based on successful and missed attempts.
     *
     * The percentage remains null when no opening attempts have occurred.
     *
     * @param playerDoubleInStats the double-in statistics to update
     */
    private void updateDoublesPercentage(X01DoubleInStatistics playerDoubleInStats) {
        int openingAttempts = playerDoubleInStats.getDoublesHit() + playerDoubleInStats.getDoublesMissed();

        if (openingAttempts == 0) {
            playerDoubleInStats.setDoublesPercentage(null);
            return;
        }

        int doublesPercentage = NumberUtils.calcPercentage(playerDoubleInStats.getDoublesHit(), openingAttempts);

        playerDoubleInStats.setDoublesPercentage(doublesPercentage);
    }
}