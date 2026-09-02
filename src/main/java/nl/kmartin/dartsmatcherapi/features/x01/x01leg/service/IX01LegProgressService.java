package nl.kmartin.dartsmatcherapi.features.x01.x01leg.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;

import java.util.List;
import java.util.Optional;

public interface IX01LegProgressService {
    X01LegRoundEntry getLegRoundOrThrow(@NotNull @Valid X01Leg leg, int roundNumber);

    Optional<X01LegRoundEntry> getCurrentLegRound(
            @NotNull @Valid X01Leg leg,
            @NotEmpty List<@NotNull @Valid X01MatchPlayer> players
    );

    Optional<X01LegRoundEntry> createNextLegRound(@NotNull @Valid X01Leg leg);

    boolean isLegConcluded(@NotNull @Valid X01Leg leg);

    boolean removeLastScoreFromLeg(@NotNull @Valid X01Leg leg);
}