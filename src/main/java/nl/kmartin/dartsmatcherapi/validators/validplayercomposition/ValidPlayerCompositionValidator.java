package nl.kmartin.dartsmatcherapi.validators.validplayercomposition;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import nl.kmartin.dartsmatcherapi.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateMatchRequest;
import nl.kmartin.dartsmatcherapi.i18n.MessageKeys;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.util.List;
import java.util.Objects;

/**
 * Validates that a list of players follows the configured player composition rules.
 *
 * A match can contain at most one dart bot, and a dart bot must be accompanied by
 * at least one human player.
 */
public class ValidPlayerCompositionValidator implements ConstraintValidator<ValidPlayerComposition, List<?>> {

    /**
     * Validates the composition of the supplied players.
     *
     * Null and empty lists are accepted because separate constraints validate their presence and size.
     * Null elements and player types are handled by separate constraints.
     *
     * @param players the players to validate
     * @param context the validation context
     * @return whether the player composition is valid
     */
    @Override
    public boolean isValid(List<?> players, ConstraintValidatorContext context) {
        if (players == null || players.isEmpty()) return true;

        List<PlayerType> playerTypes = players.stream()
                .filter(Objects::nonNull)
                .map(this::extractPlayerType)
                .toList();

        long botCount = playerTypes.stream()
                .filter(playerType -> playerType == PlayerType.DART_BOT)
                .count();

        // A match can contain at most one dart bot.
        if (botCount > 1) {
            setViolationMessage(context, MessageKeys.MESSAGE_TOO_MANY_BOTS);
            return false;
        }

        // A dart bot cannot play a match without at least one human player.
        boolean hasHumanPlayer = playerTypes.stream()
                .anyMatch(playerType -> playerType == PlayerType.HUMAN);

        if (botCount == 1 && !hasHumanPlayer) {
            setViolationMessage(context, MessageKeys.MESSAGE_BOT_REQUIRES_HUMAN);
            return false;
        }

        return true;
    }

    /**
     * Extracts the player type from a supported player representation.
     *
     * @param player the player representation
     * @return the player's type
     * @throws IllegalArgumentException when the player representation is unsupported
     */
    private PlayerType extractPlayerType(Object player) {
        if (player instanceof MatchPlayer matchPlayer) return matchPlayer.getPlayerType();
        if (player instanceof X01CreateMatchRequest.Player requestPlayer) return requestPlayer.playerType();

        throw new IllegalArgumentException("Unsupported player type: " + player.getClass());
    }

    /**
     * Replaces the default validation message with the message for the violated composition rule.
     *
     * @param context    the validation context
     * @param messageKey the message key for the violated rule
     */
    private void setViolationMessage(ConstraintValidatorContext context, String messageKey) {
        HibernateConstraintValidatorContext hibernateContext =
                context.unwrap(HibernateConstraintValidatorContext.class);

        hibernateContext.disableDefaultConstraintViolation();
        hibernateContext
                .buildConstraintViolationWithTemplate("{" + messageKey + "}")
                .addConstraintViolation();
    }
}