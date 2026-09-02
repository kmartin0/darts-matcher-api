package nl.kmartin.dartsmatcherapi.features.x01.x01leg.model;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Map;

/**
 * Represents a numbered X01 leg.
 *
 * @param legNumber the leg number
 * @param leg       the leg
 */
public record X01LegEntry(
        @Positive int legNumber,
        @NotNull @Valid X01Leg leg
) {

    /**
     * Creates a leg entry from a map entry.
     *
     * @param mapEntry the numbered leg map entry
     */
    public X01LegEntry(Map.Entry<Integer, X01Leg> mapEntry) {
        this(mapEntry.getKey(), mapEntry.getValue());
    }
}
