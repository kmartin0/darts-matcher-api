package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service;

import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.service.IX01CheckoutService;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model.X01DartBotTurnSnapshot;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model.X01DartBotTurnState;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Defines the checkout validation policy for the X01 dart bot.
 *
 * Determines whether a dart result is valid based on the remaining score,
 * checkout rules, and the bot's target number of darts for completing the leg.
 */
@Service
@Validated
public class X01DartBotCheckoutPolicyImpl implements IX01DartBotCheckoutPolicy {

    private final IX01CheckoutService checkoutService;

    public X01DartBotCheckoutPolicyImpl(IX01CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @Override
    public boolean isDartResultValid(Dart result, X01DartBotTurnSnapshot dartBotTurnSnapshot) {
        // Calculate the remaining score if the simulated dart were accepted.
        int remainingAfterThrow = getRemainingAfterThrow(result, dartBotTurnSnapshot.remainingPoints());

        // Accept throws that neither check out the leg nor result in a bust.
        if (!checkoutService.isRemainingZeroOrBust(remainingAfterThrow)) return true;

        // A zero or bust result must satisfy the bot-specific checkout rules.
        return isBotCheckoutValid(result, dartBotTurnSnapshot, remainingAfterThrow);
    }

    @Override
    public boolean isTargetNumOfDartsReached(int dartsThrown, int targetNumOfDarts) {
        return dartsThrown >= targetNumOfDarts;
    }

    /**
     * Determines whether a simulated checkout satisfies the X01 checkout rules and the bot's target dart count.
     *
     * @param result              the dart result
     * @param dartBotTurnSnapshot the current dart bot turn snapshot
     * @param remainingAfterThrow the remaining score after the throw
     * @return whether the checkout is valid
     */
    private boolean isBotCheckoutValid(Dart result, X01DartBotTurnSnapshot dartBotTurnSnapshot, int remainingAfterThrow) {
        // Include the simulated checkout dart when evaluating the target dart count.
        int dartsThrownAfterCheckout = dartBotTurnSnapshot.dartsUsedInLeg() + 1;
        int targetNumOfDarts = dartBotTurnSnapshot.targetNumOfDarts();

        // The throw must complete a legal checkout without finishing before the bot's target dart count.
        return checkoutService.isValidCheckout(remainingAfterThrow, result)
                && isTargetNumOfDartsReached(dartsThrownAfterCheckout, targetNumOfDarts);
    }

    /**
     * Calculates the remaining score after a dart throw.
     *
     * @param result               the dart result
     * @param remainingBeforeThrow the remaining score before the throw
     * @return the remaining score after the throw
     */
    private int getRemainingAfterThrow(Dart result, int remainingBeforeThrow) {
        return remainingBeforeThrow - result.getScore();
    }
}