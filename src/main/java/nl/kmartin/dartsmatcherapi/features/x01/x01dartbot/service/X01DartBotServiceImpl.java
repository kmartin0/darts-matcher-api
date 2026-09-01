package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service;

import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.DartThrow;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.DartboardSectionArea;
import nl.kmartin.dartsmatcherapi.features.x01.common.X01MatchUtils;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model.X01DartBotLegState;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.service.IX01LegResultService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Turn;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.service.IX01MatchProgressService;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;
import nl.kmartin.dartsmatcherapi.utils.NumberUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

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
public class X01DartBotServiceImpl implements IX01DartBotService {
    private static final double TARGET_DART_COUNT_VARIANCE = 0.05;
    private static final int MINIMUM_TARGET_DARTS = 1;

    private final IX01MatchProgressService matchProgressService;
    private final IX01DartBotThrowSimulator dartBotThrowSimulator;
    private final IX01LegResultService legResultService;

    public X01DartBotServiceImpl(
            IX01MatchProgressService matchProgressService,
            IX01DartBotThrowSimulator dartBotThrowSimulator,
            IX01LegResultService legResultService) {
        this.matchProgressService = matchProgressService;
        this.dartBotThrowSimulator = dartBotThrowSimulator;
        this.legResultService = legResultService;
    }

