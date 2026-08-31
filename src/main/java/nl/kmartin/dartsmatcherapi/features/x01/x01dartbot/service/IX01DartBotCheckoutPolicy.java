package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service;

import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model.X01DartBotLegState;

public interface IX01DartBotCheckoutPolicy {
    boolean isTargetNumOfDartsReached(int dartsThrown, int targetNumOfDarts);

    boolean isDartResultValid(Dart result, X01DartBotLegState dartBotLegState);
}