package nl.kmartin.dartsmatcherapi.features.x01.x01scorestatistics.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores score statistics for an X01 player.
 *
 * Tracks the number of rounds scored within each X01 score range.
 */
@Getter
@Setter
@NoArgsConstructor
public class X01ScoreStatistics {
    public static final int MINIMUM_FORTY_PLUS = 40;
    public static final int MINIMUM_SIXTY_PLUS = 60;
    public static final int MINIMUM_EIGHTY_PLUS = 80;
    public static final int MINIMUM_TON_PLUS = 100;
    public static final int MINIMUM_TON_FORTY_PLUS = 140;
    public static final int TON_EIGHTY = 180;

    private int fortyPlus;
    private int sixtyPlus;
    private int eightyPlus;
    private int tonPlus;
    private int tonFortyPlus;
    private int tonEighty;

    /**
     * Increments the number of forty-plus scores.
     */
    public void incrementFortyPlus() {
        this.fortyPlus++;
    }

    /**
     * Increments the number of sixty-plus scores.
     */
    public void incrementSixtyPlus() {
        this.sixtyPlus++;
    }

    /**
     * Increments the number of eighty-plus scores.
     */
    public void incrementEightyPlus() {
        this.eightyPlus++;
    }

    /**
     * Increments the number of ton-plus scores.
     */
    public void incrementTonPlus() {
        this.tonPlus++;
    }

    /**
     * Increments the number of ton-forty-plus scores.
     */
    public void incrementTonFortyPlus() {
        this.tonFortyPlus++;
    }

    /**
     * Increments the number of 180 scores.
     */
    public void incrementTonEighty() {
        this.tonEighty++;
    }

    /**
     * Resets all score statistics to their initial values.
     */
    public void reset() {
        this.fortyPlus = 0;
        this.sixtyPlus = 0;
        this.eightyPlus = 0;
        this.tonPlus = 0;
        this.tonFortyPlus = 0;
        this.tonEighty = 0;
    }
}