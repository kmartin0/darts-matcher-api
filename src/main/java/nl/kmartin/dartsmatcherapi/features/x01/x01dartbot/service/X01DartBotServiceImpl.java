package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service;

import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.DartThrow;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.DartboardSectionArea;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model.X01DartBotTurn;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model.X01DartBotTurnSnapshot;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model.X01DartBotTurnState;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.service.IX01LegResultService;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.util.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Orchestrates the creation of turns for X01 dart bot players.
 *
 * Builds the bot's current turn state, determines its target number of darts,
 * simulates its throws, tracks the resulting score and missed doubles,
 * and converts the result into an X01 turn.
 */
@Service
@Validated
public class X01DartBotServiceImpl implements IX01DartBotService {
    private static final double TARGET_DART_COUNT_VARIANCE = 0.05;

    private final IX01DartBotThrowSimulator dartBotThrowSimulator;
    private final IX01LegResultService legResultService;

    public X01DartBotServiceImpl(
            IX01DartBotThrowSimulator dartBotThrowSimulator,
            IX01LegResultService legResultService
    ) {
        this.dartBotThrowSimulator = dartBotThrowSimulator;
        this.legResultService = legResultService;
    }

    @Override
    public X01DartBotTurn createDartBotTurn(X01MatchPlayer dartBotPlayer, X01Leg leg, int x01, boolean trackDoubles) {
        // Build the current turn state and play the bot's turn by mutating that state.
        X01DartBotTurnState dartBotTurnState = createDartBotTurnState(dartBotPlayer, leg, x01, trackDoubles);
        playTurn(dartBotTurnState);

        // Record checkout dart usage only when this turn completed the leg.
        Integer checkoutDartsUsed = dartBotTurnState.getRemainingPoints() == 0
                ? dartBotTurnState.getDartsUsedInTurn()
                : null;

        // Convert the completed turn state into the generated dart bot turn.
        return new X01DartBotTurn(
                dartBotTurnState.getScoreInTurn(),
                dartBotTurnState.getDoublesMissedInTurn(),
                checkoutDartsUsed
        );
    }

    /**
     * Creates the dart bot state used while simulating a turn.
     *
     * @param dartBotPlayer the dart bot player
     * @param leg           the leg containing the bot's previous turns
     * @param x01           the starting score for the leg
     * @param trackDoubles  whether missed doubles are tracked
     * @return the state used to simulate the dart bot's turn
     */
    private X01DartBotTurnState createDartBotTurnState(X01MatchPlayer dartBotPlayer, X01Leg leg, int x01, boolean trackDoubles) {
        // Calculate the points and darts accumulated before the current turn.
        int scoredBeforeTurn = x01 - legResultService.getRemainingForPlayer(leg, dartBotPlayer.getPlayerId(), x01);
        int dartsUsedBeforeTurn = legResultService.calculateDartsUsed(leg, dartBotPlayer.getPlayerId());

        // Use the configured playing strength to determine the bot's target leg length for this turn.
        double targetOneDartAvg = dartBotPlayer.getX01DartBotSettings().getOneDartAverage();
        int targetNumOfDarts = createTargetNumOfDarts(x01, targetOneDartAvg);

        // Create the initial state for simulating the current dart bot turn.
        return new X01DartBotTurnState(
                x01,
                scoredBeforeTurn,
                dartsUsedBeforeTurn,
                targetNumOfDarts,
                targetOneDartAvg,
                trackDoubles
        );
    }

    /**
     * Creates the target number of darts in which the bot should complete the leg.
     *
     * @param x01              the starting score
     * @param targetOneDartAvg the bot's target one-dart average
     * @return the target number of darts for completing the leg
     */
    private int createTargetNumOfDarts(int x01, double targetOneDartAvg) {
        // Calculate the expected leg length at the bot's target average.
        int expectedNumOfDarts = (int) Math.round(x01 / targetOneDartAvg);

        // Apply variance so repeated legs do not always target the same dart count.
        double lowerBound = expectedNumOfDarts * (1 - TARGET_DART_COUNT_VARIANCE);
        double upperBound = expectedNumOfDarts * (1 + TARGET_DART_COUNT_VARIANCE);
        double randomWithinRange = NumberUtils.randomBetween(lowerBound, upperBound);

        // Keep the generated target at a minimum of one dart.
        return Math.max(1, (int) Math.round(randomWithinRange));
    }

    /**
     * Plays the dart bot's current turn by mutating the supplied turn state.
     *
     * Continues applying simulated dart throws until the bot completes the leg
     * or uses all darts available in the current turn.
     *
     * @param dartBotTurnState the dart bot turn state to mutate
     */
    private void playTurn(X01DartBotTurnState dartBotTurnState) {
        // Continue until the bot completes the leg or uses all darts available in the current turn.
        while (dartBotTurnState.getRemainingPoints() != 0 && dartBotTurnState.getDartsLeftInTurn() > 0) {
            X01DartBotTurnSnapshot dartBotTurnSnapshot = new X01DartBotTurnSnapshot(dartBotTurnState);
            List<DartThrow> dartThrows = dartBotThrowSimulator.getNextDartThrows(dartBotTurnSnapshot);

            // Apply each simulated dart throw to the current turn state.
            for (DartThrow dartThrow : dartThrows) {
                applyDartThrow(dartBotTurnState, dartThrow);
            }
        }
    }

    /**
     * Applies a simulated dart throw by mutating the current dart bot turn state.
     *
     * Updates the turn score, dart count, and missed doubles where applicable.
     *
     * @param dartBotTurnState the dart bot turn state to mutate
     * @param dartThrow        the simulated dart throw
     */
    private void applyDartThrow(X01DartBotTurnState dartBotTurnState, DartThrow dartThrow) {
        // Update the score and dart count for the current turn.
        dartBotTurnState.addScore(dartThrow.result().getScore());
        dartBotTurnState.useDart();

        // Track missed doubles only when double tracking is enabled.
        if (dartBotTurnState.isTrackDoubles() && isDoubleMiss(dartThrow)) {
            dartBotTurnState.addDoubleMiss();
        }
    }

    /**
     * Determines whether a dart aimed at a double missed its specific target.
     *
     * Double bull is treated as a double through {@link DartboardSectionArea#isDouble()}.
     *
     * @param dartThrow the dart throw to evaluate
     * @return whether the intended double was missed
     */
    private boolean isDoubleMiss(DartThrow dartThrow) {
        Dart target = dartThrow.target();
        Dart result = dartThrow.result();

        // Throws not aimed at a double cannot count as missed doubles.
        if (!target.area().isDouble()) {
            return false;
        }

        // Both the section and scoring area must match the intended double.
        return target.section() != result.section()
                || target.area() != result.area();
    }
}