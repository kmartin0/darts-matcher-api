package com.dartsmatcher.dartsmatcherapi.validators.anonymousname;

import com.dartsmatcher.dartsmatcherapi.features.match.MatchPlayer;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.*;

public class ValidMatchPlayerIdsValidator implements ConstraintValidator<ValidMatchPlayerIds, ArrayList<MatchPlayer>> {

	@Override
	public void initialize(ValidMatchPlayerIds constraintAnnotation) {

	}

	@Override
	public boolean isValid(ArrayList<MatchPlayer> matchPlayers, ConstraintValidatorContext context) {

		HibernateConstraintValidatorContext hibernateContext = context.unwrap(HibernateConstraintValidatorContext.class);
		hibernateContext.disableDefaultConstraintViolation();

		Set<String> reservedNames = new HashSet<>(Collections.singletonList("dartBot"));
		Set<Object> tmpPlayerIds = new HashSet<>();

		for (MatchPlayer matchPlayer : matchPlayers) {

			String playerId = matchPlayer.getPlayerId();
			String violatedPlayerId = "";

			switch (matchPlayer.getPlayerType()) {

				case REGISTERED: {
					if (!ObjectId.isValid(playerId)) {
						violatedPlayerId = playerId;
					}
				}
				break;
				case ANONYMOUS: {
					if (reservedNames.stream().anyMatch(playerId::equalsIgnoreCase)) {
						violatedPlayerId = playerId;
					}
				}
				break;
			}

			if (!violatedPlayerId.isEmpty()) {
				hibernateContext
						.addMessageParameter("name", matchPlayer.getPlayerId())
						.buildConstraintViolationWithTemplate("{message.player.name.not.allowed}")
						.addConstraintViolation();
				return false;
			}

			if (!tmpPlayerIds.add(playerId.toLowerCase())) {
				hibernateContext
						.addMessageParameter("name", matchPlayer.getPlayerId())
						.buildConstraintViolationWithTemplate("{message.player.name.duplicate}")
						.addConstraintViolation();
				return false;
			}
		}

		return true;
	}
}
