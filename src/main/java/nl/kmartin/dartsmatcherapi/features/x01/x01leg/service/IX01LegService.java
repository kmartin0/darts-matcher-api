package nl.kmartin.dartsmatcherapi.features.x01.x01leg.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Turn;
import org.bson.types.ObjectId;

import java.util.List;

public interface IX01LegService {

    X01LegEntry createNewLeg(
            @Positive int legNumber,
            @NotNull ObjectId throwsFirstInSet,
            @NotEmpty List<@NotNull @Valid X01MatchPlayer> players
    );

    void applyTurn(
            @Positive int x01,
            @NotNull @Valid X01Leg leg,
            @Positive int roundNumber,
            @NotNull @Valid X01Turn turn,
            @NotNull ObjectId throwerId,
            boolean trackDoubles
    );

    boolean isPlayerCheckoutRound(
            @NotNull @Valid X01Leg leg,
            int roundNumber,
            @NotNull ObjectId playerId
    );
}