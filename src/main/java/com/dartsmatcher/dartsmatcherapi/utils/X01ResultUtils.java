package com.dartsmatcher.dartsmatcherapi.utils;

import com.dartsmatcher.dartsmatcherapi.features.match.ResultType;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.X01Match;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.playerresult.X01PlayerResult;
import com.dartsmatcher.dartsmatcherapi.features.match.MatchPlayer;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg.X01Leg;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg.X01PlayerLeg;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.set.X01Set;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class X01ResultUtils {

	public static void updateMatchResult(X01Match match) {
		if (match.getTimeline() != null) {
			match.getTimeline().forEach(set -> {
				X01ResultUtils.updateLegPlayerResults(match, set);
				X01ResultUtils.updateSetPlayerResults(match, set);
			});
		}

		X01ResultUtils.updateMatchPlayerResults(match);
	}

	public static void updateMatchResult(X01Match match, X01Set set) {
		X01ResultUtils.updateLegPlayerResults(match, set);
		X01ResultUtils.updateSetPlayerResults(match, set);
		X01ResultUtils.updateMatchPlayerResults(match);
	}

	/**
	 * Updates all leg results for a given set.
	 *
	 * @param match X01Match for which to update the leg results.
	 * @param set   X01Set set for which to update the leg results.
	 */
	public static void updateLegPlayerResults(X01Match match, X01Set set) {
		if (match == null || set == null) return;

		Map<Integer, String> legsWon = X01ResultUtils.getLegsWon(match, set);
		set.getLegs().forEach(x01Leg -> x01Leg.setWinner(legsWon.getOrDefault(x01Leg.getLeg(), null)));
	}

	/**
	 * Determines which players have won legs in a given set.
	 *
	 * @param match X01Match for which to determine which player won legs in.
	 * @param set   X01Set for which to determine which player won legs in.
	 * @return Map<Integer String> Containing the leg number as key and the player that won the leg as value.
	 */
	public static Map<Integer, String> getLegsWon(X01Match match, X01Set set) {
		Map<Integer, String> legsWon = new HashMap<>();

		if (set != null && match != null) {
			for (X01Leg leg : set.getLegs()) { // Iterate through the legs and find the players with no score remaining.
				for (X01PlayerLeg playerLeg : leg.getPlayers()) {
					int remaining = match.getX01() - playerLeg.getScoring().stream().mapToInt(Integer::intValue).sum();

					// Player has won the leg.
					if (remaining <= 0) {
						legsWon.put(leg.getLeg(), playerLeg.getPlayerId());
					}
				}

			}
		}

		return legsWon;
	}

	/**
	 * Updates result for a given set.
	 *
	 * @param match X01Match which the set is a part of.
	 * @param set   X01Set to update the result of.
	 */
	public static void updateSetPlayerResults(X01Match match, X01Set set) {
		if (match == null || set == null) return;

		// Find how many legs each player has won. Store playerId as key, number of legs won as value.
		Map<String, Integer> numOfLegsWon = match.getPlayers().stream()
				.collect(Collectors.toMap(MatchPlayer::getPlayerId, player -> 0));

		// Number of legs with a result.
		AtomicInteger legsPlayed = new AtomicInteger();

		// Iterate through the legs to fill numOfLegsWon and legsPlayed.
		set.getLegs().forEach(x01Leg -> {
			String legWinner = x01Leg.getWinner();
			if (legWinner != null && !legWinner.isEmpty()) {
				numOfLegsWon.put(x01Leg.getWinner(), numOfLegsWon.getOrDefault(x01Leg.getWinner(), 0) + 1);
				legsPlayed.getAndIncrement();
			}
		});

		// Number of legs still to play.
		int legsToGo = match.getBestOf().getLegs() - legsPlayed.get();

		// Construct a list of players that have won (in case of multiple winners it's a draw).
		ArrayList<String> setWinners = X01ResultUtils.getWinners(numOfLegsWon, legsToGo);

		// Construct and add the player results to the set.
		set.setResult(X01ResultUtils.createPlayerResults(numOfLegsWon, setWinners));
	}

	/**
	 * Updates result for a given match.
	 *
	 * @param match X01Match which the result has to be updated of.
	 */
	public static void updateMatchPlayerResults(X01Match match) {
		if (match == null) return;

		// Find how many sets each player has won. Store playerId as key, number of sets won as value.
		Map<String, Integer> numWon = match.getPlayers().stream()
				.collect(Collectors.toMap(MatchPlayer::getPlayerId, player -> 0));

		// Number of sets with a result.
		AtomicInteger numPlayed = new AtomicInteger();

		if (match.getTimeline() != null) {
			// When a match is a best of 1 set then the score should reflect the number of legs won in the first set.
			// Else the score will reflect the number of sets won.
			if (match.getBestOf().getSets() == 1) {
				X01Set set = match.getTimeline().get(0);
				match.setResult(set.getResult());
			} else {
				// Iterate through the legs to fill numWon and setsPlayed.
				match.getTimeline().forEach(x01Set -> {
					boolean setHasWinner = false;

					for (X01PlayerResult playerResult : x01Set.getResult()) {
						if (playerResult.getResult() == ResultType.WIN || playerResult.getResult() == ResultType.DRAW) {
							numWon.put(playerResult.getPlayerId(), numWon.getOrDefault(playerResult.getPlayerId(), 0) + 1);
							setHasWinner = true;
						}
					}
					if (setHasWinner) numPlayed.getAndIncrement();
				});

				// Number of sets still to play.
				int toGo = match.getBestOf().getSets() - numPlayed.get();

				// Construct a list of players that have won (in case of multiple winners it's a draw).
				ArrayList<String> matchWinners = X01ResultUtils.getWinners(numWon, toGo);

				// Construct and add the player results to the match.
				match.setResult(X01ResultUtils.createPlayerResults(numWon, matchWinners));
			}
		}

	}

	/**
	 * @param playerScores Map<String, Integer> Containing the playerId as key and their score as value.
	 * @param toGo         int The number of sets or legs that can still to be played.
	 * @return ArrayList<String> Containing the players that have won (multiple winners means a draw between these players).
	 */
	public static ArrayList<String> getWinners(Map<String, Integer> playerScores, int toGo) {
		ArrayList<String> winners = new ArrayList<>();
		if (playerScores == null) return winners;

		// The highest score from the playerScores.
		int mostWon = Collections.max(playerScores.entrySet(), Map.Entry.comparingByValue()).getValue();

		playerScores.forEach((player, score) -> {
			boolean hasWonOrDraw = false;

			if (score.equals(mostWon)) {
				if (toGo == 0) { // If there are no legs to be played the players having the most legs will have won or drew.
					hasWonOrDraw = true;
				} else if (playerScores.size() > 1) {
					// If no players can exceed or equalize the player with the most legs won. hasWon is true otherwise false.
					hasWonOrDraw = playerScores.entrySet().stream()
							.noneMatch(playerLegsWonEntry ->
									!playerLegsWonEntry.getKey().equals(player) &&
											playerLegsWonEntry.getValue() + toGo >= score);

				}
			}

			if (hasWonOrDraw) {
				winners.add(player);
			}
		});

		return winners;
	}

	/**
	 * Creates a X01PlayerResult for each player with their playerId, score and result.
	 *
	 * @param playerScores Map<String, Integer> Containing the playerId as key and their score as value.
	 * @param winners      ArrayList<String> Containing the players that have won (multiple winners means a draw between these players).
	 * @return ArrayList<X01PlayerResult> Containing an X01PlayerResult for each of the players listed in the playerScores Map.
	 */
	public static ArrayList<X01PlayerResult> createPlayerResults(Map<String, Integer> playerScores, ArrayList<String> winners) {
		ArrayList<X01PlayerResult> playerResults = new ArrayList<>();

		if (playerScores == null || winners == null) return playerResults;

		// Iterate through each player score to determine their score and result. And add them to the playerResults.
		playerScores.forEach((playerId, score) -> {
			X01PlayerResult playerResult = new X01PlayerResult(
					playerId,
					score,
					null
			);

			// If there are no winners then the result of each players stays null.
			if (!winners.isEmpty()) {
				if (winners.contains(playerId)) // When a player is the sole winner then he has won otherwise its a draw between all winners.
					playerResult.setResult(winners.size() > 1 ? ResultType.DRAW : ResultType.WIN);
				else
					playerResult.setResult(ResultType.LOSE); // When a player is not in the winners list then he has lost.
			}

			playerResults.add(playerResult);

		});

		return playerResults;
	}
}
