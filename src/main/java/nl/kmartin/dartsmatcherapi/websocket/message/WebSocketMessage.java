package nl.kmartin.dartsmatcherapi.websocket.message;

/**
 * Represents a WebSocket message containing a message type and payload.
 *
 * @param messageType the WebSocket message type
 * @param payload     the message payload
 */
public record WebSocketMessage<M, P>(
        M messageType,
        P payload
) {
}