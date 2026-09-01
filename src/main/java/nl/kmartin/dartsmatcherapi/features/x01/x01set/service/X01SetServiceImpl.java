package nl.kmartin.dartsmatcherapi.features.x01.x01set.service;

import nl.kmartin.dartsmatcherapi.features.x01.common.X01MatchUtils;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01Set;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.TreeMap;

/**
 * Provides operations for creating X01 sets and determining their starting player.
 */
@Service
public class X01SetServiceImpl implements IX01SetService {

    /**
     * Creates a new numbered set with the correct starting player.
     *
     * @param setNumber the set number
     * @param players   the match players
     * @return the created set entry
     */
    @Override
    public X01SetEntry createNewSet(int setNumber, List<X01MatchPlayer> players) {
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
     * @throws IllegalArgumentException when the set number or player list is invalid
     */
    private ObjectId calcThrowsFirstInSet(int setNumber, List<X01MatchPlayer> players) {
        if (setNumber < 1) {
            throw new IllegalArgumentException("Set number must be greater than zero.");
        }

        if (X01MatchUtils.isPlayersEmpty(players)) {
            throw new IllegalArgumentException("Cannot calculate first thrower from a null or empty player list.");
        }

        // Rotate the starting position by one player for each successive set.
        int throwsFirstIndex = (setNumber - 1) % players.size();
        return players.get(throwsFirstIndex).getPlayerId();
    }
}