package nl.kmartin.dartsmatcherapi.features.x01.x01match.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01CheckoutInsufficientDartsException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegAlreadyWonException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01TurnAlreadyExistsException;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateTurnRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01EditTurnRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;

public interface IX01MatchTurnService {

    /**
     * Adds a turn for the current thrower in the active round.
     *
     * Resolves or creates the active set, leg and round, then applies the turn and rebuilds the affected leg state.
     *
     * @param match       the match to update
     * @param turnRequest the turn creation request
     * @throws X01TurnAlreadyExistsException         when the current thrower already has a turn in the active round
     * @throws X01LegAlreadyWonException             when another player's turn is applied after the leg has been won
     * @throws X01CheckoutInsufficientDartsException when the checkout requires more darts than were used
     * @throws ResourceNotFoundException             when the active set, leg or round cannot be resolved
     */
    void addTurnToCurrentThrower(@NotNull @Valid X01Match match, @NotNull @Valid X01CreateTurnRequest turnRequest);

    /**
     * Generates and applies a turn when the current thrower is a Dart Bot.
     *
     * @param match the match to update
     * @return true when a bot turn was applied, or false when no current thrower resolves to a Dart Bot
     * @throws IllegalStateException                 when the current Dart Bot's active leg cannot be resolved
     * @throws X01TurnAlreadyExistsException         when the thrower already has a turn in the active round
     * @throws X01LegAlreadyWonException             when another player's turn is applied after the leg has been won
     * @throws X01CheckoutInsufficientDartsException when the checkout requires more darts than were used
     * @throws ResourceNotFoundException             when the active set, leg or round cannot be resolved
     */
    boolean addDartBotTurnToCurrentThrower(@NotNull @Valid X01Match match);

    /**
     * Replaces an existing turn at the requested match position.
     *
     * @param match       the match to update
     * @param turnRequest the turn edit request
     * @throws X01CheckoutInsufficientDartsException when the checkout requires more darts than were used
     * @throws X01LegAlreadyWonException             when another player's turn is replaced after the leg has been won
     * @throws ResourceNotFoundException             when the match position or player's existing turn cannot be found
     */
    void replaceTurn(@NotNull @Valid X01Match match, @NotNull @Valid X01EditTurnRequest turnRequest);

    /**
     * Deletes the last human turn and any following Dart Bot turns.
     *
     * Leaves recorded turns unchanged when no human turn exists.
     * Deletion stops early if no turn remains or the removed turn's player cannot be found.
     *
     * @param match the match to update
     * @return true when at least one turn was removed
     */
    boolean deleteLastHumanTurn(@NotNull @Valid X01Match match);
}