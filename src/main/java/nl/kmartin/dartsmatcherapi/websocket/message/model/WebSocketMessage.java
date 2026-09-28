package nl.kmartin.dartsmatcherapi.websocket.message.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines a WebSocket message with a message type and payload.
 *
 * @param <M> the message type enum
 * @param <P> the payload type
 */
public interface WebSocketMessage<M extends Enum<M>, P> {

    @JsonProperty("messageType")
    M messageType();

    @JsonProperty("payload")
    P payload();
}