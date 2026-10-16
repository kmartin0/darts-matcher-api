package nl.kmartin.dartsmatcher.api.features.x01.x01statistics.service;

import nl.kmartin.dartsmatcher.api.features.x01.x01averagestatistics.model.X01AverageStatistics;
import nl.kmartin.dartsmatcher.api.features.x01.x01averagestatistics.service.IX01AverageStatisticsService;
import nl.kmartin.dartsmatcher.api.features.x01.x01checkoutstatistics.model.X01CheckoutStatistics;
import nl.kmartin.dartsmatcher.api.features.x01.x01checkoutstatistics.service.IX01CheckoutStatisticsService;
import nl.kmartin.dartsmatcher.api.features.x01.x01doubleinstatistics.model.X01DoubleInStatistics;
import nl.kmartin.dartsmatcher.api.features.x01.x01doubleinstatistics.service.IX01DoubleInStatisticsService;
import nl.kmartin.dartsmatcher.api.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcher.api.features.x01.x01leg.service.IX01LegService;
import nl.kmartin.dartsmatcher.api.features.x01.x01leground.model.X01LegRound;
import nl.kmartin.dartsmatcher.api.features.x01.x01leground.model.X01LegRoundEntry;
import nl.kmartin.dartsmatcher.api.features.x01.x01leground.model.X01Turn;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01MatchFormat;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01MatchSettings;
import nl.kmartin.dartsmatcher.api.features.x01.x01resultstatistics.service.IX01ResultStatisticsService;
import nl.kmartin.dartsmatcher.api.features.x01.x01scorestatistics.service.IX01ScoreStatisticsService;
import nl.kmartin.dartsmatcher.api.features.x01.x01set.model.X01Set;
import nl.kmartin.dartsmatcher.api.features.x01.x01statistics.model.X01Statistics;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orchestrates the calculation of statistics for all players in an X01 match.
 *
 * Rebuilds player statistics from the match history by processing sets, legs, rounds
 * and individual player turns through the specialized statistics services.
 */
@Service
@Validated
public class X01StatisticsServiceImpl implements IX01StatisticsService {

    private final IX01ResultStatisticsService resultStatisticsService;
    private final IX01ScoreStatisticsService scoreStatisticsService;
    private final IX01CheckoutStatisticsService checkoutStatisticsService;
    private final IX01AverageStatisticsService averageStatisticsService;
    private final IX01DoubleInStatisticsService doubleInStatisticsService;
    private final IX01LegService legService;

    public X01StatisticsServiceImpl(
            IX01ResultStatisticsService resultStatisticsService,
            IX01ScoreStatisticsService scoreStatisticsService,
            IX01CheckoutStatisticsService checkoutStatisticsService,
            IX01AverageStatisticsService averageStatisticsService,
            IX01DoubleInStatisticsService doubleInStatisticsService,
            IX01LegService legService
    ) {
        this.resultStatisticsService = resultStatisticsService;
        this.scoreStatisticsService = scoreStatisticsService;
        this.checkoutStatisticsService = checkoutStatisticsService;
        this.averageStatisticsService = averageStatisticsService;
        this.doubleInStatisticsService = doubleInStatisticsService;
        this.legService = legService;
    }

    @Override
    public void updatePlayerStatistics(X01Match match) {
        X01MatchSettings matchSettings = match.getMatchSettings();

        // Reset all accumulated statistics before rebuilding them from the match history.
        resetPlayerStatistics(match.getPlayers(), matchSettings.getFormat());

        // Map players by ID so recorded turns can be associated with their player.
        Map<ObjectId, X01MatchPlayer> playersMap = match.getPlayers()
                .stream()
                .collect(Collectors.toMap(X01MatchPlayer::getPlayerId, Function.identity()));

        // Process the complete match hierarchy from sets down to individual player turns.
        processSets(match.getSets(), matchSettings, playersMap);
    }

    /**
     * Processes the sets and updates set and leg statistics.
     *
     * @param sets          the match sets
     * @param matchSettings the match settings defining the starting score, double tracking and format
     * @param playersMap    the players mapped by player ID
     */
    private void processSets(
            NavigableMap<Integer, X01Set> sets,
            X01MatchSettings matchSettings,
            Map<ObjectId, X01MatchPlayer> playersMap
    ) {
        sets.values().forEach(set -> {
            resultStatisticsService.updateSetsWonStatistics(set, playersMap);
            processLegs(set.getLegs(), matchSettings, playersMap);
        });
    }

    /**
     * Processes the legs and updates leg and round statistics.
     *
     * @param legs          the set legs
     * @param matchSettings the match settings defining the starting score, double tracking and format
     * @param playersMap    the players mapped by player ID
     */
    private void processLegs(
            NavigableMap<Integer, X01Leg> legs,
            X01MatchSettings matchSettings,
            Map<ObjectId, X01MatchPlayer> playersMap
    ) {
        legs.values().forEach(leg -> {
            resultStatisticsService.updateLegsWonStatistics(leg, playersMap);
            processLegRounds(leg.getRounds(), leg, matchSettings, playersMap);
        });
    }

    /**
     * Processes all rounds belonging to a leg.
     *
     * @param rounds        the leg rounds
     * @param leg           the leg containing the rounds
     * @param matchSettings the match settings defining the starting score, double tracking and format
     * @param playersMap    the players mapped by player ID
     */
    private void processLegRounds(
            NavigableMap<Integer, X01LegRound> rounds,
            X01Leg leg,
            X01MatchSettings matchSettings,
            Map<ObjectId, X01MatchPlayer> playersMap
    ) {
        rounds.entrySet()
                .stream()
                .map(X01LegRoundEntry::new)
                .forEach(roundEntry ->
                        processRoundTurns(
                                roundEntry.round().getTurns(),
                                leg,
                                roundEntry,
                                matchSettings,
                                playersMap
                        )
                );
    }

