package nl.kmartin.dartsmatcher.api.features.x01.x01doubleinstatistics.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Stores double-in statistics for an X01 player.
 *
 * Tracks legs successfully opened, missed opening doubles and opening percentage.
 * Misses are null when not tracked; percentage is null when not tracked
 * or when no opening attempts have occurred.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class X01DoubleInStatistics {

    @PositiveOrZero
    private int doublesHit;

    @PositiveOrZero
    private Integer doublesMissed;

    @PositiveOrZero
    @Max(100)
    private Integer doublesPercentage;

    /**
     * Increments the number of legs in which the player successfully opened.
     */
    public void incrementDoublesHit() {
        this.doublesHit++;
    }

    /**
     * Resets all double-in statistics to their initial values.
     */
    public void reset() {
        this.doublesHit = 0;
        this.doublesMissed = null;
        this.doublesPercentage = null;
    }
}