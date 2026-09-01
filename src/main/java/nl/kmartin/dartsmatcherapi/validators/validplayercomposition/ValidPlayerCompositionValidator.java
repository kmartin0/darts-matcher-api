package nl.kmartin.dartsmatcherapi.validators.validplayercomposition;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import nl.kmartin.dartsmatcherapi.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapi.i18n.MessageKeys;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Validates that the player composition of a match follows the configured player rules.
 *
 * A match can contain at most one dart bot, and a dart bot must be accompanied by
 * at least one human player.
 */
public class ValidPlayerCompositionValidator
        implements ConstraintValidator<ValidPlayerComposition, List<? extends MatchPlayer>> {

    /**
     * Validates the composition of the supplied match players.
     *
     * Null and empty lists are accepted because their validation is handled by separate constraints.
     *
     * @param matchPlayers      the match players to validate
     * @param constraintContext the validation context
     * @return whether the player composition is valid
     */
    @Override
    public boolean isValid(List<? extends MatchPlayer> matchPlayers, ConstraintValidatorContext constraintContext) {
        if (CollectionUtils.isEmpty(matchPlayers)) {
            return true;
        }

        long botCount = matchPlayers.stream()
                .filter(matchPlayer -> matchPlayer.getPlayerType() == PlayerType.DART_BOT)
                .count();

        // A match can contain at most one dart bot.
        if (botCount > 1) {
            setViolationMessage(constraintContext, MessageKeys.MESSAGE_TOO_MANY_BOTS);
            return false;
        }

        // A dart bot cannot play a match without at least one human player.
        boolean hasHumanPlayer = matchPlayers.stream()
                .anyMatch(matchPlayer -> matchPlayer.getPlayerType() == PlayerType.HUMAN);

        if (botCount == 1 && !hasHumanPlayer) {
            setViolationMessage(constraintContext, MessageKeys.MESSAGE_BOT_REQUIRES_HUMAN);
            return false;
        }

        return true;
    }

    /**
     * Replaces the default validation message with the message for the violated composition rule.
     *
     * @param constraintContext the validation context
     * @param messageKey        the message key for the violated rule
     */
    private void setViolationMessage(ConstraintValidatorContext constraintContext, String messageKey) {
        // Use the Hibernate context to provide the custom validation message.
        HibernateConstraintValidatorContext hibernateContext = constraintContext.unwrap(HibernateConstraintValidatorContext.class);

        hibernateContext.disableDefaultConstraintViolation();

        hibernateContext
                .buildConstraintViolationWithTemplate("{" + messageKey + "}")
                .addConstraintViolation();
    }
}