    /**
     * Creates the next turn for the current dart bot player.
     *
     * Resolves the current leg, builds the bot's leg state, simulates the round and returns the resulting turn.
     *
     * @param match the match in which the dart bot is throwing
     * @return the generated dart bot turn
     * @throws IllegalStateException when the current thrower is not a configured dart bot or the current leg cannot be resolved
     */
    @Override
    public X01Turn createDartBotTurn(@NotNull X01Match match) {
        X01MatchPlayer dartBotPlayer = getCurrentDartBotPlayer(match);

        // Resolve the current set and leg, creating them when required by the match progress.
        Optional<X01SetEntry> currentSetEntry = matchProgressService.getCurrentSetOrCreate(match);
        X01LegEntry currentLegEntry = currentSetEntry
                .flatMap(setEntry -> matchProgressService.getCurrentLegOrCreate(match, setEntry))
                .orElseThrow(() -> new IllegalStateException(
                        "Unable to resolve the current leg while creating a dart bot turn"
                ));

        // Build the bot's current leg state and simulate its round.
        X01DartBotLegState dartBotLegState = createDartBotLegState(match, dartBotPlayer, currentLegEntry.leg());
        X01LegRoundScore roundScore = createRoundScore(dartBotLegState, match.getMatchSettings().isTrackDoubles());

        // Checkout darts are only supplied when the bot completed the leg during this round.
        Integer checkoutDartsUsed = dartBotLegState.getRemainingPoints() == 0
                ? dartBotLegState.getDartsUsedInRound()
                : null;

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

        // Find the player matching the current thrower and verify that dart bot settings are available.
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
     * The state contains the score and darts already used before the current round,
     * the bot's target one-dart average and its target number of darts for completing the leg.
     *
     * @param match         the current match
     * @param dartBotPlayer the dart bot player
     * @param currentLeg    the current leg
     * @return the state used to simulate the dart bot's turn
     */
    private X01DartBotLegState createDartBotLegState(X01Match match, X01MatchPlayer dartBotPlayer, X01Leg currentLeg) {
        // Get the starting score for the leg.
        int x01 = match.getMatchSettings().getX01();

        // Calculate how many points the bot scored before the current round.
        int legScored = x01 - legResultService.getRemainingForPlayer(currentLeg, dartBotPlayer.getPlayerId(), x01);

        // Calculate how many darts the bot used before the current round.
        int dartsUsed = legResultService.calculateDartsUsed(currentLeg, dartBotPlayer.getPlayerId());

        // Convert the configured three-dart average to the one-dart average used by the simulator.
        double targetOneDartAvg = X01MatchUtils.threeDartAvgToOneDartAvg(
                dartBotPlayer.getX01DartBotSettings().getThreeDartAverage()
        );

        // Start the new round with no darts thrown and no points scored yet.
        return new X01DartBotLegState(
                x01,
                legScored,
                0,
                dartsUsed,
                createTargetNumOfDarts(x01, targetOneDartAvg),
                targetOneDartAvg,
                new X01LegRoundScore(0, 0, x01 - legScored)
        );
    }

    /**
     * Creates the target number of darts in which the bot should complete the leg.
     *
     * The expected number of darts is calculated from the starting score and target
     * one-dart average, then varied within the configured range to produce less predictable leg lengths.
     *
     * @param x01              the starting score
     * @param targetOneDartAvg the bot's target one-dart average
     * @return the target number of darts for completing the leg
     */
    private int createTargetNumOfDarts(int x01, double targetOneDartAvg) {
        // Calculate how many darts would normally be required at the target average.
        int expectedNumOfDarts = (int) Math.round(x01 / targetOneDartAvg);

        // Calculate the allowed variance around the expected number of darts.
        double lowerBound = expectedNumOfDarts * (1 - TARGET_DART_COUNT_VARIANCE);
        double upperBound = expectedNumOfDarts * (1 + TARGET_DART_COUNT_VARIANCE);

        // Pick a random target number of darts within the allowed range.
        double randomWithinRange = NumberUtils.randomBetween(lowerBound, upperBound);

        // Round the result while guaranteeing a target of at least one dart.
        return Math.max(MINIMUM_TARGET_DARTS, (int) Math.round(randomWithinRange));
    }

    /**
     * Simulates the dart bot's current round.
     *
     * Throws are generated and applied until the bot completes the leg or uses
     * all remaining darts in the round.
     *
     * @param dartBotLegState the current dart bot leg state
     * @param trackDoubles    whether missed doubles should be tracked
     * @return the completed round score
     */
    private X01LegRoundScore createRoundScore(X01DartBotLegState dartBotLegState, boolean trackDoubles) {
        int remaining = dartBotLegState.getRemainingPoints();

        // Simulate throws until the leg is finished or no darts remain in the round.
        while (remaining != 0 && dartBotLegState.getDartsLeftInRound() > 0) {
            List<DartThrow> dartThrows = dartBotThrowSimulator.getNextDartThrows(dartBotLegState);

            // Apply each simulated throw to the current round state.
            for (DartThrow dartThrow : dartThrows) {
                updateRoundScore(dartBotLegState, dartThrow, trackDoubles);
                dartBotLegState.setDartsUsedInRound(
                        dartBotLegState.getDartsUsedInRound() + 1
                );
            }

            // Recalculate the remaining score after the simulated throws.
            remaining = dartBotLegState.getRemainingPoints();
        }

        return dartBotLegState.getLegRoundScore();
    }

    /**
     * Applies a simulated dart result to the current round score.
     *
     * Updates the scored and remaining points and, when enabled, tracks a missed
     * double if the dart did not hit the specific double it was targeting.
     *
     * @param dartBotLegState the current dart bot leg state
     * @param dartThrow       the simulated dart throw
     * @param trackDoubles    whether missed doubles should be tracked
     */
    private void updateRoundScore(X01DartBotLegState dartBotLegState, DartThrow dartThrow, boolean trackDoubles) {
        X01LegRoundScore roundScore = dartBotLegState.getLegRoundScore();

        roundScore.setScore(roundScore.getScore() + dartThrow.result().getScore());
        roundScore.setRemaining(dartBotLegState.getRemainingPoints());

        // Count a missed double only when double tracking is enabled.
        if (trackDoubles && isDoubleMiss(dartThrow)) {
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
     * A different section or scoring area therefore counts as a miss.
     *
     * @param dartThrow the dart throw to evaluate
     * @return whether the intended double was missed
     */
    private boolean isDoubleMiss(DartThrow dartThrow) {
        Dart target = dartThrow.target();
        Dart result = dartThrow.result();

        // Only throws aimed at a double can count as a missed double.
        if (!target.area().isDouble()) {
            return false;
        }

        // Both the section and scoring area must match the intended double.
        return target.section() != result.section()
                || target.area() != result.area();
    }
}