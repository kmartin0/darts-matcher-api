package nl.kmartin.dartsmatcherapi.websocket.event.model;

/**
 * Represents an internal event used to broadcast a WebSocket message to a destination.
 *
 * @param destination the WebSocket destination
 * @param messageType the message type
 * @param payload the message payload
 */
public record WebSocketBroadcastEvent<M, P>(
        String destination,
        M messageType,
        P payload
) {
}