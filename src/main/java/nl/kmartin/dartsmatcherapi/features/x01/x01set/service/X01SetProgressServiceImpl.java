package nl.kmartin.dartsmatcherapi.features.x01.x01set.service;

import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.service.IX01LegProgressService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.service.IX01LegService;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01BestOf;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01rules.service.IX01RulesService;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01Set;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;
import nl.kmartin.dartsmatcherapi.util.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Provides operations for navigating and maintaining leg progression within an X01 set.
 */
@Service
@Validated
public class X01SetProgressServiceImpl implements IX01SetProgressService {

    private final IX01LegService legService;
    private final IX01LegProgressService legProgressService;
    private final IX01RulesService rulesService;

    public X01SetProgressServiceImpl(IX01LegService legService, IX01LegProgressService legProgressService, IX01RulesService rulesService) {
        this.legService = legService;
        this.legProgressService = legProgressService;
        this.rulesService = rulesService;
    }

    @Override
    public X01LegEntry getLegOrThrow(X01Set set, int legNumber) {
        return Optional.ofNullable(set.getLegs().get(legNumber)).map(leg -> new X01LegEntry(legNumber, leg)).orElseThrow(() -> new ResourceNotFoundException(X01Leg.class, legNumber));
    }

    @Override
    public Optional<X01LegEntry> getCurrentLeg(X01Set set) {
        // Legs are ordered by number, so the first leg without a winner is the current leg.
        return set.getLegs().entrySet().stream().filter(entry -> entry.getValue().getWinner() == null).findFirst().map(X01LegEntry::new);
    }

    @Override
    public Optional<X01LegEntry> createNextLeg(X01SetEntry setEntry, List<X01MatchPlayer> players, X01BestOf bestOf) {
        X01Set set = setEntry.set();

        // Determine which leg numbers are already present.
        Set<Integer> existingLegNumbers = getLegNumbers(set);

        // Determine the maximum number of legs allowed for this set.
        int maxLegs = rulesService.getMaxToPlay(bestOf.getLegs(), bestOf.getClearByTwoLegsRuleForSet(setEntry.setNumber()));

        // Find the next available leg number within the configured limit.
        int nextLegNumber = NumberUtils.findNextNumber(existingLegNumbers, maxLegs);

        if (nextLegNumber == -1) return Optional.empty();

        // Create the leg using the set's starting player and add it to the ordered leg map.
        X01LegEntry newLegEntry = legService.createNewLeg(nextLegNumber, set.getThrowsFirst(), players);
        set.getLegs().put(newLegEntry.legNumber(), newLegEntry.leg());

        return Optional.of(newLegEntry);
    }

    @Override
    public boolean isSetConcluded(X01Set set) {
        return set.getResult() != null;
    }

    @Override
    public boolean removeLastTurnFromSet(X01Set set) {
        if (set.getLegs().isEmpty()) return false;

        // Traverse legs from newest to oldest until a turn can be removed.
        Iterator<Integer> reverseLegsIterator = set.getLegs().descendingKeySet().iterator();

        while (reverseLegsIterator.hasNext()) {
            X01Leg leg = set.getLegs().get(reverseLegsIterator.next());
            boolean isTurnRemoved = legProgressService.removeLastTurnFromLeg(leg);

            // Remove an empty leg before continuing through earlier history.
            if (leg.getRounds().isEmpty()) {
                reverseLegsIterator.remove();
            }

            if (isTurnRemoved) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the leg numbers currently present in a set.
     *
     * @param set the set to evaluate
     * @return the leg numbers
     */
    private Set<Integer> getLegNumbers(X01Set set) {
        return Set.copyOf(set.getLegs().keySet());
    }
}