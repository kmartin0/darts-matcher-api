package nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service;

import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.DartboardSection;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.DartboardSectionArea;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * Provides the scoring target strategy for the X01 dart bot.
 *
 * Scoring targets are selected using weighted treble probabilities, with Treble 20
 * always selected when the target one-dart average reaches the configured threshold.
 */
@Service
@Validated
public class X01DartBotScoringStrategyImpl implements IX01DartBotScoringStrategy {
    private static final double GUARANTEED_T20_MINIMUM_ONE_DART_AVERAGE = 50.0;

    private static final List<Map.Entry<DartboardSection, Double>> TREBLE_PROBABILITIES = List.of(
            Map.entry(DartboardSection.TWENTY, 0.83),
            Map.entry(DartboardSection.NINETEEN, 0.10),
            Map.entry(DartboardSection.EIGHTEEN, 0.05),
            Map.entry(DartboardSection.SEVENTEEN, 0.02)
    );

    /**
     * Creates a biased random treble target intended for scoring.
     *
     * Chooses between Treble 20 (83%), Treble 19 (10%), Treble 18 (5%) and Treble 17 (2%).
     * For sufficiently high target averages, Treble 20 is always selected.
     *
     * @param targetOneDartAvg the target one-dart average
     * @return the treble target used for scoring
     */
    @Override
    public Dart createScoringTarget(double targetOneDartAvg) {
        // Return Treble 20 if the target average is a nine darter or better.
        if (targetOneDartAvg >= GUARANTEED_T20_MINIMUM_ONE_DART_AVERAGE) {
            return new Dart(DartboardSection.TWENTY, DartboardSectionArea.TRIPLE);
        }

        // Generate a biased random treble using the static map.
        DartboardSection selectedTreble = selectRandomTreble();
        return new Dart(selectedTreble, DartboardSectionArea.TRIPLE);
    }

    /**
     * Selects a random treble using the configured weighted probabilities.
     *
     * @return the selected treble section
     */
    private DartboardSection selectRandomTreble() {
        double rand = Math.random(); // Generate a random value between 0 and 1
        double cumulativeProbability = 0.0;

        for (Map.Entry<DartboardSection, Double> entry : TREBLE_PROBABILITIES) {
            cumulativeProbability += entry.getValue();
            if (rand <= cumulativeProbability) {
                return entry.getKey(); // Return the selected treble
            }
        }

        // Fallback, should not be reached due to the control of probabilities
        return DartboardSection.TWENTY;
    }
}