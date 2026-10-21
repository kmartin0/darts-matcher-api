# WebSocket subscriptions

Establish a STOMP session before subscribing.

## SUBSCRIBE headers

| Header        | Type     | Required | Description                                                                           |
|---------------|----------|----------|---------------------------------------------------------------------------------------|
| `id`          | `String` | Yes      | Subscription identifier. Must be unique among active subscriptions in the connection. |
| `destination` | `String` | Yes      | Destination to subscribe to.                                                          |
| `ack`         | `String` | No       | Use `auto`. Messages require no client acknowledgement.                               |

## Destinations

| Destination                    | Purpose                                                                        |
|--------------------------------|--------------------------------------------------------------------------------|
| `/app/matches/x01/{matchId}`   | Returns the current match once, with message type `PROCESS_MATCH`.             |
| `/topic/matches/x01/{matchId}` | Receives broadcast updates for the match. Does not return an initial snapshot. |
| `/user/queue/responses`        | Receives successful command responses for the requesting session.              |
| `/user/queue/errors`           | Receives application errors for the requesting session.                        |

### Destination parameters

| Parameter | Type       | Required | Description                 |
|-----------|------------|----------|-----------------------------|
| `matchId` | `ObjectId` | Yes      | ID of the match to observe. |

Object definitions are in the collection documentation.

## Receiving messages

The server delivers data in STOMP `MESSAGE` frames. The `subscription` header identifies the subscription receiving the message.

Match snapshots, broadcast updates, and command responses have a JSON body containing `messageType` and `payload`.

Application errors have a JSON body containing the error properties and `destination`, identifying the destination associated with the failure. These arrive as `MESSAGE` frames on the error subscription, not STOMP `ERROR` frames.

When a command supplies a `publish-id` header, its session response or application error includes the same header. Broadcast updates do not include it.

A client subscribed to both match updates and command responses can receive both a broadcast and a session response for its own command.

## Initial state and live updates

Subscribe to session errors and match broadcasts before requesting the initial match state.

Subscribing to `/app/matches/x01/{matchId}` returns the current match only to that subscription. If the match does not exist, an application error with type `RESOURCE_NOT_FOUND` is sent to the session's error queue.

Match broadcasts include updates caused by REST requests and WebSocket commands.

## Unsubscribing and reconnecting

Send an `UNSUBSCRIBE` frame with the subscription's `id` to stop receiving its messages.

Subscriptions belong to the current connection. Recreate them after reconnecting and request the current match state again. Updates missed while disconnected are not replayed.