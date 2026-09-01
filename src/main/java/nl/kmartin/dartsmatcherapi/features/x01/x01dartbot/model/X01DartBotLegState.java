package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;

/**
 * Stores the state used while simulating a dart bot's turn in an X01 leg.
 *
 * Tracks the bot's progress before and during the current round together with its
 * target average and target number of darts for completing the leg.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class X01DartBotLegState {
    public static final int DARTS_PER_ROUND = 3;

    private int x01;
    private int scoredBeforeRound;
    private int dartsUsedInRound;
    private int dartsUsedBeforeRound;
    private int targetNumOfDarts;
    private double targetOneDartAvg;
    private X01LegRoundScore legRoundScore;

    /**
     * Calculates the remaining points including the score from the current round.
     *
     * @return the remaining points
     */
    public int getRemainingPoints() {
        return x01 - scoredBeforeRound - legRoundScore.getScore();
    }

    /**
     * Calculates the total points scored in the leg including the current round.
     *
     * @return the total points scored in the leg
     */
    public int getScoredInLeg() {
        return scoredBeforeRound + legRoundScore.getScore();
    }

    /**
     * Calculates the total darts used in the leg including the current round.
     *
     * @return the total darts used in the leg
     */
    public int getDartsUsedInLeg() {
        return dartsUsedBeforeRound + dartsUsedInRound;
    }

    /**
     * Calculates the number of darts remaining in the current round.
     *
     * @return the number of darts remaining
     */
    public int getDartsLeftInRound() {
        return DARTS_PER_ROUND - dartsUsedInRound;
    }

    /**
     * Calculates the current one-dart average for the leg.
     *
     * @return the current one-dart average, or 0 when no darts have been thrown
     */
    public double getCurrentOneDartAvg() {
        if (getDartsUsedInLeg() == 0) return 0;

        return (double) getScoredInLeg() / getDartsUsedInLeg();
    }
}