package nl.kmartin.dartsmatcherapi.features.x01.x01averagestatistics.model;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Stores average statistics for an X01 player.
 *
 * Tracks the total and first-nine points, darts and three-dart averages.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class X01AverageStatistics {
    public static final int ROUND_COUNT_FIRST_NINE = 3;

    @PositiveOrZero
    private int pointsThrown;

    @PositiveOrZero
    private int dartsThrown;

    @PositiveOrZero
    private Integer average;

    @PositiveOrZero
    private int pointsThrownFirstNine;

    @PositiveOrZero
    private int dartsThrownFirstNine;

    @PositiveOrZero
    private Integer averageFirstNine;

    /**
     * Resets all average statistics to their initial values.
     */
    public void reset() {
        this.pointsThrown = 0;
        this.dartsThrown = 0;
        this.average = null;
        this.pointsThrownFirstNine = 0;
        this.dartsThrownFirstNine = 0;
        this.averageFirstNine = null;
    }
}