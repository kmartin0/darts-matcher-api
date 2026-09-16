package nl.kmartin.dartsmatcherapi.features.x01.x01leground.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;

import java.util.Map;

/**
 * Represents an X01 turn and the player who made it.
 *
 * @param playerId the ID of the player who made the turn
 * @param turn     the turn
 */
public record X01TurnEntry(
        @NotNull ObjectId playerId,
        @NotNull @Valid X01Turn turn
) {

    /**
     * Creates a turn entry from a map entry.
     *
     * @param mapEntry the turn map entry keyed by player ID
     */
    public X01TurnEntry(Map.Entry<ObjectId, X01Turn> mapEntry) {
        this(mapEntry.getKey(), mapEntry.getValue());
    }
}