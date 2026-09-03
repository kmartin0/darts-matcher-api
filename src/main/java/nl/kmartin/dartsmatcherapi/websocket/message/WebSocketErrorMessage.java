package nl.kmartin.dartsmatcherapi.websocket.message;

import nl.kmartin.dartsmatcherapi.error.response.ApiErrorCode;

import java.util.Map;

/**
 * Represents an error message returned over WebSocket.
 *
 * @param destination  the destination associated with the error
 * @param error        the API error code
 * @param description  the error description
 * @param code         the HTTP status code associated with the error
 * @param targetErrors the target-specific errors
 */
public record WebSocketErrorMessage(
        String destination,
        ApiErrorCode error,
        String description,
        int code,
        Map<String, String> targetErrors
) {
}