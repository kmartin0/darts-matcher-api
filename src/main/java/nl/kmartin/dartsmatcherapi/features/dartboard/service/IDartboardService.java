package nl.kmartin.dartsmatcherapi.features.dartboard.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;

public interface IDartboardService {

    /**
     * Calculates where a dart lands when throwing at a target with a given deviation.
     *
     * The target starts at the center of its scoring area. Setting both offsets to 0 results
     * in a throw at the center of the requested target.
     *
     * @param target      the dartboard target
     * @param offsetR     radial deviation in millimeters
     * @param offsetTheta angular deviation in radians
     * @return the resulting dart
     */
    Dart getScore(@NotNull @Valid Dart target, double offsetR, double offsetTheta);
}