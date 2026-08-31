package nl.kmartin.dartsmatcherapi.features.x01.x01match.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class X01EditTurn extends X01Turn {
    public static final int MINIMUM_SET_NUMBER = 1;
    public static final int MINIMUM_LEG_NUMBER = 1;
    public static final int MINIMUM_ROUND_NUMBER = 1;

    public X01EditTurn(int score, int dartsUsed, Integer doublesMissed, ObjectId playerId, int set, int leg, int round) {
        super(score, dartsUsed, doublesMissed);
        this.playerId = playerId;
        this.set = set;
        this.leg = leg;
        this.round = round;
    }

    @NotNull
    private ObjectId playerId;

    @Min(MINIMUM_SET_NUMBER)
    private int set;

    @Min(MINIMUM_LEG_NUMBER)
    private int leg;

    @Min(MINIMUM_ROUND_NUMBER)
    private int round;
}