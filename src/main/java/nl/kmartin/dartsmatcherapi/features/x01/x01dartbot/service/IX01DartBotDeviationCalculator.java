package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public interface IX01DartBotDeviationCalculator {

    /**
     * Creates a radial throw offset based on the bot's target and current one-dart averages.
     *
     * @param targetOneDartAvg  the target one-dart average
     * @param currentOneDartAvg the current one-dart average
     * @return the radial offset in millimeters
     */
    double createOffsetR(@Positive double targetOneDartAvg, @PositiveOrZero double currentOneDartAvg);

    /**
     * Creates an angular throw offset based on the bot's target and current one-dart averages.
     *
     * @param targetOneDartAvg  the target one-dart average
     * @param currentOneDartAvg the current one-dart average
     * @return the angular offset in radians
     */
    double createOffsetTheta(@Positive double targetOneDartAvg, @PositiveOrZero double currentOneDartAvg);
}