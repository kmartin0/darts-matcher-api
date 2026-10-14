package nl.kmartin.dartsmatcher.api.features.x01.x01dartbot.service;

public interface IX01DartBotDeviationCalculator {

    /**
     * Generates a zero-centered Gaussian offset. Call separately for each axis.
     *
     * @param standardDeviation the standard deviation in millimeters, not a maximum offset
     * @return the displacement in millimeters, or zero when standard deviation is zero
     * @throws IllegalArgumentException if standard deviation is negative or non-finite
     */
    double createOffset(double standardDeviation);

    /**
     * Interpolates the baseline standard deviation from the calibration curve,
     * using the nearest endpoint outside its range.
     *
     * @param targetOneDartAvg the target one-dart average
     * @return the standard deviation per axis in millimeters
     */
    double getStandardDeviation(double targetOneDartAvg);

    /**
     * Decreases standard deviation when behind target and increases it, less strongly, when ahead.
     * Clamps the result to the curve's minimum and maximum; a zero baseline stays zero.
     *
     * @param targetOneDartAvg  the positive target one-dart average
     * @param currentOneDartAvg the current one-dart average, including zero
     * @return the calibrated standard deviation per axis in millimeters
     */
    double getCalibratedStandardDeviation(double targetOneDartAvg, double currentOneDartAvg);
}