package nl.kmartin.dartsmatcherapi.validators.validplayercomposition;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validates that a list of players follows the match player composition rules.
 */
@Target({FIELD, RECORD_COMPONENT})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidPlayerCompositionValidator.class)
@Documented
public @interface ValidPlayerComposition {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}