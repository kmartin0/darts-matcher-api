# Get X01 checkout

Returns the checkout configuration and suggested dart sequence for a remaining score.

## Request

| Detail           | Value                        |
|------------------|------------------------------|
| Method           | `GET`                        |
| URL              | `/checkouts/x01/{remaining}` |
| Authorization    | Not required                 |
| Consumes         | None                         |
| Body             | None                         |
| Query parameters | None                         |

### Path parameters

| Parameter   | Type      | Required | Description                                                                                                                                    |
|-------------|-----------|----------|------------------------------------------------------------------------------------------------------------------------------------------------|
| `remaining` | `Integer` | Yes      | Remaining score to finish. A checkout is available from `2` to `170` inclusive, excluding `159`, `162`, `163`, `165`, `166`, `168`, and `169`. |

Object definitions are in the collection documentation.

## Success response

| Detail   | Value              |
|----------|--------------------|
| Status   | `200 OK`           |
| Produces | `application/json` |
| Body     | `X01Checkout`      |

## Error responses

Produces `application/json` with an `ErrorResponse` body.

| Status                      | Type                   | Cause                                                       |
|-----------------------------|------------------------|-------------------------------------------------------------|
| `400 Bad Request`           | `MESSAGE_NOT_READABLE` | `remaining` cannot be converted to a 32-bit signed integer. |
| `404 Not Found`             | `RESOURCE_NOT_FOUND`   | No checkout exists for the remaining score.                 |
| `500 Internal Server Error` | `INTERNAL`             | An unexpected processing error occurs.                      |