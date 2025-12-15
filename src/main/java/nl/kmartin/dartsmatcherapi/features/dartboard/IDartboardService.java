package nl.kmartin.dartsmatcherapi.features.dartboard;

import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;

public interface IDartboardService {
    Dart getScore(Dart target, double offsetR, double offsetTheta);
}
