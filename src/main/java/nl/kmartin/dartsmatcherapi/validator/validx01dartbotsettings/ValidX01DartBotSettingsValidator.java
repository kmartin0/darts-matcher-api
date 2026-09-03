package nl.kmartin.dartsmatcherapi.validator.validx01dartbotsettings;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.validator.ConstraintViolationsHelper;

/**
 * Validates that dart bot settings match the type of X01 match player.
 */
public class ValidX01DartBotSettingsValidator implements ConstraintValidator<ValidX01DartBotSettings, X01MatchPlayer> {

    /**
     * Validates the dart bot settings of the supplied match player.
     *
     * Null players are accepted because nullability is handled by separate constraints.
     *
     * @param player  the match player to validate
     * @param context the validation context
     * @return whether the dart bot settings are valid for the player's type
     */
    @Override
    public boolean isValid(X01MatchPlayer player, ConstraintValidatorContext context) {
        if (player == null) return true;

        String messageKey = X01DartBotSettingsValidation.getValidationMessage(
                player.getPlayerType(),
                player.getX01DartBotSettings()
        );

        if (messageKey == null) return true;

        ConstraintViolationsHelper.addViolation(
                context,
                messageKey,
                X01DartBotSettingsValidation.FIELD_DART_BOT_SETTINGS
        );
        return false;
    }
}