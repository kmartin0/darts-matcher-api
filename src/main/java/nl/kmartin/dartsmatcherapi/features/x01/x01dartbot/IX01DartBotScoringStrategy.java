package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot;

import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;

public interface IX01DartBotScoringStrategy {
    Dart createScoringTarget(double targetOneDartAvg);
}