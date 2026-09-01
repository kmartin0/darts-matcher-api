package nl.kmartin.dartsmatcherapi.validators.validdartscore;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

/**
 * Validates whether a score can be achieved with a maximum of three darts.
 */
public class ValidDartScoreValidator implements ConstraintValidator<ValidDartScore, Integer> {
    private static final int MINIMUM_SCORE = 0;
    private static final int MAXIMUM_SCORE = 180;

    private static final Set<Integer> IMPOSSIBLE_SCORES = Set.of(179, 178, 176, 175, 173, 172, 169, 166, 163);

    /**
     * Determines whether the supplied score is a valid three-dart score.
     *
     * @param value   the score to validate
     * @param context the validation context
     * @return whether the score can be achieved with up to three darts
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        // Null values are handled by separate validation constraints.
        if (value == null) {
            return true;
        }

        if (value < MINIMUM_SCORE || value > MAXIMUM_SCORE) {
            return false;
        }

        return !IMPOSSIBLE_SCORES.contains(value);
    }
}