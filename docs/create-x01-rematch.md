# Create X01 rematch

Creates a rematch with the same match settings, player IDs, player configuration, and player order. Results and statistics start fresh.

If the linked rematch still exists, it is reused. Otherwise, a new rematch is created and linked to the original match.

The original match does not need to be concluded.

## REST

### Request

| Detail           | Value                            |
|------------------|----------------------------------|
| Method           | `POST`                           |
| URL              | `/matches/x01/{matchId}/rematch` |
| Authorization    | Not required                     |
| Consumes         | None                             |
| Body             | None                             |
| Query parameters | None                             |

#### Path parameters

| Parameter | Type       | Required | Description               |
|-----------|------------|----------|---------------------------|
| `matchId` | `ObjectId` | Yes      | ID of the original match. |

Object definitions are in [collection.md](collection.md).

### Success response

| Detail   | Value              |
|----------|--------------------|
| Status   | `200 OK`           |
| Produces | `application/json` |
| Body     | `X01Match`         |

Returns the original match with `rematchId` identifying the rematch. Retrieve the rematch using `GET /matches/x01/{rematchId}`.

### Error responses

Produces `application/json` with an `ErrorResponse` body.

| Status                      | Type                   | Cause                                                                        |
|-----------------------------|------------------------|------------------------------------------------------------------------------|
| `400 Bad Request`           | `MESSAGE_NOT_READABLE` | `matchId` cannot be converted to an `ObjectId`.                              |
| `404 Not Found`             | `RESOURCE_NOT_FOUND`   | The original match does not exist.                                           |
| `406 Not Acceptable`        | `NOT_ACCEPTABLE`       | The `Accept` header requests a response format the operation cannot produce. |
| `409 Conflict`              | `CONFLICT`             | A concurrent update prevents the match from being saved.                     |
| `500 Internal Server Error` | `INTERNAL`             | An unexpected processing error occurs.                                       |
| `503 Service Unavailable`   | `UNAVAILABLE`          | The database is unavailable.                                                 |

## WebSocket

### Command

| Detail        | Value                                |
|---------------|--------------------------------------|
| STOMP command | `SEND`                               |
| Destination   | `/app/matches/x01/{matchId}/rematch` |
| Authorization | Not required                         |
| Body          | None                                 |

#### Destination parameters

| Parameter | Type       | Required | Description               |
|-----------|------------|----------|---------------------------|
| `matchId` | `ObjectId` | Yes      | ID of the original match. |

#### Headers

| Header       | Type     | Required | Description                                                                                                                      |
|--------------|----------|----------|----------------------------------------------------------------------------------------------------------------------------------|
| `publish-id` | `String` | No       | Client-generated identifier echoed in the session response or error. Use a distinct value for each command to correlate replies. |

Object definitions are in [collection.md](collection.md).

### Success response

Delivered to the requesting session through `/user/queue/responses`.

| Detail        | Value                                                 |
|---------------|-------------------------------------------------------|
| STOMP frame   | `MESSAGE`                                             |
| Body          | `X01MatchMessage`                                     |
| `messageType` | `REMATCH`                                             |
| `payload`     | Original `X01Match` containing the rematch reference. |

The `rematchId` property identifies the rematch. Use that ID to retrieve or subscribe to the rematch.

The session receives a success response whether the rematch is created or reused.

### Errors

Delivered to the requesting session through `/user/queue/errors` as a STOMP `MESSAGE` frame with a `WebSocketErrorMessage` body.

| Status | Type                   | Cause                                                    |
|--------|------------------------|----------------------------------------------------------|
| `400`  | `MESSAGE_NOT_READABLE` | `matchId` cannot be converted to an `ObjectId`.          |
| `404`  | `RESOURCE_NOT_FOUND`   | The original match does not exist.                       |
| `409`  | `CONFLICT`             | A concurrent update prevents the match from being saved. |
| `500`  | `INTERNAL`             | An unexpected processing error occurs.                   |
| `503`  | `UNAVAILABLE`          | The database is unavailable.                             |

## Broadcasts

These broadcasts apply to both REST requests and WebSocket commands.

When a new rematch is created, subscribers to `/topic/matches/x01/{matchId}` receive a `REMATCH` message containing the original match with its updated `rematchId`.

When an existing rematch is reused, no match update is broadcast.
