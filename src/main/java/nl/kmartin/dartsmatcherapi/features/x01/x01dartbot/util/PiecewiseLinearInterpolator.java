package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.util;

import java.util.Map;
import java.util.NavigableMap;

/**
 * Performs piecewise linear interpolation between points on a two-dimensional graph.
 *
 * Values outside the defined x-range use the y-value of the nearest endpoint.
 */
public final class PiecewiseLinearInterpolator {
    private final NavigableMap<Double, Double> dataPoints;

    /**
     * Creates an interpolator using the given data points.
     *
     * @param dataPoints the data points where the key represents x and the value represents y
     * @throws IllegalArgumentException if the data points are null or empty
     */
    public PiecewiseLinearInterpolator(NavigableMap<Double, Double> dataPoints) {
        if (dataPoints == null || dataPoints.isEmpty()) {
            throw new IllegalArgumentException("Data points cannot be empty");
        }

        this.dataPoints = dataPoints;
    }

    /**
     * Calculates the y-value for the given x-value.
     *
     * @param x the x-value to interpolate
     * @return the interpolated y-value
     */
    public double interpolateY(double valueX) {
        // If there's only one data point, return the Y value of that point
        if (dataPoints.size() == 1) {
            return dataPoints.firstEntry().getValue();
        }

        // Find the nearest lower and upper data points
        Map.Entry<Double, Double> lower = dataPoints.floorEntry(valueX);
        Map.Entry<Double, Double> upper = dataPoints.ceilingEntry(valueX);

        // Handle edge cases where valueX is out of range
        if (lower == null) return dataPoints.firstEntry().getValue(); // Below range
        if (upper == null) return dataPoints.lastEntry().getValue();  // Above range

        // If valueX exactly matches a data point, return the corresponding Y value
        if (Double.compare(lower.getKey(), upper.getKey()) == 0) return lower.getValue(); // Exact match

        // Perform interpolation between the lower and upper points
        return interpolateBetween(lower, upper, valueX);
    }

    /**
     * Calculates the y-value between two data points using linear interpolation.
     *
     * @param lower the lower data point
     * @param upper the upper data point
     * @param x     the x-value to interpolate
     * @return the interpolated y-value
     */
    private double interpolateBetween(Map.Entry<Double, Double> lower, Map.Entry<Double, Double> upper, double x) {
        // Get the x1 and y1 values for the lower point
        double x1 = lower.getKey();
        double y1 = lower.getValue();

        // Get the x2 and y2 values for the upper point
        double x2 = upper.getKey();
        double y2 = upper.getValue();

        // Calculate the slope
        double slope = (y2 - y1) / (x2 - x1);

        // Calculate and return the interpolation using the calculated slope
        return y1 + slope * (x - x1);
    }
}