package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model;

/**
 * Provides the current read-only values used while simulating the next dart bot throw.
 *
 * @param remainingPoints   the points remaining in the leg
 * @param dartsUsedInLeg    the darts used in the leg so far
 * @param dartsLeftInTurn   the darts still available in the current turn
 * @param currentOneDartAvg the bot's current one-dart average
 * @param targetNumOfDarts  the target number of darts for completing the leg
 * @param targetOneDartAvg  the bot's target one-dart average
 */
public record X01DartBotTurnSnapshot(
        int remainingPoints,
        int dartsUsedInLeg,
        int dartsLeftInTurn,
        double currentOneDartAvg,
        int targetNumOfDarts,
        double targetOneDartAvg
) {

    /**
     * Creates a throw context from the current dart bot turn state.
     *
     * @param dartBotTurnState the current dart bot turn state
     */
    public X01DartBotTurnSnapshot(X01DartBotTurnState dartBotTurnState) {
        this(
                dartBotTurnState.getRemainingPoints(),
                dartBotTurnState.getDartsUsedInLeg(),
                dartBotTurnState.getDartsLeftInTurn(),
                dartBotTurnState.getCurrentOneDartAvg(),
                dartBotTurnState.getTargetNumOfDarts(),
                dartBotTurnState.getTargetOneDartAvg()
        );
    }
}