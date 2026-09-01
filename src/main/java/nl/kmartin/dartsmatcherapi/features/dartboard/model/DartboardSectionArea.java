package nl.kmartin.dartsmatcherapi.features.dartboard.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Defines the scoring areas of a dartboard and their score multipliers.
 */
@Getter
@AllArgsConstructor
public enum DartboardSectionArea {
    DOUBLE_BULL(2),
    SINGLE_BULL(1),
    INNER_SINGLE(1),
    TRIPLE(3),
    OUTER_SINGLE(1),
    DOUBLE(2),
    MISS(0);

    private final int multiplier;

    /**
     * Determines whether this area is a single scoring area.
     *
     * @return whether this area is a single
     */
    public boolean isSingle() {
        return this == INNER_SINGLE
                || this == OUTER_SINGLE
                || this == SINGLE_BULL;
    }

    /**
     * Determines whether this area is a double scoring area.
     *
     * @return whether this area is a double
     */
    public boolean isDouble() {
        return this == DOUBLE || this == DOUBLE_BULL;
    }

    /**
     * Determines whether this area is the triple scoring area.
     *
     * @return whether this area is a triple
     */
    public boolean isTriple() {
        return this == TRIPLE;
    }
}