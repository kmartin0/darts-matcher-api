package nl.kmartin.dartsmatcherapi.websocket.event;

import nl.kmartin.dartsmatcherapi.websocket.response.WebSocketErrorResponse;

public record WebSocketErrorEvent(
        WebSocketErrorResponse errorResponse,
        String sessionId,
        String publishId
) {
}
