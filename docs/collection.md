# Darts Matcher

Shared types and object definitions for the Darts Matcher API.

## Type conventions

- `T[]` is an array of `T`.
- `Map<String, T>` is a JSON object with string keys and values of type `T`.
- `PlayerMap<T>` is a JSON object with player IDs as keys and values of type `T`.
- `ObjectId` is a string containing 24 hexadecimal characters.
- `UnixTimestamp` is a number of seconds since the Unix epoch; it may include a fractional part.
- In request tables, **Required** indicates whether a property must be supplied. Properties cannot be `null` unless their type includes `null`.

## Request and settings objects

### X01CreateMatchRequest

| Property        | Type                                                             | Required | Constraints                                        |
|-----------------|------------------------------------------------------------------|----------|----------------------------------------------------|
| `matchSettings` | [`X01MatchSettings`](#x01matchsettings)                          | Yes      | Match settings.                                    |
| `players`       | [`X01CreateMatchRequest.Player[]`](#x01creatematchrequestplayer) | Yes      | Between 1 and 4 players. Entries cannot be `null`. |

Player composition rules:

- At most one player may be a `DART_BOT`.
- A match containing a dart bot must also contain at least one `HUMAN`.

### X01CreateTurnRequest

| Property                | Type      | Required       | Description                                                                                                                                                                                                                                                                                      |
|-------------------------|-----------|----------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `score`                 | `Integer` | No             | Points scored during the turn. From `0` to `180` inclusive, excluding `163`, `166`, `169`, `172`, `173`, `175`, `176`, `178`, and `179`. Defaults to `0` when omitted or `null`.                                                                                                                 |
| `checkoutDoublesMissed` | `Integer` | No             | Missed attempts at a finishing double. From `0` to `3` inclusive. When `trackDoubles` is enabled, defaults to `0` when omitted or `null`; otherwise stored as `null`.                                                                                                                            |
| `openingDoublesMissed`  | `Integer` | No             | Missed attempts at an opening double. From `0` to `3` inclusive. When `trackDoubles` is enabled and `format` is `DIDO`, defaults to `0` when omitted or `null`; otherwise stored as `null`.                                                                                                      |
| `checkoutDartsUsed`     | `Integer` | For a checkout | Darts used to finish the leg. From `1` to `3` inclusive and at least the minimum needed for the checkout. Ignored when the turn does not finish the leg. If omitted or `null` when the score would reach zero, the turn is recorded with a score of `0` and the remaining score stays unchanged. |

### X01EditTurnRequest

Includes all properties of [X01CreateTurnRequest](#x01createturnrequest), plus:

| Property   | Type       | Required | Description                                   |
|------------|------------|----------|-----------------------------------------------|
| `playerId` | `ObjectId` | Yes      | ID of the player whose turn is being edited.  |
| `set`      | `Integer`  | Yes      | Set number, starting at `1`.                  |
| `leg`      | `Integer`  | Yes      | Leg number within the set, starting at `1`.   |
| `round`    | `Integer`  | Yes      | Round number within the leg, starting at `1`. |

### X01CreateMatchRequest.Player

| Property             | Type                                                  | Required    | Constraints                                                                                                                                      |
|----------------------|-------------------------------------------------------|-------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| `playerName`         | String                                                | Yes         | Between 3 and 30 characters inclusive. Cannot be blank. Must be unique within the match; comparison is case-sensitive and names are not trimmed. |
| `playerType`         | String                                                | Yes         | `HUMAN` or `DART_BOT`.                                                                                                                           |
| `x01DartBotSettings` | [`X01DartBotSettings`](#x01dartbotsettings) or `null` | Conditional | Required and non-null for `DART_BOT`. Must be omitted or `null` for `HUMAN`.                                                                     |

### X01MatchSettings

| Property       | Type                      | Required | Constraints                                                                  |
|----------------|---------------------------|----------|------------------------------------------------------------------------------|
| `x01`          | Integer                   | Yes      | Starting score, from 101 to 1001 inclusive.                                  |
| `trackDoubles` | Boolean or `null`         | No       | Enables double-attempt tracking. Defaults to `false` when omitted or `null`. |
| `bestOf`       | [`X01BestOf`](#x01bestof) | Yes      | Best-of settings.                                                            |
| `format`       | String                    | Yes      | `SIDO` (straight in, double out) or `DIDO` (double in, double out).          |

### X01BestOf

| Property                       | Type                                      | Required | Constraints                                                                               |
|--------------------------------|-------------------------------------------|----------|-------------------------------------------------------------------------------------------|
| `sets`                         | Integer                                   | Yes      | Best-of set count, from 1 to 49 inclusive. Normalized to `1` when `bestOfType` is `LEGS`. |
| `legs`                         | Integer                                   | Yes      | Best-of leg count per set, from 1 to 49 inclusive.                                        |
| `bestOfType`                   | String                                    | Yes      | `SETS` or `LEGS`.                                                                         |
| `clearByTwoSetsRule`           | [`X01ClearByTwoRule`](#x01clearbytworule) | Yes      | Clear-by-two rule for sets.                                                               |
| `clearByTwoLegsRule`           | [`X01ClearByTwoRule`](#x01clearbytworule) | Yes      | Clear-by-two rule for legs.                                                               |
| `clearByTwoLegsInFinalSetRule` | [`X01ClearByTwoRule`](#x01clearbytworule) | Yes      | Clear-by-two rule for legs in the final set.                                              |

### X01ClearByTwoRule

When enabled, requires a winning margin of two until the best-of count plus `limit` is reached.

| Property  | Type              | Required | Constraints                                                                                                 |
|-----------|-------------------|----------|-------------------------------------------------------------------------------------------------------------|
| `enabled` | Boolean or `null` | No       | Enables the rule. Defaults to `false` when omitted or `null`.                                               |
| `limit`   | Integer or `null` | No       | Maximum extension beyond the best-of count, from 0 to 20 inclusive. Defaults to `0` when omitted or `null`. |

### X01DartBotSettings

| Property           | Type    | Required | Constraints                                        |
|--------------------|---------|----------|----------------------------------------------------|
| `threeDartAverage` | Integer | Yes      | Bot's three-dart average, from 3 to 167 inclusive. |

## Response objects

### X01Match

| Property           | Type                                                 | Description                                               |
|--------------------|------------------------------------------------------|-----------------------------------------------------------|
| `id`               | `ObjectId`                                           | Match ID.                                                 |
| `version`          | Integer                                              | Persistence version, incremented when the match is saved. |
| `broadcastVersion` | Integer                                              | Sequence number used to order match updates.              |
| `startDate`        | `UnixTimestamp`                                      | Match start time.                                         |
| `endDate`          | `UnixTimestamp` or `null`                            | Match end time; `null` while the match is in play.        |
| `matchStatus`      | String                                               | `IN_PLAY` or `CONCLUDED`.                                 |
| `rematchId`        | `ObjectId` or `null`                                 | ID of the rematch; `null` if none has been created.       |
| `players`          | [`X01MatchPlayer[]`](#x01matchplayer)                | Match players in turn order.                              |
| `matchType`        | String                                               | `X01`.                                                    |
| `matchSettings`    | [`X01MatchSettings`](#x01matchsettings)              | Match settings with defaults applied.                     |
| `sets`             | [`X01SetEntry[]`](#x01setentry)                      | Sets ordered by set number.                               |
| `matchProgress`    | [`X01MatchProgress`](#x01matchprogress)              | Current position in the match.                            |
| `standings`        | [`PlayerMap<X01StandingsEntry>`](#x01standingsentry) | Standings by player ID.                                   |

### X01MatchPlayer

| Property             | Type                                                  | Description                                                    |
|----------------------|-------------------------------------------------------|----------------------------------------------------------------|
| `playerId`           | `ObjectId`                                            | Player ID.                                                     |
| `playerName`         | String                                                | Player name.                                                   |
| `playerType`         | String                                                | `HUMAN` or `DART_BOT`.                                         |
| `resultType`         | String or `null`                                      | `WIN`, `LOSS` or `DRAW`; `null` while the result is undecided. |
| `x01DartBotSettings` | [`X01DartBotSettings`](#x01dartbotsettings) or `null` | Bot settings for `DART_BOT`; `null` for `HUMAN`.               |
| `statistics`         | [`X01Statistics`](#x01statistics)                     | Player statistics for the match.                               |

### X01MatchProgress

| Property         | Type                 | Description                                                                   |
|------------------|----------------------|-------------------------------------------------------------------------------|
| `currentSet`     | Integer or `null`    | Current set number, starting at 1; `null` when there is no current set.       |
| `currentLeg`     | Integer or `null`    | Current leg number, starting at 1; `null` when there is no current leg.       |
| `currentRound`   | Integer or `null`    | Current round number, starting at 1; `null` when there is no current round.   |
| `currentThrower` | `ObjectId` or `null` | ID of the player whose turn is next; `null` when there is no current thrower. |

### X01StandingsEntry

| Property              | Type    | Description                            |
|-----------------------|---------|----------------------------------------|
| `setsWon`             | Integer | Number of sets won.                    |
| `legsWonInCurrentSet` | Integer | Number of legs won in the current set. |

### X01SetEntry

| Property    | Type                | Description                |
|-------------|---------------------|----------------------------|
| `setNumber` | Integer             | Set number, starting at 1. |
| `set`       | [`X01Set`](#x01set) | Set data.                  |

### X01Set

| Property      | Type                            | Description                                                                    |
|---------------|---------------------------------|--------------------------------------------------------------------------------|
| `legs`        | [`X01LegEntry[]`](#x01legentry) | Legs ordered by leg number.                                                    |
| `throwsFirst` | `ObjectId`                      | ID of the player who starts the set.                                           |
| `result`      | `PlayerMap<String>` or `null`   | Results by player ID: `WIN`, `LOSS` or `DRAW`. `null` until the set concludes. |

### X01LegEntry

| Property    | Type                | Description                |
|-------------|---------------------|----------------------------|
| `legNumber` | Integer             | Leg number, starting at 1. |
| `leg`       | [`X01Leg`](#x01leg) | Leg data.                  |

### X01Leg

| Property            | Type                                      | Description                                                        |
|---------------------|-------------------------------------------|--------------------------------------------------------------------|
| `winner`            | `ObjectId` or `null`                      | Winner's player ID; `null` until the leg is won.                   |
| `throwsFirst`       | `ObjectId`                                | ID of the player who starts the leg.                               |
| `checkoutDartsUsed` | Integer or `null`                         | Darts used in the checkout, from 1 to 3; `null` when not recorded. |
| `rounds`            | [`X01LegRoundEntry[]`](#x01legroundentry) | Rounds ordered by round number.                                    |

### X01LegRoundEntry

| Property      | Type                          | Description                  |
|---------------|-------------------------------|------------------------------|
| `roundNumber` | Integer                       | Round number, starting at 1. |
| `round`       | [`X01LegRound`](#x01leground) | Round data.                  |

### X01LegRound

| Property | Type                             | Description                  |
|----------|----------------------------------|------------------------------|
| `turns`  | [`PlayerMap<X01Turn>`](#x01turn) | Recorded turns by player ID. |

### X01Turn

| Property                | Type              | Description                                                                                           |
|-------------------------|-------------------|-------------------------------------------------------------------------------------------------------|
| `score`                 | Integer           | Recorded score, from 0 to 180 inclusive.                                                              |
| `remaining`             | Integer           | Remaining score after the turn; at least 0.                                                           |
| `checkoutDoublesMissed` | Integer or `null` | Missed checkout doubles, from 0 to 3; `null` when double tracking is disabled.                        |
| `openingDoublesMissed`  | Integer or `null` | Missed opening doubles, from 0 to 3; `null` when double tracking is disabled or the format is `SIDO`. |

### X01Statistics

| Property           | Type                                                        | Description                                              |
|--------------------|-------------------------------------------------------------|----------------------------------------------------------|
| `resultStatistics` | [`X01ResultStatistics`](#x01resultstatistics)               | Sets and legs won.                                       |
| `averageStats`     | [`X01AverageStatistics`](#x01averagestatistics)             | Scoring totals and averages.                             |
| `checkoutStats`    | [`X01CheckoutStatistics`](#x01checkoutstatistics)           | Checkout statistics.                                     |
| `doubleInStats`    | [`X01DoubleInStatistics`](#x01doubleinstatistics) or `null` | Opening-double statistics for `DIDO`; `null` for `SIDO`. |
| `scoreStatistics`  | [`X01ScoreStatistics`](#x01scorestatistics)                 | Turn counts by score range.                              |

### X01ResultStatistics

| Property  | Type    | Description         |
|-----------|---------|---------------------|
| `setsWon` | Integer | Number of sets won. |
| `legsWon` | Integer | Number of legs won. |

### X01AverageStatistics

| Property                | Type              | Description                                                                                                    |
|-------------------------|-------------------|----------------------------------------------------------------------------------------------------------------|
| `pointsThrown`          | Integer           | Total points scored.                                                                                           |
| `dartsThrown`           | Integer           | Total darts counted.                                                                                           |
| `average`               | Integer or `null` | Three-dart average, rounded to the nearest integer; `null` before any darts are counted.                       |
| `pointsThrownFirstNine` | Integer           | Total points scored in the first nine darts of each leg.                                                       |
| `dartsThrownFirstNine`  | Integer           | Total darts counted in the first nine darts of each leg.                                                       |
| `averageFirstNine`      | Integer or `null` | First-nine three-dart average, rounded to the nearest integer; `null` before any first-nine darts are counted. |

### X01CheckoutStatistics

| Property             | Type              | Description                                                                                                              |
|----------------------|-------------------|--------------------------------------------------------------------------------------------------------------------------|
| `checkoutHighest`    | Integer or `null` | Highest successful checkout, from 2 to 170; `null` before a successful checkout.                                         |
| `checkoutTonPlus`    | Integer           | Number of successful checkouts of at least 100.                                                                          |
| `checkoutPercentage` | Integer or `null` | Checkout success percentage, from 0 to 100; `null` when doubles are not tracked or there have been no checkout attempts. |
| `checkoutsMissed`    | Integer or `null` | Missed checkout doubles; `null` when doubles are not tracked or no turns have been recorded.                             |
| `checkoutsHit`       | Integer           | Number of successful checkouts.                                                                                          |

### X01DoubleInStatistics

| Property            | Type              | Description                                                                                                                   |
|---------------------|-------------------|-------------------------------------------------------------------------------------------------------------------------------|
| `doublesHit`        | Integer           | Number of successful opening doubles.                                                                                         |
| `doublesMissed`     | Integer or `null` | Missed opening doubles; `null` when doubles are not tracked or no turns have been recorded.                                   |
| `doublesPercentage` | Integer or `null` | Opening-double success percentage, from 0 to 100; `null` when doubles are not tracked or there have been no opening attempts. |

### X01ScoreStatistics

| Property       | Type    | Description                      |
|----------------|---------|----------------------------------|
| `fortyPlus`    | Integer | Number of turns scoring 40–59.   |
| `sixtyPlus`    | Integer | Number of turns scoring 60–79.   |
| `eightyPlus`   | Integer | Number of turns scoring 80–99.   |
| `tonPlus`      | Integer | Number of turns scoring 100–139. |
| `tonFortyPlus` | Integer | Number of turns scoring 140–179. |
| `tonEighty`    | Integer | Number of turns scoring 180.     |

### X01Checkout

| Property    | Type      | Description                                                                                                            |
|-------------|-----------|------------------------------------------------------------------------------------------------------------------------|
| `checkout`  | `Integer` | Remaining score to finish. From `2` to `170` inclusive, excluding `159`, `162`, `163`, `165`, `166`, `168`, and `169`. |
| `minDarts`  | `Integer` | Minimum number of darts needed to finish. From `1` to `3` inclusive.                                                   |
| `suggested` | `Dart[]`  | Suggested dart sequence, in throwing order. See [Dart](#dart).                                                         |

### Dart

| Property  | Type                   | Description                                                      |
|-----------|------------------------|------------------------------------------------------------------|
| `section` | `DartboardSection`     | Dartboard section. See [DartboardSection](#dartboardsection).    |
| `area`    | `DartboardSectionArea` | Scoring area. See [DartboardSectionArea](#dartboardsectionarea). |
| `score`   | `Integer`              | Points scored by the dart, calculated from its section and area. |

### DartboardSection

String enum.

| Value       | Section                        |
|-------------|--------------------------------|
| `ONE`       | 1                              |
| `TWO`       | 2                              |
| `THREE`     | 3                              |
| `FOUR`      | 4                              |
| `FIVE`      | 5                              |
| `SIX`       | 6                              |
| `SEVEN`     | 7                              |
| `EIGHT`     | 8                              |
| `NINE`      | 9                              |
| `TEN`       | 10                             |
| `ELEVEN`    | 11                             |
| `TWELVE`    | 12                             |
| `THIRTEEN`  | 13                             |
| `FOURTEEN`  | 14                             |
| `FIFTEEN`   | 15                             |
| `SIXTEEN`   | 16                             |
| `SEVENTEEN` | 17                             |
| `EIGHTEEN`  | 18                             |
| `NINETEEN`  | 19                             |
| `TWENTY`    | 20                             |
| `BULL`      | Bull, with a base value of 25. |
| `MISS`      | Miss, scoring 0.               |

### DartboardSectionArea

String enum.

| Value          | Description                                      |
|----------------|--------------------------------------------------|
| `DOUBLE_BULL`  | Inner bull. Scores 50.                           |
| `SINGLE_BULL`  | Outer bull. Scores 25.                           |
| `INNER_SINGLE` | Single area between the bull and triple ring.    |
| `TRIPLE`       | Triple the section value.                        |
| `OUTER_SINGLE` | Single area between the triple and double rings. |
| `DOUBLE`       | Double the section value.                        |
| `MISS`         | Miss, scoring 0.                                 |

## WebSocket messages

### X01MatchMessage

JSON body used for initial match state, match broadcasts, and successful command responses.

| Property      | Type                     | Description                                                              |
|---------------|--------------------------|--------------------------------------------------------------------------|
| `messageType` | `X01MatchMessageType`    | Identifies the message. See [X01MatchMessageType](#x01matchmessagetype). |
| `payload`     | `X01Match` or `ObjectId` | Data associated with the message. The type depends on `messageType`.     |

### X01MatchMessageType

String enum.

| Value              | Payload type | Description                                                                      |
|--------------------|--------------|----------------------------------------------------------------------------------|
| `PROCESS_MATCH`    | `X01Match`   | Current or reprocessed match state.                                              |
| `ADD_HUMAN_TURN`   | `X01Match`   | Match state after adding a human turn.                                           |
| `ADD_BOT_TURN`     | `X01Match`   | Match state after adding a dart bot turn.                                        |
| `EDIT_TURN`        | `X01Match`   | Match state after editing a turn.                                                |
| `DELETE_LAST_TURN` | `X01Match`   | Match state after deleting the last human turn and any following dart bot turns. |
| `DELETE_MATCH`     | `ObjectId`   | ID of the deleted match.                                                         |
| `RESET_MATCH`      | `X01Match`   | Match state after resetting.                                                     |
| `REMATCH`          | `X01Match`   | Original match containing the rematch reference.                                 |

## Errors

### ErrorResponse

| Property       | Type                            | Description                                                                                                                                                                                     |
|----------------|---------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `type`         | [`ApiErrorCode`](#apierrorcode) | Error code.                                                                                                                                                                                     |
| `description`  | String                          | Human-readable error description.                                                                                                                                                               |
| `status`       | Integer                         | HTTP status code.                                                                                                                                                                               |
| `targetErrors` | `Map<String, String>`           | Validation messages by property path for `INVALID_ARGUMENTS` errors. Paths use dot notation and zero-based array indexes. The key `root` identifies an error that applies to the whole request. |

### WebSocketErrorMessage

Includes all properties of [ErrorResponse](#errorresponse), plus:

| Property      | Type     | Description                                             |
|---------------|----------|---------------------------------------------------------|
| `destination` | `String` | STOMP destination associated with the failed operation. |

The `status` property contains the HTTP-equivalent error code; it is not an HTTP response status for the WebSocket message.

Errors are delivered directly as the JSON body, without the `messageType` and `payload` wrapper.

### ApiErrorCode

String with one of the following values.

| Value                    | HTTP status                  | Meaning                                                                      |
|--------------------------|------------------------------|------------------------------------------------------------------------------|
| `INVALID_ARGUMENTS`      | `400 Bad Request`            | Request values fail validation.                                              |
| `MESSAGE_NOT_READABLE`   | `400 Bad Request`            | The request body or a parameter cannot be read or converted.                 |
| `RESOURCE_NOT_FOUND`     | `404 Not Found`              | The requested resource does not exist.                                       |
| `URI_NOT_FOUND`          | `404 Not Found`              | The URL does not match an API route.                                         |
| `METHOD_NOT_ALLOWED`     | `405 Method Not Allowed`     | The HTTP method is not supported for the URL.                                |
| `NOT_ACCEPTABLE`         | `406 Not Acceptable`         | The `Accept` header requests a response format the operation cannot produce. |
| `CONFLICT`               | `409 Conflict`               | A concurrent update prevents the change from being saved.                    |
| `UNSUPPORTED_MEDIA_TYPE` | `415 Unsupported Media Type` | The `Content-Type` header specifies an unsupported request body format.      |
| `INTERNAL`               | `500 Internal Server Error`  | An unexpected server error occurred.                                         |
| `UNAVAILABLE`            | `503 Service Unavailable`    | The database is unavailable.                                                 |

REST responses with status `406 Not Acceptable` return an `ErrorResponse` body as `application/json`, even when the request's `Accept` header excludes JSON.

### REST error response headers

REST responses with status `405 Method Not Allowed` include the following header:

| Header  | Type     | Description                                                  |
|---------|----------|--------------------------------------------------------------|
| `Allow` | `String` | Comma-separated HTTP methods supported by the requested URL. |