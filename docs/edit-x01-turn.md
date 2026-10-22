# Edit X01 turn

Replaces an existing turn and recalculates the match results, statistics, and progress.

## Turn processing

The supplied values replace the turn. Omitted properties use the defaults documented in `X01CreateTurnRequest`; their existing values are not preserved.

In a concluded leg, only turns belonging to the leg winner can be edited.

A replacement that leaves a remaining score below zero or equal to one, or reaches zero without a valid checkout, is recorded with a score of `0`. The same applies when the replacement would make the player's following turns in that leg invalid.

Supplying too few darts for an otherwise valid checkout produces an `INVALID_ARGUMENTS` error targeting `checkoutDartsUsed`.

Recalculation removes history that can no longer apply, including later legs or sets when an earlier leg or set becomes unfinished.

## REST

### Request

| Detail           | Value                               |
|------------------|-------------------------------------|
| Method           | `POST`                              |
| URL              | `/matches/x01/{matchId}/turns/edit` |
| Authorization    | Not required                        |
| Consumes         | `application/json`                  |
| Body             | `X01EditTurnRequest` — required     |
| Query parameters | None                                |

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

| Status                       | Type                     | Cause                                                                                                                                                                                   |
|------------------------------|--------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `400 Bad Request`            | `INVALID_ARGUMENTS`      | The request body fails validation, the selected player is not the winner of a concluded leg, or too few checkout darts are supplied. `targetErrors` identifies the affected properties. |
| `400 Bad Request`            | `MESSAGE_NOT_READABLE`   | An ID cannot be converted to an `ObjectId`, or the body is missing, malformed, or contains a value that cannot be converted to its expected type.                                       |
| `404 Not Found`              | `RESOURCE_NOT_FOUND`     | The match, set, leg, round, or player's turn does not exist.                                                                                                                            |
| `406 Not Acceptable`         | `NOT_ACCEPTABLE`         | The `Accept` header requests a response format the operation cannot produce.                                                                                                            |
| `409 Conflict`               | `CONFLICT`               | A concurrent update prevents the match from being saved.                                                                                                                                |
| `415 Unsupported Media Type` | `UNSUPPORTED_MEDIA_TYPE` | The `Content-Type` header specifies a request body format the operation does not support.                                                                                               |
| `500 Internal Server Error`  | `INTERNAL`               | An unexpected processing error occurs.                                                                                                                                                  |
| `503 Service Unavailable`    | `UNAVAILABLE`            | The database is unavailable.                                                                                                                                                            |

## WebSocket

### Command

| Detail        | Value                                   |
|---------------|-----------------------------------------|
| STOMP command | `SEND`                                  |
| Destination   | `/app/matches/x01/{matchId}/turns/edit` |
| Authorization | Not required                            |
| Content type  | `application/json`                      |
| Body          | `X01EditTurnRequest` — required         |

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
| `messageType` | `EDIT_TURN`        |
| `payload`     | Updated `X01Match` |

The session response contains the match state after bot processing.

### Errors

Delivered to the requesting session through `/user/queue/errors` as a STOMP `MESSAGE` frame with a `WebSocketErrorMessage` body.

| Status | Type                   | Cause                                                                                                                                                                                  |
|--------|------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `400`  | `INVALID_ARGUMENTS`    | A body property fails validation, the selected player is not the winner of a concluded leg, or too few checkout darts are supplied. `targetErrors` identifies the affected properties. |
| `400`  | `MESSAGE_NOT_READABLE` | An ID cannot be converted to an `ObjectId`, or the message body cannot be read as `X01EditTurnRequest`.                                                                                |
| `404`  | `RESOURCE_NOT_FOUND`   | The match, set, leg, round, or player's turn does not exist.                                                                                                                           |
| `409`  | `CONFLICT`             | A concurrent update prevents the match from being saved.                                                                                                                               |
| `500`  | `INTERNAL`             | An unexpected processing error occurs.                                                                                                                                                 |
| `503`  | `UNAVAILABLE`          | The database is unavailable.                                                                                                                                                           |

## Broadcasts

These broadcasts apply to both REST requests and WebSocket commands.

Subscribers to `/topic/matches/x01/{matchId}` receive an `EDIT_TURN` message after the replacement is saved, followed by `ADD_BOT_TURN` messages for any dart bot turns processed.
