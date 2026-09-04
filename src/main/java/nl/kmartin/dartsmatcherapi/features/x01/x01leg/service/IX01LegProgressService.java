package nl.kmartin.dartsmatcherapi.features.x01.x01leg.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;

import java.util.List;
import java.util.Optional;

public interface IX01LegProgressService {

    /**
     * Gets a round by its round number.
     *
     * @param leg         the leg to search
     * @param roundNumber the round number
     * @return the matching round entry
     * @throws ResourceNotFoundException when the round does not exist
     */
    X01LegRoundEntry getLegRoundOrThrow(@NotNull @Valid X01Leg leg, int roundNumber);

    /**
     * Gets the first round in which at least one player has not yet thrown.
     *
     * @param leg     the leg to evaluate
     * @param players the match players
     * @return the current round, or empty when no round is in progress
     */
    Optional<X01LegRoundEntry> getCurrentLegRound(
            @NotNull @Valid X01Leg leg,
            @NotEmpty List<@NotNull @Valid X01MatchPlayer> players
    );

    /**
     * Creates and adds the next available round to a leg.
     *
     * @param leg the leg to update
     * @return the created round, or empty when no round number can be assigned
     */
    Optional<X01LegRoundEntry> createNextLegRound(@NotNull @Valid X01Leg leg);

    /**
     * Determines whether a leg has concluded.
     *
     * @param leg the leg to evaluate
     * @return whether the leg has a winner
     */
    boolean isLegConcluded(@NotNull @Valid X01Leg leg);

    /**
     * Removes the most recently recorded turn from a leg.
     *
     * @param leg the leg to update
     * @return whether a turn was removed
     */
    boolean removeLastTurnFromLeg(@NotNull @Valid X01Leg leg);
}