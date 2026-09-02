package nl.kmartin.dartsmatcherapi.features.x01.x01scorestatistics.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapi.features.x01.x01scorestatistics.model.X01ScoreStatistics;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Updates score statistics for X01 players based on their round scores.
 */
@Service
@Validated
public class X01ScoreStatisticsServiceImpl implements IX01ScoreStatisticsService {

    /**
     * Updates the score statistic corresponding to the player's round score.
     *
     * @param scoreStatistics the score statistics to update
     * @param playerScore     the player's score for the current round
     */
    @Override
    public void updateScoreStatistics(X01ScoreStatistics scoreStatistics, X01LegRoundScore playerScore) {
        int score = playerScore.getScore();

        // Increment the statistic for the score range containing the round score.
        if (score == X01ScoreStatistics.TON_EIGHTY) {
            scoreStatistics.incrementTonEighty();
        } else if (score >= X01ScoreStatistics.MINIMUM_TON_FORTY_PLUS) {
            scoreStatistics.incrementTonFortyPlus();
        } else if (score >= X01ScoreStatistics.MINIMUM_TON_PLUS) {
            scoreStatistics.incrementTonPlus();
        } else if (score >= X01ScoreStatistics.MINIMUM_EIGHTY_PLUS) {
            scoreStatistics.incrementEightyPlus();
        } else if (score >= X01ScoreStatistics.MINIMUM_SIXTY_PLUS) {
            scoreStatistics.incrementSixtyPlus();
        } else if (score >= X01ScoreStatistics.MINIMUM_FORTY_PLUS) {
            scoreStatistics.incrementFortyPlus();
        }
    }
}