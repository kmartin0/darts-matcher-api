package nl.kmartin.dartsmatcherapi.features.x01.x01averagestatistics.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01averagestatistics.model.X01AverageStatistics;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Calculates and updates average statistics for X01 players.
 *
 * Tracks overall throwing statistics and the separate first-nine statistics for the first three rounds of a leg.
 */
@Service
@Validated
public class X01AverageStatisticsServiceImpl implements IX01AverageStatisticsService {
    private static final int DARTS_PER_ROUND = 3;
    private static final int FIRST_NINE_ROUNDS = 3;

    /**
     * Updates the player's average statistics based on the current round.
     *
     * @param playerAverageStats the average statistics to update
     * @param playerScore        the score for the current round
     * @param roundNumber        the current round number
     * @param checkoutDartsUsed  number of darts used when checking out, or null when no checkout occurred
     */
    @Override
    public void updateAverageStats(
            X01AverageStatistics playerAverageStats,
            X01LegRoundScore playerScore,
            int roundNumber,
            Integer checkoutDartsUsed
    ) {
        int dartsUsed = checkoutDartsUsed != null
                ? checkoutDartsUsed
                : DARTS_PER_ROUND;

        updatePointsThrown(playerAverageStats, playerScore);
        updateDartsThrown(playerAverageStats, dartsUsed);
        updateAverage(playerAverageStats);

        if (roundNumber <= FIRST_NINE_ROUNDS) {
            updatePointsThrownFirstNine(playerAverageStats, playerScore);
            updateDartsThrownFirstNine(playerAverageStats, dartsUsed);
            updateAverageFirstNine(playerAverageStats);
        }
    }

    /**
     * Updates the total points thrown.
     *
     * @param playerAverageStats the average statistics to update
     * @param playerScore        the score for the current round
     */
    private void updatePointsThrown(X01AverageStatistics playerAverageStats, X01LegRoundScore playerScore) {
        playerAverageStats.setPointsThrown(
                playerAverageStats.getPointsThrown() + playerScore.getScore()
        );
    }

    /**
     * Updates the total darts thrown.
     *
     * @param playerAverageStats the average statistics to update
     * @param dartsUsed          the number of darts used
     */
    private void updateDartsThrown(X01AverageStatistics playerAverageStats, int dartsUsed) {
        playerAverageStats.setDartsThrown(
                playerAverageStats.getDartsThrown() + dartsUsed
        );
    }

    /**
     * Updates the player's three-dart average.
     *
     * @param playerAverageStats the average statistics to update
     */
    private void updateAverage(X01AverageStatistics playerAverageStats) {
        playerAverageStats.setAverage(
                calculateThreeDartAverage(
                        playerAverageStats.getPointsThrown(),
                        playerAverageStats.getDartsThrown()
                )
        );
    }

    /**
     * Updates the total points thrown during the first nine darts.
     *
     * @param playerAverageStats the average statistics to update
     * @param playerScore        the score for the current round
     */
    private void updatePointsThrownFirstNine(X01AverageStatistics playerAverageStats, X01LegRoundScore playerScore) {
        playerAverageStats.setPointsThrownFirstNine(
                playerAverageStats.getPointsThrownFirstNine() + playerScore.getScore()
        );
    }

    /**
     * Updates the total darts thrown during the first nine darts.
     *
     * @param playerAverageStats the average statistics to update
     * @param dartsUsed          the number of darts used
     */
    private void updateDartsThrownFirstNine(X01AverageStatistics playerAverageStats, int dartsUsed) {
        playerAverageStats.setDartsThrownFirstNine(
                playerAverageStats.getDartsThrownFirstNine() + dartsUsed
        );
    }

    /**
     * Updates the player's three-dart average for the first nine darts.
     *
     * @param playerAverageStats the average statistics to update
     */
    private void updateAverageFirstNine(X01AverageStatistics playerAverageStats) {
        playerAverageStats.setAverageFirstNine(
                calculateThreeDartAverage(
                        playerAverageStats.getPointsThrownFirstNine(),
                        playerAverageStats.getDartsThrownFirstNine()
                )
        );
    }

    /**
     * Calculates a three-dart average.
     *
     * @param pointsThrown total points thrown
     * @param dartsThrown  total darts thrown
     * @return the rounded three-dart average
     */
    private int calculateThreeDartAverage(int pointsThrown, int dartsThrown) {
        double oneDartAverage = (double) pointsThrown / dartsThrown;
        return (int) Math.round(oneDartAverage * DARTS_PER_ROUND);
    }
}