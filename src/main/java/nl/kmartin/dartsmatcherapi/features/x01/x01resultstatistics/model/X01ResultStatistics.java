package nl.kmartin.dartsmatcherapi.features.x01.x01resultstatistics.model;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Stores result statistics for an X01 player.
 *
 * Tracks the number of sets and legs won by the player.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class X01ResultStatistics {
    @PositiveOrZero
    private int setsWon;

    @PositiveOrZero
    private int legsWon;

    /**
     * Increments the number of sets won.
     */
    public void incrementSetsWon() {
        this.setsWon++;
    }

    /**
     * Increments the number of legs won.
     */
    public void incrementLegsWon() {
        this.legsWon++;
    }

    /**
     * Resets all result statistics to their initial values.
     */
    public void reset() {
        this.setsWon = 0;
        this.legsWon = 0;
    }
}