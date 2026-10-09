package nl.kmartin.dartsmatcher.api.websocket.event.model;

import nl.kmartin.dartsmatcher.api.websocket.message.model.WebSocketMessage;

/**
 * Represents an internal event used to broadcast a WebSocket message to a destination.
 *
 * @param destination the WebSocket destination
 * @param message     the WebSocket message captured by the publisher
 */
public record WebSocketBroadcastEvent(
        String destination,
        WebSocketMessage<?, ?> message
) {
}