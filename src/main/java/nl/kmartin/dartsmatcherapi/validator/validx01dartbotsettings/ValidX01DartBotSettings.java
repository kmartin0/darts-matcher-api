package nl.kmartin.dartsmatcherapi.validator.validx01dartbotsettings;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validates that an X01 player's dart bot settings match its player type.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = {
        ValidX01DartBotSettingsValidator.class,
        ValidX01CreateDartBotSettingsValidator.class
})
@Documented
public @interface ValidX01DartBotSettings {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}