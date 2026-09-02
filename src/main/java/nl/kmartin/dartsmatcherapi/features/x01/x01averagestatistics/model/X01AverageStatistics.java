package nl.kmartin.dartsmatcherapi.features.x01.x01averagestatistics.model;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores average statistics for an X01 player.
 *
 * Tracks the total and first-nine points, darts and three-dart averages.
 */
@Getter
@Setter
@NoArgsConstructor
public class X01AverageStatistics {
    @PositiveOrZero
    private int pointsThrown;

    @PositiveOrZero
    private int dartsThrown;

    @PositiveOrZero
    private int average;

    @PositiveOrZero
    private int pointsThrownFirstNine;

    @PositiveOrZero
    private int dartsThrownFirstNine;

    @PositiveOrZero
    private int averageFirstNine;

    /**
     * Resets all average statistics to their initial values.
     */
    public void reset() {
        this.pointsThrown = 0;
        this.dartsThrown = 0;
        this.average = 0;
        this.pointsThrownFirstNine = 0;
        this.dartsThrownFirstNine = 0;
        this.averageFirstNine = 0;
    }
}