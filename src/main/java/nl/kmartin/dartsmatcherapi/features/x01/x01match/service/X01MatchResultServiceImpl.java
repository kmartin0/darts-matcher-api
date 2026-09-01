package nl.kmartin.dartsmatcherapi.features.x01.x01match.service;

import nl.kmartin.dartsmatcherapi.features.basematch.model.MatchStatus;
import nl.kmartin.dartsmatcherapi.features.basematch.model.ResultType;
import nl.kmartin.dartsmatcherapi.features.x01.common.X01MatchUtils;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01BestOf;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01Set;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.service.IX01SetResultService;
import nl.kmartin.dartsmatcherapi.features.x01.x01standings.service.IX01StandingsService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Rebuilds X01 match results from the processed set history.
 *
 * Reprocesses set results, removes stale history and updates player results, match state and standings.
 */
@Service
public class X01MatchResultServiceImpl implements IX01MatchResultService {

    private final IX01SetResultService setResultService;
    private final IX01StandingsService standingsService;

    public X01MatchResultServiceImpl(
            IX01SetResultService setResultService,
            IX01StandingsService standingsService
    ) {
        this.setResultService = setResultService;
        this.standingsService = standingsService;
    }

    /**
     * Rebuilds the result-related state of a match from its set history.
     *
     * Reprocesses set results, removes stale sets, determines the match winners and updates player results
     * and the overall match state.
     *
     * @param match the match whose result state should be rebuilt
     */
    @Override
    public void updateMatchResult(X01Match match) {
        if (match == null) return;

        // Rebuild set results and find the current unfinished set.
        Integer currentSetNumber = updateSetResults(match);

        // History after the current set could not have been played and is stale.
        if (currentSetNumber != null) {
            removeSetsAfter(match, currentSetNumber);
        }

        // Find the first set at which the remaining history concludes the match.
        WinnerSearch winnerSearch = findMatchWinners(match);

        // History after the first match-concluding set is stale.
        if (winnerSearch.setNumber() != null) {
            removeSetsAfter(match, winnerSearch.setNumber());
        }

        // Convert the determined winners into player results.
        updatePlayerResults(match, winnerSearch.winners());
    }

    /**
     * Rebuilds set results chronologically and finds the current unfinished set.
     *
     * @param match the match whose set results should be rebuilt
     * @return the current set number, or null when all processed sets are concluded
     */
    private Integer updateSetResults(X01Match match) {
        if (X01MatchUtils.isSetsEmpty(match)) return null;

        List<X01MatchPlayer> players = match.getPlayers();
        int x01 = match.getMatchSettings().getX01();
        X01BestOf bestOf = match.getMatchSettings().getBestOf();

        // Reprocess sets chronologically until the current unfinished set is reached.
        for (Map.Entry<Integer, X01Set> setEntry : match.getSets().entrySet()) {
            X01SetEntry entry = new X01SetEntry(setEntry);
            setResultService.updateSetResult(entry, bestOf, players, x01);

            if (entry.set().getResult() == null) {
                return entry.setNumber();
            }
        }

        return null;
    }

    /**
     * Finds the match winners and the first set at which the match becomes concluded.
     *
     * @param match the match to inspect
     * @return the winner search result
     */
    private WinnerSearch findMatchWinners(X01Match match) {
        if (X01MatchUtils.isSetsEmpty(match)) return new WinnerSearch(List.of(), null);

        Map<ObjectId, Long> winsPerPlayer = createEmptyWinCountMap(match.getPlayers());
        X01BestOf bestOf = match.getMatchSettings().getBestOf();
        int setsPlayed = 0;

        // Build the standings chronologically until the rules determine that the match is concluded.
        for (Map.Entry<Integer, X01Set> setEntry : match.getSets().entrySet()) {
            X01Set set = setEntry.getValue();
            if (set.getResult() == null) break;

            updateWinCounts(winsPerPlayer, set);
            setsPlayed++;

            List<ObjectId> winners = determineMatchWinners(winsPerPlayer, setsPlayed, bestOf);

            if (!winners.isEmpty()) {
                return new WinnerSearch(winners, setEntry.getKey());
            }
        }

        return new WinnerSearch(List.of(), null);
    }

