package nl.kmartin.dartsmatcherapi.common;

import nl.kmartin.dartsmatcherapi.exceptionhandler.response.WebSocketErrorResponse;

public record WebSocketExceptionEvent(
        WebSocketErrorResponse errorResponse,
        String sessionId,
        String publishId
) {
}
