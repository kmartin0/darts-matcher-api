package nl.kmartin.dartsmatcherapi.features.dartboard.model;

/**
 * Represents a dart throw containing both the intended target and actual result.
 *
 * @param target the dartboard target
 * @param result the position where the dart landed
 */
public record DartThrow(
        Dart target,
        Dart result
) {
}