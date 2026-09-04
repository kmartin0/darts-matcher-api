package nl.kmartin.dartsmatcherapi.features.x01.x01leg.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01CheckoutInsufficientDartsException;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.service.IX01CheckoutService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegAlreadyWonException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRound;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01Turn;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01TurnMutation;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.service.IX01LegRoundService;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.NavigableMap;
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

    private final IX01LegProgressService legProgressService;
    private final IX01LegResultService legResultService;
    private final IX01LegRoundService legRoundService;
    private final IX01CheckoutService checkoutService;

    public X01LegServiceImpl(
            IX01LegProgressService legProgressService,
            IX01LegResultService legResultService,
            IX01LegRoundService legRoundService,
            IX01CheckoutService checkoutService
    ) {
        this.legProgressService = legProgressService;
        this.legResultService = legResultService;
        this.legRoundService = legRoundService;
        this.checkoutService = checkoutService;
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

        // Resolve the round and add the processed turn.
        X01LegRoundEntry legRoundEntry = legProgressService.getLegRoundOrThrow(turnMutation.leg(), turnMutation.roundNumber());
        X01Turn turn = createTurn(turnMutation, false);
        legRoundService.addTurn(legRoundEntry.round(), turnMutation.throwerId(), turn);

        // Rebuild the leg result after applying the turn.
        legResultService.updateLegResult(turnMutation.leg(), turnMutation.x01());

        // Record checkout dart usage when the applied turn completed the leg.
        if (Objects.equals(turnMutation.leg().getWinner(), turnMutation.throwerId())) {
            turnMutation.leg().setCheckoutDartsUsed(turnMutation.checkoutDartsUsed());
        }
    }

    @Override
    public void replaceTurn(X01TurnMutation turnMutation) {
        // Determine if a turn may be replaced in this leg.
        checkLegEditable(turnMutation.leg(), turnMutation.throwerId());

        // Resolve the round and replace the existing turn with the processed replacement.
        X01LegRoundEntry legRoundEntry = legProgressService.getLegRoundOrThrow(turnMutation.leg(), turnMutation.roundNumber());
        X01Turn turn = createTurn(turnMutation, true);
        legRoundService.replaceTurn(legRoundEntry.round(), turnMutation.throwerId(), turn);

        // Rebuild remaining values, winner and stale match history after the replacement.
        legResultService.updateLegResult(turnMutation.leg(), turnMutation.x01());

        // Record checkout dart usage supplied with the replacement when the resulting leg is won.
        if (Objects.equals(turnMutation.leg().getWinner(), turnMutation.throwerId())) {
            turnMutation.leg().setCheckoutDartsUsed(turnMutation.checkoutDartsUsed());
        } else {
            turnMutation.leg().setCheckoutDartsUsed(null);
        }
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
     * @throws X01LegAlreadyWonException when another player's turn is modified after the leg has been won
     */
    private void checkLegEditable(X01Leg leg, ObjectId playerId) {
        // A concluded leg can only be changed through the winning player's turn.
        if (legProgressService.isLegConcluded(leg) && !Objects.equals(leg.getWinner(), playerId)) {
            throw new X01LegAlreadyWonException();
        }
    }

    /**
     * Creates a processed turn from a turn mutation.
     *
     * Calculates the remaining score and converts an illegal result to a
     * zero-score turn before constructing the persisted turn. When required,
     * following turns are also checked to ensure the submitted score does not
     * invalidate already-recorded turn history.
     *
     * @param turnMutation        the turn mutation to process
     * @param checkFollowingTurns whether following turns must also remain legal
     * @return the processed turn
     * @throws X01CheckoutInsufficientDartsException when the checkout requires more darts than were used
     */
    private X01Turn createTurn(X01TurnMutation turnMutation, boolean checkFollowingTurns) {
        // Calculate the remaining score produced by the submitted turn.
        int remainingBeforeTurn = legResultService.getRemainingForPlayerBeforeRound(
                turnMutation.leg(),
                turnMutation.throwerId(),
                turnMutation.roundNumber(),
                turnMutation.x01()
        );

        int score = turnMutation.score();
        int remaining = remainingBeforeTurn - score;

        // Validate the submitted turn and, for replacements, the turns that follow it.
        boolean turnLegal = isTurnLegal(score, remaining, turnMutation.checkoutDartsUsed());
        boolean followingTurnsLegal = !checkFollowingTurns || areFollowingTurnsLegal(turnMutation, remaining);
        boolean isLegal = turnLegal && followingTurnsLegal;

        // An illegal turn counts as a zero-score turn and leaves the remaining score unchanged.
        if (!isLegal) {
            score = 0;
            remaining = remainingBeforeTurn;
        }

        return new X01Turn(score, remaining, turnMutation.doublesMissed(), turnMutation.trackDoubles());
    }

    /**
     * Determines whether all turns following an edited turn remain legal.
     *
     * @param turnMutation the edit being evaluated
     * @param remaining    the remaining score after the edited turn
     * @return whether all following turns remain legal
     * @throws X01CheckoutInsufficientDartsException when the checkout requires more darts than were used
     */
    private boolean areFollowingTurnsLegal(X01TurnMutation turnMutation, int remaining) {
        NavigableMap<Integer, X01LegRound> roundsAfterTurn = turnMutation.leg()
                .getRounds()
                .tailMap(turnMutation.roundNumber(), false);

        for (X01LegRound round : roundsAfterTurn.values()) {
            X01Turn turn = round.getTurns().get(turnMutation.throwerId());
            if (turn == null) continue;

            // A checkout before an existing following turn would invalidate the existing history.
            if (remaining == 0) {
                return false;
            }

            remaining -= turn.getScore();

            if (!isTurnLegal(turn.getScore(), remaining, turnMutation.checkoutDartsUsed())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Determines whether a turn produces a legal X01 state.
     *
     * @param score             the points scored in the turn
     * @param remaining         the remaining score after the turn
     * @param checkoutDartsUsed the number of darts used for the checkout
     * @return whether the turn is legal
     * @throws X01CheckoutInsufficientDartsException when the checkout requires more darts than were used
     */
    private boolean isTurnLegal(int score, int remaining, Integer checkoutDartsUsed) {
        // A bust can never represent a legal turn.
        if (checkoutService.isRemainingBust(remaining)) return false;

        // Reaching zero additionally requires a valid checkout with the supplied dart count.
        if (checkoutService.isRemainingZero(remaining)) {
            return checkoutDartsUsed != null
                    && checkoutService.isScoreCheckout(score, checkoutDartsUsed);
        }

        return true;
    }
}