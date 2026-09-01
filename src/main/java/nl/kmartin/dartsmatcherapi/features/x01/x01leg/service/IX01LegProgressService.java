package nl.kmartin.dartsmatcherapi.features.x01.x01leg.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;

import java.util.List;
import java.util.Optional;


public interface IX01LegProgressService {
    X01LegRoundEntry getLegRoundOrThrow(X01Leg leg, int roundNumber);

    Optional<X01LegRoundEntry> getCurrentLegRound(X01Leg leg, List<X01MatchPlayer> players);

    Optional<X01LegRoundEntry> createNextLegRound(X01Leg leg);

    boolean isLegConcluded(X01Leg leg);

    boolean removeLastScoreFromLeg(X01Leg leg);
}
