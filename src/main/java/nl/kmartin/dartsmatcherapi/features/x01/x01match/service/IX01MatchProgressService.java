package nl.kmartin.dartsmatcherapi.features.x01.x01match.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;

import java.util.Optional;

public interface IX01MatchProgressService {
    X01SetEntry getSetOrThrow(@NotNull @Valid X01Match match, int setNumber);

    Optional<X01SetEntry> getCurrentSet(@NotNull @Valid X01Match match);

    Optional<X01SetEntry> getCurrentSetOrCreate(@NotNull @Valid X01Match match);

    Optional<X01LegEntry> getCurrentLegOrCreate(
            @NotNull @Valid X01Match match,
            @NotNull @Valid X01SetEntry currentSetEntry
    );

    Optional<X01LegRoundEntry> getCurrentLegRoundOrCreate(
            @NotNull @Valid X01Match match,
            @NotNull @Valid X01Leg currentLeg
    );

    void removeLastScoreFromMatch(@NotNull @Valid X01Match match);

    void updateMatchProgress(@NotNull @Valid X01Match match);
}