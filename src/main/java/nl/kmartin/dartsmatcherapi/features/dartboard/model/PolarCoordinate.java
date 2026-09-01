package nl.kmartin.dartsmatcherapi.features.dartboard.model;

/**
 * Represents a position using polar coordinates relative to the center of the dartboard.
 *
 * @param r the radial distance from the center in millimeters
 * @param theta the angle around the dartboard in radians
 */
public record PolarCoordinate(double r, double theta) {

    /**
     * Converts Cartesian coordinates to polar coordinates.
     *
     * @param cartesianCoordinate the Cartesian coordinates to convert
     * @return the converted polar coordinates
     */
    public static PolarCoordinate fromCartesian(CartesianCoordinate cartesianCoordinate) {
        double x = cartesianCoordinate.x();
        double y = cartesianCoordinate.y();

        // Convert from cartesian to polar coordinate.
        double r = Math.sqrt((x * x) + (y * y));
        double theta = Math.atan2(y, x);

        return new PolarCoordinate(r, theta);
    }

    /**
     * Returns theta normalized to a value between 0 and 2π.
     *
     * @return the normalized theta in radians
     */
    public double getThetaNormalized() {
        return normalizeTheta(theta);
    }

    /**
     * Normalizes an angle to a value between 0 and 2π.
     *
     * @param theta the angle in radians
     * @return the normalized angle in radians
     */
    public static double normalizeTheta(double theta) {
        return (theta + (Math.PI * 2)) % (Math.PI * 2);
    }

    /**
     * Converts an angle from degrees to radians.
     *
     * @param degree the angle in degrees
     * @return the angle in radians
     */
    public static double degreeToRadian(double degree) {
        return degree * (Math.PI / 180);
    }
}