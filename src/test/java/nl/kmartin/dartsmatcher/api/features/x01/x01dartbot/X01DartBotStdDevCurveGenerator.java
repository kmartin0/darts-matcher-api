package nl.kmartin.dartsmatcher.api.features.x01.x01dartbot;

import nl.kmartin.dartsmatcher.api.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcher.api.features.dartboard.service.IDartboardService;
import nl.kmartin.dartsmatcher.api.features.x01.testutils.X01FeatureTestFactory;
import nl.kmartin.dartsmatcher.api.features.x01.x01checkout.service.IX01CheckoutService;
import nl.kmartin.dartsmatcher.api.features.x01.x01dartbot.service.IX01DartBotDeviationCalculator;
import nl.kmartin.dartsmatcher.api.features.x01.x01dartbot.service.IX01DartBotScoringStrategy;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.repository.IX01MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Generates a baseline average-to-standard-deviation curve from simulated 501 legs
 * using the weighted scoring strategy, without performance or checkout correction.
 *
 * Measured averages are rounded upward to whole numbers. When keys coincide,
 * the smallest standard deviation is retained. Results vary between runs.
 *
 * Entries at and above {@link #ZERO_DEVIATION_AVERAGE} are replaced with a
 * zero-deviation endpoint to enforce perfect accuracy at the configured threshold.
 */
@SuppressWarnings("NewClassNamingConvention")
@Disabled("Manual utility for generating the standard deviation curve")
public class X01DartBotStdDevCurveGenerator {
    private static final int X01 = 501;
    private static final int ZERO_DEVIATION_AVERAGE = 150;
    private static final int LEGS_PER_STANDARD_DEVIATION = 10000;
    private static final int MAX_DARTS_PER_LEG = 10000;

    // Standard deviations in millimeters sampled to generate the calibration curve.
    private static final double[] STANDARD_DEVIATIONS = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
            12, 14, 16, 18, 20, 25, 30, 35, 40, 50, 60, 70, 80,
            100, 125, 150, 200
    };

    private IX01CheckoutService checkoutService;
    private IDartboardService dartboardService;
    private IX01DartBotDeviationCalculator deviationCalculator;
    private IX01DartBotScoringStrategy scoringStrategy;

    @BeforeEach
    void setUp() {
        X01FeatureTestFactory factory = new X01FeatureTestFactory(
                Mockito.mock(IX01MatchRepository.class),
                Mockito.mock(ApplicationEventPublisher.class)
        );

        checkoutService = factory.createCheckoutService();
        dartboardService = factory.createDartboardService();
        deviationCalculator = factory.createDartBotDeviationCalculator();
        scoringStrategy = factory.createDartBotScoringStrategy();
    }

    @Test
    void generateStandardDeviationCurve() {
        NavigableMap<Double, Double> standardDeviationCurve = new TreeMap<>();

        System.out.printf("%-12s %-14s%n", "Std dev (mm)", "Measured avg");

        for (double standardDeviation : STANDARD_DEVIATIONS) {
            double average = measureAverage(standardDeviation);
            double roundedAverage = Math.ceil(average);

            // Retain the smaller standard deviation when rounded averages coincide.
            standardDeviationCurve.merge(roundedAverage, standardDeviation, Math::min);

            System.out.printf(Locale.ROOT, "%-12.1f %-14.2f%n", standardDeviation, average);
        }

        // Replace measured entries at and above the cutoff with a zero-deviation endpoint.
        standardDeviationCurve.tailMap((double) ZERO_DEVIATION_AVERAGE, true).clear();
        standardDeviationCurve.put((double) ZERO_DEVIATION_AVERAGE, 0.0);

        printStandardDeviationCurve(standardDeviationCurve);
    }

    /**
     * Measures the aggregate three-dart average at a fixed standard deviation.
     */
    private double measureAverage(double standardDeviation) {
        long totalDarts = 0;

        for (int leg = 0; leg < LEGS_PER_STANDARD_DEVIATION; leg++) {
            totalDarts += simulateLeg(standardDeviation);
        }

        return X01 * 3.0 * LEGS_PER_STANDARD_DEVIATION / totalDarts;
    }

    /**
     * Accepts legal checkouts immediately and converts illegal results into misses.
     * Aborts unfinished legs rather than excluding them from the measurement.
     */
    private int simulateLeg(double standardDeviation) {
        int remaining = X01;

        for (int dartsUsed = 1; dartsUsed <= MAX_DARTS_PER_LEG; dartsUsed++) {
            Dart target = selectTarget(remaining);

            double offsetX = deviationCalculator.createOffset(standardDeviation);
            double offsetY = deviationCalculator.createOffset(standardDeviation);
            Dart result = dartboardService.getScore(target, offsetX, offsetY);

            int remainingAfterThrow = remaining - result.getScore();

            if (checkoutService.isValidCheckout(remainingAfterThrow, result)) {
                return dartsUsed;
            }

            if (!checkoutService.isRemainingZeroOrBust(remainingAfterThrow)) {
                remaining = remainingAfterThrow;
            }
        }

        throw new AssertionError(
                "Leg exceeded " + MAX_DARTS_PER_LEG
                        + " darts: standardDeviation=" + standardDeviation
                        + ", remaining=" + remaining
        );
    }

    private Dart selectTarget(int remaining) {
        // Use a configured average of 100 to select the weighted scoring strategy.
        double targetOneDartAvg = 100 / 3.0;

        return checkoutService.getCheckout(remaining)
                .map(checkout -> checkout.suggested().get(0))
                .orElseGet(() -> scoringStrategy.createScoringTarget(targetOneDartAvg));
    }

    private void printStandardDeviationCurve(NavigableMap<Double, Double> standardDeviationCurve) {
        System.out.println();
        System.out.println(
                "private static final NavigableMap<Double, Double> STANDARD_DEVIATION_CURVE = new TreeMap<>(Map.ofEntries("
        );

        int entryIndex = 0;

        for (Map.Entry<Double, Double> entry : standardDeviationCurve.entrySet()) {
            String separator = ++entryIndex < standardDeviationCurve.size() ? "," : "";

            System.out.printf(
                    Locale.ROOT,
                    "        Map.entry(%.2f, %.1f)%s%n",
                    entry.getKey(),
                    entry.getValue(),
                    separator
            );
        }

        System.out.println("));");
    }
}