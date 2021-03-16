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
@Constraint(validatedBy = AnonymousNameValidator.class)
@Documented
public @interface AnonymousName {
	String message() default "{message.anonymous.name.not.allowed}";
	Class<?>[] groups() default { };
	Class<? extends Payload>[] payload() default { };
}
