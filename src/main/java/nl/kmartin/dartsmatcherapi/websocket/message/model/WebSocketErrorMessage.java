package nl.kmartin.dartsmatcherapi.websocket.message.model;

import lombok.Getter;
import lombok.ToString;
import nl.kmartin.dartsmatcherapi.error.response.ApiErrorCode;
import nl.kmartin.dartsmatcherapi.error.response.ErrorResponse;
import nl.kmartin.dartsmatcherapi.error.response.TargetError;

/**
 * Represents an error response returned over WebSocket.
 *
 * Contains:
 * • {@code destination} - the destination associated with the error
 * • {@code type} - the application error classification
 * • {@code description} - the human-readable error description
 * • {@code status} - the numeric status associated with the error
 * • {@code targetErrors} - optional target-specific errors
 */
@Getter
@ToString(callSuper = true)
public class WebSocketErrorMessage extends ErrorResponse {

    private final String destination;

    /**
     * Creates a WebSocket error response.
     *
     * @param destination  the destination associated with the error
     * @param apiErrorCode the API error code
     * @param description  the error description
     * @param targetErrors the target-specific errors
     */
    public WebSocketErrorMessage(
            String destination,
            ApiErrorCode apiErrorCode,
            String description,
            TargetError... targetErrors
    ) {
        super(apiErrorCode, description, targetErrors);
        this.destination = destination;
    }
}