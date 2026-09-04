package nl.kmartin.dartsmatcherapi.features.x01.x01checkoutstatistics.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01checkoutstatistics.model.X01CheckoutStatistics;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01Turn;
import nl.kmartin.dartsmatcherapi.util.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Updates X01 checkout statistics from processed turns.
 */
@Service
@Validated
public class X01CheckoutStatisticsServiceImpl implements IX01CheckoutStatisticsService {

    @Override
    public void updateCheckoutStatistics(
            X01CheckoutStatistics playerCheckoutStats,
            X01Turn playerTurn,
            boolean isCheckout,
            boolean trackDoubles
    ) {
        // Record successful checkout statistics, including the highest and ton-plus counts.
        if (isCheckout) {
            playerCheckoutStats.incrementCheckoutsHit();
            updateHighestCheckout(playerCheckoutStats, playerTurn);
            updateTonPlusCheckout(playerCheckoutStats, playerTurn);
        }

        // Track missed checkout attempts and recalculate the checkout percentage when enabled.
        if (trackDoubles) {
            updateCheckoutsMissed(playerCheckoutStats, playerTurn);
            updateCheckoutPercentage(playerCheckoutStats);
        }
    }

    /**
     * Updates the highest checkout when no checkout has been recorded yet or the current checkout is higher.
     *
     * @param playerCheckoutStats the checkout statistics to update
     * @param playerTurn          the current turn
     */
    private void updateHighestCheckout(X01CheckoutStatistics playerCheckoutStats, X01Turn playerTurn) {
        int checkoutScore = playerTurn.getScore();
        Integer checkoutHighest = playerCheckoutStats.getCheckoutHighest();

        if (checkoutHighest == null || checkoutScore > checkoutHighest) {
            playerCheckoutStats.setCheckoutHighest(checkoutScore);
        }
    }

    /**
     * Updates the number of ton-plus checkouts.
     *
     * @param playerCheckoutStats the checkout statistics to update
     * @param playerTurn          the current turn
     */
    private void updateTonPlusCheckout(X01CheckoutStatistics playerCheckoutStats, X01Turn playerTurn) {
        if (playerTurn.getScore() >= X01CheckoutStatistics.MINIMUM_TON_PLUS_CHECKOUT) {
            playerCheckoutStats.incrementCheckoutTonPlus();
        }
    }

    /**
     * Updates the number of missed checkout attempts.
     *
     * @param playerCheckoutStats the checkout statistics to update
     * @param playerTurn          the current turn
     */
    private void updateCheckoutsMissed(X01CheckoutStatistics playerCheckoutStats, X01Turn playerTurn) {
        int doublesMissed = playerTurn.getDoublesMissed() != null
                ? playerTurn.getDoublesMissed()
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