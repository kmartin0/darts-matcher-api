package nl.kmartin.dartsmatcherapi.features.x01.x01match.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

/**
 * Represents a request to edit an existing turn at a specific position in an X01 match.
 */
@Getter
@Setter
@NoArgsConstructor
public class X01EditTurnRequest extends X01CreateTurnRequest {

    @NotNull
    private ObjectId playerId;

    @Positive
    private int set;

    @Positive
    private int leg;

    @Positive
    private int round;
}
