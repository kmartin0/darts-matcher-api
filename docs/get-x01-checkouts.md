# Get X01 checkouts

Returns all available X01 checkout configurations and their suggested dart sequences.

## Request

| Detail           | Value            |
|------------------|------------------|
| Method           | `GET`            |
| URL              | `/checkouts/x01` |
| Authorization    | Not required     |
| Consumes         | None             |
| Body             | None             |
| Path parameters  | None             |
| Query parameters | None             |

Object definitions are in the collection documentation.

## Success response

| Detail   | Value              |
|----------|--------------------|
| Status   | `200 OK`           |
| Produces | `application/json` |
| Body     | `X01Checkout[]`    |

The order of checkouts is not guaranteed.

## Error responses

Produces `application/json` with an `ErrorResponse` body.

| Status                      | Type       | Cause                                  |
|-----------------------------|------------|----------------------------------------|
| `500 Internal Server Error` | `INTERNAL` | An unexpected processing error occurs. |