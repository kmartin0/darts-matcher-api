package nl.kmartin.dartsmatcherapi.features.x01.x01match.service;

import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.service.IX01LegProgressService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.service.IX01LegRoundService;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01BestOf;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchProgress;
import nl.kmartin.dartsmatcherapi.features.x01.x01rules.service.IX01RulesService;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01Set;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.service.IX01SetProgressService;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.service.IX01SetService;
import nl.kmartin.dartsmatcherapi.util.NumberUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * Provides operations for navigating and maintaining progression through X01 matches.
 *
 * Resolves or creates the current set, leg and round and rebuilds the current match progress.
 */
@Service
@Validated
public class X01MatchProgressServiceImpl implements IX01MatchProgressService {

    private final IX01SetService setService;
    private final IX01SetProgressService setProgressService;
    private final IX01LegProgressService legProgressService;
    private final IX01LegRoundService legRoundService;
    private final IX01RulesService rulesService;

    public X01MatchProgressServiceImpl(
            IX01SetService setService,
            IX01SetProgressService setProgressService,
            IX01LegProgressService legProgressService,
            IX01LegRoundService legRoundService,
            IX01RulesService rulesService
    ) {
        this.setService = setService;
        this.setProgressService = setProgressService;
        this.legProgressService = legProgressService;
        this.legRoundService = legRoundService;
        this.rulesService = rulesService;
    }

    /**
     * Gets a set in a match by its set number.
     *
     * @param match     the match containing the sets
     * @param setNumber the set number
     * @return the matching set entry
     * @throws ResourceNotFoundException when the set does not exist
     */
    @Override
    public X01SetEntry getSetOrThrow(X01Match match, int setNumber) {
        return Optional.ofNullable(match.getSets().get(setNumber))
                .map(set -> new X01SetEntry(setNumber, set))
                .orElseThrow(() -> new ResourceNotFoundException(X01Set.class, setNumber));
    }

    /**
     * Gets the first set that has not yet been concluded.
     *
     * @param match the match containing the sets
     * @return the current set, or empty when no unfinished set exists
     */
    @Override
    public Optional<X01SetEntry> getCurrentSet(X01Match match) {
        // Sets are ordered by number, so the first unfinished set is the current set.
        return match.getSets().entrySet().stream()
                .filter(entry -> entry.getValue().getResult() == null)
                .findFirst()
                .map(X01SetEntry::new);
    }

    /**
     * Gets the current set or creates the next set when the match can continue.
     *
     * @param match the match whose current set should be resolved
     * @return the current or newly created set, or empty when the match is concluded
     */
    @Override
    public Optional<X01SetEntry> getCurrentSetOrCreate(X01Match match) {
        Optional<X01SetEntry> currentSetEntry = getCurrentSet(match);

        // Create the next set only when no unfinished set exists and the match can still continue.
        return currentSetEntry.isEmpty() && !isMatchConcluded(match)
                ? createNextSet(match)
                : currentSetEntry;
    }

    /**
     * Gets the current leg or creates the next leg when the set can continue.
     *
     * @param match           the match containing the set
     * @param currentSetEntry the current set
     * @return the current or newly created leg, or empty when the set is concluded
     */
    @Override
    public Optional<X01LegEntry> getCurrentLegOrCreate(X01Match match, X01SetEntry currentSetEntry) {
        X01Set currentSet = currentSetEntry.set();
        Optional<X01LegEntry> currentLegEntry = setProgressService.getCurrentLeg(currentSet);

        // Create the next leg only when no unfinished leg exists and the set can still continue.
        X01BestOf bestOf = match.getMatchSettings().getBestOf();
        return currentLegEntry.isEmpty() && !setProgressService.isSetConcluded(currentSet)
                ? setProgressService.createNextLeg(currentSetEntry, match.getPlayers(), bestOf)
                : currentLegEntry;
    }

    /**
     * Gets the current round or creates the next round when the leg can continue.
     *
     * @param match      the match containing the leg
     * @param currentLeg the current leg
     * @return the current or newly created round, or empty when the leg is concluded
     */
    @Override
    public Optional<X01LegRoundEntry> getCurrentLegRoundOrCreate(X01Match match, X01Leg currentLeg) {
        Optional<X01LegRoundEntry> currentRoundEntry = legProgressService.getCurrentLegRound(currentLeg, match.getPlayers());

        // Create the next round only when no unfinished round exists and the leg can still continue.
        return currentRoundEntry.isEmpty() && !legProgressService.isLegConcluded(currentLeg)
                ? legProgressService.createNextLegRound(currentLeg)
                : currentRoundEntry;
    }

