package nl.kmartin.dartsmatcherapi.features.x01.x01leg.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import org.bson.types.ObjectId;

public interface IX01LegResultService {

    /**
     * Rebuilds the result-related state of a leg.
     *
     * @param leg the leg to update
     * @param x01 the starting score for the leg
     */
    void updateLegResult(@NotNull @Valid X01Leg leg, @Positive int x01);

    /**
     * Gets the latest remaining score for a player.
     *
     * @param leg      the leg to evaluate
     * @param playerId the player ID
     * @param x01      the starting score for the leg
     * @return the latest remaining score, or the starting score when the player has not thrown
     */
    int getRemainingForPlayer(@NotNull @Valid X01Leg leg, @NotNull ObjectId playerId, @Positive int x01);

    /**
     * Recalculates the remaining score for a player across all rounds in a leg.
     *
     * @param leg      the leg to update
     * @param playerId the player whose remaining scores should be recalculated
     * @param x01      the starting score for the leg
     */
    void updateRemainingForPlayer(@NotNull @Valid X01Leg leg, @NotNull ObjectId playerId, @Positive int x01);

    /**
     * Calculates the number of darts used by a player in a leg.
     *
     * @param leg      the leg to evaluate
     * @param playerId the player ID
     * @return the total number of darts used
     */
    int calculateDartsUsed(@NotNull @Valid X01Leg leg, @NotNull ObjectId playerId);
}