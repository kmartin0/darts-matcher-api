package nl.kmartin.dartsmatcherapi.features.x01.x01match.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateMatchRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateTurnRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01EditTurnRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import org.bson.types.ObjectId;

import java.util.List;

public interface IX01MatchService {

    /**
     * Creates a new X01 match from the supplied creation request.
     *
     * @param request the match creation request
     * @return the created match
     */
    X01Match createMatch(@NotNull @Valid X01CreateMatchRequest request);

    /**
     * Gets an X01 match by id.
     *
     * @param matchId the match id
     * @return the requested match
     * @throws ResourceNotFoundException when the match does not exist
     */
    X01Match getMatch(@NotNull ObjectId matchId) throws ResourceNotFoundException;

    /**
     * Gets existing X01 matches for the supplied ids while preserving the requested order.
     *
     * Missing matches are omitted from the result.
     *
     * @param matchIds the match ids
     * @return the existing matches in requested order
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
     */
    X01Match addTurn(@NotNull ObjectId matchId, @NotNull @Valid X01CreateTurnRequest turnRequest);

    /**
     * Replaces an existing turn and reprocesses the resulting match state.
     *
     * @param matchId     the match id
     * @param turnRequest the turn edit request including its match position
     * @return the updated match
     */
    X01Match editTurn(@NotNull ObjectId matchId, @NotNull @Valid X01EditTurnRequest turnRequest);

    /**
     * Deletes the last recorded turn and reprocesses the match.
     *
     * @param matchId the match id
     * @return the updated match
     */
    X01Match deleteLastTurn(@NotNull ObjectId matchId);

    /**
     * Deletes an X01 match.
     *
     * @param matchId the match id
     */
    void deleteMatch(@NotNull ObjectId matchId);

    /**
     * Resets an X01 match to its initial state and reprocesses it.
     *
     * @param matchId the match id
     * @return the reset match
     */
    X01Match resetMatch(@NotNull ObjectId matchId);

    /**
     * Reprocesses the derived state of an existing X01 match.
     *
     * @param matchId the match id
     * @return the reprocessed match
     */
    X01Match reprocessMatch(@NotNull ObjectId matchId);
}