package com.dartsmatcher.dartsmatcherapi.utils;

import com.dartsmatcher.dartsmatcherapi.features.x01match.models.X01Match;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg.X01PlayerLeg;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.statistics.X01AverageStatistics;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.statistics.X01CheckoutStatistics;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.statistics.X01PlayerStatistics;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.statistics.X01ScoresStatistics;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Objects;

public class X01StatisticsUtils {

	/**
	 * Updates result and statistics for a match using it's timeline.
	 *
	 * @param x01Match Match reference to be updated.
	 */
	public static void updateStatistics(@Valid X01Match x01Match) {

		ArrayList<X01PlayerStatistics> statistics = new ArrayList<>();

		x01Match.getPlayers().forEach(player -> {
			// Add new statistics object.
			statistics.add(new X01PlayerStatistics(
					player.getPlayerId(),
					new X01AverageStatistics(),
					new X01CheckoutStatistics(),
					new X01ScoresStatistics()
			));
		});

		x01Match.setStatistics(statistics);

		if (x01Match.getTimeline() != null) {
			x01Match.getTimeline().forEach(set -> {
				if (set != null) {
					set.getLegs().forEach(leg -> {
						if (leg != null) {
							leg.getPlayers().forEach(playerLeg -> {
								if (playerLeg != null) {
									x01Match.getStatistics().stream()
											.filter(_statistics -> Objects.equals(_statistics.getPlayerId(), playerLeg.getPlayerId()))
											.findFirst()
											.ifPresent(playerStats -> {
												boolean isLegWinner = Objects.equals(leg.getWinner(), playerLeg.getPlayerId());
												for (int i = 0; i < playerLeg.getScoring().size(); i++) {
													int score = playerLeg.getScoring().get(i);
													int dartsThrown = 3;

													if (isLegWinner && i == playerLeg.getScoring().size() - 1) {
														dartsThrown = leg.getDartsUsedFinalThrow();
													}

													X01StatisticsUtils.updateThrowStats(playerStats, score, dartsThrown, i < 3);
													X01StatisticsUtils.updateTonPlusStats(playerStats, score);
												}
												X01StatisticsUtils.updateCheckoutStats(playerStats, playerLeg, isLegWinner);
											});
								}
							});
						}
					});
				}
			});
		}

		x01Match.getStatistics().forEach(X01StatisticsUtils::updateAverage);
	}

	/**
	 * Update MatchPlayerStatistics TonPlus, TonForty and TonEighty with the player's score from a leg.
	 *
	 * @param playerStats MatchPlayerStatistics reference to be updated.
	 * @param score       Int the score.
	 */
	public static void updateTonPlusStats(X01PlayerStatistics playerStats, int score) {

		X01ScoresStatistics scoreStats = playerStats.getScoresStats();

		// determine if the score is a TonPlus, TonForty or TonEighty scoring.
		if (score >= 100 && score < 140) scoreStats.setTonPlus(scoreStats.getTonPlus() + 1);
		if (score >= 140 && score < 180) scoreStats.setTonForty(scoreStats.getTonForty() + 1);
		if (score == 180) scoreStats.setTonEighty(scoreStats.getTonEighty() + 1);
	}

	/**
	 * Update MatchPlayerStatistics CheckoutsHit, CheckoutsMissed, CheckoutHighest and CheckoutPercentage with the
	 * player's checkouts from a leg.
	 *
	 * @param playerStats  MatchPlayerStatistics reference to be updated.
	 * @param x01PlayerLeg PlayerLegScore the leg of which the checkouts are thrown.
	 */
	public static void updateCheckoutStats(X01PlayerStatistics playerStats, X01PlayerLeg x01PlayerLeg, boolean isLegWinner) {
		X01CheckoutStatistics checkoutStats = playerStats.getCheckoutStats();

		// If the player won the leg add a checkoutHit and check for tonPlusCheckout.
		if (isLegWinner) {
			int checkout = x01PlayerLeg.getScoring().get(x01PlayerLeg.getScoring().size() - 1);

			// Check if the player has hit a higher checkout. If not set it with the current checkout.
			if (checkout > checkoutStats.getCheckoutHighest()) checkoutStats.setCheckoutHighest(checkout);

			// Check if the checkout is a ton plus checkout. Then increment tonPlus checkouts.
			if (checkout >= 100) checkoutStats.setCheckoutTonPlus(checkoutStats.getCheckoutTonPlus() + 1);

			// Increment checkouts hit by one.
			checkoutStats.setCheckoutsHit(checkoutStats.getCheckoutsHit() + 1);
		}

		// Increment the checkouts missed with the checkouts missed from this leg.
		checkoutStats.setCheckoutsMissed(checkoutStats.getCheckoutsMissed() + x01PlayerLeg.getDoublesMissed());

		// Calculate and set new checkout percentage.
		float doublesThrown = checkoutStats.getCheckoutsHit() + checkoutStats.getCheckoutsMissed();
		int checkoutPercentage = (int) ((checkoutStats.getCheckoutsHit() / doublesThrown) * 100);
		checkoutStats.setCheckoutPercentage(checkoutPercentage);
	}


	/**
	 * Update MatchPlayerStatistics Darts and Points thrown with the scoring of this leg.
	 *
	 * @param playerStats MatchPlayerStatistics reference to be updated.
	 * @param score       The score a player has thrown.
	 * @param dartsThrown The number of darts used for the score.
	 */
	public static void updateThrowStats(X01PlayerStatistics playerStats, int score, int dartsThrown, boolean firstNine) {

		X01AverageStatistics averageStats = playerStats.getAverageStats();

		// Increment the total darts thrown with the darts thrown this leg.
		averageStats.setDartsThrown(averageStats.getDartsThrown() + dartsThrown);

		// Increment the total points thrown with the points thrown this leg.
		averageStats.setPointsThrown(averageStats.getPointsThrown() + score);

		if (firstNine) {
			// Increment the total darts thrown with the darts thrown this leg.
			averageStats.setDartsThrownFirstNine(averageStats.getDartsThrownFirstNine() + dartsThrown);

			// Increment the total points thrown with the points thrown this leg.
			averageStats.setPointsThrownFirstNine(averageStats.getPointsThrownFirstNine() + score);
		}
	}

	/**
	 * Update MatchPlayerStatistics Average.
	 *
	 * @param playerStats MatchPlayerStatistics reference to be updated.
	 */
	public static void updateAverage(X01PlayerStatistics playerStats) {

		X01AverageStatistics averageStats = playerStats.getAverageStats();

		// Determine the average.
		float avg = ((float) averageStats.getPointsThrown() / (float) averageStats.getDartsThrown()) * 3;

		// Update the average.
		averageStats.setAverage((int) avg);

		// Determine the first nine average.
		float firstNineAvg = ((float) averageStats.getPointsThrownFirstNine() / (float) averageStats.getDartsThrownFirstNine()) * 3;

		// Update the first nine average.
		averageStats.setAverageFirstNine((int) firstNineAvg);
	}
}