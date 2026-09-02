package nl.kmartin.dartsmatcherapi.features.x01.x01checkoutstatistics.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkoutstatistics.model.X01CheckoutStatistics;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundScore;

public interface IX01CheckoutStatisticsService {

    void updateCheckoutStatistics(
            @NotNull @Valid X01CheckoutStatistics playerCheckoutStats,
            @NotNull @Valid X01LegRoundScore playerScore,
            boolean isCheckout,
            boolean trackDoubles
    );
}