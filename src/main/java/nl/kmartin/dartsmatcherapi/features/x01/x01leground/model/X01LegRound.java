package nl.kmartin.dartsmatcherapi.features.x01.x01leground.model;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.LinkedHashMap;

/**
 * Represents a round within an X01 leg.
 *
 * Player scores are stored in throwing order so the sequence of turns within
 * the round can be preserved.
 */
@Getter
@Setter
@NoArgsConstructor
public class X01LegRound {
    @Valid
    private LinkedHashMap<ObjectId, X01LegRoundScore> scores = new LinkedHashMap<>();

    public X01LegRound(LinkedHashMap<ObjectId, X01LegRoundScore> scores) {
        this.setScores(scores);
    }

    /**
     * Sets the round scores, replacing a null value with an empty ordered map.
     *
     * @param scores the player scores in throwing order
     */
    public void setScores(@Valid LinkedHashMap<ObjectId, X01LegRoundScore> scores) {
        this.scores = scores != null ? scores : new LinkedHashMap<>();
    }
}