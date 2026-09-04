package nl.kmartin.dartsmatcherapi.error.response;

/**
 * Represents an error associated with a client-correctable API input target.
 *
 * Targets refer to submitted fields, parameters or the submitted input as a whole.
 * Internal state and persisted domain data must not be exposed as targets.
 *
 * @param target the client-facing input target, or {@code null} for the input as a whole
 * @param error  the human-readable error message
 */
public record TargetError(String target, String error) {
}
