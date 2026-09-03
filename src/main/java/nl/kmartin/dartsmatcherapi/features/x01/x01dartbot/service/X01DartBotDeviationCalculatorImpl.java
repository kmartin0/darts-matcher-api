package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service;

import nl.kmartin.dartsmatcherapi.features.dartboard.model.PolarCoordinate;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.util.PiecewiseLinearInterpolator;
import nl.kmartin.dartsmatcherapi.util.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Calculates randomized throw offsets for the X01 dart bot based on its target and current one-dart averages.
 *
 * A piecewise linear curve defines the baseline amount a bot may miss its target by. This deviation is adjusted
 * based on the bot's current performance so that overperforming increases the allowed miss and underperforming
 * decreases it.
 *
 * The calculated deviation is used as millimeters for the radial offset (r) and degrees for the angular offset
 * (theta). Theta is converted to radians before being used by the dartboard calculations.
 */
@Service
@Validated
public class X01DartBotDeviationCalculatorImpl implements IX01DartBotDeviationCalculator {

    // Controls how strongly the current average adjusts the baseline deviation.
    private static final double CALIBRATION_FACTOR = 5.0;

    // Maps target one-dart averages to their corresponding maximum throw deviations.
    private static final NavigableMap<Double, Double> DEVIATION_CURVE = new TreeMap<>(Map.of(
            0.0, 80.0,
            10.0, 45.0,
            15.0, 30.0,
            20.0, 13.0,
            30.0, 10.0,
            33.0, 7.0,
            40.0, 6.5,
            50.0, 6.0,
            53.0, 0.0
    ));

    // Interpolates deviations for target averages between the configured curve points.
    private final PiecewiseLinearInterpolator deviationInterpolator = new PiecewiseLinearInterpolator(DEVIATION_CURVE);

    @Override
    public double createOffsetR(double targetOneDartAvg, double currentOneDartAvg) {
        // Calculate the maximum radial deviation allowed by the bot's current performance.
        double maxDeviationMm = calculateDeviation(targetOneDartAvg, currentOneDartAvg);

        // Pick a random radial offset within the allowed deviation.
        return NumberUtils.randomBetween(maxDeviationMm);
    }

    @Override
    public double createOffsetTheta(double targetOneDartAvg, double currentOneDartAvg) {
        // Calculate the maximum angular deviation allowed by the bot's current performance.
        double maxDeviationDegrees = calculateDeviation(targetOneDartAvg, currentOneDartAvg);

        // Pick a random angular offset and convert it to the radians used by the dartboard.
        double offsetDegrees = NumberUtils.randomBetween(maxDeviationDegrees);
        return PolarCoordinate.degreeToRadian(offsetDegrees);
    }

    /**
     * Calculates the allowed throw deviation for the bot.
     *
     * The baseline deviation is determined by the target one-dart average. Once darts have been thrown,
     * the deviation is calibrated based on how the current average compares to the target average.
     *
     * @param targetOneDartAvg  the target one-dart average
     * @param currentOneDartAvg the current one-dart average
     * @return the calibrated throw deviation
     */
    private double calculateDeviation(double targetOneDartAvg, double currentOneDartAvg) {
        // Get the baseline deviation for the configured playing strength.
        double baseDeviation = getBaseDeviation(targetOneDartAvg);

        // No current average exists before the first dart, so use the baseline without calibration.
        if (currentOneDartAvg == 0) return baseDeviation;

        // Adjust the deviation according to how the bot is currently performing.
        return calibrateDeviation(targetOneDartAvg, currentOneDartAvg, baseDeviation);
    }

    /**
     * Gets the baseline deviation for a target one-dart average using piecewise linear interpolation.
     *
     * @param targetOneDartAvg the target one-dart average
     * @return the baseline throw deviation
     */
    private double getBaseDeviation(double targetOneDartAvg) {
        return deviationInterpolator.interpolateY(targetOneDartAvg);
    }

    /**
     * Calibrates a throw deviation based on the bot's current performance.
     *
     * Overperforming increases the allowed deviation while underperforming decreases it.
     * The resulting deviation is constrained to the neighboring values in the deviation curve.
     *
     * @param targetOneDartAvg     the target one-dart average
     * @param currentOneDartAvg    the current one-dart average
     * @param deviationToCalibrate the baseline deviation to calibrate
     * @return the calibrated deviation within the allowed range
     */
    private double calibrateDeviation(double targetOneDartAvg, double currentOneDartAvg, double deviationToCalibrate) {
        // Calculate how far the current average is above or below the target average.
        double relativePerformanceDifference = (currentOneDartAvg - targetOneDartAvg) / targetOneDartAvg;

        // Increase deviation when overperforming and decrease it when underperforming.
        double adjustedDeviation = deviationToCalibrate * (1.0 + CALIBRATION_FACTOR * relativePerformanceDifference);

        // Constrain the adjustment to the neighboring values in the configured curve.
        double maxDeviation = getMaximumDeviation(targetOneDartAvg);
        double minDeviation = getMinimumDeviation(targetOneDartAvg);

        return Math.max(minDeviation, Math.min(maxDeviation, adjustedDeviation));
    }

    /**
     * Gets the maximum allowed deviation for a target one-dart average.
     *
     * @param targetOneDartAvg the target one-dart average
     * @return the maximum allowed deviation
     */
    private double getMaximumDeviation(double targetOneDartAvg) {
        // Find the closest curve point at or below the target average.
        Map.Entry<Double, Double> floor = DEVIATION_CURVE.floorEntry(targetOneDartAvg);
        if (floor == null) return DEVIATION_CURVE.firstEntry().getValue();

        // Move one point lower, where the allowed deviation is larger.
        Map.Entry<Double, Double> lower = DEVIATION_CURVE.lowerEntry(floor.getKey());
        if (lower == null) return DEVIATION_CURVE.firstEntry().getValue();

        return lower.getValue();
    }

    /**
     * Gets the minimum allowed deviation for a target one-dart average.
     *
     * @param targetOneDartAvg the target one-dart average
     * @return the minimum allowed deviation
     */
    private double getMinimumDeviation(double targetOneDartAvg) {
        // Find the closest curve point at or above the target average.
        Map.Entry<Double, Double> ceiling = DEVIATION_CURVE.ceilingEntry(targetOneDartAvg);
        if (ceiling == null) return DEVIATION_CURVE.lastEntry().getValue();

        // Move one point higher, where the allowed deviation is smaller.
        Map.Entry<Double, Double> upper = DEVIATION_CURVE.higherEntry(ceiling.getKey());
        if (upper == null) return DEVIATION_CURVE.lastEntry().getValue();

        return upper.getValue();
    }
}