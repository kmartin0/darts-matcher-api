package com.dartsmatcher.dartsmatcherapi.features.x01Dartbot;

import com.dartsmatcher.dartsmatcherapi.exceptionhandler.exception.ResourceNotFoundException;
import com.dartsmatcher.dartsmatcherapi.features.board.Board;
import com.dartsmatcher.dartsmatcherapi.features.board.BoardSectionArea;
import com.dartsmatcher.dartsmatcherapi.features.board.Dart;
import com.dartsmatcher.dartsmatcherapi.features.match.MatchPlayer;
import com.dartsmatcher.dartsmatcherapi.features.x01checkout.IX01CheckoutService;
import com.dartsmatcher.dartsmatcherapi.features.x01livematch.dto.X01Throw;
import com.dartsmatcher.dartsmatcherapi.features.x01match.IX01MatchService;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.X01Match;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.checkout.X01Checkout;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg.X01Leg;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg.X01LegRoundScore;
import com.dartsmatcher.dartsmatcherapi.utils.PolarCoordinate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@Service
public class X01DartBotService {

	public static final double MAX_ONE_DART_AVG = 167 / 3.0;

	public static final double MAX_ONE_DART_AVG_DEVIATION = .67;

	public static final int MAX_BOARD_DEVIATION = 126;

	private final IX01CheckoutService checkoutService;

	private final IX01MatchService matchService;

	public X01DartBotService(IX01CheckoutService checkoutService, IX01MatchService matchService) {
		this.checkoutService = checkoutService;
		this.matchService = matchService;
	}

	/**
	 * Create an automated X01Throw based on a number of factors.
	 *
	 * @param x01DartBotThrow X01DartBotThrow containing information about the throw
	 * @return X01Throw the object containing the throw information of the round.
	 * @throws IOException throws exception when an error occurred reading the checkouts file.
	 */
	public X01Throw dartBotThrow(X01DartBotThrow x01DartBotThrow) throws IOException {

		String dartBotId = x01DartBotThrow.getDartBotId();
		X01Match match = matchService.getMatch(x01DartBotThrow.getMatchId());
		MatchPlayer dartBotPlayer = match.getPlayers().stream().filter(matchPlayer -> matchPlayer.getPlayerId().equals(dartBotId)).findFirst().orElse(null);

		X01Leg x01Leg = match.getSet(x01DartBotThrow.getSet())
				.flatMap(x01Set -> x01Set.getLeg(x01DartBotThrow.getLeg()))
				.orElseThrow(() -> new ResourceNotFoundException(X01Leg.class, x01DartBotThrow.getLeg()));

		X01DartBotSettings dartBotSettings = Objects.requireNonNull(dartBotPlayer).getDartBotSettings();
		if (dartBotSettings == null) throw new ResourceNotFoundException(X01DartBotSettings.class, null);

		// Starting values.
		int legScored = x01Leg.getScored(dartBotId);
		int legDartsUsed = x01Leg.getDartsUsed(dartBotId);
		double expectedOneDartAvg = dartBotSettings.getExpectedThreeDartAverage() / 3.0;

		// Generate the X01LegRoundScore containing the score, darts used and doubles missed for this round.
		X01LegRoundScore roundScore = new X01LegRoundScore(dartBotId, 0, 0, 0);
		roundScore = createRoundScore(match.getX01(), legScored, legDartsUsed, expectedOneDartAvg, 3, roundScore);

		return new X01Throw(x01DartBotThrow.getMatchId(), dartBotId, x01Leg.getLeg(), x01DartBotThrow.getSet(),
				x01DartBotThrow.getRound(), roundScore.getScore(), roundScore.getDartsUsed(), roundScore.getDoublesMissed());
	}

