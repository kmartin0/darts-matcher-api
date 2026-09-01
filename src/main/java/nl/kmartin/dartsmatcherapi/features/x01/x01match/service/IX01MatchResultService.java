package nl.kmartin.dartsmatcherapi.features.x01.x01match.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.TreeMap;

public interface IX01MatchResultService {
    void updateMatchResult(X01Match match);
}
