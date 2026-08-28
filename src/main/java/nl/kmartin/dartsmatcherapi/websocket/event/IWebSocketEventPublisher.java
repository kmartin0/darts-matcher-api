package nl.kmartin.dartsmatcherapi.websocket.event;

import nl.kmartin.dartsmatcherapi.error.response.ApiErrorCode;
import nl.kmartin.dartsmatcherapi.error.response.TargetError;

public interface IWebSocketEventPublisher {

    <M extends Enum<M>, P> void broadcast(
            String destination,
            M messageType,
            P payload
    );

    <M extends Enum<M>, P> void sendToUser(
            M messageType,
            P payload,
            String sessionId,
            String publishId
    );

    void sendError(
            String destination,
            ApiErrorCode error,
            String description,
            TargetError[] targetErrors,
            String sessionId,
            String publishId
    );
}