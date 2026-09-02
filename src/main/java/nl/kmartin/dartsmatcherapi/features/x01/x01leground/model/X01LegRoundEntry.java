package nl.kmartin.dartsmatcherapi.features.x01.x01leground.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Map;

/**
 * Represents a numbered X01 leg round.
 *
 * @param roundNumber the round number
 * @param round       the round
 */
public record X01LegRoundEntry(
        @Positive int roundNumber,
        @NotNull @Valid X01LegRound round
) {

    /**
     * Creates a round entry from a map entry.
     *
     * @param mapEntry the numbered round map entry
     */
    public X01LegRoundEntry(Map.Entry<Integer, X01LegRound> mapEntry) {
        this(mapEntry.getKey(), mapEntry.getValue());
    }
}