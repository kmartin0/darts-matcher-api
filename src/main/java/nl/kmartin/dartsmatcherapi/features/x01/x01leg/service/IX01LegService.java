package nl.kmartin.dartsmatcherapi.features.x01.x01leg.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01TurnMutation;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import org.bson.types.ObjectId;

import java.util.List;

public interface IX01LegService {

    /**
     * Creates a new numbered leg and determines which player throws first.
     *
     * @param legNumber        the leg number
     * @param throwsFirstInSet the player that started the set
     * @param players          the match players
     * @return the created leg entry
     */
    X01LegEntry createNewLeg(
            @Positive int legNumber,
            @NotNull ObjectId throwsFirstInSet,
            @NotEmpty List<@NotNull @Valid X01MatchPlayer> players
    );

    /**
     * Applies a new player's turn to a leg round and recalculates the affected leg state.
     *
     * @param turnMutation the turn mutation to apply
     */
    void applyTurn(@NotNull @Valid X01TurnMutation turnMutation);

    /**
     * Replaces an existing player's turn in a leg round and recalculates the affected leg state.
     *
     * @param turnMutation the turn mutation to apply as a replacement
     */
    void replaceTurn(@NotNull @Valid X01TurnMutation turnMutation);

    /**
     * Determines whether a player's turn belongs to the checkout round of a leg.
     *
     * @param leg         the leg to check
     * @param roundNumber the round number
     * @param playerId    the player ID
     * @return whether the round is the player's checkout round
     */
    boolean isPlayerCheckoutRound(
            @NotNull @Valid X01Leg leg,
            int roundNumber,
            @NotNull ObjectId playerId
    );
}