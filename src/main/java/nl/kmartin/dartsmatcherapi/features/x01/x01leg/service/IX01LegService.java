package nl.kmartin.dartsmatcherapi.features.x01.x01leg.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01CheckoutInsufficientDartsException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegAlreadyWonException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01TurnAlreadyExistsException;
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
     * Applies a new player's turn to a leg round.
     *
     * Processes the submitted turn according to X01 rules, adds it to the round
     * and rebuilds the resulting leg state.
     *
     * @param turnMutation the turn mutation to apply
     * @throws X01CheckoutInsufficientDartsException when the checkout requires more darts than were used
     * @throws X01LegAlreadyWonException             when another player's turn is applied after the leg has been won
     * @throws X01TurnAlreadyExistsException         when the player already has a turn in the target round
     * @throws ResourceNotFoundException             when the target round cannot be found
     */
    void applyTurn(@NotNull @Valid X01TurnMutation turnMutation);

    /**
     * Replaces an existing player's turn in a leg round.
     *
     * Processes the replacement according to X01 rules, ensures following turns
     * remain valid and rebuilds the resulting leg state.
     *
     * @param turnMutation the turn mutation to apply as a replacement
     * @throws X01CheckoutInsufficientDartsException when the checkout requires more darts than were used
     * @throws X01LegAlreadyWonException             when another player's turn is replaced after the leg has been won
     * @throws ResourceNotFoundException             when the target round or player's existing turn cannot be found
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