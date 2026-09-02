package nl.kmartin.dartsmatcherapi.features.x01.x01leg.service;

import nl.kmartin.dartsmatcherapi.features.x01.common.X01MatchUtils;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRound;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.service.IX01LegRoundService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Calculates and updates result-related state for X01 legs.
 *
 * Recalculates remaining scores, determines the first winning turn, removes stale history and calculates darts used.
 */
@Service
public class X01LegResultServiceImpl implements IX01LegResultService {

    private final IX01LegRoundService legRoundService;

    public X01LegResultServiceImpl(IX01LegRoundService legRoundService) {
        this.legRoundService = legRoundService;
    }

    /**
     * Rebuilds the result-related state of a leg.
     *
     * Remaining scores are recalculated before determining the first winning turn. When a winner exists,
     * history after that turn is removed. Otherwise the winner and checkout state are cleared.
     *
     * @param leg the leg to update
     * @param x01 the starting score for the leg
     */
    @Override
    public void updateLegResult(X01Leg leg, int x01) {
        if (leg == null) return;

        // Recalculate all remaining scores before determining the result.
        updateAllRemaining(leg, x01);

        // Find the first checkout in chronological playing order.
        WinnerSearch winnerSearch = findLegWinner(leg);

        // Clear the result when the leg no longer has a winner.
        if (winnerSearch.winner() == null) {
            leg.setWinner(null);
            leg.setCheckoutDartsUsed(null);
            return;
        }

        leg.setWinner(winnerSearch.winner());

        // Remove history recorded after the winning turn.
        removeScoresAfterWinner(leg, winnerSearch.winner(), winnerSearch.roundNumber());
    }

    /**
     * Gets the latest remaining score for a player in a leg.
     *
     * @param leg      the leg to evaluate
     * @param playerId the player ID
     * @param x01      the starting score for the leg
     * @return the player's latest remaining score, or the starting score when the player has not thrown
     */
    @Override
    public int getRemainingForPlayer(X01Leg leg, ObjectId playerId, int x01) {
        // Search backwards so the first score found contains the player's latest remaining value.
        for (X01LegRound round : leg.getRounds().descendingMap().values()) {
            X01LegRoundScore playerScore = round.getScores().get(playerId);

            if (playerScore != null) {
                return playerScore.getRemaining();
            }
        }

        return x01;
    }

    /**
     * Recalculates the remaining score for a player across all rounds in a leg.
     *
     * @param leg      the leg to update
     * @param playerId the player whose remaining scores should be recalculated
     * @param x01      the starting score for the leg
     */
    @Override
    public void updateRemainingForPlayer(X01Leg leg, ObjectId playerId, int x01) {
        if (leg == null) return;

        int remaining = x01;

        // Rebuild the player's remaining score chronologically from the start of the leg.
        for (X01LegRound round : leg.getRounds().values()) {
            X01LegRoundScore roundScore = round.getScores().get(playerId);
            if (roundScore == null) continue;

            remaining -= roundScore.getScore();
            roundScore.setRemaining(remaining);
        }
    }

    /**
     * Calculates the number of darts used by a player in a leg.
     *
     * Complete turns count as three darts. For the leg winner, the final turn uses the recorded checkout dart count,
     * defaulting to three when that value is unavailable.
     *
     * @param leg      the leg to evaluate
     * @param playerId the player ID
     * @return the total number of darts used
     */
    @Override
    public int calculateDartsUsed(X01Leg leg, ObjectId playerId) {
        if (X01MatchUtils.isRoundsEmpty(leg) || playerId == null) return 0;

        // Only the winner can have a partial final turn; default missing checkout dart usage to three.
        boolean playerWonLeg = Objects.equals(leg.getWinner(), playerId);
        Integer checkoutRoundNumber = playerWonLeg ? leg.getRounds().lastKey() : null;
        int checkoutDartsUsed = leg.getCheckoutDartsUsed() != null ? leg.getCheckoutDartsUsed() : 3;

        // Count three darts for each recorded turn, except the winner's checkout turn.
        return leg.getRounds().entrySet()
                .stream()
                .mapToInt(entry -> {
                    X01LegRoundScore playerScore = entry.getValue().getScores().get(playerId);
                    if (playerScore == null) return 0;

                    if (Objects.equals(checkoutRoundNumber, entry.getKey())) {
                        return checkoutDartsUsed;
                    }

                    return 3;
                })
                .sum();
    }

    /**
     * Finds the first winning turn in chronological playing order.
     *
     * @param leg the leg to inspect
     * @return the winner and winning round, or an empty winner search when the leg is unfinished
     */
    private WinnerSearch findLegWinner(X01Leg leg) {
        if (X01MatchUtils.isRoundsEmpty(leg)) return new WinnerSearch(null, null);

        // Traverse rounds and scores in playing order; the first player reaching zero wins the leg.
        for (Map.Entry<Integer, X01LegRound> roundEntry : leg.getRounds().entrySet()) {
            for (Map.Entry<ObjectId, X01LegRoundScore> scoreEntry : roundEntry.getValue().getScores().entrySet()) {
                if (scoreEntry.getValue().getRemaining() == 0) {
                    return new WinnerSearch(scoreEntry.getKey(), roundEntry.getKey());
                }
            }
        }

        return new WinnerSearch(null, null);
    }

    /**
     * Removes rounds and scores recorded after the winning turn.
     *
     * @param leg                the leg to update
     * @param legWinner          the player that won the leg
     * @param winningRoundNumber the round containing the winning turn
     */
    private void removeScoresAfterWinner(X01Leg leg, ObjectId legWinner, int winningRoundNumber) {
        // Remove every round played after the winning round.
        leg.getRounds().tailMap(winningRoundNumber, false).clear();

        // Remove scores thrown after the winner within the winning round.
        legRoundService.removeScoresAfterWinner(leg.getRounds().get(winningRoundNumber), legWinner);
    }

    /**
     * Recalculates the remaining scores for all recorded players across a leg.
     *
     * @param leg the leg to update
     * @param x01 the starting score for the leg
     */
    private void updateAllRemaining(X01Leg leg, int x01) {
        if (leg == null) return;

        // Track each player's latest remaining score while rebuilding the leg chronologically.
        Map<ObjectId, Integer> remainingMap = new HashMap<>();

        leg.getRounds().values().forEach(round -> {
            round.getScores().forEach((playerId, roundScore) -> {
                int previousRemaining = remainingMap.getOrDefault(playerId, x01);
                roundScore.setRemaining(previousRemaining - roundScore.getScore());
                remainingMap.put(playerId, roundScore.getRemaining());
            });
        });
    }

    /**
     * Stores the winner found during a chronological search and the round containing the winning turn.
     *
     * @param winner      the winning player, or null when the leg is unfinished
     * @param roundNumber the winning round number, or null when the leg is unfinished
     */
    private record WinnerSearch(ObjectId winner, Integer roundNumber) {
    }
}