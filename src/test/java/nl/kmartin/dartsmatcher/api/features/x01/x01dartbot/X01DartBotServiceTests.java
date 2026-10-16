package nl.kmartin.dartsmatcher.api.features.x01.x01dartbot;

import nl.kmartin.dartsmatcher.api.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcher.api.features.x01.testutils.X01FeatureTestFactory;
import nl.kmartin.dartsmatcher.api.features.x01.x01dartbot.model.X01DartBotTurn;
import nl.kmartin.dartsmatcher.api.features.x01.x01dartbot.service.IX01DartBotService;
import nl.kmartin.dartsmatcher.api.features.x01.x01dartbot.service.X01DartBotServiceImpl;
import nl.kmartin.dartsmatcher.api.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcher.api.features.x01.x01leground.model.X01LegRound;
import nl.kmartin.dartsmatcher.api.features.x01.x01leground.model.X01Turn;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01BestOf;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01BestOfType;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01ClearByTwoRule;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01DartBotSettings;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01MatchFormat;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01MatchSettings;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.repository.IX01MatchRepository;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Verifies complete 501 legs through the production dart bot service and prints
 * the dart-count distribution for each configured average.
 *
 * Includes performance calibration and checkout correction.
 * Uses unseeded randomness and runs many simulations.
 */
@ExtendWith(MockitoExtension.class)
public class X01DartBotServiceTests {
    private static final int X01 = 501;
    private static final boolean TRACK_DOUBLES = true;
    private static final X01MatchFormat FORMAT = X01MatchFormat.SIDO;

    private static final int MAX_AVG_TO_TEST = 180;
    private static final int MIN_AVG_TO_TEST = 1;
    private static final int LEGS_PER_TARGET = 1000;

    private static final int MAX_DARTS_PER_LEG = 10000;

    private X01MatchSettings matchSettings;

    @Mock
    private IX01MatchRepository matchRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private IX01DartBotService dartBotService;

    @BeforeEach
    void setUp() {
        X01FeatureTestFactory factory = new X01FeatureTestFactory(matchRepository, eventPublisher);
        dartBotService = factory.createDartBotService();

        matchSettings = new X01MatchSettings(
                X01,
                TRACK_DOUBLES,
                new X01BestOf(
                        1, 1, X01BestOfType.LEGS,
                        new X01ClearByTwoRule(false, 0),
                        new X01ClearByTwoRule(false, 0),
                        new X01ClearByTwoRule(false, 0)
                ),
                FORMAT
        );
    }

    @Test
    void dartBotCompletesLegsWithinTargetAverageBounds() {
            ObjectId dartBotId = new ObjectId();

            for (int targetAvg = MAX_AVG_TO_TEST; targetAvg >= MIN_AVG_TO_TEST; targetAvg--) {
                verifyTargetAverage(dartBotId, targetAvg);
            }
    }

    /**
     * Verifies {@link #LEGS_PER_TARGET} legs for one target average
     * and prints the dart-count distribution.
     */
    private void verifyTargetAverage(ObjectId dartBotId, int targetAvg) {
        X01MatchPlayer dartBotPlayer = createDartBotPlayer(dartBotId, targetAvg);
        TargetDartsBoundaries boundaries = TargetDartsBoundaries.create(targetAvg, X01);
        Map<Integer, Integer> dartsUsedMap = new TreeMap<>();

        printTargetAverage(targetAvg, boundaries);

        for (int leg = 1; leg <= LEGS_PER_TARGET; leg++) {
            int dartsUsed = simulateLeg(dartBotPlayer, dartBotId);
            assertDartsUsedWithinBounds(targetAvg, leg, dartsUsed, boundaries);
            dartsUsedMap.merge(dartsUsed, 1, Integer::sum);
        }

        System.out.println("Darts Used Map: " + dartsUsedMap);
    }

    /**
     * Creates a bot player with the configured target average.
     */
    private X01MatchPlayer createDartBotPlayer(ObjectId dartBotId, int targetAvg) {
        return new X01MatchPlayer(
                dartBotId,
                "Dart Bot",
                PlayerType.DART_BOT,
                null,
                new X01DartBotSettings(targetAvg),
                null
        );
    }

