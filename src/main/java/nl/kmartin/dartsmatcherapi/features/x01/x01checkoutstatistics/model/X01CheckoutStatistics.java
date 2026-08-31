package nl.kmartin.dartsmatcherapi.features.x01.x01checkoutstatistics.model;

import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01Checkout;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01CheckoutStatistics {
    @Max(X01Checkout.MAXIMUM_CHECKOUT)
    private int checkoutHighest;

    private int checkoutTonPlus;

    private Integer checkoutPercentage;

    private Integer checkoutsMissed;

    private int checkoutsHit;

    public void incrementCheckoutTonPlus() {
        this.checkoutTonPlus++;
    }

    public void incrementCheckoutsHit() {
        this.checkoutsHit++;
    }

    public void reset() {
        this.checkoutHighest = 0;
        this.checkoutTonPlus = 0;
        this.checkoutPercentage = null;
        this.checkoutsMissed = null;
        this.checkoutsHit = 0;
    }
}