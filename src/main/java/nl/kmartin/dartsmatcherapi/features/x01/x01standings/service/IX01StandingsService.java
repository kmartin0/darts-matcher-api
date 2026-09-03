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

    /**
     * Rebuilds the standings from the processed match history.
     *
     * @param match the match whose standings should be rebuilt
     */
    void updateMatchStandings(@NotNull @Valid X01Match match);

    /**
     * Determines whether the leaders in the standings are confirmed winners.
     *
     * @param standings      players grouped by their number of wins
     * @param played         the number of legs or sets already played
     * @param bestOf         the configured best-of value
     * @param clearByTwoRule the clear-by-two rule to apply
     * @return the confirmed winners, or an empty list when the result is not yet decided
     */
    List<ObjectId> determineWinners(
            @NotNull TreeMap<@PositiveOrZero Integer, @NotEmpty List<@NotNull ObjectId>> standings,
            @PositiveOrZero int played,
            @Positive int bestOf,
            @NotNull @Valid X01ClearByTwoRule clearByTwoRule
    );

    /**
     * Groups players by their number of wins.
     *
     * @param winsPerPlayer the number of wins for each player
     * @return a sorted map keyed by win count with the players sharing that count
     */
    TreeMap<Integer, List<ObjectId>> groupByWinCounts(
            @NotNull Map<@NotNull ObjectId, @NotNull @PositiveOrZero Long> winsPerPlayer
    );
}