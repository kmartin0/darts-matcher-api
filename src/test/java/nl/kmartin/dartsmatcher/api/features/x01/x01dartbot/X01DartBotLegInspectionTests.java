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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * Prints complete 501 legs through the production bot service for manual inspection.
 * Includes performance calibration and checkout correction.
 */
@Disabled("Manual inspection of bot turn scores")
@ExtendWith(MockitoExtension.class)
public class X01DartBotLegInspectionTests {
    private static final int X01 = 501;
    private static final int TARGET_AVG = 45;
    private static final int LEGS_TO_PRINT = 5;
    private static final int MAX_DARTS_PER_LEG = 10000;
    private static final boolean TRACK_DOUBLES = true;
    private static final X01MatchFormat FORMAT = X01MatchFormat.SIDO;

    @Mock
    private IX01MatchRepository matchRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private IX01DartBotService dartBotService;
    private X01MatchSettings matchSettings;

    /**
     * Creates the bot service with mocked dependencies and configures the match settings.
     */
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

    /**
     * Prints {@value #LEGS_TO_PRINT} legs at the configured target average.
     */
    @Test
    void printBotLegs() {
        ObjectId dartBotId = new ObjectId();
        X01DartBotSettings settings = new X01DartBotSettings(TARGET_AVG);
        X01MatchPlayer player = new X01MatchPlayer(dartBotId, "Dart Bot", PlayerType.DART_BOT, null, settings, null);

        for (int leg = 1; leg <= LEGS_TO_PRINT; leg++) {
            printLegHeader(leg);
            simulateLeg(player, dartBotId);
        }
    }

    /**
     * Simulates a fresh leg and prints each turn and the final average.
     */
    private void simulateLeg(X01MatchPlayer player, ObjectId dartBotId) {
        X01Leg leg = new X01Leg(null, dartBotId, new java.util.TreeMap<>());
        int remaining = X01;
        int dartsUsed = 0;
        int round = 1;

        while (remaining != 0) {
            X01DartBotTurn turn = dartBotService.createDartBotTurn(player, leg, matchSettings);

            remaining -= turn.score();
            dartsUsed += getDartsUsed(turn);

            recordTurn(leg, dartBotId, turn, remaining);
            printTurn(round++, turn, remaining, dartsUsed);

            if (dartsUsed > MAX_DARTS_PER_LEG) {
                Assertions.fail("Leg exceeded " + MAX_DARTS_PER_LEG + " darts: remaining=" + remaining);
            }
        }

        System.out.printf(Locale.ROOT, "%nDarts used: %d | Actual avg: %.2f%n", dartsUsed, X01 * 3.0 / dartsUsed);
    }

    /**
     * Records the turn for subsequent bot decisions, rejecting a negative remaining score.
     */
    private void recordTurn(X01Leg leg, ObjectId dartBotId, X01DartBotTurn dartBotTurn, int remaining) {
        int round = leg.getRounds().size() + 1;

        if (remaining < 0) {
            Assertions.fail("Remaining went below zero: remaining=" + remaining + ", round=" + round);
        }

        X01Turn turn = new X01Turn(dartBotTurn.score(), remaining, dartBotTurn.doublesMissed());
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
     * Prints the leg number, target boundaries and turn-table headings.
     */
    private void printLegHeader(int leg) {
        double variance = X01DartBotServiceImpl.TARGET_DART_COUNT_VARIANCE;
        int target = (int) Math.max(9, Math.round(X01 / (TARGET_AVG / 3.0)));
        int lower = (int) Math.max(9, Math.round(target * (1 - variance))) - 1;
        int upper = (int) Math.max(9, Math.round(target * (1 + variance))) + 1;

        System.out.printf("%n===== Leg %d | Target avg: %d =====%n", leg, TARGET_AVG);
        System.out.printf("Lower target: %d | Target: %d | Upper target: %d%n%n", lower, target, upper);
        System.out.printf("%-7s %-8s %-11s %-8s %-12s %-8s%n",
                "Round", "Score", "Remaining", "Darts", "Dbl missed", "Avg");
    }

    /**
     * Prints a turn with cumulative darts used and the running three-dart average.
     */
    private void printTurn(int round, X01DartBotTurn turn, int remaining, int dartsUsed) {
        String doublesMissed = turn.doublesMissed() == null ? "-" : turn.doublesMissed().toString();
        double average = (X01 - remaining) * 3.0 / dartsUsed;

        System.out.printf(Locale.ROOT, "%-7d %-8d %-11d %-8d %-12s %-8.2f%n",
                round, turn.score(), remaining, dartsUsed, doublesMissed, average);
    }
}