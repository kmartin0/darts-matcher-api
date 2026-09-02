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

    void updateSetsWonStatistics(
            @NotNull @Valid X01Set set,
            @NotEmpty Map<@NotNull ObjectId, @NotNull @Valid X01MatchPlayer> playersMap
    );

    void updateLegsWonStatistics(
            @NotNull @Valid X01Leg leg,
            @NotEmpty Map<@NotNull ObjectId, @NotNull @Valid X01MatchPlayer> playersMap
    );
}