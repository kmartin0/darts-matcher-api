package nl.kmartin.dartsmatcherapi.validators.validx01dartbotsettings;

import jakarta.validation.ConstraintValidatorContext;
import nl.kmartin.dartsmatcherapi.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01DartBotSettings;
import nl.kmartin.dartsmatcherapi.i18n.MessageKeys;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

/**
 * Provides shared validation logic for X01 dart bot settings.
 */
final class X01DartBotSettingsValidation {

    private static final String FIELD_DART_BOT_SETTINGS = "x01DartBotSettings";

    private X01DartBotSettingsValidation() {
    }

    /**
     * Determines whether the player type and dart bot settings are compatible.
     *
     * @param playerType      the player type
     * @param dartBotSettings the configured dart bot settings
     * @return the validation message key when invalid, otherwise null
     */
    static String getValidationMessage(PlayerType playerType, X01DartBotSettings dartBotSettings) {
        if (playerType == null) return null;

        return switch (playerType) {
            case HUMAN -> dartBotSettings != null
                    ? MessageKeys.MESSAGE_X01_DART_BOT_SETTINGS_HUMAN
                    : null;
            case DART_BOT -> dartBotSettings == null
                    ? MessageKeys.MESSAGE_X01_DART_BOT_SETTINGS_BOT_MISSING
                    : null;
        };
    }

    /**
     * Adds a validation violation to the dart bot settings property.
     *
     * @param messageKey the validation message key
     * @param context    the validation context
     */
    static void addConstraintViolation(String messageKey, ConstraintValidatorContext context) {
        HibernateConstraintValidatorContext hibernateContext = context.unwrap(HibernateConstraintValidatorContext.class);

        hibernateContext.disableDefaultConstraintViolation();
        hibernateContext
                .buildConstraintViolationWithTemplate("{" + messageKey + "}")
                .addPropertyNode(FIELD_DART_BOT_SETTINGS)
                .addConstraintViolation();
    }
}
