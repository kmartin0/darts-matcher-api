package nl.kmartin.dartsmatcherapi.websocket.event.model;

public record WebSocketSendToUserEvent<M, P>(
        M messageType,
        P payload,
        String sessionId,
        String publishId
) {
}