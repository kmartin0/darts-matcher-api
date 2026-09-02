package nl.kmartin.dartsmatcherapi.features.dartboard.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a dart throw containing both the intended target and actual result.
 *
 * @param target the dartboard target
 * @param result the position where the dart landed
 */
public record DartThrow(
        @NotNull @Valid Dart target,
        @NotNull @Valid Dart result
) {
}