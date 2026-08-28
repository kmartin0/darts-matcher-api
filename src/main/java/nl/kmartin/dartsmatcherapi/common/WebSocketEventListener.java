package nl.kmartin.dartsmatcherapi.common;

import nl.kmartin.dartsmatcherapi.features.x01.x01match.event.X01MatchEvent;
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
     * Broadcasts X01 match events to subscribers of the match destination.
     *
     * @param event the X01 match event to broadcast
     */
    @EventListener
    public void handleX01MatchEvent(X01MatchEvent event) {
        String destination = WebsocketDestinations.broadcast(WebsocketDestinations.X01.MATCH, event.getMatchId());
        sendBroadcast(destination, event);
    }

    /**
     * Sends WebSocket exception responses to the originating client session.
     *
     * @param event the WebSocket exception event to send
     */
    @EventListener
    public void handleWebSocketExceptionEvent(WebSocketExceptionEvent event) {
        sendToUser(
                WebsocketDestinations.ERROR_QUEUE,
                event.errorResponse(),
                event.sessionId(),
                event.publishId()
        );
    }

    /**
     * Sends WebSocket responses to the originating client session.
     *
     * @param event the WebSocket response event to send
     */
    @EventListener
    public void handleWebSocketSendToUserEvent(WebSocketSendToUserEvent event) {
        sendToUser(
                WebsocketDestinations.RESPONSE_QUEUE,
                event.payload(),
                event.sessionId(),
                event.publishId()
        );
    }

    /**
     * Sends a payload to a WebSocket broadcast destination.
     *
     * @param destination the destination to send to
     * @param payload     the payload to send
     */
    private void sendBroadcast(String destination, Object payload) {
        messagingTemplate.convertAndSend(destination, payload);
    }

    /**
     * Sends a payload to a specific WebSocket client session.
     *
     * @param destination the user destination to send to
     * @param payload     the payload to send
     * @param sessionId   the target WebSocket session ID
     * @param publishId   the publish correlation ID, or null when not applicable
     */
    private void sendToUser(String destination, Object payload, String sessionId, String publishId) {
        SimpMessageHeaderAccessor outgoingAccessor = SimpMessageHeaderAccessor.create();
        outgoingAccessor.setSessionId(sessionId);

        if (publishId != null) {
            outgoingAccessor.setNativeHeader(Constants.PUBLISH_ID_HEADER, publishId);
        }

        messagingTemplate.convertAndSendToUser(
                sessionId,
                destination,
                payload,
                outgoingAccessor.getMessageHeaders()
        );
    }
}