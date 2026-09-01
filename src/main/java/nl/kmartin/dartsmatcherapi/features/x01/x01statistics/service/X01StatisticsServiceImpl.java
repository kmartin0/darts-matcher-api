package nl.kmartin.dartsmatcherapi.features.x01.x01statistics.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01averagestatistics.model.X01AverageStatistics;
import nl.kmartin.dartsmatcherapi.features.x01.x01averagestatistics.service.IX01AverageStatisticsService;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkoutstatistics.model.X01CheckoutStatistics;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkoutstatistics.service.IX01CheckoutStatisticsService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.service.IX01LegService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRound;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01resultstatistics.service.IX01ResultStatisticsService;
import nl.kmartin.dartsmatcherapi.features.x01.x01scorestatistics.service.IX01ScoreStatisticsService;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01Set;
import nl.kmartin.dartsmatcherapi.features.x01.x01statistics.model.X01Statistics;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orchestrates the calculation of statistics for all players in an X01 match.
 *
 * Rebuilds player statistics from the match history by processing sets, legs, rounds
 * and individual player scores through the specialized statistics services.
 */
@Service
public class X01StatisticsServiceImpl implements IX01StatisticsService {
    private final IX01ResultStatisticsService resultStatisticsService;
    private final IX01ScoreStatisticsService scoreStatisticsService;
    private final IX01CheckoutStatisticsService checkoutStatisticsService;
    private final IX01AverageStatisticsService averageStatisticsService;
    private final IX01LegService legService;

    public X01StatisticsServiceImpl(
            IX01ResultStatisticsService resultStatisticsService,
            IX01ScoreStatisticsService scoreStatisticsService,
            IX01CheckoutStatisticsService checkoutStatisticsService,
            IX01AverageStatisticsService averageStatisticsService,
            IX01LegService legService
    ) {
        this.resultStatisticsService = resultStatisticsService;
        this.scoreStatisticsService = scoreStatisticsService;
        this.checkoutStatisticsService = checkoutStatisticsService;
        this.averageStatisticsService = averageStatisticsService;
        this.legService = legService;
    }

    /**
     * Recalculates the statistics for all players from the complete match history.
     *
     * Existing statistics are reset before all sets, legs, rounds and scores are processed again.
     *
     * @param match the match for which player statistics should be recalculated
     */
    @Override
    public void updatePlayerStatistics(X01Match match) {
        if (match == null) return;

        // Reset all player statistics before rebuilding them from the match history.
        resetPlayerStatistics(match.getPlayers());

        // Map players by ID so scores can quickly be associated with their player.
        Map<ObjectId, X01MatchPlayer> playersMap = match.getPlayers()
                .stream()
                .collect(Collectors.toMap(X01MatchPlayer::getPlayerId, Function.identity()));

        // Rebuild statistics by processing the match from sets down to individual scores.
        processSets(match.getSets(), match.getMatchSettings().isTrackDoubles(), playersMap);
    }

    /**
     * Processes the sets and updates set and leg statistics.
     *
     * @param sets         the match sets
     * @param trackDoubles whether missed doubles should be tracked
     * @param playersMap   the players mapped by player ID
     */
    private void processSets(NavigableMap<Integer, X01Set> sets, boolean trackDoubles, Map<ObjectId, X01MatchPlayer> playersMap) {
        if (sets == null) return;

        sets.values().forEach(set -> {
            resultStatisticsService.updateSetsWonStatistics(set, playersMap);
            processLegs(set.getLegs(), trackDoubles, playersMap);
        });
    }

    /**
     * Processes the legs and updates leg and round statistics.
     *
     * @param legs         the set legs
     * @param trackDoubles whether missed doubles should be tracked
     * @param playersMap   the players mapped by player ID
     */
    private void processLegs(
            NavigableMap<Integer, X01Leg> legs,
            boolean trackDoubles,
            Map<ObjectId, X01MatchPlayer> playersMap
    ) {
        if (legs == null) return;

        legs.values().forEach(leg -> {
            resultStatisticsService.updateLegsWonStatistics(leg, playersMap);
            processLegRounds(leg.getRounds(), leg, trackDoubles, playersMap);
        });
    }

