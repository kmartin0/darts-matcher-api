package nl.kmartin.dartsmatcherapi.features.x01.x01match.model;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

/**
 * Stores the current set, leg, round and thrower within an X01 match.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class X01MatchProgress {

    @Positive
    private Integer currentSet;

    @Positive
    private Integer currentLeg;

    @Positive
    private Integer currentRound;

    private ObjectId currentThrower;
}