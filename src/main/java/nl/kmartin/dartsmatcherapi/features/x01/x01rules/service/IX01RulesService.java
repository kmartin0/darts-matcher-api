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
     * Determines whether the current leader can be confirmed as the winner.
     *
     * @param diff            the score difference between the leader and runner-up
     * @param bestOfRemaining the number remaining to be played
     * @param played          the number already played
     * @param bestOf          the best-of setting
     * @param clearByTwoRule  the clear-by-two rule
     * @return whether the winner can be confirmed
     */
    boolean isWinnerConfirmed(
            @PositiveOrZero int diff,
            @PositiveOrZero int bestOfRemaining,
            @PositiveOrZero int played,
            @Positive int bestOf,
            @NotNull @Valid X01ClearByTwoRule clearByTwoRule
    );
}