package nl.kmartin.dartsmatcherapi.features.x01.x01leground.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;

import java.util.LinkedHashMap;

/**
 * Represents a round within an X01 leg.
 *
 * Player turns are stored in throwing order so the sequence within the round can be preserved.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class X01LegRound {
    @NotNull
    private LinkedHashMap<@NotNull ObjectId, @NotNull @Valid X01Turn> turns = new LinkedHashMap<>();
}