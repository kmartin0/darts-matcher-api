package nl.kmartin.dartsmatcherapi.websocket.event.model;

import nl.kmartin.dartsmatcherapi.websocket.message.model.WebSocketMessage;

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