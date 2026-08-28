package nl.kmartin.dartsmatcherapi.features.x01.x01match.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;

import java.util.Optional;
import java.util.Set;

public interface IX01MatchProgressService {
    Optional<X01SetEntry> getSet(X01Match match, int setNumber, boolean throwIfNotFound);

    Optional<X01SetEntry> getCurrentSet(X01Match match);

    Optional<X01SetEntry> createNextSet(X01Match match);

    Set<Integer> getSetNumbers(X01Match match);

    Optional<X01SetEntry> getCurrentSetOrCreate(X01Match match);

    Optional<X01LegEntry> getCurrentLegOrCreate(X01Match match, X01SetEntry currentSetEntry);

    Optional<X01LegRoundEntry> getCurrentLegRoundOrCreate(X01Match match, X01Leg currentLeg);

    boolean isMatchConcluded(X01Match match);

    void removeLastScoreFromMatch(X01Match match);

    void updateMatchProgress(X01Match match);
}
