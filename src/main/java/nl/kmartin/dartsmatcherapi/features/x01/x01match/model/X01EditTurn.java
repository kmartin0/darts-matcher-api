package nl.kmartin.dartsmatcherapi.features.x01.x01match.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

/**
 * Represents an edit to an existing turn at a specific position in an X01 match.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class X01EditTurn extends X01Turn {

    public static final int MINIMUM_SET_NUMBER = 1;
    public static final int MINIMUM_LEG_NUMBER = 1;
    public static final int MINIMUM_ROUND_NUMBER = 1;

    @NotNull
    private ObjectId playerId;

    @Min(MINIMUM_SET_NUMBER)
    private int set;

    @Min(MINIMUM_LEG_NUMBER)
    private int leg;

    @Min(MINIMUM_ROUND_NUMBER)
    private int round;
}