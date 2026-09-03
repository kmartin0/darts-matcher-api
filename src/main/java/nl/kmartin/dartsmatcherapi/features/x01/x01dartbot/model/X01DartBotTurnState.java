package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model;

import lombok.Getter;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;

/**
 * Stores the state used while playing a dart bot's turn in an X01 leg.
 *
 * Combines the fixed turn configuration with the progress accumulated while darts are played.
 */
@Getter
public class X01DartBotTurnState {
    private final int x01;
    private final int scoredBeforeTurn;
    private final int dartsUsedBeforeTurn;
    private final int targetNumOfDarts;
    private final double targetOneDartAvg;
    private final boolean trackDoubles;

    private int scoreInTurn;
    private int dartsUsedInTurn;
    private Integer doublesMissedInTurn;

    public X01DartBotTurnState(
            int x01,
            int scoredBeforeTurn,
            int dartsUsedBeforeTurn,
            int targetNumOfDarts,
            double targetOneDartAvg,
            boolean trackDoubles
    ) {
        this.x01 = x01;
        this.scoredBeforeTurn = scoredBeforeTurn;
        this.dartsUsedBeforeTurn = dartsUsedBeforeTurn;
        this.targetNumOfDarts = targetNumOfDarts;
        this.targetOneDartAvg = targetOneDartAvg;
        this.trackDoubles = trackDoubles;
    }

    /**
     * Calculates the remaining points including the score from the current turn.
     *
     * @return the remaining points
     */
    public int getRemainingPoints() {
        return x01 - scoredBeforeTurn - scoreInTurn;
    }

    /**
     * Calculates the total points scored in the leg including the current turn.
     *
     * @return the total points scored in the leg
     */
    public int getScoredInLeg() {
        return scoredBeforeTurn + scoreInTurn;
    }

    /**
     * Calculates the total darts used in the leg including the current turn.
     *
     * @return the total darts used
     */
    public int getDartsUsedInLeg() {
        return dartsUsedBeforeTurn + dartsUsedInTurn;
    }

    /**
     * Calculates the number of darts remaining in the current turn.
     *
     * @return the number of darts remaining
     */
    public int getDartsLeftInTurn() {
        return X01LegRoundScore.MAXIMUM_DARTS_PER_ROUND - dartsUsedInTurn;
    }

    /**
     * Calculates the current one-dart average for the leg.
     *
     * @return the current one-dart average, or 0 when no darts have been thrown
     */
    public double getCurrentOneDartAvg() {
        if (getDartsUsedInLeg() == 0) {
            return 0;
        }

        return (double) getScoredInLeg() / getDartsUsedInLeg();
    }

    /**
     * Adds scored points to the current turn.
     *
     * @param score the points to add
     */
    public void addScore(int score) {
        scoreInTurn += score;
    }

    /**
     * Records one dart used in the current turn.
     */
    public void useDart() {
        dartsUsedInTurn++;
    }

    /**
     * Records a missed double in the current turn.
     */
    public void addDoubleMiss() {
        doublesMissedInTurn = doublesMissedInTurn == null ? 1 : doublesMissedInTurn + 1;
    }
}