	/**
	 * Creates a X01LegRoundScore object containing the results for a dart bot his round.
	 *
	 * @param x01                int the x01 starting point.
	 * @param legScored          int the score in the leg excluding this round.
	 * @param legDartsUsed       int the darts used in the leg excluding this round.
	 * @param expectedOneDartAvg double the expected one dart average.
	 * @param roundDartsLeft     int the darts left to throw in this round.
	 * @param x01LegRoundScore   X01LegRoundScore the round score object to populate.
	 * @return X01LegRoundScore containing the results for a round.
	 * @throws IOException throws exception when an error occurred reading the checkouts file.
	 */
	private X01LegRoundScore createRoundScore(int x01, int legScored, int legDartsUsed, double expectedOneDartAvg,
											  int roundDartsLeft, X01LegRoundScore x01LegRoundScore) throws IOException {

		int remaining = x01 - legScored - x01LegRoundScore.getScore();
		X01Checkout checkout = checkoutService.getCheckout(remaining).orElse(null);
		double actualOneDartAvg = calcOneDartAvg(legScored + x01LegRoundScore.getScore(),
				legDartsUsed + x01LegRoundScore.getDartsUsed());

		// Throw checkout if eligible.
		if (isCheckoutEligible(checkout, expectedOneDartAvg, actualOneDartAvg, legDartsUsed)) {
			int score = throwCheckoutTarget(checkout, roundDartsLeft);
			x01LegRoundScore.setScore(x01LegRoundScore.getScore() + score);

			// If the checkout has been thrown, update darts used with the actual darts thrown.
			if (score == checkout.getCheckout()) {
				x01LegRoundScore.setDartsUsed(x01LegRoundScore.getDartsUsed() + checkout.getSuggested().size());
			}

			// No further darts are thrown after a checkout.
			return x01LegRoundScore;
		} else { // Throw score if checkout not eligible.
			int score = throwScoringTarget(expectedOneDartAvg, actualOneDartAvg, legDartsUsed + x01LegRoundScore.getDartsUsed());

			// Increment doubles missed when a double could have been thrown.
			if ((remaining <= 40 || remaining == 50) && remaining % 2 == 0)
				x01LegRoundScore.setDoublesMissed(x01LegRoundScore.getDoublesMissed() + 1);

			// Create bust when the bot shouldn't checkout but scores a checkout.
			if (remaining - (x01LegRoundScore.getScore() + score) < 2) score = 0;

			// Increment darts used in the round by 1.
			x01LegRoundScore.setDartsUsed(x01LegRoundScore.getDartsUsed() + 1);

			x01LegRoundScore.setScore(x01LegRoundScore.getScore() + score);
		}

		// Decrease darts left by 1.
		roundDartsLeft--;

		// When no darts are left, return the round score. Otherwise recursively call this method until no darts are left.
		return roundDartsLeft == 0
				? x01LegRoundScore
				: createRoundScore(x01, legScored, legDartsUsed, expectedOneDartAvg, roundDartsLeft, x01LegRoundScore);
	}

	/**
	 * Virtually throws a dart at the board. Will hit at a random point between de center of the target and the
	 * deviation based on the expected average. Regular throws are always at T20 (80%), T19 (10%) or T18 (10%).
	 * Will calibrate the average if needed by throwing at a specific target that gets the actual average closer
	 * to the expected average.
	 *
	 * @param expectedOneDartAvg double the expected one dart average of bot.
	 * @param actualOneDartAvg   double the actual one dart average of bot in the leg.
	 * @param dartsThrown        int the darts thrown in the leg.
	 * @return int the score of one dart.
	 */
	private int throwScoringTarget(double expectedOneDartAvg, double actualOneDartAvg, int dartsThrown) {

		// The aimed target, default is triple 20.
		Dart target = new Dart(20, BoardSectionArea.TRIPLE);

		boolean shouldCalibrateAvg = shouldCalibrateAvg(expectedOneDartAvg, actualOneDartAvg, dartsThrown);

		// When the average needs to be calibrated the target is a triple that gets the average within a random point
		// inside the offset range of the expected average. Otherwise the target is T20 (80%), T19 (10%) or T18 (10%).
		if (shouldCalibrateAvg) {
			double totalNeeded = (expectedOneDartAvg + getOffsetAvg(dartsThrown)) * (dartsThrown + 1);
			double totalScored = actualOneDartAvg * dartsThrown;

			int tripleNeeded = (int) (totalNeeded - totalScored) / 3;

			if (tripleNeeded < 1) {
				tripleNeeded = 1;
				target.setArea(BoardSectionArea.OUTER_SINGLE);
			} else if (tripleNeeded > 20) tripleNeeded = 20;

			target.setSection(tripleNeeded);
		} else {
			double rand = Math.random();
			if (rand < .1) target.setSection(18);
			if (rand < .2) target.setSection(19);
		}

		// Calculate the offset of the angle and radial.
		double offsetR = getOffsetR(shouldCalibrateAvg, expectedOneDartAvg);
		double offsetTheta = getOffsetTheta(shouldCalibrateAvg, expectedOneDartAvg);

		return getScore(target, offsetR, offsetTheta);
	}

