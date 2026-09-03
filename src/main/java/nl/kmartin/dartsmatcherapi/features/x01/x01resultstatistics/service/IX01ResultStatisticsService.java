package nl.kmartin.dartsmatcherapi.features.x01.x01resultstatistics.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01Set;
import org.bson.types.ObjectId;

import java.util.Map;

public interface IX01ResultStatisticsService {

    /**
     * Updates set-win statistics from a completed set.
     *
     * Players with a win or draw result receive one set win.
     *
     * @param set        the set to process
     * @param playersMap the players mapped by player ID
     */
    void updateSetsWonStatistics(
            @NotNull @Valid X01Set set,
            @NotEmpty Map<@NotNull ObjectId, @NotNull @Valid X01MatchPlayer> playersMap
    );

    /**
     * Updates leg-win statistics from a completed leg.
     *
     * @param leg        the leg to process
     * @param playersMap the players mapped by player ID
     */
    void updateLegsWonStatistics(
            @NotNull @Valid X01Leg leg,
            @NotEmpty Map<@NotNull ObjectId, @NotNull @Valid X01MatchPlayer> playersMap
    );
}