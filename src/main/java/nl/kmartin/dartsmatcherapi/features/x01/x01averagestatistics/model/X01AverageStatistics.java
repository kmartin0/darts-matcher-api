package nl.kmartin.dartsmatcherapi.features.x01.x01averagestatistics.model;

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
    private int pointsThrown;
    private int dartsThrown;
    private int average;
    private int pointsThrownFirstNine;
    private int dartsThrownFirstNine;
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