package nl.kmartin.dartsmatcherapi.websocket.event.publisher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 *
 * Captures broadcast and successful response payloads as JSON snapshots so subsequent
 * changes to the original objects do not affect messages awaiting transaction commit.
 */
@Service
public class WebSocketEventPublisherImpl implements IWebSocketEventPublisher {

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public WebSocketEventPublisherImpl(
            ApplicationEventPublisher eventPublisher,
            ObjectMapper objectMapper
    ) {
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @Override
    public <M extends Enum<M>, P> void broadcast(String destination, M messageType, P payload) {
        // Capture the payload before publishing an event whose delivery may be delayed.
        JsonNode payloadSnapshot = objectMapper.valueToTree(payload);

        eventPublisher.publishEvent(
                new WebSocketBroadcastEvent<>(destination, messageType, payloadSnapshot)
        );
    }

    @Override
    public <M extends Enum<M>, P> void sendToUser(M messageType, P payload, String sessionId, String publishId) {
        // Capture the payload before publishing an event whose delivery may be delayed.
        JsonNode payloadSnapshot = objectMapper.valueToTree(payload);

        eventPublisher.publishEvent(
                new WebSocketSendToUserEvent<>(messageType, payloadSnapshot, sessionId, publishId)
        );
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