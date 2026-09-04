package nl.kmartin.dartsmatcherapi.features.x01.x01leg.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRound;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01Turn;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.service.IX01LegRoundService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Calculates and updates result-related state for X01 legs.
 *
 * Recalculates remaining scores, determines the first winning turn, removes stale history and calculates darts used.
 */
@Service
@Validated
public class X01LegResultServiceImpl implements IX01LegResultService {

    private final IX01LegRoundService legRoundService;

    public X01LegResultServiceImpl(IX01LegRoundService legRoundService) {
        this.legRoundService = legRoundService;
    }

    @Override
    public void updateLegResult(X01Leg leg, int x01) {
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
        removeTurnsAfterWinner(leg, winnerSearch.winner(), winnerSearch.roundNumber());
    }

    @Override
    public int getRemainingForPlayer(X01Leg leg, ObjectId playerId, int x01) {
        // Search backwards so the first turn found contains the player's latest remaining value.
        for (X01LegRound round : leg.getRounds().descendingMap().values()) {
            X01Turn playerTurn = round.getTurns().get(playerId);

            if (playerTurn != null) {
                return playerTurn.getRemaining();
            }
        }

        return x01;
    }

    @Override
    public void updateRemainingForPlayer(X01Leg leg, ObjectId playerId, int x01) {
        int remaining = x01;

        // Rebuild the player's remaining score chronologically from the start of the leg.
        for (X01LegRound round : leg.getRounds().values()) {
            X01Turn turn = round.getTurns().get(playerId);
            if (turn == null) continue;

            remaining -= turn.getScore();
            turn.setRemaining(remaining);
        }
    }

    @Override
    public int calculateDartsUsed(X01Leg leg, ObjectId playerId) {
        // Only the winner can have a partial final turn; otherwise use the maximum darts per turn.
        boolean playerWonLeg = Objects.equals(leg.getWinner(), playerId);
        Integer checkoutRoundNumber = playerWonLeg ? leg.getRounds().lastKey() : null;
        int checkoutDartsUsed = leg.getCheckoutDartsUsed() != null
                ? leg.getCheckoutDartsUsed()
                : X01Turn.MAXIMUM_DARTS_PER_TURN;

        // Count the maximum darts per turn for each recorded turn, except the winner's checkout turn.
        return leg.getRounds().entrySet()
                .stream()
                .mapToInt(entry -> {
                    X01Turn playerTurn = entry.getValue().getTurns().get(playerId);
                    if (playerTurn == null) return 0;

                    if (Objects.equals(checkoutRoundNumber, entry.getKey())) {
                        return checkoutDartsUsed;
                    }

                    return X01Turn.MAXIMUM_DARTS_PER_TURN;
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
        // Traverse rounds and turns in playing order; the first player reaching zero wins the leg.
        for (Map.Entry<Integer, X01LegRound> roundEntry : leg.getRounds().entrySet()) {
            for (Map.Entry<ObjectId, X01Turn> turnEntry : roundEntry.getValue().getTurns().entrySet()) {
                if (turnEntry.getValue().getRemaining() == 0) {
                    return new WinnerSearch(turnEntry.getKey(), roundEntry.getKey());
                }
            }
        }

        return new WinnerSearch(null, null);
    }

    /**
     * Removes rounds and turns recorded after the winning turn.
     *
     * @param leg                the leg to update
     * @param legWinner          the player that won the leg
     * @param winningRoundNumber the round containing the winning turn
     */
    private void removeTurnsAfterWinner(X01Leg leg, ObjectId legWinner, int winningRoundNumber) {
        // Remove every round played after the winning round.
        leg.getRounds().tailMap(winningRoundNumber, false).clear();

        // Remove turns recorded after the winner within the winning round.
        legRoundService.removeTurnsAfterWinner(leg.getRounds().get(winningRoundNumber), legWinner);
    }

    /**
     * Recalculates the remaining scores for all recorded players across a leg.
     *
     * @param leg the leg to update
     * @param x01 the starting score for the leg
     */
    private void updateAllRemaining(X01Leg leg, int x01) {
        // Track each player's latest remaining score while rebuilding the leg chronologically.
        Map<ObjectId, Integer> remainingMap = new HashMap<>();

        leg.getRounds().values().forEach(round -> {
            round.getTurns().forEach((playerId, turn) -> {
                int previousRemaining = remainingMap.getOrDefault(playerId, x01);
                turn.setRemaining(previousRemaining - turn.getScore());
                remainingMap.put(playerId, turn.getRemaining());
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