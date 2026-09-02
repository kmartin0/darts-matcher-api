package nl.kmartin.dartsmatcherapi.validators.validx01dartbotsettings;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateMatchRequest;

/**
 * Validates that dart bot settings match the player type in an X01 create-match request.
 */
public class ValidX01CreateDartBotSettingsValidator implements ConstraintValidator<ValidX01DartBotSettings, X01CreateMatchRequest.Player> {

    /**
     * Validates the dart bot settings of the supplied create-match player.
     *
     * Null players are accepted because nullability is handled by separate constraints.
     *
     * @param player  the create-match player to validate
     * @param context the validation context
     * @return whether the dart bot settings are valid for the player's type
     */
    @Override
    public boolean isValid(X01CreateMatchRequest.Player player, ConstraintValidatorContext context) {
        if (player == null) return true;

        String messageKey = X01DartBotSettingsValidation.getValidationMessage(
                player.playerType(),
                player.x01DartBotSettings()
        );

        if (messageKey == null) return true;

        X01DartBotSettingsValidation.addConstraintViolation(messageKey, context);
        return false;
    }
}
