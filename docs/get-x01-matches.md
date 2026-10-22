# Get X01 matches by IDs

Retrieves existing X01 matches in the order of the supplied IDs. Matches that do not exist are omitted.

## Request

| Detail          | Value          |
|-----------------|----------------|
| Method          | `GET`          |
| URL             | `/matches/x01` |
| Authorization   | Not required   |
| Consumes        | None           |
| Body            | None           |
| Path parameters | None           |

### Query parameters

| Parameter | Type         | Required | Constraints                |
|-----------|--------------|----------|----------------------------|
| `ids`     | `ObjectId[]` | Yes      | Comma-separated match IDs. |

Object definitions are in the collection documentation.

## Success response

| Detail   | Value              |
|----------|--------------------|
| Status   | `200 OK`           |
| Produces | `application/json` |
| Body     | `X01Match[]`       |

## Error responses

Produces `application/json` with an `ErrorResponse` body.

| Status                      | Type                   | Cause                                                                        |
|-----------------------------|------------------------|------------------------------------------------------------------------------|
| `400 Bad Request`           | `INVALID_ARGUMENTS`    | The `ids` query parameter is missing. `targetErrors` identifies `ids`.       |
| `400 Bad Request`           | `MESSAGE_NOT_READABLE` | One or more IDs cannot be converted to an `ObjectId`.                        |
| `406 Not Acceptable`        | `NOT_ACCEPTABLE`       | The `Accept` header requests a response format the operation cannot produce. |
| `500 Internal Server Error` | `INTERNAL`             | An unexpected error occurs while retrieving the matches.                     |
| `503 Service Unavailable`   | `UNAVAILABLE`          | The database is unavailable.                                                 |