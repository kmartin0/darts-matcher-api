package nl.kmartin.dartsmatcherapi.features.x01.x01checkoutstatistics.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01checkoutstatistics.model.X01CheckoutStatistics;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapi.util.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Updates X01 checkout statistics from processed leg-round scores.
 */
@Service
@Validated
public class X01CheckoutStatisticsServiceImpl implements IX01CheckoutStatisticsService {

    @Override
    public void updateCheckoutStatistics(
            X01CheckoutStatistics playerCheckoutStats,
            X01LegRoundScore playerScore,
            boolean isCheckout,
            boolean trackDoubles
    ) {
        // Record successful checkout statistics, including the highest and ton-plus counts.
        if (isCheckout) {
            playerCheckoutStats.incrementCheckoutsHit();
            updateHighestCheckout(playerCheckoutStats, playerScore);
            updateTonPlusCheckout(playerCheckoutStats, playerScore);
        }

        // Track missed checkout attempts and recalculate the checkout percentage when enabled.
        if (trackDoubles) {
            updateCheckoutsMissed(playerCheckoutStats, playerScore);
            updateCheckoutPercentage(playerCheckoutStats);
        }
    }

    /**
     * Updates the highest checkout when no checkout has been recorded yet or the current checkout is higher.
     *
     * @param playerCheckoutStats the checkout statistics to update
     * @param playerScore         the score for the current round
     */
    private void updateHighestCheckout(X01CheckoutStatistics playerCheckoutStats, X01LegRoundScore playerScore) {
        int checkoutScore = playerScore.getScore();
        Integer checkoutHighest = playerCheckoutStats.getCheckoutHighest();

        if (checkoutHighest == null || checkoutScore > checkoutHighest) {
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

        playerCheckoutStats.setCheckoutsMissed(checkoutsMissed + doublesMissed);
    }

    /**
     * Updates the checkout percentage based on successful and missed checkout attempts.
     *
     * @param playerCheckoutStats the checkout statistics to update
     */
    private void updateCheckoutPercentage(X01CheckoutStatistics playerCheckoutStats) {
        int checkoutAttempts = playerCheckoutStats.getCheckoutsHit() + playerCheckoutStats.getCheckoutsMissed();

        int checkoutPercentage = NumberUtils.calcPercentage(
                playerCheckoutStats.getCheckoutsHit(),
                checkoutAttempts
        );

        playerCheckoutStats.setCheckoutPercentage(checkoutPercentage);
    }
}