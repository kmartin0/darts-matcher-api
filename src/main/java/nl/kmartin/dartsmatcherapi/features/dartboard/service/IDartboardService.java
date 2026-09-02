package nl.kmartin.dartsmatcherapi.features.dartboard.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;

public interface IDartboardService {
    Dart getScore(@NotNull @Valid Dart target, double offsetR, double offsetTheta);
}