    /**
     * Processes all rounds belonging to a leg.
     *
     * @param rounds       the leg rounds
     * @param leg          the leg containing the rounds
     * @param trackDoubles whether missed doubles should be tracked
     * @param playersMap   the players mapped by player ID
     */
    private void processLegRounds(
            NavigableMap<Integer, X01LegRound> rounds,
            X01Leg leg,
            boolean trackDoubles,
            Map<ObjectId, X01MatchPlayer> playersMap
    ) {
        if (rounds == null) return;

        rounds.entrySet()
                .stream()
                .map(X01LegRoundEntry::new)
                .forEach(roundEntry ->
                        processRoundScores(
                                roundEntry.round().getScores(),
                                leg,
                                roundEntry,
                                trackDoubles,
                                playersMap
                        )
                );
    }

    /**
     * Processes all player scores belonging to a round.
     *
     * @param roundScores   the player scores in the round
     * @param leg           the leg containing the round
     * @param legRoundEntry the round entry
     * @param trackDoubles  whether missed doubles should be tracked
     * @param playersMap    the players mapped by player ID
     */
    private void processRoundScores(
            Map<ObjectId, X01LegRoundScore> roundScores,
            X01Leg leg,
            X01LegRoundEntry legRoundEntry,
            boolean trackDoubles,
            Map<ObjectId, X01MatchPlayer> playersMap
    ) {
        if (roundScores == null) return;

        // Process statistics for each player that recorded a score in the round.
        roundScores.forEach((playerId, roundScore) -> {
            X01MatchPlayer player = playersMap.get(playerId);

            if (player != null) {
                processPlayerScore(player, leg, legRoundEntry, roundScore, trackDoubles);
            }
        });
    }

    /**
     * Updates all statistics affected by a player's round score.
     *
     * @param player        the player that scored
     * @param leg           the leg containing the score
     * @param legRoundEntry the round containing the score
     * @param playerScore   the player's round score
     * @param trackDoubles  whether missed doubles should be tracked
     */
    private void processPlayerScore(
            X01MatchPlayer player,
            X01Leg leg,
            X01LegRoundEntry legRoundEntry,
            X01LegRoundScore playerScore,
            boolean trackDoubles
    ) {
        X01Statistics playerStats = player.getStatistics();

        boolean isScoreCheckout = legService.isPlayerCheckoutRound(
                leg,
                legRoundEntry.roundNumber(),
                player.getPlayerId()
        );

        // Update the score-range statistics.
        scoreStatisticsService.updateScoreStatistics(playerStats.getScoreStatistics(), playerScore);

        // Update checkout statistics using the checkout and double-tracking state.
        X01CheckoutStatistics checkoutStats = playerStats.getCheckoutStats();
        checkoutStatisticsService.updateCheckoutStatistics(
                checkoutStats,
                playerScore,
                isScoreCheckout,
                trackDoubles
        );

        // Update overall and first-nine averages.
        X01AverageStatistics averageStats = playerStats.getAverageStats();
        Integer checkoutDartsUsed = isScoreCheckout ? leg.getCheckoutDartsUsed() : null;

        averageStatisticsService.updateAverageStats(
                averageStats,
                playerScore,
                legRoundEntry.roundNumber(),
                checkoutDartsUsed
        );
    }

    /**
     * Resets the statistics for all match players.
     *
     * Players without an existing statistics object are initialized with a new one.
     *
     * @param matchPlayers the players whose statistics should be reset
     */
    private void resetPlayerStatistics(List<X01MatchPlayer> matchPlayers) {
        matchPlayers.forEach(matchPlayer -> {
            if (matchPlayer.getStatistics() == null) {
                matchPlayer.setStatistics(new X01Statistics());
                return;
            }

            matchPlayer.getStatistics().reset();
        });
    }
}