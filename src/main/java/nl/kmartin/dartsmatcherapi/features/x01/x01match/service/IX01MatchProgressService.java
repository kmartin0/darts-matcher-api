package nl.kmartin.dartsmatcherapi.features.x01.x01match.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;

import java.util.Optional;

public interface IX01MatchProgressService {

    /**
     * Gets a set in a match by its set number.
     *
     * @param match     the match containing the sets
     * @param setNumber the set number
     * @return the matching set entry
     * @throws ResourceNotFoundException when the set does not exist
     */
    X01SetEntry getSetOrThrow(@NotNull @Valid X01Match match, int setNumber);

    /**
     * Gets the first set that has not yet been concluded.
     *
     * @param match the match containing the sets
     * @return the current set, or empty when no unfinished set exists
     */
    Optional<X01SetEntry> getCurrentSet(@NotNull @Valid X01Match match);

    /**
     * Gets the current set or creates the next set when the match can continue.
     *
     * @param match the match whose current set should be resolved
     * @return the current or newly created set, or empty when the match is concluded
     */
    Optional<X01SetEntry> getCurrentSetOrCreate(@NotNull @Valid X01Match match);

    /**
     * Gets the current unfinished leg in the match.
     *
     * @param match the match to inspect
     * @return the current leg, or empty when no unfinished leg exists
     */
    Optional<X01LegEntry> getCurrentLeg(@NotNull @Valid X01Match match);

    /**
     * Gets the current leg or creates the next leg when the set can continue.
     *
     * @param match           the match containing the set
     * @param currentSetEntry the current set
     * @return the current or newly created leg, or empty when the set is concluded
     */
    Optional<X01LegEntry> getCurrentLegOrCreate(
            @NotNull @Valid X01Match match,
            @NotNull @Valid X01SetEntry currentSetEntry
    );

    /**
     * Gets the current round or creates the next round when the leg can continue.
     *
     * @param match      the match containing the leg
     * @param currentLeg the current leg
     * @return the current or newly created round, or empty when the leg is concluded
     */
    Optional<X01LegRoundEntry> getCurrentLegRoundOrCreate(
            @NotNull @Valid X01Match match,
            @NotNull @Valid X01Leg currentLeg
    );

    /**
     * Removes the most recently recorded turn and any trailing empty match structure.
     *
     * @param match the match whose last turn should be removed
     */
    void removeLastTurnFromMatch(@NotNull @Valid X01Match match);

    /**
     * Rebuilds the current match progress and creates missing structure when the match can continue.
     *
     * @param match the match whose progress should be rebuilt
     */
    void updateMatchProgress(@NotNull @Valid X01Match match);
}