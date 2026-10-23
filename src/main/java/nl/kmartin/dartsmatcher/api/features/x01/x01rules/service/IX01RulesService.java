package nl.kmartin.dartsmatcher.api.features.x01.x01rules.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01ClearByTwoRule;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.TreeMap;

public interface IX01RulesService {

    /**
     * Calculates the maximum number that can be played.
     *
     * @param bestOf         the best-of setting
     * @param clearByTwoRule the clear-by-two rule
     * @return the maximum number that can be played
     */
    int getMaxToPlay(@Positive int bestOf, @NotNull @Valid X01ClearByTwoRule clearByTwoRule);

    /**
     * Determines whether the standings represent a single-player match.
     *
     * @param standings     the current standings
     * @param leaderScore   the highest score
     * @param runnerUpScore the second-highest score, or null when there is no runner-up
     * @return whether the match contains exactly one player
     */
    boolean isSinglePlayerMatch(
            @NotNull TreeMap<Integer, List<ObjectId>> standings,
            int leaderScore,
            Integer runnerUpScore
    );

    /**
     * Determines whether the leaders in the standings are confirmed winners.
     *
     * Calculates the lead and remaining number of legs or sets from the standings
     * and match progress. Tied leaders have a lead of zero. Single-player matches
     * conclude once the configured best-of has been reached.
     *
     * @param standings      players grouped by their number of wins
     * @param played         the number of legs or sets already played
     * @param bestOf         the configured best-of value
     * @param clearByTwoRule the clear-by-two rule to apply
     * @return whether the result is confirmed, or false when the standings are empty
     */
    boolean isWinnerConfirmed(
            @NotNull TreeMap<@PositiveOrZero Integer, @NotEmpty List<@NotNull ObjectId>> standings,
            @PositiveOrZero int played,
            @Positive int bestOf,
            @NotNull @Valid X01ClearByTwoRule clearByTwoRule
    );
}