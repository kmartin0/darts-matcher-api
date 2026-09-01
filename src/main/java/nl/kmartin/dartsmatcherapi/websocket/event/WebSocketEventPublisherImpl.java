package nl.kmartin.dartsmatcherapi.websocket.event;

import nl.kmartin.dartsmatcherapi.error.response.ApiErrorCode;
import nl.kmartin.dartsmatcherapi.error.response.TargetError;
import nl.kmartin.dartsmatcherapi.websocket.event.model.WebSocketBroadcastEvent;
import nl.kmartin.dartsmatcherapi.websocket.event.model.WebSocketErrorEvent;
import nl.kmartin.dartsmatcherapi.websocket.event.model.WebSocketSendToUserEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Publishes internal events used to send WebSocket messages to clients.
 *
 * Supports broadcasting messages, sending messages to a specific client session
 * and sending WebSocket error responses.
 */
@Service
public class WebSocketEventPublisherImpl implements IWebSocketEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public WebSocketEventPublisherImpl(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publishes an event for broadcasting a WebSocket message.
     *
     * @param destination the WebSocket destination
     * @param messageType the WebSocket message type
     * @param payload     the message payload
     * @param <E>         the message type enum
     * @param <P>         the payload type
     */
    @Override
    public <E extends Enum<E>, P> void broadcast(String destination, E messageType, P payload) {
        eventPublisher.publishEvent(new WebSocketBroadcastEvent<>(destination, messageType, payload));
    }

    /**
     * Publishes an event for sending a WebSocket message to a specific client session.
     *
     * @param messageType the WebSocket message type
     * @param payload     the message payload
     * @param sessionId   the target WebSocket session ID
     * @param publishId   the publish correlation ID
     * @param <E>         the message type enum
     * @param <P>         the payload type
     */
    @Override
    public <E extends Enum<E>, P> void sendToUser(E messageType, P payload, String sessionId, String publishId) {
        eventPublisher.publishEvent(new WebSocketSendToUserEvent<>(messageType, payload, sessionId, publishId));
    }

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
    @Override
    public void sendError(
            String destination,
            ApiErrorCode error,
            String description,
            TargetError[] targetErrors,
            String sessionId,
            String publishId
    ) {
        eventPublisher.publishEvent(
                new WebSocketErrorEvent(destination, error, description, targetErrors, sessionId, publishId)
        );
    }
}