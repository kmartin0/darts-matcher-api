package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot;

import nl.kmartin.dartsmatcherapi.features.dartboard.model.DartThrow;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model.X01DartBotLegState;

import java.util.List;

public interface IX01DartBotThrowSimulator {
    List<DartThrow> getNextDartThrows(X01DartBotLegState dartBotLegState);
}