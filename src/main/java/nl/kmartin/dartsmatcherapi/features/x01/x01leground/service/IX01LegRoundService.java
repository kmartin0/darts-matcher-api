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

    ObjectId getCurrentThrowerInRound(
            @NotNull @Valid X01LegRound legRound,
            @NotNull ObjectId throwsFirstInLeg,
            @NotEmpty List<@NotNull @Valid X01MatchPlayer> players
    );

    boolean removeLastScoreFromRound(@NotNull @Valid X01LegRound legRound);

    void removeScoresAfterWinner(@NotNull @Valid X01LegRound round, @NotNull ObjectId legWinner);

    boolean isRoundScoreLegal(@NotNull @Valid X01LegRoundScore roundScore, Integer checkoutDartsUsed);
}