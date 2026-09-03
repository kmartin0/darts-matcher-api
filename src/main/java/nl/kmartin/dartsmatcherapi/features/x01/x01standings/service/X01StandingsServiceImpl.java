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
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Rebuilds X01 match standings and determines winners from grouped win counts.
 *
 * Match standings contain cumulative set wins and leg wins for the current or final set.
 */
@Service
@Validated
public class X01StandingsServiceImpl implements IX01StandingsService {

    private final IX01MatchProgressService matchProgressService;
    private final IX01RulesService rulesService;

    public X01StandingsServiceImpl(IX01MatchProgressService matchProgressService, IX01RulesService rulesService) {
        this.matchProgressService = matchProgressService;
        this.rulesService = rulesService;
    }

    @Override
    public void updateMatchStandings(X01Match match) {
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

    @Override
    public List<ObjectId> determineWinners(
            TreeMap<Integer, List<ObjectId>> standings,
            int played,
            int bestOf,
            X01ClearByTwoRule clearByTwoRule
    ) {
        // Empty standings cannot produce a winner.
        if (standings.isEmpty()) return List.of();

        // Calculate the current lead and how much of the configured best-of remains.
        int leaderScore = standings.lastKey();
        Integer runnerUpScore = standings.lowerKey(leaderScore);
        int diff = leaderScore - (runnerUpScore != null ? runnerUpScore : leaderScore);
        int bestOfRemaining = Math.max(0, bestOf - played);

        // A single-player match concludes only after all configured legs or sets have been played.
        if (rulesService.isSinglePlayerMatch(standings, leaderScore, runnerUpScore)) {
            return bestOfRemaining == 0 ? List.copyOf(standings.get(leaderScore)) : List.of();
        }

        // Return the leaders once the current result can no longer be overturned.
        if (rulesService.isWinnerConfirmed(diff, bestOfRemaining, played, bestOf, clearByTwoRule)) {
            return List.copyOf(standings.get(leaderScore));
        }

        return List.of();
    }

    @Override
    public TreeMap<Integer, List<ObjectId>> groupByWinCounts(Map<ObjectId, Long> winsPerPlayer) {
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
    private void updateStandingsWithLegWinners(
            X01SetEntry currentSetEntry,
            LinkedHashMap<ObjectId, X01StandingsEntry> matchStandings
    ) {
        for (X01Leg leg : currentSetEntry.set().getLegs().values()) {
            // Skip unfinished legs.
            ObjectId legWinner = leg.getWinner();
            if (legWinner == null) continue;

            // Skip results that cannot be associated with a match player.
            X01StandingsEntry playerStandings = matchStandings.get(legWinner);
            if (playerStandings == null) continue;

            // Increment the player's leg wins in the current or final set.
            playerStandings.setLegsWonInCurrentSet(playerStandings.getLegsWonInCurrentSet() + 1);
        }
    }

    /**
     * Adds set wins and drawn-set results to the match standings.
     *
     * @param setEntry       the set whose result should be counted
     * @param matchStandings the standings to update
     */
    private void updateStandingsWithSetWinners(
            X01SetEntry setEntry,
            LinkedHashMap<ObjectId, X01StandingsEntry> matchStandings
    ) {
        if (CollectionUtils.isEmpty(setEntry.set().getResult())) return;

        for (Map.Entry<ObjectId, ResultType> resultEntry : setEntry.set().getResult().entrySet()) {
            ResultType result = resultEntry.getValue();

            // Both wins and drawn sets count as one set win.
            if (result != ResultType.WIN && result != ResultType.DRAW) continue;

            // Skip results that cannot be associated with a match player.
            X01StandingsEntry playerStandings = matchStandings.get(resultEntry.getKey());
            if (playerStandings == null) continue;

            playerStandings.setSetsWon(playerStandings.getSetsWon() + 1);
        }
    }
}