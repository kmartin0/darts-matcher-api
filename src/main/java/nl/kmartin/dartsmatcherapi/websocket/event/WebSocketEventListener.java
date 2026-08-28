package nl.kmartin.dartsmatcherapi.websocket.event;

import nl.kmartin.dartsmatcherapi.error.ErrorUtil;
import nl.kmartin.dartsmatcherapi.websocket.WebSocketDestinations;
import nl.kmartin.dartsmatcherapi.websocket.WebSocketHeaders;
import nl.kmartin.dartsmatcherapi.websocket.event.model.WebSocketBroadcastEvent;
import nl.kmartin.dartsmatcherapi.websocket.event.model.WebSocketErrorEvent;
import nl.kmartin.dartsmatcherapi.websocket.event.model.WebSocketSendToUserEvent;
import nl.kmartin.dartsmatcherapi.websocket.message.WebSocketErrorMessage;
import nl.kmartin.dartsmatcherapi.websocket.message.WebSocketMessage;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sends a WebSocket broadcast event to all subscribers of its destination.
     *
     * @param event the WebSocket broadcast event
     */
    @EventListener
    public void handleWebSocketBroadcastEvent(WebSocketBroadcastEvent<?, ?> event) {
        WebSocketMessage<?, ?> message = new WebSocketMessage<>(event.messageType(), event.payload());

        sendBroadcast(event.destination(), message);
    }

    /**
     * Sends a WebSocket message to the originating client session.
     *
     * @param event the WebSocket send-to-user event
     */
    @EventListener
    public void handleWebSocketSendToUserEvent(WebSocketSendToUserEvent<?, ?> event) {
        WebSocketMessage<?, ?> message = new WebSocketMessage<>(event.messageType(), event.payload());

        sendToUser(WebSocketDestinations.RESPONSE_QUEUE, message, event.sessionId(), event.publishId());
    }

    /**
     * Sends a WebSocket error message to the originating client session.
     *
     * @param event the WebSocket error event
     */
    @EventListener
    public void handleWebSocketErrorEvent(WebSocketErrorEvent event) {
        WebSocketErrorMessage message = new WebSocketErrorMessage(
                event.destination(),
                event.error(),
                event.description(),
                event.error().getHttpStatus().value(),
                ErrorUtil.targetErrorsToMap(event.targetErrors())
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