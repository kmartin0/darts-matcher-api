package nl.kmartin.dartsmatcherapi.error.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enum class which maps the api error codes with the corresponding HTTP Status Codes.
 */
@AllArgsConstructor
@Getter
public enum ApiErrorCode {
    INVALID_ARGUMENTS(HttpStatus.BAD_REQUEST), // 400
    MESSAGE_NOT_READABLE(HttpStatus.BAD_REQUEST), // 400
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND), // 404
    URI_NOT_FOUND(HttpStatus.NOT_FOUND), // 404
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED), // 405
    CONFLICT(HttpStatus.CONFLICT), // 409
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE), // 415
    INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR), // 500
    UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE); // 503
    private final HttpStatus httpStatus;
}
