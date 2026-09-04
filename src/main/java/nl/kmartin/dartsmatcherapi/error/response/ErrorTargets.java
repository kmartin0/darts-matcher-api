package nl.kmartin.dartsmatcherapi.error.response;

/**
 * Defines client-facing targets used by explicit API validation errors.
 *
 * Targets identify fields or parameters that the client can correct in the
 * request that triggered the error.
 */
public final class ErrorTargets {
    private ErrorTargets() {
    }

    public static final String ROOT = "root";
    public static final String SCORE = "score";
    public static final String CHECKOUT_DARTS_USED = "checkoutDartsUsed";
}