package nl.kmartin.dartsmatcherapi.features.x01.x01leg.service;

import nl.kmartin.dartsmatcherapi.error.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapi.error.response.TargetError;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01Turn;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01TurnMutation;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.service.IX01LegRoundService;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateTurnRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.i18n.MessageKeys;
import nl.kmartin.dartsmatcherapi.i18n.MessageResolver;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.IntStream;

/**
 * Coordinates creation, turn processing and rule handling for X01 legs.
 *
 * Applies and replaces turns, maintains checkout state and delegates progression and result calculations.
 */
@Service
@Validated
public class X01LegServiceImpl implements IX01LegService {

    private final MessageResolver messageResolver;
    private final IX01LegProgressService legProgressService;
    private final IX01LegResultService legResultService;
    private final IX01LegRoundService legRoundService;

    public X01LegServiceImpl(
            MessageResolver messageResolver,
            IX01LegProgressService legProgressService,
            IX01LegResultService legResultService,
            IX01LegRoundService legRoundService
    ) {
        this.messageResolver = messageResolver;
        this.legProgressService = legProgressService;
        this.legResultService = legResultService;
        this.legRoundService = legRoundService;
    }

    @Override
    public X01LegEntry createNewLeg(int legNumber, ObjectId throwsFirstInSet, List<X01MatchPlayer> players) {
        // Rotate the set starter through the player order to determine who starts this leg.
        ObjectId throwsFirstInLeg = calcThrowsFirstInLeg(legNumber, throwsFirstInSet, players);

        return new X01LegEntry(legNumber, new X01Leg(null, throwsFirstInLeg, new TreeMap<>()));
    }

    @Override
    public void applyTurn(X01TurnMutation turnMutation) {
        // Determine if a turn may be applied to this leg.
        checkLegEditable(turnMutation.leg(), turnMutation.throwerId());

        // Resolve the round and add the new turn.
        X01LegRoundEntry legRoundEntry = legProgressService.getLegRoundOrThrow(turnMutation.leg(), turnMutation.roundNumber());
        X01Turn turn = new X01Turn(turnMutation.score(), turnMutation.doublesMissed(), turnMutation.trackDoubles());
        legRoundService.addTurn(legRoundEntry.round(), turnMutation.throwerId(), turn);

        // Process the applied turn result and rebuild the affected player state.
        processTurnResult(turnMutation.leg(), turn, turnMutation.checkoutDartsUsed(), turnMutation.throwerId(), turnMutation.x01());

        // Rebuild the leg result after applying the turn.
        legResultService.updateLegResult(turnMutation.leg(), turnMutation.x01());
    }

    @Override
    public void replaceTurn(X01TurnMutation turnMutation) {
        // Determine if a turn may be replaced in this leg.
        checkLegEditable(turnMutation.leg(), turnMutation.throwerId());

        // Capture checkout state before replacing the existing turn.
        boolean wasCheckoutRound = isPlayerCheckoutRound(turnMutation.leg(), turnMutation.roundNumber(), turnMutation.throwerId());

        // Resolve the round and replace the existing turn.
        X01LegRoundEntry legRoundEntry = legProgressService.getLegRoundOrThrow(turnMutation.leg(), turnMutation.roundNumber());
        X01Turn turn = new X01Turn(turnMutation.score(), turnMutation.doublesMissed(), turnMutation.trackDoubles());
        legRoundService.replaceTurn(legRoundEntry.round(), turnMutation.throwerId(), turn);

        // Process the replacement turn result and rebuild the affected player state.
        processTurnResult(turnMutation.leg(), turn, turnMutation.checkoutDartsUsed(), turnMutation.throwerId(), turnMutation.x01());

        // Clear checkout dart usage when the previous checkout is no longer a checkout.
        if (wasCheckoutRound && turn.getRemaining() != 0) {
            turnMutation.leg().setCheckoutDartsUsed(null);
        }

        // Rebuild the winner and remove history that became stale after the replacement.
        legResultService.updateLegResult(turnMutation.leg(), turnMutation.x01());
    }

