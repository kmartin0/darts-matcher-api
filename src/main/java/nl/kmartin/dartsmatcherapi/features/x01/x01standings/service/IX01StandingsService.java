package nl.kmartin.dartsmatcherapi.features.x01.x01standings.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01ClearByTwoRule;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface IX01StandingsService {
    void updateMatchStandings(@NotNull @Valid X01Match match);

    List<ObjectId> determineWinners(
            @NotNull TreeMap<@PositiveOrZero Integer, @NotEmpty List<@NotNull ObjectId>> standings,
            @PositiveOrZero int played,
            @Positive int bestOf,
            @NotNull @Valid X01ClearByTwoRule clearByTwoRule
    );

    TreeMap<Integer, List<ObjectId>> groupByWinCounts(
            @NotNull Map<@NotNull ObjectId, @NotNull @PositiveOrZero Long> winsPerPlayer
    );
}