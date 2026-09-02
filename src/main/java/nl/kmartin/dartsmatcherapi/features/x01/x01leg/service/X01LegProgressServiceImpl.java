package nl.kmartin.dartsmatcherapi.features.x01.x01leg.service;

import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRound;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.service.IX01LegRoundService;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.utils.NumberUtils;
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

    /**
     * Gets a round in a leg by its round number.
     *
     * @param leg         the leg to search
     * @param roundNumber the round number
     * @return the matching round entry
     * @throws ResourceNotFoundException when the round does not exist
     */
    @Override
    public X01LegRoundEntry getLegRoundOrThrow(X01Leg leg, int roundNumber) {
        if (roundNumber < 1) {
            throw new ResourceNotFoundException(X01LegRound.class, roundNumber);
        }

        return Optional.ofNullable(leg.getRounds().get(roundNumber))
                .map(round -> new X01LegRoundEntry(roundNumber, round))
                .orElseThrow(() -> new ResourceNotFoundException(X01LegRound.class, roundNumber));
    }

    /**
     * Finds the first round in which at least one player has not yet scored.
     *
     * @param leg     the leg to evaluate
     * @param players the match players
     * @return the current round, or empty when no round is in progress
     */
    @Override
    public Optional<X01LegRoundEntry> getCurrentLegRound(X01Leg leg, List<X01MatchPlayer> players) {
        // Filter rounds with missing player scores and get the lowest round number.
        return leg.getRounds().entrySet()
                .stream()
                .filter(entry -> players.stream()
                        .anyMatch(player -> !entry.getValue().getScores().containsKey(player.getPlayerId())))
                .findFirst()
                .map(X01LegRoundEntry::new);
    }

    /**
     * Creates and adds the next missing round to a leg.
     *
     * @param leg the leg to update
     * @return the created round, or empty when no round number can be assigned
     */
    @Override
    public Optional<X01LegRoundEntry> createNextLegRound(X01Leg leg) {
        // Get existing round numbers.
        Set<Integer> existingRoundNumbers = getLegRoundNumbers(leg);

        // Find the next available leg round number.
        int nextRoundNumber = NumberUtils.findNextNumber(existingRoundNumbers);
        if (nextRoundNumber == -1) return Optional.empty();

        // Create and add a new leg round to the leg.
        X01LegRoundEntry newLegRoundEntry = new X01LegRoundEntry(nextRoundNumber, new X01LegRound());

        leg.getRounds().put(newLegRoundEntry.roundNumber(), newLegRoundEntry.round());
        return Optional.of(newLegRoundEntry);
    }

    /**
     * Determines whether a leg has a winner.
     *
     * @param leg the leg to evaluate
     * @return whether the leg is concluded
     */
    @Override
    public boolean isLegConcluded(X01Leg leg) {
        return leg.getWinner() != null;
    }

    /**
     * Removes the most recently recorded score from a leg.
     *
     * Empty rounds encountered while traversing backwards are removed until a score is successfully removed.
     *
     * @param leg the leg to update
     * @return whether a score was removed
     */
    @Override
    public boolean removeLastScoreFromLeg(X01Leg leg) {
        if (leg.getRounds().isEmpty()) return false;

        Iterator<Integer> reverseRoundsIterator = leg.getRounds().descendingKeySet().iterator();

        while (reverseRoundsIterator.hasNext()) {
            X01LegRound round = leg.getRounds().get(reverseRoundsIterator.next());
            boolean scoreRemoved = legRoundService.removeLastScoreFromRound(round);

            if (round.getScores().isEmpty()) {
                reverseRoundsIterator.remove();
            }

            if (scoreRemoved) {
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
        // Map all round numbers to a set of integers.
        return Set.copyOf(leg.getRounds().keySet());
    }
}
