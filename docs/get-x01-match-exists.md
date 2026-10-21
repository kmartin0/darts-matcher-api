# Check whether an X01 match exists

Checks whether an X01 match exists.

## Request

| Detail           | Value                           |
|------------------|---------------------------------|
| Method           | `GET`                           |
| URL              | `/matches/x01/{matchId}/exists` |
| Authorization    | Not required                    |
| Consumes         | None                            |
| Body             | None                            |
| Query parameters | None                            |

### Path parameters

| Parameter | Type       | Required | Description               |
|-----------|------------|----------|---------------------------|
| `matchId` | `ObjectId` | Yes      | ID of the match to check. |

Object definitions are in the collection documentation.

## Success response

| Detail   | Value            |
|----------|------------------|
| Status   | `204 No Content` |
| Produces | None             |
| Body     | None             |

## Error responses

Produces `application/json` with an `ErrorResponse` body.

| Status                      | Type                   | Cause                                                               |
|-----------------------------|------------------------|---------------------------------------------------------------------|
| `400 Bad Request`           | `MESSAGE_NOT_READABLE` | `matchId` cannot be converted to an `ObjectId`.                     |
| `404 Not Found`             | `RESOURCE_NOT_FOUND`   | No match exists with the supplied ID.                               |
| `500 Internal Server Error` | `INTERNAL`             | An unexpected error occurs while checking whether the match exists. |
| `503 Service Unavailable`   | `UNAVAILABLE`          | The database is unavailable.                                        |