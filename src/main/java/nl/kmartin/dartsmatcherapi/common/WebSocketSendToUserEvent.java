package nl.kmartin.dartsmatcherapi.common;

public record WebSocketSendToUserEvent(
        Object payload,
        String sessionId,
        String publishId
) {
}
