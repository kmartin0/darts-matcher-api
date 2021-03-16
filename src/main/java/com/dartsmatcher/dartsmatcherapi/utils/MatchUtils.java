package com.dartsmatcher.dartsmatcherapi.utils;

import com.dartsmatcher.dartsmatcherapi.features.match.models.*;
import org.bson.types.ObjectId;

import javax.validation.Valid;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class MatchUtils {

	/**
	 * Updates result and statistics for a match using it's timeline.
	 *
	 * @param match Match reference to be updated.
	 * @param clock Clock used for LocalDateTime.now()
	 */
	public static void updateMatchStatisticsAndResult(@Valid Match match, Clock clock) {

		// Statistics can't be updated if there is no timeline or players.
		if (match.getTimeline() == null || match.getPlayers() == null) return;

		// Add empty results and statistics for each player to the match.
		MatchUtils.populateMatchResultAndStatisticsWithPlayers(match);

		// Go through the sets, legs and player scores to update the statistics.
		match.getTimeline().forEach(set -> {
			MatchUtils.updateResultScore(match, set);
			set.getLegs().forEach(leg -> {
				MatchUtils.updateResultScore(match, leg);
				leg.getPlayers().forEach(playerLeg ->
						match.getStatistics().stream()
								.filter(_playerStats -> _playerStats.getPlayer().equals(playerLeg.getPlayer()))
								.findFirst()
								.ifPresent(playerStats -> {
									MatchUtils.updateTonPlusScores(playerStats, playerLeg);
									MatchUtils.updateCheckoutStats(playerStats, leg, playerLeg);
									MatchUtils.updateThrowStats(playerStats, leg, playerLeg);
								})
				);
			});
		});

		// Update the player average.
		match.getStatistics().forEach(MatchUtils::updateAverage);

		// Update the result status of each player.
		MatchUtils.updateResultStatus(match, clock);
	}

	/**
	 * Adds statistics populated with a MatchPlayerStatistics reference for each player to a match.
	 * Adds result populated with a PlayerResult reference for each player to a match.
	 *
	 * @param match Match to be updated.
	 */
	public static void populateMatchResultAndStatisticsWithPlayers(Match match) {
		// Add a new statistics arraylist to the match statistics reference.
		match.setStatistics(new ArrayList<>());

		// Add a new result arraylist to the match result reference.
		match.setResult(new ArrayList<>());

		if (match.getPlayers().getRegistered() != null) {
			// update match statistics and result with the registered players.
			match.getPlayers().getRegistered().forEach(playerId -> {
				// Add new player statistics instance to the match for the player.
				MatchPlayerStatistics playerStats = new MatchPlayerStatistics();
				playerStats.setPlayer(playerId.toString());
				match.getStatistics().add(playerStats);

				// Add new player result instance to the match for the player.
				PlayerResult playerResult = new PlayerResult();
				playerResult.setPlayer(playerId.toString());
				match.getResult().add(playerResult);
			});
		}

		if (match.getPlayers().getAnonymous() != null) {
			// update match statistics and result with the anonymous players.
			match.getPlayers().getAnonymous().forEach(playerName -> {
				// Add new player statistics instance to the match for the player.
				MatchPlayerStatistics playerStats = new MatchPlayerStatistics();
				playerStats.setPlayer(playerName);
				match.getStatistics().add(playerStats);

				// Add new player result instance to the match for the player.
				PlayerResult playerResult = new PlayerResult();
				playerResult.setPlayer(playerName);
				match.getResult().add(playerResult);
			});
		}
	}

	/**
	 * Update MatchPlayerStatistics TonPlus, TonForty and TonEighty with the player's scores from a leg.
	 *
	 * @param playerStats MatchPlayerStatistics reference to be updated.
	 * @param playerLeg   PlayerLegScore the leg of which the scores are determined.
	 */
	public static void updateTonPlusScores(MatchPlayerStatistics playerStats, PlayerLegScore playerLeg) {

		// Go through the scores of a leg to determine TonPlus, TonForty and TonEighty scoring.
		playerLeg.getScoring().forEach(score -> {
			if (score >= 100 && score < 140) playerStats.setTonPlus(playerStats.getTonPlus() + 1);
			if (score >= 140 && score < 180) playerStats.setTonForty(playerStats.getTonForty() + 1);
			if (score == 180) playerStats.setTonEighty(playerStats.getTonEighty() + 1);
		});
	}

	/**
	 * Update MatchPlayerStatistics CheckoutsHit, CheckoutsMissed, CheckoutHighest and CheckoutPercentage with the
	 * player's checkouts from a leg.
	 *
	 * @param playerStats MatchPlayerStatistics reference to be updated.
	 * @param leg         MatchLeg the leg of which the checkouts are thrown.
	 * @param playerLeg   PlayerLegScore the leg of which the checkouts are thrown.
	 */
	public static void updateCheckoutStats(MatchPlayerStatistics playerStats, MatchLeg leg, PlayerLegScore playerLeg) {

		// If the player won the leg add a checkoutHit and check for tonPlusCheckout.
		if (leg.getWinner().equals(playerLeg.getPlayer())) {
			int checkout = playerLeg.getScoring().get(playerLeg.getScoring().size() - 1);

			// Check if the checkout has hit a higher checkout.
			if (checkout > playerStats.getCheckoutHighest()) playerStats.setCheckoutHighest(checkout);

			// Check if the checkout is a ton plus checkout
			if (checkout >= 100) playerStats.setCheckoutTonPlus(playerStats.getCheckoutTonPlus() + 1);

			// Increment checkouts hit by one.
			playerStats.setCheckoutsHit(playerStats.getCheckoutsHit() + 1);
		}

		// Increment the checkouts missed with the checkouts missed from this leg.
		playerStats.setCheckoutsMissed(playerStats.getCheckoutsMissed() + playerLeg.getDoublesMissed());

		// Calculate and set new checkout percentage.
		float doublesThrown = playerStats.getCheckoutsHit() + playerStats.getCheckoutsMissed();
		int checkoutPercentage = (int) ((playerStats.getCheckoutsHit() / doublesThrown) * 100);
		playerStats.setCheckoutPercentage(checkoutPercentage);
	}

	/**
	 * Update MatchPlayerStatistics Darts and Points thrown with the scoring of this leg.
	 *
	 * @param playerStats MatchPlayerStatistics reference to be updated.
	 * @param leg         MatchLeg the leg in which the player has thrown.
	 * @param playerLeg   PlayerLegScore the leg in which the player has thrown.
	 */
	public static void updateThrowStats(MatchPlayerStatistics playerStats, MatchLeg leg, PlayerLegScore playerLeg) {

		// Determine number of darts the player has thrown this leg.
		int dartsThrown = playerLeg.getScoring().size() * 3;
		if (leg.getWinner().equals(playerLeg.getPlayer())) {
			dartsThrown -= 3 - leg.getDartsUsedFinalThrow();
		}

		// Increment the total darts thrown with the darts thrown this leg.
		playerStats.setDartsThrown(playerStats.getDartsThrown() + dartsThrown);

		// Determine the points the player has thrown this leg.
		int pointsThrown = playerLeg.getScoring().stream()
				.mapToInt(Integer::intValue)
				.sum();

		// Increment the total points thrown with the points thrown this leg.
		playerStats.setPointsThrown(playerStats.getPointsThrown() + pointsThrown);
	}

	/**
	 * Update MatchPlayerStatistics Average.
	 *
	 * @param playerStats MatchPlayerStatistics reference to be updated.
	 */
	public static void updateAverage(MatchPlayerStatistics playerStats) {

		// Determine the 3-dart average.
		float avg = ((float) playerStats.getPointsThrown() / (float) playerStats.getDartsThrown()) * 3;

		// Update the 3-dart average.
		playerStats.setAverage((int) avg);
	}

	/**
	 * Update player result scores from the leg if the match uses best of x legs.
	 *
	 * @param match Match to have updated PlayerResult
	 * @param leg   MatchLeg to count towards the end result.
	 */
	public static void updateResultScore(Match match, MatchLeg leg) {

		// Increment PlayerResult score for the player who won the leg.
		if (match.getBestOf().getType() == MatchBestOfType.LEGS) {
			match.getResult().stream()
					.peek(playerResult -> System.out.println(playerResult + " " + leg.getWinner()))
					.filter(_playerResult -> _playerResult.getPlayer().equals(leg.getWinner()))
					.findFirst().ifPresent(playerResult -> playerResult.setScore(playerResult.getScore() + 1));
		}
	}

	/**
	 * Update player result scores from the set if the match uses best of x sets.
	 *
	 * @param match Match to have updated PlayerResult
	 * @param set   MatchSet to count towards the end result.
	 */
	public static void updateResultScore(Match match, MatchSet set) {

		// Increment PlayerResult score for the player who won the set.
		if (match.getBestOf().getType() == MatchBestOfType.SETS) {
			match.getResult().stream()
					.filter(_playerResult -> _playerResult.getPlayer().equals(set.getWinner()))
					.findFirst().ifPresent(playerResult -> playerResult.setScore(playerResult.getScore() + 1));
		}
	}

	/**
	 * Update the PlayerResults by checking if the match has ended, then adds the corresponding result (win, loss, draw)
	 * to each player and updates the match status to CONCLUDED and adds the endDate if the match has ended. Otherwise
	 * keeps the matchStatus to IN_PLAY.
	 *
	 * @param match Match reference to be updated.
	 * @param clock Clock used to get the end date.
	 */
	public static void updateResultStatus(Match match, Clock clock) {

		// Initialize winner and draw to track whether someone has won or if a draw has happened.
		String winner = null;
		boolean draw = false;
		int registeredPlayers = match.getPlayers().getRegistered() != null ? match.getPlayers().getRegistered().size() : 0;
		int anonymousPlayers = match.getPlayers().getAnonymous() != null ? match.getPlayers().getAnonymous().size() : 0;
		int numOfPlayers = registeredPlayers + anonymousPlayers;

		// Check if the match has a winner or is a draw.
		for (PlayerResult playerResult : match.getResult()) {
			switch (match.getBestOf().getType()) {
				case SETS: {
					// Determine the minimum sets needed to win.
					float setsNeededForWin = (float) match.getBestOf().getSets() / numOfPlayers;

					// Determine if the player has enough sets to win, draw or neither.
					if (playerResult.getScore() > setsNeededForWin) winner = playerResult.getPlayer();
					else if (playerResult.getScore() == setsNeededForWin) draw = true;
					else if (playerResult.getScore() < setsNeededForWin) draw = false;
				}
				break;
				case LEGS: {
					// Determine the minimum sets needed to win.
					float legsNeededForWin = (float) match.getBestOf().getLegs() / numOfPlayers;

					// Determine if the player has enough sets to win, draw or neither.
					if (playerResult.getScore() > legsNeededForWin) winner = playerResult.getPlayer();
					else if (playerResult.getScore() == legsNeededForWin) draw = true;
					else if (playerResult.getScore() < legsNeededForWin) draw = false;
				}
				break;
			}

			// When a winner is determined, by default all other players have lost.
			if (winner != null) break;
		}

		// Assign the result status and conclude it if the match has ended in a draw or win.
		if (winner != null || draw) {

			// Assign the result (win, loss, draw) of each player to their PlayerResult.
			for (PlayerResult playerResult : match.getResult()) {
				if (playerResult.getPlayer().equals(winner)) playerResult.setResult(ResultType.WIN);
				else if (draw) playerResult.setResult(ResultType.DRAW);
				else playerResult.setResult(ResultType.LOSE);
			}

			// Conclude the match and set the end date to now.
			match.setMatchStatus(MatchStatus.CONCLUDED);
			match.setEndDate(LocalDateTime.now(clock));

		} else match.setMatchStatus(MatchStatus.IN_PLAY); // The match is still in-play.
	}
}
