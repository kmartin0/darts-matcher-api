package nl.kmartin.dartsmatcherapi.rest;

/**
 * Defines the REST endpoint paths exposed by the application.
 */
public final class RestEndpoints {

    private RestEndpoints() {
    }

    /**
     * Defines REST endpoints for the X01 feature.
     */
    public static final class X01 {

        private X01() {
        }

        // Checkout endpoints
        public static final String CHECKOUTS = "/checkouts/x01";
        public static final String CHECKOUT = "/checkouts/x01/{remaining}";

        // Match resource endpoints
        public static final String MATCHES = "/matches/x01";
        public static final String MATCH = "/matches/x01/{matchId}";

        // Match operation endpoints
        public static final String MATCH_EXISTS = "/matches/x01/{matchId}/exists";
        public static final String MATCH_RESET = "/matches/x01/{matchId}/reset";
        public static final String MATCH_REPROCESS = "/matches/x01/{matchId}/reprocess";
        public static final String MATCH_TURNS = "/matches/x01/{matchId}/turns";
        public static final String MATCH_TURNS_EDIT = "/matches/x01/{matchId}/turns/edit";
        public static final String MATCH_TURNS_DELETE_LAST = "/matches/x01/{matchId}/turns/delete-last";
        public static final String MATCH_REMATCH = "/matches/x01/{matchId}/rematch";
    }
}