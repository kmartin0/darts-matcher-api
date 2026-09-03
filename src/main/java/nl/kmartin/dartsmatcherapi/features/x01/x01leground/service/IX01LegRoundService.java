package nl.kmartin.dartsmatcherapi.features.x01.x01leground.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRound;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import org.bson.types.ObjectId;

import java.util.List;

public interface IX01LegRoundService {

    /**
     * Determines which player should throw next in the round.
     *
     * @param legRound         the round to evaluate
     * @param throwsFirstInLeg the player that started the leg
     * @param players          the match players
     * @return the next player to throw, or null when no player remains
     */
    ObjectId getCurrentThrowerInRound(
            @NotNull @Valid X01LegRound legRound,
            @NotNull ObjectId throwsFirstInLeg,
            @NotEmpty List<@NotNull @Valid X01MatchPlayer> players
    );

    /**
     * Removes the most recently added score from a round.
     *
     * @param legRound the round from which to remove the score
     * @return whether a score was removed
     */
    boolean removeLastScoreFromRound(@NotNull @Valid X01LegRound legRound);

    /**
     * Removes scores recorded after the player that won the leg.
     *
     * @param round     the round to trim
     * @param legWinner the player that won the leg
     */
    void removeScoresAfterWinner(@NotNull @Valid X01LegRound round, @NotNull ObjectId legWinner);

    /**
     * Determines whether a round score represents a legal X01 state.
     *
     * @param roundScore        the round score to validate
     * @param checkoutDartsUsed the number of darts used for the checkout
     * @return whether the round score is legal
     */
    boolean isRoundScoreLegal(@NotNull @Valid X01LegRoundScore roundScore, Integer checkoutDartsUsed);
}