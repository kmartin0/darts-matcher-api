package nl.kmartin.dartsmatcherapi.features.x01.x01match.message;

/**
 * Defines the WebSocket message types used for X01 match updates.
 */
public enum X01MatchMessageType {
    PROCESS_MATCH,
    ADD_HUMAN_TURN,
    ADD_BOT_TURN,
    EDIT_TURN,
    DELETE_LAST_TURN,
    DELETE_MATCH,
    RESET_MATCH,
    REMATCH
}