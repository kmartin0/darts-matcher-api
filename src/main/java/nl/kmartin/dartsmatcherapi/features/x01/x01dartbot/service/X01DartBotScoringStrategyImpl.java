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

    @Override
    public Dart createScoringTarget(double targetOneDartAvg) {
        // Always target Treble 20 once the configured playing-strength threshold is reached.
        if (targetOneDartAvg >= GUARANTEED_T20_MINIMUM_ONE_DART_AVERAGE) {
            return new Dart(DartboardSection.TWENTY, DartboardSectionArea.TRIPLE);
        }

        // Select a weighted treble target for lower playing strengths.
        DartboardSection selectedTreble = selectRandomTreble();
        return new Dart(selectedTreble, DartboardSectionArea.TRIPLE);
    }

    /**
     * Selects a treble using the configured weighted probabilities.
     *
     * @return the selected treble section
     */
    private DartboardSection selectRandomTreble() {
        double randomValue = Math.random();
        double cumulativeProbability = 0.0;

        // Return the first treble whose cumulative probability contains the random value.
        for (Map.Entry<DartboardSection, Double> entry : TREBLE_PROBABILITIES) {
            cumulativeProbability += entry.getValue();

            if (randomValue <= cumulativeProbability) {
                return entry.getKey();
            }
        }

        // Fall back to Treble 20 if floating-point rounding leaves the random value unmatched.
        return DartboardSection.TWENTY;
    }
}