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
 * Updates result statistics for X01 players based on completed sets and legs.
 */
@Service
@Validated
public class X01ResultStatisticsServiceImpl implements IX01ResultStatisticsService {

    /**
     * Updates the sets won statistic for players that won or drew the set.
     *
     * @param set        the completed set
     * @param playersMap the players mapped by player ID
     */
    @Override
    public void updateSetsWonStatistics(X01Set set, Map<ObjectId, X01MatchPlayer> playersMap) {
        if (set.getResult() == null) return;

        set.getResult().forEach((playerId, resultType) -> {
            if (resultType != ResultType.WIN && resultType != ResultType.DRAW) {
                return;
            }

            X01MatchPlayer player = playersMap.get(playerId);
            if (player == null) {
                return;
            }

            player.getStatistics()
                    .getResultStatistics()
                    .incrementSetsWon();
        });
    }

    /**
     * Updates the legs won statistic for the player that won the leg.
     *
     * @param leg        the completed leg
     * @param playersMap the players mapped by player ID
     */
    @Override
    public void updateLegsWonStatistics(X01Leg leg, Map<ObjectId, X01MatchPlayer> playersMap) {
        if (leg.getWinner() == null) return;

        X01MatchPlayer winner = playersMap.get(leg.getWinner());
        if (winner == null) {
            return;
        }

        winner.getStatistics()
                .getResultStatistics()
                .incrementLegsWon();
    }
}