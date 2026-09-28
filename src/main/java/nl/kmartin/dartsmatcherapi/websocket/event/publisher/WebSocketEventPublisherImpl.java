package nl.kmartin.dartsmatcherapi.websocket.event.publisher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.kmartin.dartsmatcherapi.error.response.ApiErrorCode;
import nl.kmartin.dartsmatcherapi.error.response.TargetError;
import nl.kmartin.dartsmatcherapi.websocket.event.model.WebSocketBroadcastEvent;
import nl.kmartin.dartsmatcherapi.websocket.event.model.WebSocketErrorEvent;
import nl.kmartin.dartsmatcherapi.websocket.event.model.WebSocketSendToUserEvent;
import nl.kmartin.dartsmatcherapi.websocket.message.model.WebSocketMessage;
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

    public WebSocketEventPublisherImpl(ApplicationEventPublisher eventPublisher, ObjectMapper objectMapper) {
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @Override
    public <M extends Enum<M>, P> void broadcast(String destination, WebSocketMessage<M, P> message) {
        eventPublisher.publishEvent(
                new WebSocketBroadcastEvent(destination, createSnapshot(message))
        );
    }

    @Override
    public <M extends Enum<M>, P> void sendToUser(WebSocketMessage<M, P> message, String sessionId, String publishId) {
        eventPublisher.publishEvent(
                new WebSocketSendToUserEvent(createSnapshot(message), sessionId, publishId)
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

    /**
     * Captures a message's payload before publishing an event whose delivery may be delayed.
     *
     * @param message the message to capture
     * @param <M>     the message type enum
     * @return the message with its payload captured as JSON
     */
    private <M extends Enum<M>> WebSocketMessage<M, JsonNode> createSnapshot(WebSocketMessage<M, ?> message) {
        JsonNode payloadSnapshot = objectMapper.valueToTree(message.payload());

        return new MessageSnapshot<>(message.messageType(), payloadSnapshot);
    }

    /**
     * Holds a message type and its captured JSON payload.
     *
     * @param messageType the original message type
     * @param payload     the captured payload
     * @param <M>         the message type enum
     */
    private record MessageSnapshot<M extends Enum<M>>(
            M messageType,
            JsonNode payload
    ) implements WebSocketMessage<M, JsonNode> {
    }
}