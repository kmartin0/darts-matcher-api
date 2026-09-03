package nl.kmartin.dartsmatcherapi.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.util.Map;

/**
 * Provides shared helpers for creating custom constraint violations.
 */
public final class ConstraintViolationsHelper {

    private ConstraintViolationsHelper() {
    }

    /**
     * Replaces the default constraint violation with the message for the supplied key.
     *
     * @param context    the validation context
     * @param messageKey the validation message key
     */
    public static void addViolation(ConstraintValidatorContext context, String messageKey) {
        HibernateConstraintValidatorContext hibernateContext = getHibernateContext(context);

        hibernateContext
                .buildConstraintViolationWithTemplate("{" + messageKey + "}")
                .addConstraintViolation();
    }

    /**
     * Replaces the default constraint violation and attaches it to the supplied property.
     *
     * @param context      the validation context
     * @param messageKey   the validation message key
     * @param propertyName the property to attach the violation to
     */
    public static void addViolation(ConstraintValidatorContext context, String messageKey, String propertyName) {
        HibernateConstraintValidatorContext hibernateContext = getHibernateContext(context);

        hibernateContext
                .buildConstraintViolationWithTemplate("{" + messageKey + "}")
                .addPropertyNode(propertyName)
                .addConstraintViolation();
    }

    /**
     * Replaces the default constraint violation with a message containing the supplied parameters.
     *
     * @param context       the validation context
     * @param messageKey    the validation message key
     * @param messageParams the parameters to substitute into the message template
     */
    public static void addViolation(ConstraintValidatorContext context, String messageKey, Map<String, Object> messageParams) {
        HibernateConstraintValidatorContext hibernateContext = getHibernateContext(context);

        messageParams.forEach(hibernateContext::addMessageParameter);

        hibernateContext
                .buildConstraintViolationWithTemplate("{" + messageKey + "}")
                .addConstraintViolation();
    }

    /**
     * Gets the Hibernate validation context and disables the default constraint violation.
     *
     * @param context the validation context
     * @return the Hibernate validation context
     */
    private static HibernateConstraintValidatorContext getHibernateContext(ConstraintValidatorContext context) {
        HibernateConstraintValidatorContext hibernateContext = context.unwrap(HibernateConstraintValidatorContext.class);

        hibernateContext.disableDefaultConstraintViolation();
        return hibernateContext;
    }
}