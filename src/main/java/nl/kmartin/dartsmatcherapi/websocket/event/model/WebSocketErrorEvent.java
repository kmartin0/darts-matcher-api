package nl.kmartin.dartsmatcherapi.websocket.event.model;

import nl.kmartin.dartsmatcherapi.error.response.ApiErrorCode;
import nl.kmartin.dartsmatcherapi.error.response.TargetError;

public record WebSocketErrorEvent(
        String destination,
        ApiErrorCode error,
        String description,
        TargetError[] targetErrors,
        String sessionId,
        String publishId
) {
}