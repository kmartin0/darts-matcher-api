package nl.kmartin.dartsmatcherapi.websocket;

import org.bson.types.ObjectId;

/**
 * Defines WebSocket destinations and provides helpers for resolving match-specific destinations.
 */
public final class WebSocketDestinations {
    private WebSocketDestinations() {
    }

    public static final String RESPONSE_QUEUE = "/queue/responses";
    public static final String ERROR_QUEUE = "/queue/errors";

    /**
     * Defines WebSocket destinations for X01 match operations.
     */
    public static final class X01 {
        private X01() {
        }

        public static final String MATCH = "/x01/matches/{matchId}";
        public static final String DELETE_MATCH = MATCH + "/delete";
        public static final String RESET_MATCH = MATCH + "/reset";
        public static final String REPROCESS_MATCH = MATCH + "/reprocess";
        public static final String ADD_TURN = MATCH + "/turn/add";
        public static final String EDIT_TURN = MATCH + "/turn/edit";
        public static final String DELETE_LAST_TURN = MATCH + "/turn/delete-last";
    }

    /**
     * Resolves the match ID placeholder in a WebSocket destination template.
     *
     * @param template the destination template
     * @param matchId the match ID
     * @return the resolved destination
     */
    public static String resolveMatchId(String template, ObjectId matchId) {
        return template.replace("{matchId}", matchId.toHexString());
    }

    /**
     * Creates the broadcast destination for a match-specific WebSocket destination.
     *
     * @param template the destination template
     * @param matchId the match ID
     * @return the resolved broadcast destination
     */
    public static String broadcast(String template, ObjectId matchId) {
        return WebSocketConfig.BROADCAST_PREFIX + resolveMatchId(template, matchId);
    }
}