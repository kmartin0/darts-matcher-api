package nl.kmartin.dartsmatcherapi.websocket.destination;

import nl.kmartin.dartsmatcherapi.websocket.config.WebSocketConfig;
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

        public static final String MATCH = "/matches/x01/{matchId}";
        public static final String DELETE_MATCH = "/matches/x01/{matchId}/delete";
        public static final String RESET_MATCH = "/matches/x01/{matchId}/reset";
        public static final String REPROCESS_MATCH = "/matches/x01/{matchId}/reprocess";
        public static final String ADD_TURN = "/matches/x01/{matchId}/turns/add";
        public static final String EDIT_TURN = "/matches/x01/{matchId}/turns/edit";
        public static final String DELETE_LAST_TURN = "/matches/x01/{matchId}/turns/delete-last";
        public static final String REMATCH = "/matches/x01/{matchId}/rematch";
    }

    /**
     * Resolves the match ID placeholder in a WebSocket destination template.
     *
     * @param template the destination template
     * @param matchId  the match ID
     * @return the resolved destination
     */
    public static String resolveMatchId(String template, ObjectId matchId) {
        return template.replace("{matchId}", matchId.toHexString());
    }

    /**
     * Creates the broadcast destination for a match-specific WebSocket destination.
     *
     * @param template the destination template
     * @param matchId  the match ID
     * @return the resolved broadcast destination
     */
    public static String broadcast(String template, ObjectId matchId) {
        return WebSocketConfig.BROADCAST_PREFIX + resolveMatchId(template, matchId);
    }
}