package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service;

import jakarta.validation.constraints.Positive;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;

public interface IX01DartBotScoringStrategy {

    /**
     * Creates a scoring target based on the bot's target one-dart average.
     *
     * @param targetOneDartAvg the target one-dart average
     * @return the dartboard target used for scoring
     */
    Dart createScoringTarget(@Positive double targetOneDartAvg);
}