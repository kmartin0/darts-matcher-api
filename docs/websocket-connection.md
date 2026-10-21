# WebSocket connection

The API uses STOMP over WebSocket.

## Connection

| Detail             | Value                      |
|--------------------|----------------------------|
| Path               | `/darts-matcher-websocket` |
| Transport          | WebSocket                  |
| Messaging protocol | STOMP                      |
| Authorization      | Not required               |

Use `ws://` for an unencrypted connection or `wss://` for a TLS connection.

After opening the WebSocket connection, send a STOMP `CONNECT` frame. Wait for the server's `CONNECTED` frame before subscribing or sending commands.

### CONNECT headers

| Header           | Type     | Required | Description                                                                                                               |
|------------------|----------|----------|---------------------------------------------------------------------------------------------------------------------------|
| `accept-version` | `String` | Yes      | Supported STOMP versions. Use `1.2`.                                                                                      |
| `host`           | `String` | Yes      | Server hostname.                                                                                                          |
| `heart-beat`     | `String` | No       | Outgoing and incoming heartbeat intervals in milliseconds, separated by a comma. Defaults to `0,0`, disabling heartbeats. |

STOMP headers belong inside the STOMP frame, separate from the HTTP headers used to establish the WebSocket connection.

### Frame format

A STOMP frame contains a command, headers, a blank line, an optional body, and a terminating NULL byte (`0x00`).

### Heartbeats

The server advertises `heart-beat:5000,5000`.

Heartbeat intervals are negotiated independently in each direction. If both sides enable a direction, its interval is the greater of the sender's outgoing interval and the receiver's requested incoming interval. A zero value on either side disables that direction.

### Disconnecting

Send a STOMP `DISCONNECT` frame before closing the WebSocket connection.

After reconnecting, establish a new STOMP session and recreate the required subscriptions.