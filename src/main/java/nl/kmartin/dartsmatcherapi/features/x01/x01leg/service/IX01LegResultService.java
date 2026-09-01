package nl.kmartin.dartsmatcherapi.features.x01.x01leg.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import org.bson.types.ObjectId;

public interface IX01LegResultService {
    void updateLegResult(X01Leg leg, int x01);

    int getRemainingForPlayer(X01Leg leg, ObjectId playerId, int x01);

    void updateRemainingForPlayer(X01Leg leg, ObjectId playerId, int x01);

    int calculateDartsUsed(X01Leg leg, ObjectId playerId);
}
