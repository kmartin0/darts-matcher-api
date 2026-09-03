package nl.kmartin.dartsmatcherapi.features.x01.x01set.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01Set;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.TreeMap;

/**
 * Provides operations for creating X01 sets and determining their starting player.
 */
@Service
@Validated
public class X01SetServiceImpl implements IX01SetService {

    @Override
    public X01SetEntry createNewSet(int setNumber, List<X01MatchPlayer> players) {
        // Determine which player starts the set from the set number and match-player order.
        ObjectId throwsFirstInSet = calcThrowsFirstInSet(setNumber, players);

        X01Set set = new X01Set(new TreeMap<>(), throwsFirstInSet, null);
        return new X01SetEntry(setNumber, set);
    }

    /**
     * Determines which player throws first in a set.
     *
     * The starting player rotates through the match-player order for each successive set.
     *
     * @param setNumber the set number
     * @param players   the match players
     * @return the player that throws first in the set
     */
    private ObjectId calcThrowsFirstInSet(int setNumber, List<X01MatchPlayer> players) {
        // Rotate the starting position by one player for each successive set.
        int throwsFirstIndex = (setNumber - 1) % players.size();

        return players.get(throwsFirstIndex).getPlayerId();
    }
}