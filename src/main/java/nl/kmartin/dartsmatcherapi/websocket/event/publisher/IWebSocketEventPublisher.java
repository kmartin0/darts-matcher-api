package nl.kmartin.dartsmatcherapi.websocket.event.publisher;

import nl.kmartin.dartsmatcherapi.error.response.ApiErrorCode;
import nl.kmartin.dartsmatcherapi.error.response.TargetError;
import nl.kmartin.dartsmatcherapi.websocket.message.model.WebSocketMessage;

public interface IWebSocketEventPublisher {

    /**
     * Publishes an event for broadcasting a snapshot of a WebSocket message.
     *
     * @param destination the WebSocket destination
     * @param message     the message to broadcast
     * @param <M>         the message type enum
     * @param <P>         the payload type
     */
    <M extends Enum<M>, P> void broadcast(
            String destination,
            WebSocketMessage<M, P> message
    );

    /**
     * Publishes an event for sending a snapshot of a WebSocket message to a specific client session.
     *
     * @param message   the message to send
     * @param sessionId the target WebSocket session ID
     * @param publishId the publish correlation ID, or null when not applicable
     * @param <M>       the message type enum
     * @param <P>       the payload type
     */
    <M extends Enum<M>, P> void sendToUser(
            WebSocketMessage<M, P> message,
            String sessionId,
            String publishId
    );

    /**
     * Publishes an event for sending a WebSocket error to a specific client session.
     *
     * @param destination  the destination associated with the error
     * @param error        the API error code
     * @param description  the error description
     * @param targetErrors the target-specific errors
     * @param sessionId    the target WebSocket session ID
     * @param publishId    the publish correlation ID, or null when not applicable
     */
    void sendError(
            String destination,
            ApiErrorCode error,
            String description,
            TargetError[] targetErrors,
            String sessionId,
            String publishId
    );
}