    /**
     * Processes all player turns belonging to a round.
     *
     * @param roundTurns    the player turns in the round
     * @param leg           the leg containing the round
     * @param legRoundEntry the round entry
     * @param matchSettings the match settings defining the starting score, double tracking and format
     * @param playersMap    the players mapped by player ID
     */
    private void processRoundTurns(
            Map<ObjectId, X01Turn> roundTurns,
            X01Leg leg,
            X01LegRoundEntry legRoundEntry,
            X01MatchSettings matchSettings,
            Map<ObjectId, X01MatchPlayer> playersMap
    ) {
        // Process statistics only for turns that can be associated with a match player.
        roundTurns.forEach((playerId, turn) -> {
            X01MatchPlayer player = playersMap.get(playerId);

            if (player != null) {
                processPlayerTurn(player, leg, legRoundEntry, turn, matchSettings);
            }
        });
    }

    /**
     * Updates all statistics affected by a player's turn.
     *
     * @param player        the player that threw the turn
     * @param leg           the leg containing the turn
     * @param legRoundEntry the round containing the turn
     * @param playerTurn    the player's turn
     * @param matchSettings the match settings defining the starting score, double tracking and format
     */
    private void processPlayerTurn(
            X01MatchPlayer player,
            X01Leg leg,
            X01LegRoundEntry legRoundEntry,
            X01Turn playerTurn,
            X01MatchSettings matchSettings
    ) {
        X01Statistics playerStats = player.getStatistics();
        boolean isCheckoutTurn = legService.isPlayerCheckoutRound(leg, legRoundEntry.roundNumber(), player.getPlayerId());

        updateScoreStatistics(playerStats, playerTurn);
        updateCheckoutStatistics(playerStats, playerTurn, isCheckoutTurn, matchSettings.isTrackDoubles());
        updateDoubleInStatistics(playerStats, playerTurn, matchSettings);
        updateAverageStatistics(playerStats, playerTurn, leg, legRoundEntry.roundNumber(), isCheckoutTurn);
    }

    /**
     * Updates the score-range statistics from a processed turn.
     *
     * @param playerStats the player's statistics
     * @param playerTurn  the processed turn
     */
    private void updateScoreStatistics(X01Statistics playerStats, X01Turn playerTurn) {
        scoreStatisticsService.updateScoreStatistics(playerStats.getScoreStatistics(), playerTurn);
    }

    /**
     * Updates successful and missed checkout statistics.
     *
     * @param playerStats    the player's statistics
     * @param playerTurn     the processed turn
     * @param isCheckoutTurn whether this turn completed the player's leg
     * @param trackDoubles   whether missed doubles should be tracked
     */
    private void updateCheckoutStatistics(
            X01Statistics playerStats,
            X01Turn playerTurn,
            boolean isCheckoutTurn,
            boolean trackDoubles
    ) {
        checkoutStatisticsService.updateCheckoutStatistics(
                playerStats.getCheckoutStats(),
                playerTurn,
                isCheckoutTurn,
                trackDoubles
        );
    }

    /**
     * Updates opening statistics for a processed turn in a double-in match.
     *
     * @param playerStats   the player's statistics
     * @param playerTurn    the processed turn
     * @param matchSettings the match settings defining the starting score, double tracking and format
     */
    private void updateDoubleInStatistics(
            X01Statistics playerStats,
            X01Turn playerTurn,
            X01MatchSettings matchSettings
    ) {
        if (matchSettings.getFormat() != X01MatchFormat.DIDO) {
            return;
        }

        // The first scoring turn starts with the full starting score remaining.
        boolean openedThisTurn = playerTurn.getScore() > 0
                && playerTurn.getRemaining() + playerTurn.getScore() == matchSettings.getX01();

        doubleInStatisticsService.updateDoubleInStatistics(
                playerStats.getDoubleInStats(),
                playerTurn,
                openedThisTurn,
                matchSettings.isTrackDoubles()
        );
    }

    /**
     * Updates overall and first-nine averages using the actual dart count for a checkout turn.
     *
     * @param playerStats    the player's statistics
     * @param playerTurn     the processed turn
     * @param leg            the leg containing the turn
     * @param roundNumber    the round number
     * @param isCheckoutTurn whether this turn completed the player's leg
     */
    private void updateAverageStatistics(
            X01Statistics playerStats,
            X01Turn playerTurn,
            X01Leg leg,
            int roundNumber,
            boolean isCheckoutTurn
    ) {
        Integer checkoutDartsUsed = isCheckoutTurn ? leg.getCheckoutDartsUsed() : null;

        averageStatisticsService.updateAverageStats(
                playerStats.getAverageStats(),
                playerTurn,
                roundNumber,
                checkoutDartsUsed
        );
    }

    /**
     * Resets the statistics for all match players.
     *
     * @param matchPlayers the players whose statistics should be reset
     * @param format       the match format
     */
    private void resetPlayerStatistics(List<X01MatchPlayer> matchPlayers, X01MatchFormat format) {
        matchPlayers.forEach(matchPlayer -> matchPlayer.getStatistics().reset(format));
    }
}