    @Override
    public boolean isPlayerCheckoutRound(X01Leg leg, int roundNumber, ObjectId playerId) {
        return playerId.equals(leg.getWinner()) && leg.getRounds().higherKey(roundNumber) == null;
    }

    /**
     * Determines which player throws first in a leg.
     *
     * The starting player rotates through the match-player order for each successive leg.
     *
     * @param legNumber        the leg number
     * @param throwsFirstInSet the player that throws first in the set
     * @param players          the match players
     * @return the player that throws first in the leg
     */
    private ObjectId calcThrowsFirstInLeg(int legNumber, ObjectId throwsFirstInSet, List<X01MatchPlayer> players) {
        // Find the set starter's position in the match-player order.
        int numOfPlayers = players.size();
        int startingIndexForSet = IntStream.range(0, numOfPlayers)
                .filter(i -> Objects.equals(players.get(i).getPlayerId(), throwsFirstInSet))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Set starter not found in player list."));

        // Rotate the starting position by one player for each successive leg.
        int throwsFirstIndex = (startingIndexForSet + (legNumber - 1)) % numOfPlayers;
        return players.get(throwsFirstIndex).getPlayerId();
    }

    /**
     * Verifies whether a player's turn may be modified in a leg.
     *
     * Once the leg is concluded, only the winning player's turn may be modified.
     *
     * @param leg      the leg to check
     * @param playerId the player whose turn is being modified
     * @throws InvalidArgumentsException when another player's turn is modified after the leg has been won
     */
    private void checkLegEditable(X01Leg leg, ObjectId playerId) {
        // A concluded leg can only be changed through the winning player's turn.
        if (legProgressService.isLegConcluded(leg) && !Objects.equals(leg.getWinner(), playerId)) {
            throw new InvalidArgumentsException(
                    new TargetError(
                            X01CreateTurnRequest.FIELD_SCORE,
                            messageResolver.getMessage(MessageKeys.MESSAGE_LEG_ALREADY_WON)
                    )
            );
        }
    }

    /**
     * Processes the result of an applied or replaced turn.
     *
     * Rebuilds the player's remaining values, repairs illegal turns,
     * and updates checkout dart usage when the turn completes the leg.
     *
     * @param leg               the leg containing the turn
     * @param turn              the turn to process
     * @param checkoutDartsUsed the checkout darts supplied with the turn
     * @param playerId          the player that threw the turn
     * @param x01               the starting score for the leg
     */
    private void processTurnResult(X01Leg leg, X01Turn turn, Integer checkoutDartsUsed, ObjectId playerId, int x01) {
        // Rebuild the player's remaining values after updating the turn.
        legResultService.updateRemainingForPlayer(leg, playerId, x01);

        // Repair illegal turns before updating checkout state.
        if (!legRoundService.isTurnLegal(turn, checkoutDartsUsed)) {
            handleIllegalTurn(leg, turn, playerId, x01);
            return;
        }

        // Update checkout dart usage when the turn completes the leg.
        if (turn.getRemaining() == 0) {
            leg.setCheckoutDartsUsed(checkoutDartsUsed);
        }
    }

    /**
     * Repairs an illegal turn by converting it to a zero-score turn.
     *
     * The player's remaining score history is recalculated after changing the recorded turn.
     *
     * @param leg      the leg containing the turn
     * @param turn     the illegal turn
     * @param playerId the player that threw the turn
     * @param x01      the starting score for the leg
     */
    private void handleIllegalTurn(X01Leg leg, X01Turn turn, ObjectId playerId, int x01) {
        // A bust or invalid checkout counts as a zero-score turn.
        turn.setScore(0);

        // Rebuild remaining values after changing the recorded turn.
        legResultService.updateRemainingForPlayer(leg, playerId, x01);
    }
}