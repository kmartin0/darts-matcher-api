package nl.kmartin.dartsmatcherapi.features.x01.x01resultstatistics.service;

import nl.kmartin.dartsmatcherapi.features.basematch.model.ResultType;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01Set;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * Updates X01 result statistics from processed sets and legs.
 */
@Service
@Validated
public class X01ResultStatisticsServiceImpl implements IX01ResultStatisticsService {

    @Override
    public void updateSetsWonStatistics(X01Set set, Map<ObjectId, X01MatchPlayer> playersMap) {
        // Ignore sets that do not have a result yet.
        if (set.getResult() == null) return;

        set.getResult().forEach((playerId, resultType) -> {
            // A set win is awarded to players that won or drew the set.
            if (resultType != ResultType.WIN && resultType != ResultType.DRAW) {
                return;
            }

            // Ignore result entries that cannot be associated with a match player.
            X01MatchPlayer player = playersMap.get(playerId);
            if (player == null) {
                return;
            }

            // Accumulate the set win on the player's result statistics.
            player.getStatistics()
                    .getResultStatistics()
                    .incrementSetsWon();
        });
    }

    @Override
    public void updateLegsWonStatistics(X01Leg leg, Map<ObjectId, X01MatchPlayer> playersMap) {
        // Ignore legs that do not have a winner yet.
        if (leg.getWinner() == null) return;

        // Resolve the winning player from the match players.
        X01MatchPlayer winner = playersMap.get(leg.getWinner());
        if (winner == null) {
            return;
        }

        // Accumulate the leg win on the player's result statistics.
        winner.getStatistics()
                .getResultStatistics()
                .incrementLegsWon();
    }
}