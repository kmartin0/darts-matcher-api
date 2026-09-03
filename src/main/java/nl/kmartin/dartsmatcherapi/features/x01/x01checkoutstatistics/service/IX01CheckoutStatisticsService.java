package nl.kmartin.dartsmatcherapi.features.x01.x01checkoutstatistics.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkoutstatistics.model.X01CheckoutStatistics;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;

public interface IX01CheckoutStatisticsService {

    /**
     * Updates a player's checkout statistics from a processed round.
     *
     * @param playerCheckoutStats the checkout statistics to update
     * @param playerScore         the score for the processed round
     * @param isCheckout          whether the round resulted in a checkout
     * @param trackDoubles        whether double statistics are tracked
     */
    void updateCheckoutStatistics(
            @NotNull @Valid X01CheckoutStatistics playerCheckoutStats,
            @NotNull @Valid X01LegRoundScore playerScore,
            boolean isCheckout,
            boolean trackDoubles
    );
}