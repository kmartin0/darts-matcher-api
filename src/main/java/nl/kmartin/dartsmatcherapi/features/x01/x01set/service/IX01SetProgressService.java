package nl.kmartin.dartsmatcherapi.features.x01.x01set.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01BestOf;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01Set;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;

import java.util.List;
import java.util.Optional;

public interface IX01SetProgressService {

    /**
     * Gets a leg in a set by its leg number.
     *
     * @param set       the set to search
     * @param legNumber the leg number
     * @return the matching leg entry
     * @throws ResourceNotFoundException when the leg does not exist
     */
    X01LegEntry getLegOrThrow(@NotNull @Valid X01Set set, int legNumber);

    /**
     * Gets the first leg in a set that has not yet been concluded.
     *
     * @param set the set to evaluate
     * @return the current leg, or empty when no leg is in progress
     */
    Optional<X01LegEntry> getCurrentLeg(@NotNull @Valid X01Set set);

    /**
     * Creates and adds the next available leg without exceeding the configured maximum.
     *
     * @param setEntry the set to update
     * @param players  the match players
     * @param bestOf   the best-of settings
     * @return the created leg, or empty when no additional leg may be created
     */
    Optional<X01LegEntry> createNextLeg(
            @NotNull @Valid X01SetEntry setEntry,
            @NotEmpty List<@NotNull @Valid X01MatchPlayer> players,
            @NotNull @Valid X01BestOf bestOf
    );

    /**
     * Determines whether a set has been concluded.
     *
     * @param set the set to evaluate
     * @return whether the set has a result
     */
    boolean isSetConcluded(@NotNull @Valid X01Set set);

    /**
     * Removes the most recently recorded score from a set.
     *
     * @param set the set to update
     * @return whether a score was removed
     */
    boolean removeLastScoreFromSet(@NotNull @Valid X01Set set);
}