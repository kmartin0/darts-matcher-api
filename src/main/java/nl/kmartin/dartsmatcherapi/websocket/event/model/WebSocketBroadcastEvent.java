package nl.kmartin.dartsmatcherapi.websocket.event.model;

public record WebSocketBroadcastEvent<M, P>(
        String destination,
        M messageType,
        P payload
) {
}