    /**
     * Removes the last score and any trailing empty match structure.
     *
     * @param match the match whose last score should be removed
     */
    @Override
    public void removeLastScoreFromMatch(X01Match match) {
        // Traverse sets in reverse so trailing empty rounds, legs and sets are cleaned up with the removed score.
        Iterator<Integer> reverseSetsIterator = match.getSets().descendingKeySet().iterator();

        while (reverseSetsIterator.hasNext()) {
            X01Set set = match.getSets().get(reverseSetsIterator.next());
            boolean scoreRemoved = setProgressService.removeLastScoreFromSet(set);

            // Remove a set when deleting its final score also removed its final leg.
            if (set.getLegs().isEmpty()) reverseSetsIterator.remove();

            // Stop once an actual score has been removed.
            if (scoreRemoved) break;
        }
    }

    /**
     * Rebuilds the current match progress.
     *
     * Missing set, leg and round structures are created when the match can continue.
     *
     * @param match the match whose progress should be rebuilt
     */
    @Override
    public void updateMatchProgress(X01Match match) {
        // Resolve or create the current set, leg and round.
        Optional<X01SetEntry> currentSetEntry = getCurrentSetOrCreate(match);
        Optional<X01LegEntry> currentLegEntry = currentSetEntry.flatMap(setEntry -> getCurrentLegOrCreate(match, setEntry));
        Optional<X01LegRoundEntry> currentRoundEntry = currentLegEntry.flatMap(legEntry -> getCurrentLegRoundOrCreate(match, legEntry.leg()));

        // Determine whose turn it is from the current leg starter and scores already present in the round.
        Optional<ObjectId> currentThrower = currentLegEntry.flatMap(legEntry ->
                currentRoundEntry.map(roundEntry -> legRoundService.getCurrentThrowerInRound(
                        roundEntry.round(),
                        legEntry.leg().getThrowsFirst(),
                        match.getPlayers()
                ))
        );

        // Replace the stored progress with the newly resolved match position.
        match.setMatchProgress(new X01MatchProgress(
                currentSetEntry.map(X01SetEntry::setNumber).orElse(null),
                currentLegEntry.map(X01LegEntry::legNumber).orElse(null),
                currentRoundEntry.map(X01LegRoundEntry::roundNumber).orElse(null),
                currentThrower.orElse(null)
        ));
    }

    /**
     * Creates and adds the next available set without exceeding the configured maximum.
     *
     * @param match the match to add the set to
     * @return the created set, or empty when no further set can be played
     */
    private Optional<X01SetEntry> createNextSet(X01Match match) {
        Set<Integer> existingSetNumbers = getSetNumbers(match);

        // Determine the next available set number within the configured match limit.
        X01BestOf bestOf = match.getMatchSettings().getBestOf();
        int maxSets = rulesService.getMaxToPlay(bestOf.getSets(), bestOf.getClearByTwoSetsRule());
        int nextSetNumber = NumberUtils.findNextNumber(existingSetNumbers, maxSets);
        if (nextSetNumber == -1) return Optional.empty();

        // Create the set and add it to the ordered match history.
        X01SetEntry newSetEntry = setService.createNewSet(nextSetNumber, match.getPlayers());
        match.getSets().put(newSetEntry.setNumber(), newSetEntry.set());

        return Optional.of(newSetEntry);
    }

    /**
     * Gets the set numbers currently present in the match.
     *
     * @param match the match containing the sets
     * @return the existing set numbers
     */
    private Set<Integer> getSetNumbers(X01Match match) {
        return Set.copyOf(match.getSets().keySet());
    }

    /**
     * Determines whether every player has received a match result.
     *
     * @param match the match to check
     * @return true when the match is concluded
     */
    private boolean isMatchConcluded(X01Match match) {
        return match.getPlayers().stream()
                .allMatch(player -> player.getResultType() != null);
    }
}