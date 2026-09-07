package nl.kmartin.dartsmatcherapi.error.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.ToString;
import nl.kmartin.dartsmatcherapi.error.util.TargetErrorUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the error response returned by the API.
 *
 * Contains the API error code, description, HTTP status code and optional
 * target-specific errors that clients can use to associate errors with fields.
 */
@Getter
@ToString
public class ErrorResponse implements Serializable {
    private final String error;
    private final String description;
    private final int code;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Map<String, String> targetErrors;

    /**
     * Creates an error response containing target-specific errors.
     *
     * @param apiErrorCode the API error code
     * @param description  the error description
     * @param targetErrors the target-specific errors
     */
    public ErrorResponse(ApiErrorCode apiErrorCode, String description, TargetError... targetErrors) {
        this.code = apiErrorCode.getHttpStatus().value();
        this.description = description;
        this.error = apiErrorCode.name();
        this.targetErrors = TargetErrorUtil.targetErrorsToMap(targetErrors);
    }

    /**
     * Creates an error response without target-specific errors.
     *
     * @param apiErrorCode the API error code
     * @param description  the error description
     */
    public ErrorResponse(ApiErrorCode apiErrorCode, String description) {
        this.code = apiErrorCode.getHttpStatus().value();
        this.description = description;
        this.error = apiErrorCode.name();
        this.targetErrors = new HashMap<>();
    }
}