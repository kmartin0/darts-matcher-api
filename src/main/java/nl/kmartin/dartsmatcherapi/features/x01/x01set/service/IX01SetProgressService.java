package nl.kmartin.dartsmatcherapi.features.x01.x01set.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01BestOf;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01Set;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;

import java.util.List;
import java.util.Optional;

public interface IX01SetProgressService {
    X01LegEntry getLegOrThrow(@NotNull @Valid X01Set set, int legNumber);

    Optional<X01LegEntry> getCurrentLeg(@NotNull @Valid X01Set set);

    Optional<X01LegEntry> createNextLeg(
            @NotNull @Valid X01SetEntry setEntry,
            @NotEmpty List<@NotNull @Valid X01MatchPlayer> players,
            @NotNull @Valid X01BestOf bestOf
    );

    boolean isSetConcluded(@NotNull @Valid X01Set set);

    boolean removeLastScoreFromSet(@NotNull @Valid X01Set set);
}