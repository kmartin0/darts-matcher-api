package nl.kmartin.dartsmatcherapi.validator.noduplicatematchplayername;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import nl.kmartin.dartsmatcherapi.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateMatchRequest;
import nl.kmartin.dartsmatcherapi.i18n.MessageKeys;
import nl.kmartin.dartsmatcherapi.validator.ConstraintViolationsHelper;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validates that all supported match-player representations have unique player names.
 *
 * Null players and null or empty player names are ignored because separate constraints validate them.
 */
public class NoDuplicateMatchPlayerNameValidator
        implements ConstraintValidator<NoDuplicateMatchPlayerName, List<?>> {

    /**
     * Checks whether all supplied players have unique names.
     *
     * Null lists are accepted because nullability is handled by separate constraints.
     *
     * @param players the players to validate
     * @param context the validation context
     * @return whether all non-empty player names are unique
     */
    @Override
    public boolean isValid(List<?> players, ConstraintValidatorContext context) {
        if (players == null) return true;

        return arePlayerNamesUnique(players, context);
    }

    /**
     * Checks the player names and creates a validation violation for the first duplicate found.
     *
     * @param players the players to validate
     * @param context the validation context
     * @return whether all non-empty player names are unique
     */
    private boolean arePlayerNamesUnique(List<?> players, ConstraintValidatorContext context) {
        Set<String> playerNames = new HashSet<>();

        for (Object player : players) {
            if (player == null) continue;

            String playerName = extractPlayerName(player);

            // Null and empty names are handled by separate validation constraints.
            if (playerName == null || playerName.isEmpty()) continue;

            // Set.add returns false when the name already exists in the set.
            if (!playerNames.add(playerName)) {
                ConstraintViolationsHelper.addViolation(
                        context,
                        MessageKeys.MESSAGE_PLAYER_NAME_DUPLICATE,
                        Map.of(MessageKeys.Params.NAME, playerName)
                );
                return false;
            }
        }

        return true;
    }

    /**
     * Extracts the player name from a supported player representation.
     *
     * @param player the player representation
     * @return the player's name
     * @throws IllegalArgumentException when the player representation is unsupported
     */
    private String extractPlayerName(Object player) {
        if (player instanceof MatchPlayer matchPlayer) return matchPlayer.getPlayerName();
        if (player instanceof X01CreateMatchRequest.Player requestPlayer) return requestPlayer.playerName();

        throw new IllegalArgumentException("Unsupported player type: " + player.getClass());
    }

    /**
     * Replaces the default validation message with one containing the duplicate player name.
     *
     * @param duplicateName the duplicated player name
     * @param context       the validation context
     */
    private void setDuplicateNameViolationMessage(String duplicateName, ConstraintValidatorContext context) {
        HibernateConstraintValidatorContext hibernateContext =
                context.unwrap(HibernateConstraintValidatorContext.class);

        hibernateContext.disableDefaultConstraintViolation();
        hibernateContext
                .addMessageParameter(MessageKeys.Params.NAME, duplicateName)
                .buildConstraintViolationWithTemplate("{" + MessageKeys.MESSAGE_PLAYER_NAME_DUPLICATE + "}")
                .addConstraintViolation();
    }
}