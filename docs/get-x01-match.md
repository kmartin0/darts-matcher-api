# Get X01 match

Retrieves an X01 match by its ID.

## Request

| Detail           | Value                    |
|------------------|--------------------------|
| Method           | `GET`                    |
| URL              | `/matches/x01/{matchId}` |
| Authorization    | Not required             |
| Consumes         | None                     |
| Body             | None                     |
| Query parameters | None                     |

### Path parameters

| Parameter | Type       | Required | Description                  |
|-----------|------------|----------|------------------------------|
| `matchId` | `ObjectId` | Yes      | ID of the match to retrieve. |

Object definitions are in the collection documentation.

## Success response

| Detail   | Value              |
|----------|--------------------|
| Status   | `200 OK`           |
| Produces | `application/json` |
| Body     | `X01Match`         |

## Error responses

Produces `application/json` with an `ErrorResponse` body.

| Status                      | Type                   | Cause                                                  |
|-----------------------------|------------------------|--------------------------------------------------------|
| `400 Bad Request`           | `MESSAGE_NOT_READABLE` | `matchId` cannot be converted to an `ObjectId`.        |
| `404 Not Found`             | `RESOURCE_NOT_FOUND`   | No match exists with the supplied ID.                  |
| `500 Internal Server Error` | `INTERNAL`             | An unexpected error occurs while retrieving the match. |
| `503 Service Unavailable`   | `UNAVAILABLE`          | The database is unavailable.                           |