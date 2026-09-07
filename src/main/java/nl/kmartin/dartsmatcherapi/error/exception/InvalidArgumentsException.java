package nl.kmartin.dartsmatcherapi.error.exception;

import lombok.Getter;
import lombok.ToString;
import nl.kmartin.dartsmatcherapi.error.response.TargetError;

import java.util.List;

/**
 * Thrown when one or more supplied arguments are invalid.
 *
 * Stores the target-specific errors so they can be included in the API error response.
 */
@Getter
@ToString(callSuper = true)
public class InvalidArgumentsException extends RuntimeException {
    private final List<TargetError> errors;

    /**
     * Creates an invalid arguments exception containing the supplied target errors.
     *
     * @param errors the target-specific validation errors
     */
    public InvalidArgumentsException(TargetError... errors) {
        super("Invalid arguments have been supplied.");
        this.errors = List.of(errors);
    }
}