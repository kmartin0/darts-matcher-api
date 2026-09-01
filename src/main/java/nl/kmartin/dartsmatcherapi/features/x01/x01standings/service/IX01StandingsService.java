package nl.kmartin.dartsmatcherapi.features.x01.x01standings.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01ClearByTwoRule;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface IX01StandingsService {
    void updateMatchStandings(X01Match match);

    List<ObjectId> determineWinners(TreeMap<Integer, List<ObjectId>> standings, int played, int bestOf, X01ClearByTwoRule clearByTwoRule);

    TreeMap<Integer, List<ObjectId>> groupByWinCounts(Map<ObjectId, Long> winsPerPlayer);
}