	/**
	 * Throws the darts left at a checkout sequence. These will always hit. If the darts left are less than the minimum
	 * amount of darts needed for the suggested checkout sequence then the score will be returned of the thrown darts.
	 *
	 * @param checkout  @Nullable X01Checkout the checkout that needs to be thrown.
	 * @param dartsLeft int the darts left to use in this round.
	 * @return int the score that the darts left have scored.
	 */
	private int throwCheckoutTarget(@Nullable X01Checkout checkout, int dartsLeft) {
		int score = 0;

		if (checkout != null) {
			for (int j = 0; j < checkout.getSuggested().size(); j++) {
				if (j == dartsLeft) break;

				score += checkout.getSuggested().get(j).getScore();
			}
		}

		return score;
	}

	/**
	 * Virtually throws a dart at a dartboard and returns the score that was hit, taking a random point measured from
	 * the center of the aimed target and the offset around it.
	 *
	 * @param target      Dart the target that will be aimed for.
	 * @param offsetR     double the radial offset in mm.
	 * @param offsetTheta double the theta (angle) offset in radian between -pi and pi.
	 * @return int the scored score of a single dart.
	 */
	private int getScore(Dart target, double offsetR, double offsetTheta) {
		Board board = new Board();

		PolarCoordinate polarTarget = board.getCenter(target.getSection(), target.getArea());

		double r = polarTarget.getR() + offsetR;
		double theta = PolarCoordinate.normalizeTheta(polarTarget.getTheta() + offsetTheta);

		return board.getScorePolar(new PolarCoordinate(r, theta));
	}

	/**
	 * Determines whether the average should be calibrated. An average should be calibrated if the one dart average is
	 * over/under the expected one dart average plus/minus the max deviation. The first round (first 3 darts) are never
	 * calibrated for expected three dart averages over or equal to 40
	 *
	 * @param expectedOneDartAvg double the expected one dart average.
	 * @param actualOneDartAvg   double the actual one dart average of the leg.
	 * @param dartsThrown        int the darts thrown already this leg.
	 * @return boolean if the average should be calibrated.
	 */
	private boolean shouldCalibrateAvg(double expectedOneDartAvg, double actualOneDartAvg, int dartsThrown) {

		// Never calibrate the first 3 darts at a three dart average over or equal to 40
		if (expectedOneDartAvg >= (40 / 3.0) && dartsThrown < 3) return false;

		return actualOneDartAvg < expectedOneDartAvg - MAX_ONE_DART_AVG_DEVIATION ||
				actualOneDartAvg > expectedOneDartAvg + MAX_ONE_DART_AVG_DEVIATION;
	}

	/**
	 * Randomizes an offset point the one dart average can be off of the expected average.
	 *
	 * @param dartsThrown int the darts thrown this leg.
	 * @return double an offset point the one dart average can be off of the expected average.
	 */
	private double getOffsetAvg(int dartsThrown) {
		double maxOneDartAvgDeviation = MAX_ONE_DART_AVG_DEVIATION;

		// First 3 darts have a wider range, to allow more variance.
		if (dartsThrown < 3) maxOneDartAvgDeviation = maxOneDartAvgDeviation * 6;

		// Return a random point between deviation...0...-deviation.
		return (Math.random() * maxOneDartAvgDeviation) - (maxOneDartAvgDeviation / 2);
	}

