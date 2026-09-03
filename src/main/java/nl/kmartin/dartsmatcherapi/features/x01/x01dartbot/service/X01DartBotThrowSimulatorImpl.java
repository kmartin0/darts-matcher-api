package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service;

import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.DartThrow;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.DartboardSectionArea;
import nl.kmartin.dartsmatcherapi.features.dartboard.service.IDartboardService;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01Checkout;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.service.IX01CheckoutService;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model.X01DartBotTurnSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * Simulates scoring and checkout throws for the X01 dart bot.
 *
 * Determines whether the bot should score or attempt a checkout, selects the appropriate target,
 * applies the bot's calculated throw deviation, and validates the resulting dart against the
 * bot's checkout policy.
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
     * The scoring target is based on the bot's target average and the deviation
     * is calibrated using its current performance.
     *
     * @param dartBotTurnSnapshot the current dart bot turn snapshot
     * @return the generated scoring throw
     */
    private DartThrow createScoringDartThrow(X01DartBotTurnSnapshot dartBotTurnSnapshot) {
        // Simulate a throw at a scoring target.
        DartThrow dartThrow = throwAtTarget(
                dartBotTurnSnapshot.targetOneDartAvg(),
                dartBotTurnSnapshot.currentOneDartAvg(),
                dartBotScoringStrategy.createScoringTarget(dartBotTurnSnapshot.targetOneDartAvg())
        );

        // Replace a result that violates the checkout policy with a miss.
        Dart result = validateResult(dartThrow.result(), dartBotTurnSnapshot);

        return new DartThrow(dartThrow.target(), result);
    }

    /**
     * Creates the next checkout throw or throws for the dart bot.
     *
     * @param dartBotTurnSnapshot the current dart bot turn snapshot
     * @return the generated checkout or fallback scoring throws
     */
    private List<DartThrow> createCheckoutDartThrows(X01DartBotTurnSnapshot dartBotTurnSnapshot) {
        Optional<X01Checkout> checkout = checkoutService.getCheckout(dartBotTurnSnapshot.remainingPoints());

        // Fall back to scoring when no checkout sequence is configured.
        if (checkout.isEmpty()) {
            return List.of(createScoringDartThrow(dartBotTurnSnapshot));
        }

        return throwAtCheckout(checkout.get(), dartBotTurnSnapshot);
    }

    /**
     * Simulates dart throws while the bot is aiming for a checkout.
     *
     * @param checkout        the checkout sequence to follow
     * @param dartBotTurnSnapshot the current dart bot turn snapshot
     * @return the generated checkout throws
     */
    private List<DartThrow> throwAtCheckout(X01Checkout checkout, X01DartBotTurnSnapshot dartBotTurnSnapshot) {
        // Calculate the dart count after completing the suggested checkout sequence.
        int dartsUsedAfterCheckout = dartBotTurnSnapshot.dartsUsedInLeg() + checkout.minDarts();

        // Complete the checkout once doing so reaches the bot's target dart count.
        boolean hasToCheckout = dartBotCheckoutPolicy.isTargetNumOfDartsReached(
                dartsUsedAfterCheckout,
                dartBotTurnSnapshot.targetNumOfDarts()
        );

        if (hasToCheckout) {
            return createGuaranteedCheckoutThrows(checkout, dartBotTurnSnapshot.dartsLeftInTurn());
        }

        // Otherwise, simulate a single throw at the first target in the checkout sequence.
        DartThrow dartThrow = throwAtTarget(
                dartBotTurnSnapshot.targetOneDartAvg(),
                dartBotTurnSnapshot.currentOneDartAvg(),
                checkout.suggested().get(0)
        );

        // Replace a result that violates the checkout policy with a miss.
        Dart result = validateResult(dartThrow.result(), dartBotTurnSnapshot);

        return List.of(new DartThrow(dartThrow.target(), result));
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
     * Simulates a dart throw at the given target.
     *
     * @param targetOneDartAvg  the target one-dart average
     * @param currentOneDartAvg the current one-dart average
     * @param target            the dartboard target
     * @return the target and resulting dart
     */
    private DartThrow throwAtTarget(double targetOneDartAvg, double currentOneDartAvg, Dart target) {
        // Generate the radial and angular deviation for the simulated throw.
        double offsetR = dartBotDeviationCalculator.createOffsetR(targetOneDartAvg, currentOneDartAvg);
        double offsetTheta = dartBotDeviationCalculator.createOffsetTheta(targetOneDartAvg, currentOneDartAvg);

        // Apply the deviation to determine where the dart actually lands.
        Dart result = dartboardService.getScore(target, offsetR, offsetTheta);

        return new DartThrow(target, result);
    }

    /**
     * Validates a dart result against the bot's checkout policy.
     *
     * @param result          the dart result to validate
     * @param dartBotTurnSnapshot the current dart bot turn snapshot
     * @return the original result when valid, otherwise a missed dart
     */
    private Dart validateResult(Dart result, X01DartBotTurnSnapshot dartBotTurnSnapshot) {
        if (!dartBotCheckoutPolicy.isDartResultValid(result, dartBotTurnSnapshot)) {
            return new Dart(result.section(), DartboardSectionArea.MISS);
        }

        return result;
    }
}