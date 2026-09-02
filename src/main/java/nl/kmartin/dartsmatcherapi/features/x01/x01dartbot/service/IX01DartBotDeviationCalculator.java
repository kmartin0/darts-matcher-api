package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public interface IX01DartBotDeviationCalculator {
    double createOffsetR(@Positive double targetOneDartAvg, @PositiveOrZero double currentOneDartAvg);

    double createOffsetTheta(@Positive double targetOneDartAvg, @PositiveOrZero double currentOneDartAvg);
}