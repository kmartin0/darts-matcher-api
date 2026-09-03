package nl.kmartin.dartsmatcherapi.websocket.event;

import nl.kmartin.dartsmatcherapi.error.response.ApiErrorCode;
import nl.kmartin.dartsmatcherapi.error.response.TargetError;

public interface IWebSocketEventPublisher {

    /**
     * Publishes an event for broadcasting a WebSocket message.
     *
     * @param destination the WebSocket destination
     * @param messageType the WebSocket message type
     * @param payload     the message payload
     * @param <M>         the message type enum
     * @param <P>         the payload type
     */
    <M extends Enum<M>, P> void broadcast(String destination, M messageType, P payload);

    /**
     * Publishes an event for sending a WebSocket message to a specific client session.
     *
     * @param messageType the WebSocket message type
     * @param payload     the message payload
     * @param sessionId   the target WebSocket session ID
     * @param publishId   the publish correlation ID
     * @param <M>         the message type enum
     * @param <P>         the payload type
     */
    <M extends Enum<M>, P> void sendToUser(M messageType, P payload, String sessionId, String publishId);

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