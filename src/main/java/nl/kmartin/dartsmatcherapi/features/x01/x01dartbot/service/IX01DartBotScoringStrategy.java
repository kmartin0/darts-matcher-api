package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service;

import jakarta.validation.constraints.Positive;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;

public interface IX01DartBotScoringStrategy {
    Dart createScoringTarget(@Positive double targetOneDartAvg);
}