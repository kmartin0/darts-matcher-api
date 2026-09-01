package nl.kmartin.dartsmatcherapi.validators.noduplicatematchplayername;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import nl.kmartin.dartsmatcherapi.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapi.i18n.MessageKeys;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates that all match players have unique player names.
 *
 * Null and empty player names are ignored because they are handled by separate field validators.
 */
public class NoDuplicateMatchPlayerNameValidator
        implements ConstraintValidator<NoDuplicateMatchPlayerName, List<? extends MatchPlayer>> {

    /**
     * Checks whether all supplied match players have unique names.
     *
     * @param matchPlayers      the match players to validate
     * @param constraintContext the validation context
     * @return whether all non-empty player names are unique
     */
    @Override
    public boolean isValid(List<? extends MatchPlayer> matchPlayers, ConstraintValidatorContext constraintContext) {
        // Null values are handled by separate validation constraints.
        if (matchPlayers == null) {
            return true;
        }

        return arePlayerNamesUnique(matchPlayers, constraintContext);
    }

    /**
     * Checks the player names and creates a validation violation for the first duplicate found.
     *
     * @param matchPlayers      the match players to validate
     * @param constraintContext the validation context
     * @return whether all non-empty player names are unique
     */
    private boolean arePlayerNamesUnique(List<? extends MatchPlayer> matchPlayers, ConstraintValidatorContext constraintContext) {
        Set<String> playerNames = new HashSet<>();

        for (MatchPlayer player : matchPlayers) {
            String playerName = player.getPlayerName();

            // Null and empty names are handled by separate validation constraints.
            if (playerName == null || playerName.isEmpty()) {
                continue;
            }

            // Set.add returns false when the name already exists in the set.
            if (!playerNames.add(playerName)) {
                setDuplicateNameViolationMessage(playerName, constraintContext);
                return false;
            }
        }

        return true;
    }

    /**
     * Replaces the default validation message with one containing the duplicate player name.
     *
     * @param duplicateName     the duplicated player name
     * @param constraintContext the validation context
     */
    private void setDuplicateNameViolationMessage(String duplicateName, ConstraintValidatorContext constraintContext) {
        // Use the Hibernate context so the duplicate name can be supplied as a message parameter.
        HibernateConstraintValidatorContext hibernateContext = constraintContext.unwrap(HibernateConstraintValidatorContext.class);

        hibernateContext.disableDefaultConstraintViolation();

        hibernateContext
                .addMessageParameter(MessageKeys.Params.NAME, duplicateName)
                .buildConstraintViolationWithTemplate("{" + MessageKeys.MESSAGE_PLAYER_NAME_DUPLICATE + "}")
                .addConstraintViolation();
    }
}