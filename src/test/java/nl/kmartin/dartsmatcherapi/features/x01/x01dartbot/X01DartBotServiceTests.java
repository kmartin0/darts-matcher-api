package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot;

import nl.kmartin.dartsmatcherapi.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapi.features.x01.testutils.X01FeatureTestFactory;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model.X01DartBotTurn;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service.IX01DartBotService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRound;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01Turn;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01DartBotSettings;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.repository.IX01MatchRepository;
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

@ExtendWith(MockitoExtension.class)
public class X01DartBotServiceTests {
    private static final int X01 = 501;
    private static final boolean TRACK_DOUBLES = true;
    private static final int MAX_AVG_TO_TEST = 180;
    private static final int MIN_AVG_TO_TEST = 1;
    private static final int ITERATION_PER_TARGET = 100;

    @Mock
    private IX01MatchRepository matchRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private IX01DartBotService dartBotService;

    @BeforeEach
    void setUp() {
        X01FeatureTestFactory featureTestFactory = new X01FeatureTestFactory(matchRepository, eventPublisher);
        dartBotService = featureTestFactory.createDartBotService();
    }

    @Test
    void dartBotCompletesLegsWithinTargetAverageBounds() {
        final ObjectId dartBotId = new ObjectId();

        // Test the configured average range.
        for (int targetAvg = MAX_AVG_TO_TEST; targetAvg >= MIN_AVG_TO_TEST; targetAvg--) {
            executeTestForTargetAvg(dartBotId, targetAvg);
        }
    }

    private void executeTestForTargetAvg(ObjectId dartBotId, int targetAvg) {
        printTargetAvg(targetAvg);

        // Rebuild the bot player and leg state for the current target average.
        X01Leg x01Leg = new X01Leg(null, dartBotId, new TreeMap<>());

        X01DartBotSettings dartBotSettings = new X01DartBotSettings(targetAvg);
        X01MatchPlayer dartBotPlayer = new X01MatchPlayer(dartBotId, "Dart Bot", PlayerType.DART_BOT, null, dartBotSettings, null);

        // Print the target dart range for inspection.
        System.out.println(TargetDartsBoundaries.create(targetAvg, X01));

        // Track the dart-count distribution.
        Map<Integer, Integer> dartsUsedMap = new TreeMap<>();

        // Simulate multiple legs for this target average.
        for (int j = 0; j < ITERATION_PER_TARGET; j++) {
            int dartsUsed = simulateLeg(targetAvg, dartBotPlayer, x01Leg, dartBotId);
            dartsUsedMap.put(dartsUsed, dartsUsedMap.getOrDefault(dartsUsed, 0) + 1);

            // Reset the recorded turns so the next simulation starts from a fresh leg.
            x01Leg.getRounds().clear();
        }

        System.out.println("Darts Used Map: " + dartsUsedMap);
    }

    private int simulateLeg(int targetAvg, X01MatchPlayer dartBotPlayer, X01Leg currentLeg, ObjectId dartBotId) {
        int round = 1;
        int remaining = X01;
        int dartsUsed = 0;

        // Continue generating bot turns until the leg has been checked out.
        while (remaining != 0) {
            // A negative remaining score means the bot produced an illegal turn.
            if (remaining < 0) {
                Assertions.fail("Remaining went below zero (remaining=" + remaining + ", round=" + round + ")");
            }

            // Generate the next bot turn and map it to an X01Turn.
            X01DartBotTurn dartBotTurn = dartBotService.createDartBotTurn(
                    dartBotPlayer,
                    currentLeg,
                    X01,
                    TRACK_DOUBLES
            );
            int remainingAfterTurn = remaining - dartBotTurn.score();
            X01Turn x01Turn = new X01Turn(dartBotTurn.score(), remainingAfterTurn, dartBotTurn.doublesMissed());

            // Preserve each generated turn because subsequent bot turns use the existing leg history.
            currentLeg.getRounds().put(round++, new X01LegRound(new LinkedHashMap<>(Map.of(dartBotId, x01Turn))));

            // Non-checkout turns consume all three darts; checkout turns expose their exact dart count.
            dartsUsed += dartBotTurn.checkoutDartsUsed() == null ? 3 : dartBotTurn.checkoutDartsUsed();
            remaining = remainingAfterTurn;
        }

        // Verify the completed leg stays within the expected dart-count range.
        assertDartsUsedWithinBounds(targetAvg, dartsUsed);

        // Return the number of darts used by the bot to finish the leg.
        return dartsUsed;
    }

    private void assertDartsUsedWithinBounds(int targetAvg, int dartsUsed) {
        // Derive the acceptable dart-count range from the configured target average.
        TargetDartsBoundaries targetDartsBoundaries = TargetDartsBoundaries.create(targetAvg, X01);

        // Verify the bot did not complete the leg too quickly for the configured average.
        Assertions.assertTrue(
                dartsUsed >= targetDartsBoundaries.lowerTargetNumOfDarts,
                "AssertionFailed(expected: >= " + targetDartsBoundaries.lowerTargetNumOfDarts
                        + ", actual: " + dartsUsed + ")"
        );

        // Verify the bot did not take too long to complete the leg for the configured average.
        Assertions.assertTrue(
                dartsUsed <= targetDartsBoundaries.upperTargetNumOfDarts,
                "exp: <= " + targetDartsBoundaries.upperTargetNumOfDarts + ", act: " + dartsUsed + ")"
        );
    }

    private void printTargetAvg(int targetAvg) {
        System.out.println();
        System.out.println("=".repeat(20));
        System.out.println("Target Avg. " + targetAvg);
        System.out.println("=".repeat(20));
    }

    private record TargetDartsBoundaries(int upperTargetNumOfDarts, int targetNumOfDarts, int lowerTargetNumOfDarts) {

        public static TargetDartsBoundaries create(int targetAvg, int x01) {
            // Convert the configured three-dart average into the expected number of darts required for the leg.
            int targetNumOfDarts = (int) Math.max(9, Math.round(x01 / (targetAvg / 3.0)));

            // Allow a small tolerance around the target to account for the bot's randomized scoring.
            int upperTargetNumOfDarts = (int) Math.max(9, Math.round(targetNumOfDarts * 1.05)) + 1;
            int lowerTargetNumOfDarts = (int) Math.max(9, Math.round(targetNumOfDarts * 0.95)) - 1;

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