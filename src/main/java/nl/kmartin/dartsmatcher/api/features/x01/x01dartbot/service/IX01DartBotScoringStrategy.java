package nl.kmartin.dartsmatcher.api.features.x01.x01dartbot.service;

import jakarta.validation.constraints.Positive;
import nl.kmartin.dartsmatcher.api.features.dartboard.model.Dart;

public interface IX01DartBotScoringStrategy {

    /**
     * Creates a scoring target based on the bot's target one-dart average.
     *
     * @param targetOneDartAvg the target one-dart average
     * @return the dartboard target used for scoring
     */
    Dart createScoringTarget(@Positive double targetOneDartAvg);

    /**
     * Creates the target used to open a double-in leg.
     *
     * @return the opening double target
     */
    Dart createDoubleInTarget();
}