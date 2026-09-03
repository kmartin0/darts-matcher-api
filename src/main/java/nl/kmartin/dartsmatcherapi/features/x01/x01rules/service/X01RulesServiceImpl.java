package nl.kmartin.dartsmatcherapi.features.x01.x01rules.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01ClearByTwoRule;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.TreeMap;

/**
 * Applies the rules used to determine progression and winners in X01 matches.
 *
 * Handles standard best-of limits together with optional clear-by-two rules.
 */
@Service
@Validated
public class X01RulesServiceImpl implements IX01RulesService {

    @Override
    public int getMaxToPlay(int bestOf, X01ClearByTwoRule clearByTwoRule) {
        return clearByTwoRule.isEnabled() ? bestOf + clearByTwoRule.getLimit() : bestOf;
    }

    @Override
    public boolean isSinglePlayerMatch(
            TreeMap<Integer, List<ObjectId>> standings,
            int leaderScore,
            Integer runnerUpScore
    ) {
        List<ObjectId> leaders = standings.get(leaderScore);

        // A single-player match has one leader and no runner-up.
        return runnerUpScore == null
                && leaders != null
                && leaders.size() == 1;
    }

    @Override
    public boolean isWinnerConfirmed(
            int diff,
            int bestOfRemaining,
            int played,
            int bestOf,
            X01ClearByTwoRule clearByTwoRule
    ) {
        // The leader must be impossible to catch and satisfy the configured clear-by-two rule.
        return winnerCannotBeCaught(diff, bestOfRemaining)
                && isClearByTwoSatisfied(diff, played, bestOf, clearByTwoRule);
    }

    /**
     * Determines whether the leader can no longer be caught by the runner-up.
     *
     * @param diff            the score difference between the leader and runner-up
     * @param bestOfRemaining the number remaining to be played
     * @return whether the leader can no longer be caught
     */
    private boolean winnerCannotBeCaught(int diff, int bestOfRemaining) {
        return bestOfRemaining == 0 || diff > bestOfRemaining;
    }

    /**
     * Determines whether the clear-by-two rule has been satisfied.
     *
     * The rule is satisfied when it is disabled, the leader is ahead by at least two,
     * or the maximum number allowed by the clear-by-two limit has been reached.
     *
     * @param diff           the score difference between the leader and runner-up
     * @param played         the number already played
     * @param bestOf         the best-of setting
     * @param clearByTwoRule the clear-by-two rule
     * @return whether the clear-by-two rule is satisfied
     */
    private boolean isClearByTwoSatisfied(
            int diff,
            int played,
            int bestOf,
            X01ClearByTwoRule clearByTwoRule
    ) {
        // When clear by two is disabled, the rule is always satisfied.
        if (!clearByTwoRule.isEnabled()) return true;

        // A lead of two or more satisfies the clear-by-two requirement.
        if (diff >= 2) return true;

        // The match must also end when the configured maximum is reached.
        int maxToPlay = getMaxToPlay(bestOf, clearByTwoRule);
        return played >= maxToPlay;
    }
}