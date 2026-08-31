package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service;

public interface IX01DartBotAccuracyCalculator {
    double createOffsetR(double targetOneDartAvg, double currentOneDartAvg);

    double createOffsetTheta(double targetOneDartAvg, double currentOneDartAvg);
}