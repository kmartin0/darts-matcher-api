package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service;

import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.DartThrow;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.DartboardSectionArea;
import nl.kmartin.dartsmatcherapi.features.dartboard.service.IDartboardService;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01Checkout;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.service.IX01CheckoutService;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model.X01DartBotLegState;
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

    /**
     * Generates the next dart throws for the dart bot based on the current leg state.
     *
     * When the remaining score is outside checkout range, a single scoring throw is generated.
     * When a checkout is available, either a single checkout attempt or the required part of a
     * guaranteed checkout sequence is generated.
     *
     * @param dartBotLegState the current dart bot leg state
     * @return the dart throws generated for the next part of the turn
     */
    @Override
    public List<DartThrow> getNextDartThrows(X01DartBotLegState dartBotLegState) {
        // Determine whether the bot should aim for scoring or a checkout.
        boolean isRemainingCheckout = checkoutService.isScoreCheckout(dartBotLegState.getRemainingPoints());

        return isRemainingCheckout
                ? createCheckoutThrowResult(dartBotLegState)
                : List.of(createScoringThrowResult(dartBotLegState));
    }

    /**
     * Creates a scoring throw for the dart bot.
     *
     * A scoring target is selected based on the bot's target one-dart average. The throw deviation
     * is based on both the target and current one-dart averages. The result is then validated to
     * prevent the scoring throw from producing an invalid checkout result.
     *
     * @param dartBotLegState the current dart bot leg state
     * @return the generated scoring throw
     */
    private DartThrow createScoringThrowResult(X01DartBotLegState dartBotLegState) {
        // Simulate a throw at a scoring target.
        DartThrow dartThrow = throwAtTarget(
                dartBotLegState.getTargetOneDartAvg(),
                dartBotLegState.getCurrentOneDartAvg(),
                dartBotScoringStrategy.createScoringTarget(dartBotLegState.getTargetOneDartAvg())
        );

        // Replace an invalid result with a miss.
        Dart result = validateResult(dartThrow.result(), dartBotLegState);

        // Return the scoring target together with the validated result.
        return new DartThrow(dartThrow.target(), result);
    }

    /**
     * Creates the next checkout throw or throws for the dart bot.
     *
     * If checkout information is available, the bot follows its suggested checkout sequence.
     * When no checkout sequence is available, a scoring throw is generated instead.
     *
     * @param dartBotLegState the current dart bot leg state
     * @return the generated checkout or fallback scoring throws
     */
    private List<DartThrow> createCheckoutThrowResult(X01DartBotLegState dartBotLegState) {
        Optional<X01Checkout> checkout = checkoutService.getCheckout(dartBotLegState.getRemainingPoints());

        // When there is no checkout sequence available, fall back to a scoring throw.
        if (checkout.isEmpty()) {
            return List.of(createScoringThrowResult(dartBotLegState));
        }

        // Simulate throwing at the checkout sequence.
        return throwAtCheckout(checkout.get(), dartBotLegState);
    }

    /**
     * Simulates dart throws while the bot is aiming for a checkout.
     *
     * When completing the checkout would reach or exceed the bot's target number of darts,
     * a guaranteed checkout sequence is generated. Otherwise, a single simulated throw is
     * made at the first target in the checkout sequence.
     *
     * @param checkout        the checkout sequence to follow
     * @param dartBotLegState the current dart bot leg state
     * @return the generated checkout throws
     */
    private List<DartThrow> throwAtCheckout(X01Checkout checkout, X01DartBotLegState dartBotLegState) {
        // Calculate how many darts will have been used after completing the checkout sequence.
        int dartsUsedAfterCheckout = dartBotLegState.getDartsUsedInLeg() + checkout.minDarts();

        // Determine whether the bot has reached the point where it should complete the checkout.
        boolean hasToCheckout = dartBotCheckoutPolicy.isTargetNumOfDartsReached(
                dartsUsedAfterCheckout,
                dartBotLegState.getTargetNumOfDarts()
        );

        if (hasToCheckout) {
            return createGuaranteedCheckoutThrows(checkout, dartBotLegState.getDartsLeftInRound());
        }

        // Simulate throwing at the first target in the checkout sequence.
        DartThrow dartThrow = throwAtTarget(
                dartBotLegState.getTargetOneDartAvg(),
                dartBotLegState.getCurrentOneDartAvg(),
                checkout.suggested().get(0)
        );

        // Replace an invalid checkout result with a miss.
        Dart result = validateResult(dartThrow.result(), dartBotLegState);

        // Return the checkout target together with the validated result.
        return List.of(new DartThrow(dartThrow.target(), result));
    }

    /**
     * Creates guaranteed throws following the checkout sequence.
     *
     * Each generated throw hits its intended target exactly. The number of throws is limited
     * to the number of darts remaining in the current round.
     *
     * @param checkout       the checkout sequence to follow
     * @param dartsRemaining the number of darts remaining in the round
     * @return the guaranteed checkout throws
     */
    private List<DartThrow> createGuaranteedCheckoutThrows(X01Checkout checkout, int dartsRemaining) {
        // Determine how much of the checkout sequence can be thrown in the current round.
        int dartsRequired = checkout.suggested().size();
        int dartsToUse = Math.min(dartsRemaining, dartsRequired);

        // Create throws where the result exactly matches the intended checkout target.
        return checkout.suggested()
                .stream()
                .limit(dartsToUse)
                .map(dart -> new DartThrow(dart, dart))
                .toList();
    }

    /**
     * Simulates a dart throw at the given target.
     *
     * The bot's target and current one-dart averages determine the maximum radial and angular
     * deviation. These offsets are applied by the dartboard service to determine where the dart lands.
     *
     * @param targetOneDartAvg  the target one-dart average
     * @param currentOneDartAvg the current one-dart average
     * @param target            the dartboard target
     * @return the target and resulting dart
     */
    private DartThrow throwAtTarget(double targetOneDartAvg, double currentOneDartAvg, Dart target) {
        // Generate the radial and angular offsets for the throw.
        double offsetR = dartBotDeviationCalculator.createOffsetR(targetOneDartAvg, currentOneDartAvg);
        double offsetTheta = dartBotDeviationCalculator.createOffsetTheta(targetOneDartAvg, currentOneDartAvg);

        // Apply the offsets to the target and determine where the dart actually lands.
        Dart result = dartboardService.getScore(target, offsetR, offsetTheta);

        // Return both the intended target and actual result.
        return new DartThrow(target, result);
    }

    /**
     * Validates a dart result against the bot's checkout policy.
     *
     * An invalid result is replaced with a miss while preserving the resulting board section.
     *
     * @param result          the dart result to validate
     * @param dartBotLegState the current dart bot leg state
     * @return the original result when valid, otherwise a missed dart
     */
    private Dart validateResult(Dart result, X01DartBotLegState dartBotLegState) {
        if (!dartBotCheckoutPolicy.isDartResultValid(result, dartBotLegState)) {
            return new Dart(result.section(), DartboardSectionArea.MISS);
        }

        return result;
    }
}