package nl.kmartin.dartsmatcherapi.features.x01.x01leground.service;

import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRound;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01Turn;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01TurnAlreadyExistsException;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Provides operations for determining and maintaining player turns within X01 leg rounds.
 *
 * Uses the insertion order of turns to preserve the order in which players threw.
 */
@Service
@Validated
public class X01LegRoundServiceImpl implements IX01LegRoundService {

    @Override
    public ObjectId getCurrentThrowerInRound(
            X01LegRound legRound,
            ObjectId throwsFirstInLeg,
            List<X01MatchPlayer> players
    ) {
        // Order the players starting with the player that threw first in the leg.
        List<X01MatchPlayer> orderedPlayers = getThrowingOrder(throwsFirstInLeg, players);

        // The first player without a turn is the next player to throw.
        return orderedPlayers.stream()
                .map(X01MatchPlayer::getPlayerId)
                .filter(playerId -> !legRound.getTurns().containsKey(playerId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void addTurn(X01LegRound legRound, ObjectId playerId, X01Turn turn) {
        if (legRound.getTurns().containsKey(playerId)) {
            throw new X01TurnAlreadyExistsException();
        }

        legRound.getTurns().put(playerId, turn);
    }

    @Override
    public void replaceTurn(X01LegRound legRound, ObjectId playerId, X01Turn turn) {
        if (!legRound.getTurns().containsKey(playerId)) {
            throw new ResourceNotFoundException(X01Turn.class, playerId);
        }

        legRound.getTurns().put(playerId, turn);
    }

    @Override
    public boolean removeLastTurnFromRound(X01LegRound legRound) {
        if (legRound.getTurns().isEmpty()) return false;

        // Turns are stored in insertion order, so remove the final entry.
        Iterator<ObjectId> turnsIterator = legRound.getTurns().keySet().iterator();

        while (turnsIterator.hasNext()) {
            turnsIterator.next();

            if (!turnsIterator.hasNext()) {
                turnsIterator.remove();
                return true;
            }
        }

        return false;
    }

    @Override
    public void removeTurnsAfterWinner(X01LegRound round, ObjectId legWinner) {
        // Walk the turns in throwing order and remove everything after the winning turn.
        Iterator<ObjectId> turnsIterator = round.getTurns().keySet().iterator();
        boolean winnerHasThrown = false;

        while (turnsIterator.hasNext()) {
            ObjectId playerId = turnsIterator.next();

            if (winnerHasThrown) {
                turnsIterator.remove();
            } else if (playerId.equals(legWinner)) {
                winnerHasThrown = true;
            }
        }
    }

    /**
     * Orders the players starting with the player that throws first.
     *
     * @param throwsFirst the ID of the player that throws first
     * @param players     the players to order
     * @return the players in throwing order
     * @throws IllegalArgumentException when the player that throws first cannot be found
     */
    private List<X01MatchPlayer> getThrowingOrder(ObjectId throwsFirst, List<X01MatchPlayer> players) {
        // Find the index of the player that starts the round.
        int throwsFirstIndex = IntStream.range(0, players.size())
                .filter(i -> players.get(i).getPlayerId().equals(throwsFirst))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        // Rotate the player list so the leg starter appears first.
        List<X01MatchPlayer> orderedPlayers = new ArrayList<>(players.size());
        orderedPlayers.addAll(players.subList(throwsFirstIndex, players.size()));
        orderedPlayers.addAll(players.subList(0, throwsFirstIndex));

        return orderedPlayers;
    }
}