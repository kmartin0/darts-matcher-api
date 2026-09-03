package nl.kmartin.dartsmatcherapi.features.x01.x01set.service;

import nl.kmartin.dartsmatcherapi.features.basematch.model.ResultType;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.service.IX01LegResultService;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01BestOf;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01ClearByTwoRule;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01Set;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01standings.service.IX01StandingsService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Rebuilds X01 set results from the processed leg history.
 *
 * Reprocesses leg results, removes stale history and determines the player results for the set.
 */
@Service
@Validated
public class X01SetResultServiceImpl implements IX01SetResultService {

    private final IX01LegResultService legResultService;
    private final IX01StandingsService standingsService;

    public X01SetResultServiceImpl(
            IX01LegResultService legResultService,
            IX01StandingsService standingsService
    ) {
        this.legResultService = legResultService;
        this.standingsService = standingsService;
    }

    @Override
    public void updateSetResult(
            X01SetEntry setEntry,
            X01BestOf bestOf,
            List<X01MatchPlayer> players,
            int x01
    ) {
        X01Set set = setEntry.set();

        // Rebuild leg results and find the current unfinished leg.
        Integer currentLegNumber = updateLegResults(set, x01);

        // History after the current leg could not have been played and is stale.
        if (currentLegNumber != null) {
            removeLegsAfter(set, currentLegNumber);
        }

        // Find the first leg at which the remaining history concludes the set.
        WinnerSearch winnerSearch = findSetWinners(setEntry, bestOf, players);

        // History after the first set-concluding leg is stale.
        if (winnerSearch.legNumber() != null) {
            removeLegsAfter(set, winnerSearch.legNumber());
        }

        // Apply the determined winners to the set result.
        updatePlayerResults(set, players, winnerSearch.winners());
    }

    /**
     * Rebuilds leg results chronologically and finds the current unfinished leg.
     *
     * @param set the set whose leg results should be rebuilt
     * @param x01 the starting X01 score
     * @return the current leg number in play, or null when all processed legs are concluded
     */
    private Integer updateLegResults(X01Set set, int x01) {
        // Reprocess legs chronologically until the current unfinished leg is reached.
        for (Map.Entry<Integer, X01Leg> legEntry : set.getLegs().entrySet()) {
            X01Leg leg = legEntry.getValue();
            legResultService.updateLegResult(leg, x01);

            if (leg.getWinner() == null) {
                return legEntry.getKey();
            }
        }

        return null;
    }

    /**
     * Finds the set winners and the first leg at which the set becomes concluded.
     *
     * @param setEntry the set to inspect
     * @param bestOf   the match format
     * @param players  the match players
     * @return the winner search result
     */
    private WinnerSearch findSetWinners(
            X01SetEntry setEntry,
            X01BestOf bestOf,
            List<X01MatchPlayer> players
    ) {
        X01Set set = setEntry.set();

        Map<ObjectId, Long> winsPerPlayer = createEmptyWinCountMap(players);
        X01ClearByTwoRule clearByTwoRule = bestOf.getClearByTwoLegsRuleForSet(setEntry.setNumber());
        int legsPlayed = 0;

        // Build the win counts chronologically until the rules determine that the set is concluded.
        for (Map.Entry<Integer, X01Leg> legEntry : set.getLegs().entrySet()) {
            X01Leg leg = legEntry.getValue();
            if (leg.getWinner() == null) break;

            winsPerPlayer.merge(leg.getWinner(), 1L, Long::sum);
            legsPlayed++;

            List<ObjectId> winners = determineSetWinners(
                    winsPerPlayer,
                    legsPlayed,
                    bestOf.getLegs(),
                    clearByTwoRule
            );

            if (!winners.isEmpty()) {
                return new WinnerSearch(winners, legEntry.getKey());
            }
        }

        return new WinnerSearch(List.of(), null);
    }

    /**
     * Creates a win-count map containing every player with zero leg wins.
     *
     * @param players the match players
     * @return the initialized win counts
     */
    private Map<ObjectId, Long> createEmptyWinCountMap(List<X01MatchPlayer> players) {
        return players.stream()
                .collect(Collectors.toMap(X01MatchPlayer::getPlayerId, player -> 0L));
    }

    /**
     * Determines whether the current leg win counts have concluded the set.
     *
     * @param winsPerPlayer  the current leg win counts
     * @param legsPlayed     the number of concluded legs
     * @param bestOfLegs     the configured best-of legs value
     * @param clearByTwoRule the applicable clear-by-two rule
     * @return the set winners, or an empty list when the set is not yet concluded
     */
    private List<ObjectId> determineSetWinners(
            Map<ObjectId, Long> winsPerPlayer,
            int legsPlayed,
            int bestOfLegs,
            X01ClearByTwoRule clearByTwoRule
    ) {
        TreeMap<Integer, List<ObjectId>> standings = standingsService.groupByWinCounts(winsPerPlayer);

        return standingsService.determineWinners(
                standings,
                legsPlayed,
                bestOfLegs,
                clearByTwoRule
        );
    }

    /**
     * Removes all legs after the given leg number.
     *
     * @param set       the set containing the legs
     * @param legNumber the final leg number to retain
     */
    private void removeLegsAfter(X01Set set, int legNumber) {
        set.getLegs().tailMap(legNumber, false).clear();
    }

    /**
     * Updates the set result from the determined winners.
     *
     * @param set        the set whose result should be updated
     * @param players    the match players
     * @param setWinners the determined set winners
     */
    private void updatePlayerResults(X01Set set, List<X01MatchPlayer> players, List<ObjectId> setWinners) {
        if (setWinners.isEmpty()) {
            set.setResult(null);
            return;
        }

        ResultType winnerResult = setWinners.size() > 1 ? ResultType.DRAW : ResultType.WIN;

        Map<ObjectId, ResultType> playerResults = players.stream()
                .collect(Collectors.toMap(
                        X01MatchPlayer::getPlayerId,
                        player -> setWinners.contains(player.getPlayerId()) ? winnerResult : ResultType.LOSS
                ));

        set.setResult(playerResults);
    }

    /**
     * Stores the winners found during a chronological search and the leg that concluded the set.
     *
     * @param winners   the determined winners
     * @param legNumber the concluding leg number, or null when the set is unfinished
     */
    private record WinnerSearch(List<ObjectId> winners, Integer legNumber) {
    }
}