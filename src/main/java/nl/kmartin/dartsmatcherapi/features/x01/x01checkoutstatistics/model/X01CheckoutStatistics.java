package nl.kmartin.dartsmatcherapi.features.x01.x01checkoutstatistics.model;

import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01Checkout;

/**
 * Stores checkout statistics for an X01 player.
 *
 * Tracks successful and missed checkouts, checkout percentage, highest checkout
 * and the number of ton-plus checkouts.
 */
@Getter
@Setter
@NoArgsConstructor
public class X01CheckoutStatistics {
    public static final int MINIMUM_TON_PLUS_CHECKOUT = 100;

    @Max(X01Checkout.MAXIMUM_CHECKOUT)
    private int checkoutHighest;

    private int checkoutTonPlus;

    private Integer checkoutPercentage;

    private Integer checkoutsMissed;

    private int checkoutsHit;

    /**
     * Increments the number of ton-plus checkouts.
     */
    public void incrementCheckoutTonPlus() {
        this.checkoutTonPlus++;
    }

    /**
     * Increments the number of successful checkouts.
     */
    public void incrementCheckoutsHit() {
        this.checkoutsHit++;
    }

    /**
     * Resets all checkout statistics to their initial values.
     */
    public void reset() {
        this.checkoutHighest = 0;
        this.checkoutTonPlus = 0;
        this.checkoutPercentage = null;
        this.checkoutsMissed = null;
        this.checkoutsHit = 0;
    }
}