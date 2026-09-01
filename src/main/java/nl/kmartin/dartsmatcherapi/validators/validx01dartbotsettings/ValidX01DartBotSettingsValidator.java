package nl.kmartin.dartsmatcherapi.validators.validx01dartbotsettings;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import nl.kmartin.dartsmatcherapi.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01DartBotSettings;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.i18n.MessageKeys;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

/**
 * Validates that X01 dart bot settings match the player's type.
 *
 * Human players cannot have dart bot settings, while dart bot players are required to have them.
 */
public class ValidX01DartBotSettingsValidator
        implements ConstraintValidator<ValidX01DartBotSettings, X01MatchPlayer> {

    /**
     * Validates the dart bot settings of the supplied match player.
     *
     * Null players are accepted because their validation is handled by separate constraints.
     *
     * @param x01MatchPlayer    the match player to validate
     * @param constraintContext the validation context
     * @return whether the dart bot settings are valid for the player's type
     */
    @Override
    public boolean isValid(
            X01MatchPlayer x01MatchPlayer,
            ConstraintValidatorContext constraintContext
    ) {
        if (x01MatchPlayer == null) {
            return true;
        }

        return validateDartBotSettings(x01MatchPlayer, constraintContext);
    }

    /**
     * Validates whether the player type and dart bot settings are compatible.
     *
     * @param x01MatchPlayer    the match player to validate
     * @param constraintContext the validation context
     * @return whether the player type and dart bot settings are compatible
     */
    private boolean validateDartBotSettings(X01MatchPlayer x01MatchPlayer, ConstraintValidatorContext constraintContext) {
        PlayerType playerType = x01MatchPlayer.getPlayerType();
        X01DartBotSettings dartBotSettings = x01MatchPlayer.getX01DartBotSettings();

        // Null player types are handled by separate validation constraints.
        if (playerType == null) {
            return true;
        }

        return switch (playerType) {
            case HUMAN -> {
                if (dartBotSettings != null) {
                    setDartBotSettingsConstraintViolation(MessageKeys.MESSAGE_X01_DART_BOT_SETTINGS_HUMAN, constraintContext);
                    yield false;
                }

                yield true;
            }
            case DART_BOT -> {
                if (dartBotSettings == null) {
                    setDartBotSettingsConstraintViolation(MessageKeys.MESSAGE_X01_DART_BOT_SETTINGS_BOT_MISSING, constraintContext);
                    yield false;
                }

                yield true;
            }
        };
    }

    /**
     * Replaces the default validation message with the supplied dart bot settings message.
     *
     * @param messageKey        the validation message key
     * @param constraintContext the validation context
     */
    private void setDartBotSettingsConstraintViolation(String messageKey, ConstraintValidatorContext constraintContext) {
        // Use the Hibernate context to provide the custom validation message.
        HibernateConstraintValidatorContext hibernateContext = constraintContext.unwrap(HibernateConstraintValidatorContext.class);

        hibernateContext.disableDefaultConstraintViolation();

        hibernateContext
                .buildConstraintViolationWithTemplate("{" + messageKey + "}")
                .addConstraintViolation();
    }
}