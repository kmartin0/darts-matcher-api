package nl.kmartin.dartsmatcherapi.error.response;

/**
 * @param target Key of the field.
 * @param error  Error description describing the error to an end user.
 */
public record TargetError(
        String target,
        String error
) {
}
