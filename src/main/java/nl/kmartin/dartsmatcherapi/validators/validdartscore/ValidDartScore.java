package nl.kmartin.dartsmatcherapi.validators.validdartscore;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import nl.kmartin.dartsmatcherapi.i18n.MessageKeys;

import java.lang.annotation.*;

/**
 * Validates that a field contains a score achievable with a maximum of three darts.
 */
@Documented
@Constraint(validatedBy = ValidDartScoreValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDartScore {
    String message() default "{" + MessageKeys.MESSAGE_X01_INVALID_SCORE + "}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
