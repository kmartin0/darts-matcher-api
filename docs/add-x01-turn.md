# Add X01 turn

Adds a turn for the current thrower in an X01 match.

## Turn processing

A turn that leaves a remaining score below zero or equal to one, or reaches zero without a valid checkout, is recorded with a score of `0` and leaves the remaining score unchanged.

Supplying too few darts for an otherwise valid checkout produces an `INVALID_ARGUMENTS` error targeting `checkoutDartsUsed`.

## REST

### Request

| Detail           | Value                             |
|------------------|-----------------------------------|
| Method           | `POST`                            |
| URL              | `/matches/x01/{matchId}/turns`    |
| Authorization    | Not required                      |
| Consumes         | `application/json`                |
| Body             | `X01CreateTurnRequest` — required |
| Query parameters | None                              |

#### Path parameters

| Parameter | Type       | Required | Description                |
|-----------|------------|----------|----------------------------|
| `matchId` | `ObjectId` | Yes      | ID of the match to update. |

Object definitions are in [collection.md](collection.md).

### Success response

| Detail   | Value              |
|----------|--------------------|
| Status   | `200 OK`           |
| Produces | `application/json` |
| Body     | `X01Match`         |

### Error responses

Produces `application/json` with an `ErrorResponse` body.

| Status                       | Type                     | Cause                                                                                                                                            |
|------------------------------|--------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| `400 Bad Request`            | `INVALID_ARGUMENTS`      | The request body fails validation or the turn cannot be applied to the current match state. `targetErrors` identifies the affected properties.   |
| `400 Bad Request`            | `MESSAGE_NOT_READABLE`   | `matchId` cannot be converted to an `ObjectId`, or the body is missing, contains invalid JSON, or cannot be converted to `X01CreateTurnRequest`. |
| `404 Not Found`              | `RESOURCE_NOT_FOUND`     | The match or its active set, leg or round cannot be found.                                                                                       |
| `406 Not Acceptable`         | `NOT_ACCEPTABLE`         | The `Accept` header requests a response format the operation cannot produce.                                                                     |
| `409 Conflict`               | `CONFLICT`               | A concurrent update prevents the match from being saved.                                                                                         |
| `415 Unsupported Media Type` | `UNSUPPORTED_MEDIA_TYPE` | The `Content-Type` header specifies a request body format the operation does not support.                                                        |
| `500 Internal Server Error`  | `INTERNAL`               | An unexpected error occurs while processing the turn.                                                                                            |
| `503 Service Unavailable`    | `UNAVAILABLE`            | The database is unavailable.                                                                                                                     |

## WebSocket

### Command

| Detail        | Value                                  |
|---------------|----------------------------------------|
| STOMP command | `SEND`                                 |
| Destination   | `/app/matches/x01/{matchId}/turns/add` |
| Authorization | Not required                           |
| Content type  | `application/json`                     |
| Body          | `X01CreateTurnRequest` — required      |

#### Destination parameters

| Parameter | Type       | Required | Description                |
|-----------|------------|----------|----------------------------|
| `matchId` | `ObjectId` | Yes      | ID of the match to update. |

#### Headers

| Header       | Type     | Required | Description                                                                                                                      |
|--------------|----------|----------|----------------------------------------------------------------------------------------------------------------------------------|
| `publish-id` | `String` | No       | Client-generated identifier echoed in the session response or error. Use a distinct value for each command to correlate replies. |

Object definitions are in [collection.md](collection.md).

### Success response

Delivered to the requesting session through `/user/queue/responses`.

| Detail        | Value              |
|---------------|--------------------|
| STOMP frame   | `MESSAGE`          |
| Body          | `X01MatchMessage`  |
| `messageType` | `ADD_HUMAN_TURN`   |
| `payload`     | Updated `X01Match` |

The session response contains the match state after bot processing.

### Errors

Delivered to the requesting session through `/user/queue/errors` as a STOMP `MESSAGE` frame with a `WebSocketErrorMessage` body.

| Status | Type                   | Cause                                                                                                                                         |
|--------|------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| `400`  | `INVALID_ARGUMENTS`    | A body property fails validation or the turn cannot be applied to the current match state. `targetErrors` identifies the affected properties. |
| `400`  | `MESSAGE_NOT_READABLE` | `matchId` cannot be converted to an `ObjectId`, or the message body cannot be read as `X01CreateTurnRequest`.                                 |
| `404`  | `RESOURCE_NOT_FOUND`   | The match or active set, leg, or round cannot be found.                                                                                       |
| `409`  | `CONFLICT`             | A concurrent update prevents the match from being saved.                                                                                      |
| `500`  | `INTERNAL`             | An unexpected processing error occurs.                                                                                                        |
| `503`  | `UNAVAILABLE`          | The database is unavailable.                                                                                                                  |

## Broadcasts

These broadcasts apply to both REST requests and WebSocket commands.

Subscribers to `/topic/matches/x01/{matchId}` receive an `ADD_HUMAN_TURN` message after the turn is saved, followed by `ADD_BOT_TURN` messages for any dart bot turns processed.
