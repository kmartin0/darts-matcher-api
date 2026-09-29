package nl.kmartin.dartsmatcherapi.features.x01.x01match.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import org.bson.types.ObjectId;

import java.util.List;

public interface IX01MatchRematchService {

    /**
     * Returns the existing rematch or initializes a new one when none exists.
     *
     * Clears any stale rematch reference on the supplied match.
     * Does not persist either match or link a newly initialized rematch.
     *
     * @param match the match to get or create a rematch for
     * @return the existing or newly initialized rematch
     */
    X01Match getOrCreateRematch(@NotNull @Valid X01Match match);

    /**
     * Validates the match's rematch id and clears it when the referenced rematch no longer exists.
     *
     * @param match the match to validate and update
     */
    void validateAndUpdateRematchId(@NotNull @Valid X01Match match);

    /**
     * Clears rematch references to the specified match.
     *
     * Mutates and returns the affected matches without persisting them.
     *
     * @param matchId the referenced match id
     * @return the matches whose rematch reference was cleared
     */
    List<X01Match> clearRematchReferencesToMatch(@NotNull ObjectId matchId);
}