    /**
     * Updates player win counts from a concluded set result.
     *
     * @param winsPerPlayer the current set win counts
     * @param set           the concluded set
     */
    private void updateWinCounts(Map<ObjectId, Long> winsPerPlayer, X01Set set) {
        for (Map.Entry<ObjectId, ResultType> resultEntry : set.getResult().entrySet()) {
            ResultType result = resultEntry.getValue();

            // Both wins and drawn sets count as one set win.
            if (result == ResultType.WIN || result == ResultType.DRAW) {
                winsPerPlayer.merge(resultEntry.getKey(), 1L, Long::sum);
            }
        }
    }

    /**
     * Creates a win-count map containing every match player with zero set wins.
     *
     * @param players the match players
     * @return the initialized win counts
     */
    private Map<ObjectId, Long> createEmptyWinCountMap(List<X01MatchPlayer> players) {
        return players.stream()
                .collect(Collectors.toMap(X01MatchPlayer::getPlayerId, player -> 0L));
    }

    /**
     * Determines whether the current set win counts have concluded the match.
     *
     * @param winsPerPlayer the current set win counts
     * @param setsPlayed    the number of concluded sets
     * @param bestOf        the match format
     * @return the match winners, or an empty list when the match is not yet concluded
     */
    private List<ObjectId> determineMatchWinners(Map<ObjectId, Long> winsPerPlayer, int setsPlayed, X01BestOf bestOf) {
        TreeMap<Integer, List<ObjectId>> standings = standingsService.groupByWinCounts(winsPerPlayer);

        return standingsService.determineWinners(
                standings,
                setsPlayed,
                bestOf.getSets(),
                bestOf.getClearByTwoSetsRule()
        );
    }

    /**
     * Removes all sets after the given set number.
     *
     * @param match     the match containing the sets
     * @param setNumber the final set number to retain
     */
    private void removeSetsAfter(X01Match match, int setNumber) {
        match.getSets().tailMap(setNumber, false).clear();
    }

    /**
     * Updates each player's result from the determined match winners.
     *
     * @param match        the match whose player results should be updated
     * @param matchWinners the determined match winners
     */
    private void updatePlayerResults(X01Match match, List<ObjectId> matchWinners) {
        if (matchWinners.isEmpty()) {
            match.getPlayers().forEach(player -> player.setResultType(null));
            return;
        }

        ResultType winnerResult = matchWinners.size() > 1 ? ResultType.DRAW : ResultType.WIN;

        match.getPlayers().forEach(player -> player.setResultType(
                matchWinners.contains(player.getPlayerId()) ? winnerResult : ResultType.LOSS
        ));
    }

    /**
     * Updates the match status and end date from the determined match result.
     *
     * @param match        the match to update
     * @param matchWinners the determined match winners
     */
    private void updateMatchState(X01Match match, List<ObjectId> matchWinners) {
        // An unfinished match remains in play and must not have an end date.
        if (matchWinners.isEmpty()) {
            match.setEndDate(null);
            match.setMatchStatus(MatchStatus.IN_PLAY);
            return;
        }

        // Preserve the original conclusion time when reprocessing an already concluded match.
        if (match.getMatchStatus() == MatchStatus.IN_PLAY || match.getEndDate() == null) {
            match.setEndDate(Instant.now());
        }

        match.setMatchStatus(MatchStatus.CONCLUDED);
    }

    /**
     * Stores the winners found during a chronological search and the set that concluded the match.
     *
     * @param winners   the determined winners
     * @param setNumber the concluding set number, or null when the match is unfinished
     */
    private record WinnerSearch(List<ObjectId> winners, Integer setNumber) {
    }
}