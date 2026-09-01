package nl.kmartin.dartsmatcherapi.features.x01.x01match.model;

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
@AllArgsConstructor
@NoArgsConstructor
public class X01MatchProgress {

    private Integer currentSet;
    private Integer currentLeg;
    private Integer currentRound;
    private ObjectId currentThrower;
}