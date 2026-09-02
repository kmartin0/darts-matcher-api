package nl.kmartin.dartsmatcherapi.features.x01.x01leground.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class X01LegRound {
    @NotNull
    private LinkedHashMap<@NotNull ObjectId, @NotNull @Valid X01LegRoundScore> scores = new LinkedHashMap<>();
}