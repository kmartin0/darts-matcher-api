package nl.kmartin.dartsmatcherapi.features.x01.x01leground.model;

import java.util.Map;

/**
 * Represents a numbered X01 leg round.
 *
 * @param roundNumber the round number
 * @param round the round
 */
public record X01LegRoundEntry(int roundNumber, X01LegRound round) {

    /**
     * Creates a round entry from a map entry.
     *
     * @param mapEntry the numbered round map entry
     */
    public X01LegRoundEntry(Map.Entry<Integer, X01LegRound> mapEntry) {
        this(mapEntry.getKey(), mapEntry.getValue());
    }
}