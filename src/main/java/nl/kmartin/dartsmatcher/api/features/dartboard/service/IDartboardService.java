package nl.kmartin.dartsmatcher.api.features.dartboard.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcher.api.features.dartboard.model.Dart;

public interface IDartboardService {

    /**
     * Calculates where a dart lands after applying Cartesian offsets to its aiming point.
     *
     * Zero offsets leave the dart at the requested target's aiming point.
     *
     * @param target  the dartboard target
     * @param offsetX the horizontal displacement in millimeters
     * @param offsetY the vertical displacement in millimeters
     * @return the resulting dart
     */
    Dart getScore(@NotNull @Valid Dart target, double offsetX, double offsetY);
}