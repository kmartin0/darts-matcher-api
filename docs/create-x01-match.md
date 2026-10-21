# Create X01 match

Creates an X01 match with the specified match settings and players.

## Request

| Detail           | Value                              |
|------------------|------------------------------------|
| Method           | `POST`                             |
| URL              | `/matches/x01`                     |
| Authorization    | Not required                       |
| Consumes         | `application/json`                 |
| Body             | `X01CreateMatchRequest` — required |
| Path parameters  | None                               |
| Query parameters | None                               |

Object definitions are in the collection documentation.

## Success response

| Detail   | Value              |
|----------|--------------------|
| Status   | `201 Created`      |
| Produces | `application/json` |
| Body     | `X01Match`         |

## Error responses

Produces `application/json` with an `ErrorResponse` body.

| Status                       | Type                     | Cause                                                                                          |
|------------------------------|--------------------------|------------------------------------------------------------------------------------------------|
| `400 Bad Request`            | `INVALID_ARGUMENTS`      | The request body fails validation. `targetErrors` identifies the affected properties.          |
| `400 Bad Request`            | `MESSAGE_NOT_READABLE`   | The body is missing, contains invalid JSON, or cannot be converted to `X01CreateMatchRequest`. |
| `409 Conflict`               | `CONFLICT`               | A concurrent update prevents the match from being saved during processing.                     |
| `415 Unsupported Media Type` | `UNSUPPORTED_MEDIA_TYPE` | The request body uses an unsupported media type.                                               |
| `500 Internal Server Error`  | `INTERNAL`               | An unexpected error occurs while creating or processing the match.                             |
| `503 Service Unavailable`    | `UNAVAILABLE`            | The database is unavailable.                                                                   |