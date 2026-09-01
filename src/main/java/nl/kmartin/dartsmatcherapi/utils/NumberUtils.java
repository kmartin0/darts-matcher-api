package nl.kmartin.dartsmatcherapi.utils;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Provides utility methods for common numeric calculations and operations.
 */
public final class NumberUtils {
    private NumberUtils() {
    }

    /**
     * Finds the next sequential number from a set of numbers.
     *
     * @param numbers the numbers to evaluate
     * @return the smallest missing positive number, or -1 when the input is null
     */
    public static int findNextNumber(Set<Integer> numbers) {
        return findNextNumber(numbers, Integer.MAX_VALUE);
    }

    /**
     * Finds the next sequential number from a set within the given upper bound.
     *
     * @param numbers the numbers to evaluate
     * @param max     the maximum allowed number
     * @return the smallest missing positive number, or -1 when none is available
     */
    public static int findNextNumber(Set<Integer> numbers, int max) {
        if (numbers == null || max < 1) {
            return -1;
        }

        return IntStream.rangeClosed(1, max)
                .filter(number -> !numbers.contains(number))
                .findFirst()
                .orElse(-1);
    }

    /**
     * Calculates a percentage from the given numerator and denominator.
     *
     * @param numerator   the numerator
     * @param denominator the denominator
     * @return the rounded percentage, or 0 when the denominator is not positive
     */
    public static int calcPercentage(int numerator, int denominator) {
        if (denominator <= 0) {
            return 0;
        }

        return (int) Math.round(((double) numerator / denominator) * 100);
    }

    /**
     * Returns a random double between the given values.
     *
     * Reversed values are normalized and equal values return that value.
     *
     * @param n1 the first value
     * @param n2 the second value
     * @return a random value between the normalized bounds
     */
    public static double randomBetween(double n1, double n2) {
        if (!Double.isFinite(n1) || !Double.isFinite(n2)) {
            throw new IllegalArgumentException("Bounds must be finite");
        }

        if (n1 == n2) return n1;

        double lowerBound = Math.min(n1, n2);
        double upperBound = Math.max(n1, n2);

        return ThreadLocalRandom.current().nextDouble(lowerBound, upperBound);
    }

    /**
     * Returns a random double between the negative and positive maximum absolute value.
     *
     * @param maxAbsoluteValue the maximum absolute value
     * @return a random value between -maxAbsoluteValue and maxAbsoluteValue
     */
    public static double randomBetween(double maxAbsoluteValue) {
        return randomBetween(-maxAbsoluteValue, maxAbsoluteValue);
    }
}