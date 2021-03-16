package com.dartsmatcher.dartsmatcherapi.validators.anonymousname;

import org.bson.types.ObjectId;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.*;
import java.util.stream.Collectors;

public class AnonymousNameValidator implements ConstraintValidator<AnonymousName, ArrayList<String>> {

	@Override
	public void initialize(AnonymousName constraintAnnotation) {

	}

	@Override
	public boolean isValid(ArrayList<String> players, ConstraintValidatorContext context) {
		Set<String> reservedNames = new HashSet<>(Collections.singletonList("dartBot"));
		Set<String> invalidPlayers = new HashSet<>();
		Set<String> tmpPlayers = new HashSet<>();

		players.forEach(s -> {
			if (tmpPlayers.contains(s)
					|| reservedNames.contains(s)
					|| ObjectId.isValid(s)) invalidPlayers.add(s);

			tmpPlayers.add(s);
		});

		// When there are no invalid names the players array is valid. Else add a constraint violation message with the invalid names separated by a comma.
		if (invalidPlayers.isEmpty()) return true;
		else {
			HibernateConstraintValidatorContext hibernateContext = context.unwrap(HibernateConstraintValidatorContext.class);
			hibernateContext.disableDefaultConstraintViolation();
			hibernateContext
					.addMessageParameter("names", String.join(", ", invalidPlayers))
					.buildConstraintViolationWithTemplate("{message.anonymous.name.not.allowed}")
					.addConstraintViolation();

			return false;
		}
	}
}
