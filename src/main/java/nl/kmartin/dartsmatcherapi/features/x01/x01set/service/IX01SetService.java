package nl.kmartin.dartsmatcherapi.features.x01.x01set.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;
import org.bson.types.ObjectId;

import java.util.List;

public interface IX01SetService {
    X01SetEntry createNewSet(int setNumber, List<X01MatchPlayer> players);

    ObjectId calcThrowsFirstInSet(int setNumber, List<X01MatchPlayer> players);
}