	/**
	 * Randomizes an offset point the radial should be off of the target. When a calibration is set, the offset is always zero.
	 *
	 * @param shouldCalibrate    boolean indicates if the calibration is active.
	 * @param expectedOneDartAvg double the expected one dart average.
	 * @return double a radial offset in mm.
	 */
	private double getOffsetR(boolean shouldCalibrate, double expectedOneDartAvg) {
		// When the average is not being calibrated, add a random offset within the offset range calculated with the expected average.
		if (!shouldCalibrate) {
			double deviation = calcDeviation(expectedOneDartAvg);
			return (Math.random() * deviation) - (deviation / 2);
		}

		// Return no offset when calibrating.
		return 0;
	}

	/**
	 * Randomizes an offset point the theta (angle) should be off of the target. When a calibration is set, the offset is always zero.
	 *
	 * @param shouldCalibrate    boolean indicates if the calibration is active.
	 * @param expectedOneDartAvg double the expected one dart average.
	 * @return double a theta (angle) offset in mm.
	 */
	private double getOffsetTheta(boolean shouldCalibrate, double expectedOneDartAvg) {
		// When the average is not being calibrated, add a random offset within the offset range calculated with the expected average.
		if (!shouldCalibrate) {
			double deviation = calcDeviation(expectedOneDartAvg);
			return PolarCoordinate.degreeToRadian(((Math.random() * deviation) - (deviation / 2)));
		}

		// Return no offset when calibrating.
		return 0;
	}

	/**
	 * Calculates what the deviation of a throw is in mm based on the expected one dart average.
	 *
	 * @param expectedOneDartAvg double the expected one dart average.
	 * @return double the deviation in mm.
	 */
	private double calcDeviation(double expectedOneDartAvg) {
		// When the average is higher or equal than the maximum there is no deviation.
		if (expectedOneDartAvg >= MAX_ONE_DART_AVG) return 0;

		// Calculate the inaccuracy in percentage.
		double accuracy = (expectedOneDartAvg * 100.0) / MAX_ONE_DART_AVG;
		double inaccuracy = 100 - accuracy;

		return (inaccuracy * MAX_BOARD_DEVIATION) / 100;
	}

	/**
	 * Calculates the one dart average of a score.
	 *
	 * @param totalScored double the scores that the average needs to be calculated for.
	 * @param dartsThrown double the darts thrown to get to the score.
	 * @return double one dart average of the score.
	 */
	private double calcOneDartAvg(double totalScored, int dartsThrown) {
		double oneDartAvg = totalScored / dartsThrown;

		return Double.isNaN(oneDartAvg) ? 0 : oneDartAvg;
	}

	/**
	 * Determines if a checkout is eligible to checkout. A checkout is eligible if the average after the checkout is
	 * less than or equal to the the expected one dart average plus the offset.
	 *
	 * @param checkout           @Nullable X01Checkout the checkout to check against.
	 * @param expectedOneDartAvg double the expected one dart average for the leg.
	 * @param actualOneDartAvg   double the actual one dart average for the leg.
	 * @param dartsThrown        int the number of darts thrown this leg.
	 * @return boolean whether the checkout is eligible.
	 */
	private boolean isCheckoutEligible(@Nullable X01Checkout checkout, double expectedOneDartAvg, double actualOneDartAvg, int dartsThrown) {
		if (checkout == null) return false;

		int dartsThrownAfterCheckout = dartsThrown + checkout.getSuggested().size();

		double totalScored = actualOneDartAvg * dartsThrown;
		double totalScoredAfterCheckout = totalScored + checkout.getCheckout();

		double averageAfterCheckout = calcOneDartAvg(totalScoredAfterCheckout, dartsThrownAfterCheckout);

		return averageAfterCheckout <= expectedOneDartAvg + getOffsetAvg(dartsThrown);
	}

}
