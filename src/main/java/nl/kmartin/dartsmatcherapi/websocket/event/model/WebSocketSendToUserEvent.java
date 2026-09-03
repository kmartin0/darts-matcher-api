package nl.kmartin.dartsmatcherapi.websocket.event.model;

/**
 * Represents an internal WebSocket event sent to a specific client session.
 *
 * @param messageType the message type
 * @param payload     the message payload
 * @param sessionId   the target WebSocket session
 * @param publishId   the publish request identifier
 */
public record WebSocketSendToUserEvent<M, P>(
        M messageType,
        P payload,
        String sessionId,
        String publishId
) {
}