package nl.kmartin.dartsmatcherapi.websocket.event.model;

import nl.kmartin.dartsmatcherapi.websocket.message.model.WebSocketMessage;

/**
 * Represents an internal WebSocket event sent to a specific client session.
 *
 * @param message   the WebSocket message captured by the publisher
 * @param sessionId the target WebSocket session ID
 * @param publishId the publish correlation ID, or null when not applicable
 */
public record WebSocketSendToUserEvent(
        WebSocketMessage<?, ?> message,
        String sessionId,
        String publishId
) {
}