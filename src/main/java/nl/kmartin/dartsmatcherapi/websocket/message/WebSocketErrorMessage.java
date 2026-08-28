package nl.kmartin.dartsmatcherapi.websocket.message;

import nl.kmartin.dartsmatcherapi.error.response.ApiErrorCode;

import java.util.Map;

public record WebSocketErrorMessage(
        String destination,
        ApiErrorCode error,
        String description,
        int code,
        Map<String, String> targetErrors
) {
}
