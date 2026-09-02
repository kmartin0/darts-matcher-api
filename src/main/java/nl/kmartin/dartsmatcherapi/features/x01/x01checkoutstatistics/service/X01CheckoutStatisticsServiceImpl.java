package nl.kmartin.dartsmatcherapi.features.x01.x01checkoutstatistics.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01checkoutstatistics.model.X01CheckoutStatistics;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapi.utils.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Calculates and updates checkout statistics for X01 players.
 *
 * Tracks successful and missed checkouts, checkout percentage, highest checkout
 * and the number of ton-plus checkouts.
 */
@Service
@Validated
public class X01CheckoutStatisticsServiceImpl implements IX01CheckoutStatisticsService {
    /**
     * Updates the player's checkout statistics for the current round.
     *
     * @param playerCheckoutStats the checkout statistics to update
     * @param playerScore         the score for the current round
     * @param isCheckout          whether the round resulted in a successful checkout
     * @param trackDoubles        whether missed doubles and checkout percentage are tracked
     */
    @Override
    public void updateCheckoutStatistics(
            X01CheckoutStatistics playerCheckoutStats,
            X01LegRoundScore playerScore,
            boolean isCheckout,
            boolean trackDoubles
    ) {
        if (isCheckout) {
            playerCheckoutStats.incrementCheckoutsHit();
            updateHighestCheckout(playerCheckoutStats, playerScore);
            updateTonPlusCheckout(playerCheckoutStats, playerScore);
        }

        if (trackDoubles) {
            updateCheckoutsMissed(playerCheckoutStats, playerScore);
            updateCheckoutPercentage(playerCheckoutStats);
        }
    }

    /**
     * Updates the highest checkout when the current checkout is higher.
     *
     * @param playerCheckoutStats the checkout statistics to update
     * @param playerScore         the score for the current round
     */
    private void updateHighestCheckout(X01CheckoutStatistics playerCheckoutStats, X01LegRoundScore playerScore) {
        int checkoutScore = playerScore.getScore();

        if (checkoutScore > playerCheckoutStats.getCheckoutHighest()) {
            playerCheckoutStats.setCheckoutHighest(checkoutScore);
        }
    }

    /**
     * Updates the number of ton-plus checkouts.
     *
     * @param playerCheckoutStats the checkout statistics to update
     * @param playerScore         the score for the current round
     */
    private void updateTonPlusCheckout(X01CheckoutStatistics playerCheckoutStats, X01LegRoundScore playerScore) {
        if (playerScore.getScore() >= X01CheckoutStatistics.MINIMUM_TON_PLUS_CHECKOUT) {
            playerCheckoutStats.incrementCheckoutTonPlus();
        }
    }

    /**
     * Updates the number of missed checkout attempts.
     *
     * @param playerCheckoutStats the checkout statistics to update
     * @param playerScore         the score for the current round
     */
    private void updateCheckoutsMissed(X01CheckoutStatistics playerCheckoutStats, X01LegRoundScore playerScore) {
        int doublesMissed = playerScore.getDoublesMissed() != null
                ? playerScore.getDoublesMissed()
                : 0;

        int checkoutsMissed = playerCheckoutStats.getCheckoutsMissed() != null
                ? playerCheckoutStats.getCheckoutsMissed()
                : 0;

        playerCheckoutStats.setCheckoutsMissed(
                checkoutsMissed + doublesMissed
        );
    }

    /**
     * Updates the checkout percentage based on successful and missed checkout attempts.
     *
     * @param playerCheckoutStats the checkout statistics to update
     */
    private void updateCheckoutPercentage(X01CheckoutStatistics playerCheckoutStats) {
        int checkoutAttempts = playerCheckoutStats.getCheckoutsHit()
                + playerCheckoutStats.getCheckoutsMissed();

        int checkoutPercentage = NumberUtils.calcPercentage(
                playerCheckoutStats.getCheckoutsHit(),
                checkoutAttempts
        );

        playerCheckoutStats.setCheckoutPercentage(checkoutPercentage);
    }
}