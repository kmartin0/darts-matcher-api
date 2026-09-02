package nl.kmartin.dartsmatcherapi.features.x01.x01rules.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01ClearByTwoRule;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.TreeMap;

public interface IX01RulesService {

    int getMaxToPlay(@Positive int bestOf, @NotNull @Valid X01ClearByTwoRule clearByTwoRule);

    boolean isSinglePlayerMatch(
            @NotNull TreeMap<Integer, List<ObjectId>> standings,
            int leaderScore,
            Integer runnerUpScore
    );

    boolean isWinnerConfirmed(
            @PositiveOrZero int diff,
            @PositiveOrZero int bestOfRemaining,
            @PositiveOrZero int played,
            @Positive int bestOf,
            @NotNull @Valid X01ClearByTwoRule clearByTwoRule
    );
}