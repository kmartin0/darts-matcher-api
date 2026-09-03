package nl.kmartin.dartsmatcherapi.websocket.event.model;

import nl.kmartin.dartsmatcherapi.error.response.ApiErrorCode;
import nl.kmartin.dartsmatcherapi.error.response.TargetError;

/**
 * Represents an internal WebSocket error event sent to a specific client session.
 *
 * @param destination  the WebSocket destination
 * @param error        the API error code
 * @param description  the error description
 * @param targetErrors the target-specific errors
 * @param sessionId    the target WebSocket session
 * @param publishId    the publish request identifier
 */
public record WebSocketErrorEvent(
        String destination,
        ApiErrorCode error,
        String description,
        TargetError[] targetErrors,
        String sessionId,
        String publishId
) {
}