package nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class X01CheckoutInsufficientDartsException extends RuntimeException {

    private final int score;
    private final int dartsUsed;

    public X01CheckoutInsufficientDartsException(int score, int dartsUsed) {
        this.score = score;
        this.dartsUsed = dartsUsed;
    }
}
