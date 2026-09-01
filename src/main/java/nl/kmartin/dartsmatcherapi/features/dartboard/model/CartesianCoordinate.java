package nl.kmartin.dartsmatcherapi.features.dartboard.model;

/**
 * Represents a position using Cartesian coordinates relative to the center of the dartboard.
 *
 * @param x the horizontal distance from the center in millimeters
 * @param y the vertical distance from the center in millimeters
 */
public record CartesianCoordinate(double x, double y) {
}