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

    @Override
    public <M extends Enum<M>, P> void broadcast(String destination, M messageType, P payload) {
        eventPublisher.publishEvent(new WebSocketBroadcastEvent<>(destination, messageType, payload));
    }

    @Override
    public <M extends Enum<M>, P> void sendToUser(M messageType, P payload, String sessionId, String publishId) {
        eventPublisher.publishEvent(new WebSocketSendToUserEvent<>(messageType, payload, sessionId, publishId));
    }

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