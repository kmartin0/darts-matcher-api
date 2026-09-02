package nl.kmartin.dartsmatcherapi.features.x01.x01statistics.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import nl.kmartin.dartsmatcherapi.features.x01.x01averagestatistics.model.X01AverageStatistics;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkoutstatistics.model.X01CheckoutStatistics;
import nl.kmartin.dartsmatcherapi.features.x01.x01resultstatistics.model.X01ResultStatistics;
import nl.kmartin.dartsmatcherapi.features.x01.x01scorestatistics.model.X01ScoreStatistics;

/**
 * Stores all statistics tracked for an X01 player.
 *
 * Groups result, average, checkout and score statistics and provides a single operation
 * for resetting them to their initial state.
 */
@Getter
@Setter
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

    @NotNull
    @Valid
    private X01ScoreStatistics scoreStatistics;

    public X01Statistics() {
        this.resultStatistics = new X01ResultStatistics();
        this.averageStats = new X01AverageStatistics();
        this.checkoutStats = new X01CheckoutStatistics();
        this.scoreStatistics = new X01ScoreStatistics();
    }

    /**
     * Resets all player statistics to their initial values.
     *
     * Missing statistics objects are recreated before continuing.
     */
    public void reset() {
        resultStatistics.reset();
        averageStats.reset();
        checkoutStats.reset();
        scoreStatistics.reset();
    }
}