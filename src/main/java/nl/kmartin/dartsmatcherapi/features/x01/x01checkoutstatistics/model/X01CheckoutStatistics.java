package nl.kmartin.dartsmatcherapi.features.x01.x01checkoutstatistics.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01Checkout;

/**
 * Stores checkout statistics for an X01 player.
 *
 * Tracks successful and missed checkouts, checkout percentage, highest checkout
 * and the number of ton-plus checkouts.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class X01CheckoutStatistics {
    public static final int MINIMUM_TON_PLUS_CHECKOUT = 100;

    @Min(X01Checkout.MINIMUM_CHECKOUT)
    @Max(X01Checkout.MAXIMUM_CHECKOUT)
    private Integer checkoutHighest;

    @PositiveOrZero
    private int checkoutTonPlus;

    @PositiveOrZero
    @Max(100)
    private Integer checkoutPercentage;

    @PositiveOrZero
    private Integer checkoutsMissed;

    @PositiveOrZero
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
        this.checkoutHighest = null;
        this.checkoutTonPlus = 0;
        this.checkoutPercentage = null;
        this.checkoutsMissed = null;
        this.checkoutsHit = 0;
    }
}