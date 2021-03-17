package com.dartsmatcher.dartsmatcherapi.validators.anonymousname;

import org.bson.types.ObjectId;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.*;

public class NoReservedNamesValidator implements ConstraintValidator<NoReservedNames, Object> {

	@Override
	public void initialize(NoReservedNames constraintAnnotation) {

	}

	@Override
	public boolean isValid(Object username, ConstraintValidatorContext context) {
		if (!(username instanceof String)) return true;

		String _username = username.toString();

		Set<String> reservedNames = new HashSet<>(Collections.singletonList("dartBot"));

		// When there are no invalid names the username is valid. Else add a constraint violation message with the invalid name.
		if (reservedNames.contains(_username) || ObjectId.isValid(_username)) {
			HibernateConstraintValidatorContext hibernateContext = context.unwrap(HibernateConstraintValidatorContext.class);
			hibernateContext.disableDefaultConstraintViolation();
			hibernateContext
					.addMessageParameter("name", username)
					.buildConstraintViolationWithTemplate("{message.username.not.allowed}")
					.addConstraintViolation();

			return false;
		} else return true;
	}
}
