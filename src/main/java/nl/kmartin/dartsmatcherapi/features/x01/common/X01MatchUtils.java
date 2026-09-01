package nl.kmartin.dartsmatcherapi.features.x01.common;

import nl.kmartin.dartsmatcherapi.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRound;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01Set;
import org.bson.types.ObjectId;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides utility methods for working with X01 match structures, players and averages.
 */
public final class X01MatchUtils {
    private X01MatchUtils() {}

    /**
     * Determines whether a match has no sets.
     *
     * @param match the match to check
     * @return whether the match is null or contains no sets
     */
    public static boolean isSetsEmpty(X01Match match) {
        return match == null || CollectionUtils.isEmpty(match.getSets());
    }

    /**
     * Determines whether a set has no legs.
     *
     * @param set the set to check
     * @return whether the set is null or contains no legs
     */
    public static boolean isLegsEmpty(X01Set set) {
        return set == null || CollectionUtils.isEmpty(set.getLegs());
    }

    /**
     * Determines whether a leg has no rounds.
     *
     * @param leg the leg to check
     * @return whether the leg is null or contains no rounds
     */
    public static boolean isRoundsEmpty(X01Leg leg) {
        return leg == null || CollectionUtils.isEmpty(leg.getRounds());
    }

    /**
     * Determines whether a leg round has no scores.
     *
     * @param legRound the leg round to check
     * @return whether the leg round is null or contains no scores
     */
    public static boolean isScoresEmpty(X01LegRound legRound) {
        return legRound == null || CollectionUtils.isEmpty(legRound.getScores());
    }

    /**
     * Determines whether a player list is empty.
     *
     * @param players the players to check
     * @return whether the list is null or contains no players
     */
    public static boolean isPlayersEmpty(List<? extends MatchPlayer> players) {
        return players == null || CollectionUtils.isEmpty(players);
    }

    /**
     * Orders the players starting with the player that throws first.
     *
     * @param throwsFirst the ID of the player that throws first
     * @param players the players to order
     * @return the players in throwing order
     * @throws IllegalArgumentException when the player that throws first cannot be found
     */
    public static List<X01MatchPlayer> getThrowingOrder(ObjectId throwsFirst, List<X01MatchPlayer> players) {
        if (throwsFirst == null || players == null) return players;

        // Find the index of the player that starts the round.
        int throwsFirstIndex = players.indexOf(players.stream()
                .filter(player -> player.getPlayerId().equals(throwsFirst))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found")));

        // Order the players in the throwing order of the round.
        List<X01MatchPlayer> orderedPlayers = new ArrayList<>();
        orderedPlayers.addAll(players.subList(throwsFirstIndex, players.size()));
        orderedPlayers.addAll(players.subList(0, throwsFirstIndex));

        // Return the ordered list.
        return orderedPlayers;
    }

    /**
     * Converts a three-dart average to a one-dart average.
     *
     * @param threeDartAverage the three-dart average
     * @return the equivalent one-dart average
     */
    public static double threeDartAvgToOneDartAvg(double threeDartAverage) {
        return threeDartAverage / 3;
    }
}