# Delete X01 match

Deletes a match and clears `rematchId` on any matches that reference it. Other matches are not deleted.

## REST

### Request

| Detail           | Value                    |
|------------------|--------------------------|
| Method           | `DELETE`                 |
| URL              | `/matches/x01/{matchId}` |
| Authorization    | Not required             |
| Consumes         | None                     |
| Body             | None                     |
| Query parameters | None                     |

#### Path parameters

| Parameter | Type       | Required | Description                |
|-----------|------------|----------|----------------------------|
| `matchId` | `ObjectId` | Yes      | ID of the match to delete. |

Object definitions are in [collection.md](collection.md).

### Success response

| Detail   | Value    |
|----------|----------|
| Status   | `200 OK` |
| Produces | None     |
| Body     | None     |

### Error responses

Produces `application/json` with an `ErrorResponse` body.

| Status                      | Type                   | Cause                                                              |
|-----------------------------|------------------------|--------------------------------------------------------------------|
| `400 Bad Request`           | `MESSAGE_NOT_READABLE` | `matchId` cannot be converted to an `ObjectId`.                    |
| `404 Not Found`             | `RESOURCE_NOT_FOUND`   | The match does not exist.                                          |
| `409 Conflict`              | `CONFLICT`             | A concurrent update prevents a referencing match from being saved. |
| `500 Internal Server Error` | `INTERNAL`             | An unexpected processing error occurs.                             |
| `503 Service Unavailable`   | `UNAVAILABLE`          | The database is unavailable.                                       |

## WebSocket

### Command

| Detail        | Value                               |
|---------------|-------------------------------------|
| STOMP command | `SEND`                              |
| Destination   | `/app/matches/x01/{matchId}/delete` |
| Authorization | Not required                        |
| Body          | None                                |

#### Destination parameters

| Parameter | Type       | Required | Description                |
|-----------|------------|----------|----------------------------|
| `matchId` | `ObjectId` | Yes      | ID of the match to delete. |

#### Headers

| Header       | Type     | Required | Description                                                                                                                      |
|--------------|----------|----------|----------------------------------------------------------------------------------------------------------------------------------|
| `publish-id` | `String` | No       | Client-generated identifier echoed in the session response or error. Use a distinct value for each command to correlate replies. |

Object definitions are in [collection.md](collection.md).

### Success response

Delivered to the requesting session through `/user/queue/responses`.

| Detail        | Value                            |
|---------------|----------------------------------|
| STOMP frame   | `MESSAGE`                        |
| Body          | `X01MatchMessage`                |
| `messageType` | `DELETE_MATCH`                   |
| `payload`     | `ObjectId` of the deleted match. |

### Errors

Delivered to the requesting session through `/user/queue/errors` as a STOMP `MESSAGE` frame with a `WebSocketErrorMessage` body.

| Status | Type                   | Cause                                                              |
|--------|------------------------|--------------------------------------------------------------------|
| `400`  | `MESSAGE_NOT_READABLE` | `matchId` cannot be converted to an `ObjectId`.                    |
| `404`  | `RESOURCE_NOT_FOUND`   | The match does not exist.                                          |
| `409`  | `CONFLICT`             | A concurrent update prevents a referencing match from being saved. |
| `500`  | `INTERNAL`             | An unexpected processing error occurs.                             |
| `503`  | `UNAVAILABLE`          | The database is unavailable.                                       |

## Broadcasts

These broadcasts apply to both REST requests and WebSocket commands.

Subscribers to `/topic/matches/x01/{matchId}` receive a `DELETE_MATCH` message containing the deleted match's ID.

When another match's `rematchId` is cleared, subscribers to that match's topic receive a `PROCESS_MATCH` message containing its updated state.
