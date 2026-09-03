package nl.kmartin.dartsmatcherapi.validator.validdartscore;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import nl.kmartin.dartsmatcherapi.i18n.MessageKeys;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a field contains a score achievable with a maximum of three darts.
 */
@Documented
@Constraint(validatedBy = ValidDartScoreValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDartScore {
    String message() default "{" + MessageKeys.MESSAGE_X01_INVALID_SCORE + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
