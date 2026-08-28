package nl.kmartin.dartsmatcherapi.websocket.response;

public record WebSocketResponseEvent(
        Object payload,
        String sessionId,
        String publishId
) {
}
