package nl.kmartin.dartsmatcherapi.features.x01.x01leg.service;

import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRound;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.service.IX01LegRoundService;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.util.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Provides operations for navigating and maintaining round progression within an X01 leg.
 */
@Service
@Validated
public class X01LegProgressServiceImpl implements IX01LegProgressService {

    private final IX01LegRoundService legRoundService;

    public X01LegProgressServiceImpl(IX01LegRoundService legRoundService) {
        this.legRoundService = legRoundService;
    }

    @Override
    public X01LegRoundEntry getLegRoundOrThrow(X01Leg leg, int roundNumber) {
        if (roundNumber < 1) {
            throw new ResourceNotFoundException(X01LegRound.class, roundNumber);
        }

        return Optional.ofNullable(leg.getRounds().get(roundNumber))
                .map(round -> new X01LegRoundEntry(roundNumber, round))
                .orElseThrow(() -> new ResourceNotFoundException(X01LegRound.class, roundNumber));
    }

    @Override
    public Optional<X01LegRoundEntry> getCurrentLegRound(X01Leg leg, List<X01MatchPlayer> players) {
        // Return the earliest round that is still missing a turn for at least one player.
        return leg.getRounds().entrySet()
                .stream()
                .filter(entry -> players.stream()
                        .anyMatch(player -> !entry.getValue().getTurns().containsKey(player.getPlayerId())))
                .findFirst()
                .map(X01LegRoundEntry::new);
    }

    @Override
    public Optional<X01LegRoundEntry> createNextLegRound(X01Leg leg) {
        // Find the next available round number from the rounds already present in the leg.
        Set<Integer> existingRoundNumbers = getLegRoundNumbers(leg);
        int nextRoundNumber = NumberUtils.findNextNumber(existingRoundNumbers);

        if (nextRoundNumber == -1) return Optional.empty();

        // Add the newly created round to the leg before returning its entry.
        X01LegRoundEntry newLegRoundEntry = new X01LegRoundEntry(nextRoundNumber, new X01LegRound());

        leg.getRounds().put(newLegRoundEntry.roundNumber(), newLegRoundEntry.round());
        return Optional.of(newLegRoundEntry);
    }

    @Override
    public boolean isLegConcluded(X01Leg leg) {
        return leg.getWinner() != null;
    }

    @Override
    public boolean removeLastTurnFromLeg(X01Leg leg) {
        if (leg.getRounds().isEmpty()) return false;

        Iterator<Integer> reverseRoundsIterator = leg.getRounds().descendingKeySet().iterator();

        // Traverse backwards so the most recently recorded turn is removed first.
        while (reverseRoundsIterator.hasNext()) {
            X01LegRound round = leg.getRounds().get(reverseRoundsIterator.next());
            boolean isTurnRemoved = legRoundService.removeLastTurnFromRound(round);

            // Remove rounds that become empty, including already-empty trailing rounds.
            if (round.getTurns().isEmpty()) {
                reverseRoundsIterator.remove();
            }

            if (isTurnRemoved) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the round numbers currently present in a leg.
     *
     * @param leg the leg to evaluate
     * @return the round numbers
     */
    private Set<Integer> getLegRoundNumbers(X01Leg leg) {
        return Set.copyOf(leg.getRounds().keySet());
    }
}