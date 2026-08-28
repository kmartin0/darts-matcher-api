package nl.kmartin.dartsmatcherapi.websocket.message;

public record WebSocketMessage<M, P>(
        M messageType,
        P payload
) {
}