    /**
     * Simulates a fresh leg through the bot service, failing if the dart limit is exceeded.
     */
    private int simulateLeg(X01MatchPlayer dartBotPlayer, ObjectId dartBotId) {
        X01Leg currentLeg = new X01Leg(null, dartBotId, new TreeMap<>());
        int remaining = X01;
        int dartsUsed = 0;

        while (remaining != 0) {
            X01DartBotTurn turn = dartBotService.createDartBotTurn(dartBotPlayer, currentLeg, matchSettings);

            remaining -= turn.score();
            dartsUsed += getDartsUsed(turn);

            recordTurn(currentLeg, dartBotId, turn, remaining);

            if (dartsUsed > MAX_DARTS_PER_LEG) {
                Assertions.fail("Leg exceeded " + MAX_DARTS_PER_LEG + " darts: remaining=" + remaining);
            }
        }

        return dartsUsed;
    }

    /**
     * Records the turn for subsequent bot decisions, rejecting a negative remaining score.
     */
    private void recordTurn(X01Leg leg, ObjectId dartBotId, X01DartBotTurn dartBotTurn, int remaining) {
        int round = leg.getRounds().size() + 1;

        if (remaining < 0) {
            Assertions.fail("Remaining went below zero: remaining=" + remaining + ", round=" + round);
        }

        X01Turn turn = new X01Turn(dartBotTurn.score(), remaining, dartBotTurn.checkoutDoublesMissed(), dartBotTurn.openingDoublesMissed());
        LinkedHashMap<ObjectId, X01Turn> playerTurns = new LinkedHashMap<>();
        playerTurns.put(dartBotId, turn);

        leg.getRounds().put(round, new X01LegRound(playerTurns));
    }

    /**
     * Returns the exact checkout dart count, or three for a non-checkout turn.
     */
    private int getDartsUsed(X01DartBotTurn turn) {
        return turn.checkoutDartsUsed() == null ? X01Turn.MAXIMUM_DARTS_PER_TURN : turn.checkoutDartsUsed();
    }

    /**
     * Verifies that the completed leg's dart count falls within the accepted range.
     */
    private void assertDartsUsedWithinBounds(int targetAvg, int leg, int dartsUsed, TargetDartsBoundaries boundaries) {
        Assertions.assertTrue(
                dartsUsed >= boundaries.lowerTargetNumOfDarts()
                        && dartsUsed <= boundaries.upperTargetNumOfDarts(),
                () -> "Target average: " + targetAvg
                        + ", leg: " + leg
                        + ", expected darts: " + boundaries.lowerTargetNumOfDarts()
                        + "–" + boundaries.upperTargetNumOfDarts()
                        + ", actual: " + dartsUsed
        );
    }

    /**
     * Prints the target average and its accepted dart-count range.
     */
    private void printTargetAverage(int targetAvg, TargetDartsBoundaries boundaries) {
        System.out.println();
        System.out.println("=".repeat(20));
        System.out.println("Target Avg. " + targetAvg);
        System.out.println("=".repeat(20));
        System.out.println(boundaries);
    }

    /**
     * Defines acceptance bounds for 501 legs using a rounded 5% tolerance
     * and an additional one-dart allowance in each direction.
     */
    private record TargetDartsBoundaries(
            int upperTargetNumOfDarts,
            int targetNumOfDarts,
            int lowerTargetNumOfDarts
    ) {
        public static TargetDartsBoundaries create(int targetAvg, int x01) {
            double variance = X01DartBotServiceImpl.TARGET_DART_COUNT_VARIANCE;

            int targetNumOfDarts = (int) Math.max(9, Math.round(x01 / (targetAvg / 3.0)));
            int upperTargetNumOfDarts = (int) Math.max(9, Math.round(targetNumOfDarts * (1 + variance))) + 1;
            int lowerTargetNumOfDarts = (int) Math.max(9, Math.round(targetNumOfDarts * (1 - variance))) - 1;

            return new TargetDartsBoundaries(upperTargetNumOfDarts, targetNumOfDarts, lowerTargetNumOfDarts);
        }

        @Override
        @NotNull
        public String toString() {
            return String.format(
                    "Lower Target: %-10dTarget: %-10dUpper Target: %-10d",
                    lowerTargetNumOfDarts,
                    targetNumOfDarts,
                    upperTargetNumOfDarts
            );
        }
    }
}