# Reprocess X01 match

Recalculates remaining scores, results, statistics, progress, and standings from the recorded turns.

Removes history that no longer applies, such as turns after a winning turn or later legs and sets after an unfinished leg or set. Clears `rematchId` if the referenced match no longer exists and processes pending dart bot turns.

## REST

### Request

| Detail           | Value                              |
|------------------|------------------------------------|
| Method           | `POST`                             |
| URL              | `/matches/x01/{matchId}/reprocess` |
| Authorization    | Not required                       |
| Consumes         | None                               |
| Body             | None                               |
| Query parameters | None                               |

#### Path parameters

| Parameter | Type       | Required | Description                   |
|-----------|------------|----------|-------------------------------|
| `matchId` | `ObjectId` | Yes      | ID of the match to reprocess. |

Object definitions are in [collection.md](collection.md).

### Success response

| Detail   | Value              |
|----------|--------------------|
| Status   | `200 OK`           |
| Produces | `application/json` |
| Body     | `X01Match`         |

### Error responses

Produces `application/json` with an `ErrorResponse` body.

| Status                      | Type                   | Cause                                                    |
|-----------------------------|------------------------|----------------------------------------------------------|
| `400 Bad Request`           | `MESSAGE_NOT_READABLE` | `matchId` cannot be converted to an `ObjectId`.          |
| `404 Not Found`             | `RESOURCE_NOT_FOUND`   | The match does not exist.                                |
| `409 Conflict`              | `CONFLICT`             | A concurrent update prevents the match from being saved. |
| `500 Internal Server Error` | `INTERNAL`             | An unexpected processing error occurs.                   |
| `503 Service Unavailable`   | `UNAVAILABLE`          | The database is unavailable.                             |

## WebSocket

### Command

| Detail        | Value                                  |
|---------------|----------------------------------------|
| STOMP command | `SEND`                                 |
| Destination   | `/app/matches/x01/{matchId}/reprocess` |
| Authorization | Not required                           |
| Body          | None                                   |

#### Destination parameters

| Parameter | Type       | Required | Description                   |
|-----------|------------|----------|-------------------------------|
| `matchId` | `ObjectId` | Yes      | ID of the match to reprocess. |

#### Headers

| Header       | Type     | Required | Description                                                                                                                      |
|--------------|----------|----------|----------------------------------------------------------------------------------------------------------------------------------|
| `publish-id` | `String` | No       | Client-generated identifier echoed in the session response or error. Use a distinct value for each command to correlate replies. |

Object definitions are in [collection.md](collection.md).

### Success response

Delivered to the requesting session through `/user/queue/responses`.

| Detail        | Value                  |
|---------------|------------------------|
| STOMP frame   | `MESSAGE`              |
| Body          | `X01MatchMessage`      |
| `messageType` | `PROCESS_MATCH`        |
| `payload`     | Reprocessed `X01Match` |

The session response contains the match state after bot processing.

### Errors

Delivered to the requesting session through `/user/queue/errors` as a STOMP `MESSAGE` frame with a `WebSocketErrorMessage` body.

| Status | Type                   | Cause                                                    |
|--------|------------------------|----------------------------------------------------------|
| `400`  | `MESSAGE_NOT_READABLE` | `matchId` cannot be converted to an `ObjectId`.          |
| `404`  | `RESOURCE_NOT_FOUND`   | The match does not exist.                                |
| `409`  | `CONFLICT`             | A concurrent update prevents the match from being saved. |
| `500`  | `INTERNAL`             | An unexpected processing error occurs.                   |
| `503`  | `UNAVAILABLE`          | The database is unavailable.                             |

## Broadcasts

These broadcasts apply to both REST requests and WebSocket commands.

Subscribers to `/topic/matches/x01/{matchId}` receive a `PROCESS_MATCH` message after the reprocessed match is saved, followed by `ADD_BOT_TURN` messages for any dart bot turns processed.
