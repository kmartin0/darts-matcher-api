package nl.kmartin.dartsmatcherapi.features.x01.x01leg.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Turn;
import org.bson.types.ObjectId;

import java.util.List;

public interface IX01LegService {
    X01LegEntry createNewLeg(int legNumber, ObjectId throwsFirstInSet, List<X01MatchPlayer> players);

    void applyTurn(int x01, X01Leg leg, int roundNumber, X01Turn turn, ObjectId throwerId, boolean trackDoubles);

    boolean isPlayerCheckoutRound(X01Leg leg, int roundNumber, ObjectId playerId);
}
