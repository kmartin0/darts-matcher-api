package nl.kmartin.dartsmatcherapi.features.x01.x01leground.service;

import nl.kmartin.dartsmatcherapi.features.x01.common.X01MatchUtils;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.service.IX01CheckoutService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRound;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

/**
 * Provides operations for determining and maintaining player turns within X01 leg rounds.
 *
 * Uses the insertion order of round scores to preserve the order in which players threw.
 */
@Service
public class X01LegRoundServiceImpl implements IX01LegRoundService {

    private final IX01CheckoutService checkoutService;

    public X01LegRoundServiceImpl(IX01CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    /**
     * Determines which player should throw next in the round.
     *
     * Players are evaluated in the throwing order established by the player that
     * started the leg. The first player without a score in the round is returned.
     *
     * @param legRound         the round to evaluate
     * @param throwsFirstInLeg the player that started the leg
     * @param players          the match players
     * @return the next player to throw, or null when no player remains
     */
    @Override
    public ObjectId getCurrentThrowerInRound(X01LegRound legRound, ObjectId throwsFirstInLeg, List<X01MatchPlayer> players) {
        if (legRound == null || X01MatchUtils.isPlayersEmpty(players)) return null;

        // Order the players starting with the player that threw first in the leg.
        List<X01MatchPlayer> orderedPlayers = X01MatchUtils.getThrowingOrder(throwsFirstInLeg, players);
        if (orderedPlayers == null) return null;

        // The first player without a score is the next player to throw.
        return orderedPlayers.stream()
                .map(X01MatchPlayer::getPlayerId)
                .filter(playerId -> !legRound.getScores().containsKey(playerId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Removes the most recently added score from a round.
     *
     * @param legRound the round from which to remove the score
     * @return whether a score was removed
     */
    @Override
    public boolean removeLastScoreFromRound(X01LegRound legRound) {
        if (X01MatchUtils.isScoresEmpty(legRound)) return false;

        // Scores are stored in insertion order, so the final entry is the most recent turn.
        Iterator<ObjectId> scoresIterator = legRound.getScores().keySet().iterator();

        while (scoresIterator.hasNext()) {
            scoresIterator.next();

            if (!scoresIterator.hasNext()) {
                scoresIterator.remove();
                return true;
            }
        }

        return false;
    }

    /**
     * Removes scores that were recorded after the player that won the leg.
     *
     * @param round     the round to trim
     * @param legWinner the player that won the leg
     */
    @Override
    public void removeScoresAfterWinner(X01LegRound round, ObjectId legWinner) {
        if (round == null || legWinner == null) return;

        // Walk the scores in throwing order and remove everything after the winning turn.
        Iterator<ObjectId> scoresIterator = round.getScores().keySet().iterator();
        boolean winnerHasThrown = false;

        while (scoresIterator.hasNext()) {
            ObjectId playerId = scoresIterator.next();

            if (winnerHasThrown) {
                scoresIterator.remove();
            } else if (playerId.equals(legWinner)) {
                winnerHasThrown = true;
            }
        }
    }

    /**
     * Determines whether a round score represents a legal X01 state.
     *
     * A bust is invalid. When the remaining score reaches zero, the score must also
     * represent a valid checkout using the supplied number of checkout darts.
     *
     * @param roundScore the round score to validate
     * @param checkoutDartsUsed the number of darts used for the checkout
     * @return whether the round score is legal
     */
    @Override
    public boolean isRoundScoreLegal(X01LegRoundScore roundScore, Integer checkoutDartsUsed) {
        int remaining = roundScore.getRemaining();

        if (checkoutService.isRemainingBust(remaining)) return false;

        if (checkoutService.isRemainingZero(remaining)) {
            return checkoutService.isScoreCheckout(roundScore.getScore(), checkoutDartsUsed);
        }

        return true;
    }
}
