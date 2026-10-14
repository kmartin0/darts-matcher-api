package nl.kmartin.dartsmatcher.api.features.x01.x01dartbot.service;

import nl.kmartin.dartsmatcher.api.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcher.api.features.dartboard.model.DartThrow;
import nl.kmartin.dartsmatcher.api.features.dartboard.model.DartboardSection;
import nl.kmartin.dartsmatcher.api.features.dartboard.model.DartboardSectionArea;
import nl.kmartin.dartsmatcher.api.features.dartboard.service.IDartboardService;
import nl.kmartin.dartsmatcher.api.features.x01.x01checkout.model.X01Checkout;
import nl.kmartin.dartsmatcher.api.features.x01.x01checkout.service.IX01CheckoutService;
import nl.kmartin.dartsmatcher.api.features.x01.x01dartbot.model.X01DartBotTurnSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * Simulates scoring and checkout throws for the X01 dart bot.
 *
 * Selects a scoring or checkout target, applies calibrated Gaussian offsets,
 * and validates simulated results against the bot's checkout policy.
 *
 * Checkout routes use guaranteed hits once their projected finishing count reaches
 * the target dart count. Earlier checkout attempts fall back to their intended
 * target when a simulated result leaves a suggested route beyond the dart budget.
 */
@Service
@Validated
public class X01DartBotThrowSimulatorImpl implements IX01DartBotThrowSimulator {

    private final IDartboardService dartboardService;
    private final IX01CheckoutService checkoutService;
    private final IX01DartBotCheckoutPolicy dartBotCheckoutPolicy;
    private final IX01DartBotDeviationCalculator dartBotDeviationCalculator;
    private final IX01DartBotScoringStrategy dartBotScoringStrategy;

    public X01DartBotThrowSimulatorImpl(
            IDartboardService dartboardService,
            IX01CheckoutService checkoutService,
            IX01DartBotCheckoutPolicy dartBotCheckoutPolicy,
            IX01DartBotDeviationCalculator dartBotDeviationCalculator,
            IX01DartBotScoringStrategy dartBotScoringStrategy
    ) {
        this.dartboardService = dartboardService;
        this.checkoutService = checkoutService;
        this.dartBotCheckoutPolicy = dartBotCheckoutPolicy;
        this.dartBotDeviationCalculator = dartBotDeviationCalculator;
        this.dartBotScoringStrategy = dartBotScoringStrategy;
    }

    @Override
    public List<DartThrow> getNextDartThrows(X01DartBotTurnSnapshot dartBotTurnSnapshot) {
        // Use the checkout strategy when the remaining score can be checked out.
        boolean isRemainingCheckout = checkoutService.isScoreCheckout(dartBotTurnSnapshot.remainingPoints());

        // Otherwise, generate a regular scoring throw.
        return isRemainingCheckout
                ? createCheckoutDartThrows(dartBotTurnSnapshot)
                : List.of(createScoringDartThrow(dartBotTurnSnapshot));
    }

    /**
     * Creates a scoring throw for the dart bot.
     *
     * The scoring target is based on the bot's target average. Standard deviation
     * is calibrated using its current performance after the first dart.
     *
     * @param dartBotTurnSnapshot the current dart bot turn snapshot
     * @return the generated scoring throw
     */
    private DartThrow createScoringDartThrow(X01DartBotTurnSnapshot dartBotTurnSnapshot) {
        // Simulate a throw at a scoring target.
        DartThrow dartThrow = throwAtTarget(
                dartBotTurnSnapshot,
                dartBotScoringStrategy.createScoringTarget(dartBotTurnSnapshot.targetOneDartAvg())
        );

        // Replace a result that violates the checkout policy with a miss.
        Dart result = validateResult(dartThrow.result(), dartBotTurnSnapshot);

        return new DartThrow(dartThrow.target(), result);
    }

    /**
     * Selects scoring, a guaranteed finish or a simulated checkout throw.
     *
     * @param snapshot the current dart bot turn snapshot
     * @return the generated throws
     */
    private List<DartThrow> createCheckoutDartThrows(X01DartBotTurnSnapshot snapshot) {
        Optional<X01Checkout> checkout = checkoutService.getCheckout(snapshot.remainingPoints());

        if (checkout.isEmpty()) {
            return List.of(createScoringDartThrow(snapshot));
        }

        if (shouldFinishOnBull(snapshot)) {
            Dart bull = new Dart(DartboardSection.BULL, DartboardSectionArea.DOUBLE_BULL);
            return List.of(new DartThrow(bull, bull));
        }

        if (shouldGuaranteeCheckout(checkout.get(), snapshot)) {
            return createGuaranteedCheckoutThrows(checkout.get(), snapshot.dartsLeftInTurn());
        }

        return List.of(throwAtCheckout(checkout.get(), snapshot));
    }

    /**
     * Checks whether a bull finish reaches or exceeds the target dart count.
     *
     * @param snapshot the current dart bot turn snapshot
     * @return whether to guarantee a one-dart finish from 50
     */
    private boolean shouldFinishOnBull(X01DartBotTurnSnapshot snapshot) {
        return snapshot.remainingPoints() == 50
                && dartBotCheckoutPolicy.isTargetNumOfDartsReached(
                snapshot.dartsUsedInLeg() + 1,
                snapshot.targetNumOfDarts()
        );
    }

