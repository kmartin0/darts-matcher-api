package nl.kmartin.dartsmatcherapi.features.x01.x01leg.service;

import nl.kmartin.dartsmatcherapi.error.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapi.error.response.TargetError;
import nl.kmartin.dartsmatcherapi.features.x01.common.X01MatchUtils;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.service.IX01LegRoundService;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Turn;
import nl.kmartin.dartsmatcherapi.i18n.MessageKeys;
import nl.kmartin.dartsmatcherapi.i18n.MessageResolver;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.IntStream;

/**
 * Coordinates creation, scoring and rule handling for X01 legs.
 *
 * Applies turns, maintains checkout state and delegates progression and result calculations.
 */
@Service
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

    /**
     * Creates a new numbered leg and determines which player throws first.
     *
     * @param legNumber        the leg number
     * @param throwsFirstInSet the player that started the set
     * @param players          the match players
     * @return the created leg entry
     */
    @Override
    public X01LegEntry createNewLeg(int legNumber, ObjectId throwsFirstInSet, List<X01MatchPlayer> players) {
        ObjectId throwsFirstInLeg = calcThrowsFirstInLeg(legNumber, throwsFirstInSet, players);
        return new X01LegEntry(legNumber, new X01Leg(null, throwsFirstInLeg, new TreeMap<>()));
    }

    /**
     * Applies a player's turn to a leg round.
     *
     * The turn is stored or replaces an existing turn, after which remaining points, score validity, checkout state
     * and the leg result are recalculated.
     *
     * @param x01          the starting score for the leg
     * @param leg          the leg to update
     * @param roundNumber  the round number
     * @param turn         the turn to apply
     * @param throwerId    the player that threw the turn
     * @param trackDoubles whether missed doubles should be tracked
     */
    @Override
    public void applyTurn(int x01, X01Leg leg, int roundNumber, X01Turn turn, ObjectId throwerId, boolean trackDoubles) {
        if (leg == null || turn == null) return;

        checkLegEditable(leg, throwerId);

        // Capture checkout state before replacing the existing score.
        boolean wasCheckoutRound = isPlayerCheckoutRound(leg, roundNumber, throwerId);

        X01LegRoundScore roundScore = addRoundScore(leg, roundNumber, turn, throwerId, trackDoubles);
        if (roundScore == null) return;

        // Rebuild this player's remaining values before processing the new score.
        legResultService.updateRemainingForPlayer(leg, throwerId, x01);
        processRoundScore(leg, roundScore, turn.getCheckoutDartsUsed(), throwerId, x01, wasCheckoutRound);

        // Rebuild the winner and remove history that became stale after the edit.
        legResultService.updateLegResult(leg, x01);
    }

    /**
     * Determines whether a player's score belongs to the checkout round of a leg.
     *
     * @param leg         the leg to check
     * @param roundNumber the round number
     * @param playerId    the player ID
     * @return whether the round is the player's checkout round
     */
    @Override
    public boolean isPlayerCheckoutRound(X01Leg leg, int roundNumber, ObjectId playerId) {
        if (leg == null || playerId == null) return false;

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
     * @throws IllegalArgumentException when the leg number or player list is invalid
     * @throws IllegalStateException    when the set starter cannot be found among the match players
     */
    private ObjectId calcThrowsFirstInLeg(int legNumber, ObjectId throwsFirstInSet, List<X01MatchPlayer> players) {
        if (legNumber < 1) {
            throw new IllegalArgumentException("Leg number must be greater than zero.");
        }

        if (X01MatchUtils.isPlayersEmpty(players)) {
            throw new IllegalArgumentException("Cannot calculate first thrower from a null or empty player list.");
        }

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
     * Verifies whether a player's score may be modified in a leg.
     *
     * Once the leg is concluded, only the winning player's score may be modified.
     *
     * @param leg      the leg to check
     * @param playerId the player whose score is being modified
     * @throws InvalidArgumentsException when another player's score is modified after the leg has been won
     */
    private void checkLegEditable(X01Leg leg, ObjectId playerId) {
        if (leg == null) return;

        // A concluded leg can only be changed through the winning player's turn.
        if (legProgressService.isLegConcluded(leg) && !Objects.equals(leg.getWinner(), playerId)) {
            throw new InvalidArgumentsException(
                    new TargetError(
                            X01Turn.FIELD_SCORE,
                            messageResolver.getMessage(MessageKeys.MESSAGE_LEG_ALREADY_WON)
                    )
            );
        }
    }

    /**
     * Adds or replaces a player's score in a leg round.
     *
     * @param leg          the leg containing the round
     * @param roundNumber  the round number
     * @param turn         the turn to store
     * @param throwerId    the player that threw the turn
     * @param trackDoubles whether missed doubles should be tracked
     * @return the stored round score, or null when the round cannot be resolved
     */
    private X01LegRoundScore addRoundScore(X01Leg leg, int roundNumber, X01Turn turn, ObjectId throwerId, boolean trackDoubles) {
        Optional<X01LegRoundEntry> legRoundEntry = legProgressService.getLegRoundOrThrow(leg, roundNumber, true);
        if (legRoundEntry.isEmpty()) return null;

        X01LegRoundScore roundScore = new X01LegRoundScore(turn, trackDoubles);
        legRoundEntry.get().round().getScores().put(throwerId, roundScore);

        return roundScore;
    }

    /**
     * Processes a round score after its remaining value has been recalculated.
     *
     * Illegal scores are repaired, while valid scores update checkout state when necessary.
     *
     * @param leg               the leg containing the score
     * @param roundScore        the updated round score
     * @param checkoutDartsUsed the checkout darts supplied with the turn
     * @param playerId          the player that threw the turn
     * @param x01               the starting score for the leg
     * @param wasCheckoutRound  whether the turn previously represented the checkout
     */
    private void processRoundScore(X01Leg leg, X01LegRoundScore roundScore, Integer checkoutDartsUsed, ObjectId playerId, int x01, boolean wasCheckoutRound) {
        if (!legRoundService.isRoundScoreLegal(roundScore, checkoutDartsUsed)) {
            handleIllegalRoundScore(leg, roundScore, playerId, x01, wasCheckoutRound);
            return;
        }

        updateCheckoutDartsUsed(leg, roundScore, checkoutDartsUsed, wasCheckoutRound);
    }

    /**
     * Repairs an illegal round score by converting it to a zero-score turn.
     *
     * Checkout dart usage is cleared when the repaired turn was the previous checkout, after which the player's
     * remaining score history is recalculated.
     *
     * @param leg              the leg containing the score
     * @param roundScore       the illegal round score
     * @param playerId         the player that threw the score
     * @param x01              the starting score for the leg
     * @param wasCheckoutRound whether the score previously represented the checkout
     */
    private void handleIllegalRoundScore(X01Leg leg, X01LegRoundScore roundScore, ObjectId playerId, int x01, boolean wasCheckoutRound) {
        // A bust or invalid checkout counts as a zero-score turn.
        roundScore.setScore(0);

        if (wasCheckoutRound) {
            leg.setCheckoutDartsUsed(null);
        }

        // Rebuild remaining values after changing the recorded score.
        legResultService.updateRemainingForPlayer(leg, playerId, x01);
    }

    /**
     * Updates checkout dart usage after applying a valid round score.
     *
     * @param leg               the leg to update
     * @param roundScore        the updated round score
     * @param checkoutDartsUsed the number of darts used for the checkout
     * @param wasCheckoutRound  whether the score previously represented the checkout
     */
    private void updateCheckoutDartsUsed(X01Leg leg, X01LegRoundScore roundScore, Integer checkoutDartsUsed, boolean wasCheckoutRound) {
        if (roundScore.getRemaining() == 0) {
            // The updated score is now the checkout.
            leg.setCheckoutDartsUsed(checkoutDartsUsed);
        } else if (wasCheckoutRound) {
            // The previous checkout was edited into a non-winning score.
            leg.setCheckoutDartsUsed(null);
        }
    }
}