package nl.kmartin.dartsmatcherapi.features.x01.x01checkoutstatistics;

import nl.kmartin.dartsmatcherapi.features.x01.x01checkoutstatistics.model.X01CheckoutStatistics;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;

public interface IX01CheckoutStatisticsService {
    void updateCheckoutStatistics(X01CheckoutStatistics playerCheckoutStats, X01LegRoundScore playerScore, boolean isCheckout, boolean trackDoubles);
}