    /**
     * Checks whether the suggested route reaches or exceeds the target dart count.
     *
     * @param checkout the checkout sequence to follow
     * @param snapshot the current dart bot turn snapshot
     * @return whether to guarantee the checkout route
     */
    private boolean shouldGuaranteeCheckout(X01Checkout checkout, X01DartBotTurnSnapshot snapshot) {
        int dartsUsedAfterCheckout = snapshot.dartsUsedInLeg() + checkout.suggested().size();

        return dartBotCheckoutPolicy.isTargetNumOfDartsReached(
                dartsUsedAfterCheckout,
                snapshot.targetNumOfDarts()
        );
    }

    /**
     * Simulates one checkout dart, falling back to the intended target if the validated
     * result leaves no suggested route within the dart budget. The fallback is also validated.
     *
     * @param checkout the checkout sequence to follow
     * @param snapshot the current dart bot turn snapshot
     * @return the generated checkout throw
     */
    private DartThrow throwAtCheckout(X01Checkout checkout, X01DartBotTurnSnapshot snapshot) {
        DartThrow dartThrow = throwAtTarget(snapshot, checkout.suggested().get(0));
        Dart result = validateResult(dartThrow.result(), snapshot);

        if (!canFinishWithinTarget(result, snapshot)) {
            result = validateResult(dartThrow.target(), snapshot);
        }

        return new DartThrow(dartThrow.target(), result);
    }

    /**
     * Creates guaranteed throws following the checkout sequence.
     *
     * Each generated throw hits its intended target exactly and the number of throws
     * is limited to the darts remaining in the current turn.
     *
     * @param checkout       the checkout sequence to follow
     * @param dartsRemaining the number of darts remaining in the turn
     * @return the guaranteed checkout throws
     */
    private List<DartThrow> createGuaranteedCheckoutThrows(X01Checkout checkout, int dartsRemaining) {
        // Limit the checkout sequence to the number of darts still available in the turn.
        int dartsRequired = checkout.suggested().size();
        int dartsToUse = Math.min(dartsRemaining, dartsRequired);

        // Guaranteed checkout throws hit their intended targets exactly.
        return checkout.suggested()
                .stream()
                .limit(dartsToUse)
                .map(dart -> new DartThrow(dart, dart))
                .toList();
    }

    /**
     * Simulates a throw at the target without validating the checkout policy.
     *
     * @param snapshot the current dart bot turn snapshot
     * @param target   the dartboard target
     * @return the target and simulated result
     */
    private DartThrow throwAtTarget(X01DartBotTurnSnapshot snapshot, Dart target) {
        double standardDeviation = getStandardDeviation(snapshot);
        double offsetX = dartBotDeviationCalculator.createOffset(standardDeviation);
        double offsetY = dartBotDeviationCalculator.createOffset(standardDeviation);
        Dart result = dartboardService.getScore(target, offsetX, offsetY);

        return new DartThrow(target, result);
    }

    /**
     * Uses baseline standard deviation before the first dart and calibrated values thereafter.
     *
     * @param snapshot the current dart bot turn snapshot
     * @return the standard deviation per axis in millimeters
     */
    private double getStandardDeviation(X01DartBotTurnSnapshot snapshot) {
        if (snapshot.dartsUsedInLeg() == 0) {
            return dartBotDeviationCalculator.getStandardDeviation(snapshot.targetOneDartAvg());
        }

        return dartBotDeviationCalculator.getCalibratedStandardDeviation(
                snapshot.targetOneDartAvg(), snapshot.currentOneDartAvg()
        );
    }

    /**
     * Validates a dart result against the bot's checkout policy.
     *
     * @param result              the dart result to validate
     * @param dartBotTurnSnapshot the current dart bot turn snapshot
     * @return the original result when valid, otherwise a missed dart
     */
    private Dart validateResult(Dart result, X01DartBotTurnSnapshot dartBotTurnSnapshot) {
        if (!dartBotCheckoutPolicy.isDartResultValid(result, dartBotTurnSnapshot)) {
            return new Dart(result.section(), DartboardSectionArea.MISS);
        }

        return result;
    }

    /**
     * Checks whether a validated throw completes the leg or leaves a suggested
     * checkout route within the remaining target dart budget.
     *
     * The supplied throw consumes one dart, including a miss. This check uses
     * the suggested route length without accounting for the one-dart bull exception.
     *
     * @param result              the validated dart result
     * @param dartBotTurnSnapshot the current dart bot turn snapshot
     * @return whether the leg is complete or the suggested route fits the budget
     */
    private boolean canFinishWithinTarget(Dart result, X01DartBotTurnSnapshot dartBotTurnSnapshot) {
        int remainingAfterThrow = dartBotTurnSnapshot.remainingPoints() - result.getScore();

        if (remainingAfterThrow == 0) {
            return true;
        }

        int dartsAvailable = dartBotTurnSnapshot.targetNumOfDarts() - dartBotTurnSnapshot.dartsUsedInLeg() - 1;

        return checkoutService.getCheckout(remainingAfterThrow)
                .map(checkout -> checkout.suggested().size() <= dartsAvailable)
                .orElse(false);
    }
}