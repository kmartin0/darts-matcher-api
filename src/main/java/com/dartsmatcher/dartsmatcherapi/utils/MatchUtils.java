package com.dartsmatcher.dartsmatcherapi.utils;

import com.dartsmatcher.dartsmatcherapi.features.match.models.*;
import org.bson.types.ObjectId;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

public class MatchUtils {

	/**
	 * Updates result and statistics for a match using it's timeline.
	 *
	 * @param match Match reference to be updated.
	 */
	public static void updateMatchStatistics(@Valid Match match) {
		ArrayList<MatchPlayer<ObjectId>> registeredPlayers = match.getPlayers().getRegistered();
		ArrayList<MatchPlayer<String>> anonymousPlayers = match.getPlayers().getAnonymous();

		Stream.of(registeredPlayers, anonymousPlayers)
				.filter(Objects::nonNull)
				.forEach(matchPlayers -> matchPlayers.forEach(matchPlayer -> {
					// No statistics available if there is no timeline.
					if (matchPlayer == null || matchPlayer.getTimeline() == null) return;

					// Add new statistics object.
					matchPlayer.setStatistics(new MatchPlayerStatistics());

					// Iterate over the timeline to gather and update the statistics.
					matchPlayer.getTimeline().forEach(playerSet -> {
						// No statistics change if the set is null or has no legs.
						if (playerSet == null || playerSet.getLegs() == null) return;

						// Iterate over the legs to update the checkout, throws and ton plus stats.
						playerSet.getLegs().forEach(playerLeg -> {
							Iterator<Integer> iterator = playerLeg.getScoring().iterator();
							while (iterator.hasNext()) {
								int score = iterator.next();
								int dartsThrown = 3;
								if (playerLeg.getResult() != null && playerLeg.getResult() == ResultType.WIN && !iterator.hasNext()) {
									dartsThrown = playerLeg.getDartsUsedFinalThrow();
								}
								MatchUtils.updateThrowStats(matchPlayer.getStatistics(), score, dartsThrown);
								MatchUtils.updateTonPlusStats(matchPlayer.getStatistics(), score);
							}
							MatchUtils.updateCheckoutStats(matchPlayer.getStatistics(), playerLeg);
						});
					});

					// Update the average.
					MatchUtils.updateAverage(matchPlayer.getStatistics());
				}));
	}

	/**
	 * Update MatchPlayerStatistics TonPlus, TonForty and TonEighty with the player's score from a leg.
	 *
	 * @param playerStats MatchPlayerStatistics reference to be updated.
	 * @param score       PlayerLegScore the leg of which the scores are determined.
	 */
	public static void updateTonPlusStats(MatchPlayerStatistics playerStats, int score) {

		// Go through the scores of a leg to determine TonPlus, TonForty and TonEighty scoring.
		if (score >= 100 && score < 140) playerStats.setTonPlus(playerStats.getTonPlus() + 1);
		if (score >= 140 && score < 180) playerStats.setTonForty(playerStats.getTonForty() + 1);
		if (score == 180) playerStats.setTonEighty(playerStats.getTonEighty() + 1);
	}

	/**
	 * Update MatchPlayerStatistics CheckoutsHit, CheckoutsMissed, CheckoutHighest and CheckoutPercentage with the
	 * player's checkouts from a leg.
	 *
	 * @param playerStats MatchPlayerStatistics reference to be updated.
	 * @param playerLeg   PlayerLegScore the leg of which the checkouts are thrown.
	 */
	public static void updateCheckoutStats(MatchPlayerStatistics playerStats, PlayerLeg playerLeg) {

		// If the player won the leg add a checkoutHit and check for tonPlusCheckout.
		if (playerLeg.getResult() == ResultType.WIN) {
			int checkout = playerLeg.getScoring().get(playerLeg.getScoring().size() - 1);

			// Check if the player has hit a higher checkout. If not set it with the current checkout.
			if (checkout > playerStats.getCheckoutHighest()) playerStats.setCheckoutHighest(checkout);

			// Check if the checkout is a ton plus checkout. Then increment tonPlus checkouts.
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
	 * @param score       The score a player has thrown.
	 * @param dartsThrown The number of darts used for the score.
	 */
	public static void updateThrowStats(MatchPlayerStatistics playerStats, int score, int dartsThrown) {
		// Increment the total darts thrown with the darts thrown this leg.
		playerStats.setDartsThrown(playerStats.getDartsThrown() + dartsThrown);

		// Increment the total points thrown with the points thrown this leg.
		playerStats.setPointsThrown(playerStats.getPointsThrown() + score);
	}
//

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
}