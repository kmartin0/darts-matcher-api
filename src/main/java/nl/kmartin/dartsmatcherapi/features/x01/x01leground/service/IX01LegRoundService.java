package nl.kmartin.dartsmatcherapi.features.x01.x01leground.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRound;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01Turn;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01TurnAlreadyExistsException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01TurnEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface IX01LegRoundService {

    /**
     * Determines which player should throw next in the round.
     *
     * @param legRound         the round to evaluate
     * @param throwsFirstInLeg the player that started the leg
     * @param players          the match players
     * @return the next player to throw, or null when no player remains
     */
    ObjectId getCurrentThrowerInRound(
            @NotNull @Valid X01LegRound legRound,
            @NotNull ObjectId throwsFirstInLeg,
            @NotEmpty List<@NotNull @Valid X01MatchPlayer> players
    );

    /**
     * Adds a player's turn to the round.
     *
     * @param legRound the round to update
     * @param playerId the player whose turn is being added
     * @param turn     the turn to add
     * @throws X01TurnAlreadyExistsException when the player already has a turn in the round
     */
    void addTurn(
            @NotNull @Valid X01LegRound legRound,
            @NotNull ObjectId playerId,
            @NotNull @Valid X01Turn turn
    );

    /**
     * Replaces a player's existing turn in the round.
     *
     * @param legRound the round to update
     * @param playerId the player whose turn is being replaced
     * @param turn     the replacement turn
     * @throws ResourceNotFoundException when the player has no turn in the round
     */
    void replaceTurn(
            @NotNull @Valid X01LegRound legRound,
            @NotNull ObjectId playerId,
            @NotNull @Valid X01Turn turn
    );

    /**
     * Removes the most recently recorded turn from a round.
     *
     * @param legRound the round from which to remove the turn
     * @return the removed turn entry, or empty when no turn exists
     */
    Optional<X01TurnEntry> removeLastTurnFromRound(@NotNull @Valid X01LegRound legRound);

    /**
     * Removes turns recorded after the player that won the leg.
     *
     * @param round     the round to trim
     * @param legWinner the player that won the leg
     */
    void removeTurnsAfterWinner(@NotNull @Valid X01LegRound round, @NotNull ObjectId legWinner);
}