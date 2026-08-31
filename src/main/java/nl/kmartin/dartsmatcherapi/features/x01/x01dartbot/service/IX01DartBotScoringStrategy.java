package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service;

import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;

public interface IX01DartBotScoringStrategy {
    Dart createScoringTarget(double targetOneDartAvg);
}