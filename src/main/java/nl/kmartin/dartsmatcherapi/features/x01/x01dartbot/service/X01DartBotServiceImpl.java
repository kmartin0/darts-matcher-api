package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service;

import nl.kmartin.dartsmatcherapi.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.DartThrow;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.DartboardSectionArea;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model.X01DartBotLegState;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.service.IX01LegResultService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Turn;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.service.IX01MatchProgressService;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;
import nl.kmartin.dartsmatcherapi.util.NumberUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * Orchestrates the creation of turns for X01 dart bot players.
 *
 * Builds the bot's current leg state, determines its target number of darts,
 * simulates its throws, tracks the resulting round score and missed doubles,
 * and converts the result into an X01 turn.
 */
@Service
@Validated
public class X01DartBotServiceImpl implements IX01DartBotService {
    private static final double TARGET_DART_COUNT_VARIANCE = 0.05;

    private final IX01MatchProgressService matchProgressService;
    private final IX01DartBotThrowSimulator dartBotThrowSimulator;
    private final IX01LegResultService legResultService;

    public X01DartBotServiceImpl(
            IX01MatchProgressService matchProgressService,
            IX01DartBotThrowSimulator dartBotThrowSimulator,
            IX01LegResultService legResultService
    ) {
        this.matchProgressService = matchProgressService;
        this.dartBotThrowSimulator = dartBotThrowSimulator;
        this.legResultService = legResultService;
    }

    @Override
    public X01Turn createDartBotTurn(X01Match match) {
        // Resolve and verify the current dart bot player.
        X01MatchPlayer dartBotPlayer = getCurrentDartBotPlayer(match);

        // Resolve the current set and leg, creating them when required by the match progress.
        Optional<X01SetEntry> currentSetEntry = matchProgressService.getCurrentSetOrCreate(match);
        X01LegEntry currentLegEntry = currentSetEntry
                .flatMap(setEntry -> matchProgressService.getCurrentLegOrCreate(match, setEntry))
                .orElseThrow(() -> new IllegalStateException(
                        "Unable to resolve the current leg while creating a dart bot turn"
                ));

        // Build the current leg state and simulate the bot's round.
        X01DartBotLegState dartBotLegState = createDartBotLegState(match, dartBotPlayer, currentLegEntry.leg());
        X01LegRoundScore roundScore = createRoundScore(dartBotLegState);

        // Record checkout dart usage only when this round completed the leg.
        Integer checkoutDartsUsed = dartBotLegState.getRemainingPoints() == 0
                ? dartBotLegState.getDartsUsedInRound()
                : null;

        // Convert the completed round state into the turn persisted on the match.
        return new X01Turn(roundScore.getScore(), checkoutDartsUsed, roundScore.getDoublesMissed());
    }

    /**
     * Resolves the current thrower and verifies that it is a configured dart bot.
     *
     * @param match the current match
     * @return the current dart bot player
     * @throws IllegalStateException when the current thrower is not a configured dart bot
     */
    private X01MatchPlayer getCurrentDartBotPlayer(X01Match match) {
        ObjectId currentThrower = match.getMatchProgress().getCurrentThrower();

        // Find the current player and verify that it has dart bot configuration.
        return match.getPlayers()
                .stream()
                .filter(matchPlayer ->
                        matchPlayer.getPlayerId().equals(currentThrower)
                                && matchPlayer.getPlayerType() == PlayerType.DART_BOT
                                && matchPlayer.getX01DartBotSettings() != null
                )
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Current thrower is not a configured dart bot"));
    }

    /**
     * Creates the dart bot state used while simulating the current leg.
     *
     * @param match         the current match
     * @param dartBotPlayer the dart bot player
     * @param currentLeg    the current leg
     * @return the state used to simulate the dart bot's turn
     */
    private X01DartBotLegState createDartBotLegState(
            X01Match match,
            X01MatchPlayer dartBotPlayer,
            X01Leg currentLeg
    ) {
        // Get the starting score for the leg.
        int x01 = match.getMatchSettings().getX01();
        boolean trackDoubles = match.getMatchSettings().isTrackDoubles();

        // Calculate the points and darts already accumulated before the current round.
        int legScored = x01 - legResultService.getRemainingForPlayer(currentLeg, dartBotPlayer.getPlayerId(), x01);
        int dartsUsed = legResultService.calculateDartsUsed(currentLeg, dartBotPlayer.getPlayerId());

        // Use the configured playing strength to determine the bot's target leg length.
        double targetOneDartAvg = dartBotPlayer.getX01DartBotSettings().getOneDartAverage();

        // Start the current round with no darts thrown and no points scored.
        return new X01DartBotLegState(
                x01,
                legScored,
                0,
                dartsUsed,
                createTargetNumOfDarts(x01, targetOneDartAvg),
                targetOneDartAvg,
                new X01LegRoundScore(null, 0, x01 - legScored),
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
     * Simulates the dart bot's current round.
     *
     * @param dartBotLegState the current dart bot leg state
     * @return the completed round score
     */
    private X01LegRoundScore createRoundScore(X01DartBotLegState dartBotLegState) {
        int remaining = dartBotLegState.getRemainingPoints();

        // Continue until the bot completes the leg or exhausts the darts available in the round.
        while (remaining != 0 && dartBotLegState.getDartsLeftInRound() > 0) {
            List<DartThrow> dartThrows = dartBotThrowSimulator.getNextDartThrows(dartBotLegState);

            // Apply each simulated dart to the round state.
            for (DartThrow dartThrow : dartThrows) {
                updateRoundScore(dartBotLegState, dartThrow);
                dartBotLegState.setDartsUsedInRound(dartBotLegState.getDartsUsedInRound() + 1);
            }

            // Re-evaluate whether the simulated round has completed the leg.
            remaining = dartBotLegState.getRemainingPoints();
        }

        return dartBotLegState.getLegRoundScore();
    }

    /**
     * Applies a simulated dart result to the current round score.
     *
     * @param dartBotLegState the current dart bot leg state
     * @param dartThrow       the simulated dart throw
     */
    private void updateRoundScore(X01DartBotLegState dartBotLegState, DartThrow dartThrow) {
        X01LegRoundScore roundScore = dartBotLegState.getLegRoundScore();

        // Add the dart result and recalculate the remaining score.
        roundScore.setScore(roundScore.getScore() + dartThrow.result().getScore());
        roundScore.setRemaining(dartBotLegState.getRemainingPoints());

        // Count missed doubles only when double tracking is enabled.
        if (dartBotLegState.isTrackDoubles() && isDoubleMiss(dartThrow)) {
            if (roundScore.getDoublesMissed() == null) {
                roundScore.setDoublesMissed(0);
            }

            roundScore.setDoublesMissed(roundScore.getDoublesMissed() + 1);
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