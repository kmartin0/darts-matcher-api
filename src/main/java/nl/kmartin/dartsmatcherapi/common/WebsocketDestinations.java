package nl.kmartin.dartsmatcherapi.common;

import nl.kmartin.dartsmatcherapi.config.WebsocketConfig;
import org.bson.types.ObjectId;

public final class WebsocketDestinations {

    private WebsocketDestinations() {
    }

    public static final String RESPONSE_QUEUE = "/queue/responses";
    public static final String ERROR_QUEUE = "/queue/errors";

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

    public static String resolveMatchId(String template, ObjectId matchId) {
        return template.replace("{matchId}", matchId.toString());
    }

    public static String broadcast(String template, ObjectId matchId) {
        return WebsocketConfig.BROADCAST_PREFIX + resolveMatchId(template, matchId);
    }
}