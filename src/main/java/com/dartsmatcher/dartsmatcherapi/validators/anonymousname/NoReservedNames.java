package com.dartsmatcher.dartsmatcherapi.validators.anonymousname;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = NoReservedNamesValidator.class)
@Documented
public @interface NoReservedNames {
	String message() default "{message.username.not.allowed}";
	Class<?>[] groups() default { };
	Class<? extends Payload>[] payload() default { };
}
