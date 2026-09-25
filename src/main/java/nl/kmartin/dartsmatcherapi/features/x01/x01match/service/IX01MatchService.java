package nl.kmartin.dartsmatcherapi.features.x01.x01match.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.error.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateMatchRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateTurnRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01EditTurnRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import org.bson.types.ObjectId;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;

public interface IX01MatchService {

    /**
     * Creates a new X01 match from the supplied creation request.
     *
     * @param request the match creation request
     * @return the created match
     * @throws IllegalStateException when Dart Bot processing encounters invalid match state
     */
    X01Match createMatch(@NotNull @Valid X01CreateMatchRequest request);

    /**
     * Returns the existing rematch or creates one using the original match's configuration.
     *
     * New rematches preserve settings, player identities and order, with fresh match state.
     * Links the rematch to the original match and publishes the updated original.
     * Missing rematches are replaced. The original match does not need to be concluded.
     *
     * @param matchId the original match id
     * @return the existing or newly created rematch
     * @throws ResourceNotFoundException         when the original match does not exist
     * @throws OptimisticLockingFailureException when an affected match was modified concurrently
     * @throws IllegalStateException             when Dart Bot processing encounters invalid match state
     */
    X01Match createRematch(@NotNull ObjectId matchId);

    /**
     * Gets an X01 match by id.
     *
     * Clears any stale rematch reference, saving the correction and publishing an update.
     *
     * @param matchId the match id
     * @return the requested match
     * @throws ResourceNotFoundException         when the match does not exist
     * @throws OptimisticLockingFailureException when the match was modified concurrently during reference cleanup
     */
    X01Match getMatch(@NotNull ObjectId matchId);

    /**
     * Gets existing X01 matches for the supplied ids while preserving the requested order.
     *
     * Missing matches are omitted. Clears stale rematch references, saving and publishing affected matches.
     *
     * @param matchIds the match ids
     * @return the existing matches in requested order
     * @throws OptimisticLockingFailureException when an affected match was modified concurrently during reference cleanup
     */
    List<X01Match> getMatches(@NotNull List<@NotNull ObjectId> matchIds);

    /**
     * Verifies that an X01 match exists.
     *
     * @param matchId the match id
     * @throws ResourceNotFoundException when the match does not exist
     */
    void checkMatchExists(@NotNull ObjectId matchId);

    /**
     * Adds a turn for the current thrower and processes the resulting match state.
     *
     * @param matchId     the match id
     * @param turnRequest the turn creation request
     * @return the updated match
     * @throws InvalidArgumentsException         when the submitted turn cannot be applied to the current match state
     * @throws ResourceNotFoundException         when the match or active set, leg or round cannot be resolved
     * @throws OptimisticLockingFailureException when the match was modified concurrently
     * @throws IllegalStateException             when Dart Bot processing encounters invalid match state
     */
    X01Match addTurn(@NotNull ObjectId matchId, @NotNull @Valid X01CreateTurnRequest turnRequest);

    /**
     * Replaces an existing turn and reprocesses the resulting match state.
     *
     * @param matchId     the match id
     * @param turnRequest the turn edit request including its match position
     * @return the updated match
     * @throws InvalidArgumentsException         when the submitted turn cannot be applied to the resulting match state
     * @throws ResourceNotFoundException         when the match, target set, leg, round or player's existing turn cannot be found
     * @throws OptimisticLockingFailureException when the match was modified concurrently
     * @throws IllegalStateException             when Dart Bot processing encounters invalid match state
     */
    X01Match editTurn(@NotNull ObjectId matchId, @NotNull @Valid X01EditTurnRequest turnRequest);

    /**
     * Deletes the last human turn and any following Dart Bot turns, then reprocesses the match.
     *
     * Leaves recorded turns unchanged when no human turn exists.
     * Deletion stops early if no turn remains or the removed turn's player cannot be found.
     *
     * @param matchId the match id
     * @return the updated match
     * @throws ResourceNotFoundException         when the match does not exist
     * @throws OptimisticLockingFailureException when the match was modified concurrently
     * @throws IllegalStateException             when Dart Bot processing encounters invalid match state
     */
    X01Match deleteLastHumanTurn(@NotNull ObjectId matchId);

    /**
     * Deletes an X01 match and clears references to it from other matches.
     *
     * Saves and publishes affected matches and publishes the deletion.
     * Any rematch belonging to the deleted match is preserved.
     *
     * @param matchId the match id
     * @throws ResourceNotFoundException         when the match does not exist
     * @throws OptimisticLockingFailureException when an affected match was modified concurrently
     */
    void deleteMatch(@NotNull ObjectId matchId);

    /**
     * Resets an X01 match to its initial state and reprocesses it.
     *
     * Preserves match identity, configuration and any reference to an existing rematch.
     *
     * @param matchId the match id
     * @return the reset match
     * @throws ResourceNotFoundException         when the match does not exist
     * @throws OptimisticLockingFailureException when the match was modified concurrently
     * @throws IllegalStateException             when Dart Bot processing encounters invalid match state
     */
    X01Match resetMatch(@NotNull ObjectId matchId);

    /**
     * Reprocesses the derived state of an existing X01 match.
     *
     * @param matchId the match id
     * @return the reprocessed match
     * @throws ResourceNotFoundException         when the match does not exist
     * @throws OptimisticLockingFailureException when the match was modified concurrently
     * @throws IllegalStateException             when Dart Bot processing encounters invalid match state
     */
    X01Match reprocessMatch(@NotNull ObjectId matchId);
}