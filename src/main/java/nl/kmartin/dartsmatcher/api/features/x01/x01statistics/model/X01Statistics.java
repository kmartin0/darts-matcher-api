package nl.kmartin.dartsmatcher.api.features.x01.x01statistics.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.kmartin.dartsmatcher.api.features.x01.x01averagestatistics.model.X01AverageStatistics;
import nl.kmartin.dartsmatcher.api.features.x01.x01checkoutstatistics.model.X01CheckoutStatistics;
import nl.kmartin.dartsmatcher.api.features.x01.x01doubleinstatistics.model.X01DoubleInStatistics;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01MatchFormat;
import nl.kmartin.dartsmatcher.api.features.x01.x01resultstatistics.model.X01ResultStatistics;
import nl.kmartin.dartsmatcher.api.features.x01.x01scorestatistics.model.X01ScoreStatistics;

/**
 * Stores all statistics tracked for an X01 player.
 *
 * Groups result, average, checkout, double-in and score statistics and provides
 * a single operation for resetting them to their initial state.
 * Double-in statistics are null for SIDO matches.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class X01Statistics {
    @NotNull
    @Valid
    private X01ResultStatistics resultStatistics;

    @NotNull
    @Valid
    private X01AverageStatistics averageStats;

    @NotNull
    @Valid
    private X01CheckoutStatistics checkoutStats;

    @Valid
    private X01DoubleInStatistics doubleInStats;

    @NotNull
    @Valid
    private X01ScoreStatistics scoreStatistics;

    public X01Statistics() {
        this.resultStatistics = new X01ResultStatistics();
        this.averageStats = new X01AverageStatistics();
        this.checkoutStats = new X01CheckoutStatistics();
        this.doubleInStats = null;
        this.scoreStatistics = new X01ScoreStatistics();
    }

    public X01Statistics(X01MatchFormat format) {
        this.resultStatistics = new X01ResultStatistics();
        this.averageStats = new X01AverageStatistics();
        this.checkoutStats = new X01CheckoutStatistics();
        this.doubleInStats = format == X01MatchFormat.DIDO
                ? new X01DoubleInStatistics()
                : null;
        this.scoreStatistics = new X01ScoreStatistics();
    }

    /**
     * Resets all player statistics to their initial values for the match format.
     *
     * @param format the match format
     */
    public void reset(X01MatchFormat format) {
        resultStatistics.reset();
        averageStats.reset();
        checkoutStats.reset();
        scoreStatistics.reset();

        switch (format) {
            case SIDO -> doubleInStats = null;
            case DIDO -> {
                if (doubleInStats == null) {
                    doubleInStats = new X01DoubleInStatistics();
                } else {
                    doubleInStats.reset();
                }
            }
        }
    }
}