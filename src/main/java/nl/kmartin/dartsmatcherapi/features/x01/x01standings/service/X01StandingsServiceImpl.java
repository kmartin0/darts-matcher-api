package nl.kmartin.dartsmatcherapi.features.x01.x01standings.service;

import nl.kmartin.dartsmatcherapi.features.basematch.model.ResultType;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01ClearByTwoRule;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.service.IX01MatchProgressService;
import nl.kmartin.dartsmatcherapi.features.x01.x01rules.service.IX01RulesService;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01Set;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01standings.model.X01StandingsEntry;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Rebuilds X01 match standings and determines winners from grouped win counts.
 *
 * Match standings contain cumulative set wins and leg wins for the current or final set.
 */
@Service
public class X01StandingsServiceImpl implements IX01StandingsService {

    private final IX01MatchProgressService matchProgressService;
    private final IX01RulesService rulesService;

    public X01StandingsServiceImpl(IX01MatchProgressService matchProgressService, IX01RulesService rulesService) {
        this.matchProgressService = matchProgressService;
        this.rulesService = rulesService;
    }

    /**
     * Rebuilds the standings from the processed match history.
     *
     * Set wins are accumulated across all sets, while leg wins are taken from the current set or
     * the final set when the match is concluded.
     *
     * @param match the match whose standings should be rebuilt
     */
    @Override
    public void updateMatchStandings(X01Match match) {
        if (match == null) return;

        // Use the current set for leg standings, or the final set when the match is concluded.
        Integer currentOrFinalSetNumber = matchProgressService.getCurrentSet(match)
                .map(X01SetEntry::setNumber)
                .orElseGet(() -> match.getSets().isEmpty() ? null : match.getSets().lastKey());

        // Start with an empty standings entry for every player.
        LinkedHashMap<ObjectId, X01StandingsEntry> matchStandings = createInitialStandings(match.getPlayers());

        // Rebuild standings from the processed set history.
        for (Map.Entry<Integer, X01Set> setEntry : match.getSets().entrySet()) {
            X01SetEntry entry = new X01SetEntry(setEntry);

            // Only the current or final set contributes to the leg standings.
            if (Objects.equals(currentOrFinalSetNumber, entry.setNumber())) {
                updateStandingsWithLegWinners(entry, matchStandings);
            }

            // Set wins are cumulative across the entire match.
            updateStandingsWithSetWinners(entry, matchStandings);
        }

        // Replace the stored standings with the rebuilt result.
        match.setStandings(matchStandings);
    }

    /**
     * Determines whether the leaders in the standings are confirmed winners.
     *
     * @param standings      players grouped by their number of wins
     * @param played         the number of legs or sets already played
     * @param bestOf         the configured best-of value
     * @param clearByTwoRule the clear-by-two rule to apply
     * @return the confirmed winners, or an empty list when the result is not yet decided
     */
    @Override
    public List<ObjectId> determineWinners(TreeMap<Integer, List<ObjectId>> standings, int played, int bestOf, X01ClearByTwoRule clearByTwoRule) {
        // Step 1: Empty standings means there can be no winner.
        if (CollectionUtils.isEmpty(standings)) return List.of();

        // Step 2: Get the leader and runner-up scores and calculate the current lead.
        int leaderScore = standings.lastKey();
        Integer runnerUpScore = standings.lowerKey(leaderScore);
        int diff = leaderScore - (runnerUpScore != null ? runnerUpScore : leaderScore);
        int bestOfRemaining = bestOf - played;

        // Step 3: A single-player match is only concluded once all configured legs or sets have been played.
        if (rulesService.isSinglePlayerMatch(standings, leaderScore, runnerUpScore)) {
            return bestOfRemaining == 0 ? List.copyOf(standings.get(leaderScore)) : List.of();
        }

        // Step 4: Return the leaders when the current result can no longer be overturned.
        if (rulesService.isWinnerConfirmed(diff, bestOfRemaining, played, bestOf, clearByTwoRule)) {
            return List.copyOf(standings.get(leaderScore));
        }

        // Step 5: The result is not yet decided.
        return List.of();
    }

    /**
     * Groups players by their number of wins.
     *
     * @param winsPerPlayer the number of wins for each player
     * @return a sorted map keyed by win count with the players sharing that count
     */
    @Override
    public TreeMap<Integer, List<ObjectId>> groupByWinCounts(Map<ObjectId, Long> winsPerPlayer) {
        if (winsPerPlayer == null || winsPerPlayer.isEmpty()) return new TreeMap<>();

        return winsPerPlayer.entrySet()
                .stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getValue().intValue(),
                        TreeMap::new,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                ));
    }

    /**
     * Creates empty standings for all match players.
     *
     * @param players the players to include
     * @return standings initialized with zero set and leg wins
     */
    private LinkedHashMap<ObjectId, X01StandingsEntry> createInitialStandings(List<X01MatchPlayer> players) {
        return players.stream()
                .collect(Collectors.toMap(
                        X01MatchPlayer::getPlayerId,
                        player -> new X01StandingsEntry(0, 0),
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
    }

    /**
     * Adds the completed leg wins from a set to the match standings.
     *
     * @param currentSetEntry the set whose leg wins should be counted
     * @param matchStandings  the standings to update
     */
    private void updateStandingsWithLegWinners(X01SetEntry currentSetEntry, LinkedHashMap<ObjectId, X01StandingsEntry> matchStandings) {
        for (X01Leg leg : currentSetEntry.set().getLegs().values()) {
            // Get the leg winner; unfinished legs can be skipped.
            ObjectId legWinner = leg.getWinner();
            if (legWinner == null) continue;

            // Get the standings for the leg winner; if absent, the leg can be skipped.
            X01StandingsEntry playerStandings = matchStandings.get(legWinner);
            if (playerStandings == null) continue;

            // Increment the player's leg wins in the current set.
            playerStandings.setLegsWonInCurrentSet(playerStandings.getLegsWonInCurrentSet() + 1);
        }
    }

    /**
     * Adds set wins and drawn-set results to the match standings.
     *
     * @param setEntry       the set whose result should be counted
     * @param matchStandings the standings to update
     */
    private void updateStandingsWithSetWinners(X01SetEntry setEntry, LinkedHashMap<ObjectId, X01StandingsEntry> matchStandings) {
        if (CollectionUtils.isEmpty(setEntry.set().getResult())) return;

        for (Map.Entry<ObjectId, ResultType> resultEntry : setEntry.set().getResult().entrySet()) {
            ResultType result = resultEntry.getValue();

            // Both winners and players sharing a drawn set receive one set win.
            if (result != ResultType.WIN && result != ResultType.DRAW) continue;

            // Get the standings for the player; if absent, the result can be skipped.
            X01StandingsEntry playerStandings = matchStandings.get(resultEntry.getKey());
            if (playerStandings == null) continue;

            // Increment the player's sets won.
            playerStandings.setSetsWon(playerStandings.getSetsWon() + 1);
        }
    }
}