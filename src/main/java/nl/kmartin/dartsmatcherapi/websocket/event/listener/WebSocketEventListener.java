package nl.kmartin.dartsmatcherapi.websocket.event.listener;

import nl.kmartin.dartsmatcherapi.websocket.destination.WebSocketDestinations;
import nl.kmartin.dartsmatcherapi.websocket.destination.WebSocketHeaders;
import nl.kmartin.dartsmatcherapi.websocket.event.model.WebSocketBroadcastEvent;
import nl.kmartin.dartsmatcherapi.websocket.event.model.WebSocketErrorEvent;
import nl.kmartin.dartsmatcherapi.websocket.event.model.WebSocketSendToUserEvent;
import nl.kmartin.dartsmatcherapi.websocket.message.model.WebSocketErrorMessage;
import nl.kmartin.dartsmatcherapi.websocket.message.model.WebSocketMessage;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Handles internal WebSocket events and sends their corresponding messages to clients.
 *
 * Broadcasts and successful responses are sent after commit when published within a transaction,
 * or immediately when no transaction exists. Errors are sent immediately.
 */
@Component
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sends a WebSocket broadcast event to all subscribers of its destination.
     *
     * Delivery is delayed until successful commit when a transaction is active.
     * Events published without a transaction are delivered immediately.
     *
     * @param event the WebSocket broadcast event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleWebSocketBroadcastEvent(WebSocketBroadcastEvent<?, ?> event) {
        WebSocketMessage<?, ?> message = new WebSocketMessage<>(event.messageType(), event.payload());

        sendBroadcast(event.destination(), message);
    }

    /**
     * Sends a successful WebSocket response to the originating client session.
     *
     * Delivery is delayed until successful commit when a transaction is active.
     * Events published without a transaction are delivered immediately.
     *
     * @param event the WebSocket send-to-user event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleWebSocketSendToUserEvent(WebSocketSendToUserEvent<?, ?> event) {
        WebSocketMessage<?, ?> message = new WebSocketMessage<>(event.messageType(), event.payload());

        sendToUser(WebSocketDestinations.RESPONSE_QUEUE, message, event.sessionId(), event.publishId());
    }

    /**
     * Sends a WebSocket error message immediately, independently of transaction outcome.
     *
     * @param event the WebSocket error event
     */
    @EventListener
    public void handleWebSocketErrorEvent(WebSocketErrorEvent event) {
        WebSocketErrorMessage message = new WebSocketErrorMessage(
                event.destination(),
                event.error(),
                event.description(),
                event.targetErrors()
        );

        sendToUser(WebSocketDestinations.ERROR_QUEUE, message, event.sessionId(), event.publishId());
    }

    /**
     * Sends a message to a WebSocket broadcast destination.
     *
     * @param destination the destination to send to
     * @param message     the message to send
     */
    private void sendBroadcast(String destination, Object message) {
        messagingTemplate.convertAndSend(destination, message);
    }

    /**
     * Sends a message to a specific WebSocket client session.
     *
     * @param destination the user destination to send to
     * @param message     the message to send
     * @param sessionId   the target WebSocket session ID
     * @param publishId   the publish correlation ID, or null when not applicable
     */
    private void sendToUser(String destination, Object message, String sessionId, String publishId) {
        SimpMessageHeaderAccessor outgoingAccessor = SimpMessageHeaderAccessor.create();
        outgoingAccessor.setSessionId(sessionId);

        if (publishId != null) {
            outgoingAccessor.setNativeHeader(WebSocketHeaders.PUBLISH_ID, publishId);
        }

        messagingTemplate.convertAndSendToUser(sessionId, destination, message, outgoingAccessor.getMessageHeaders());
    }
}