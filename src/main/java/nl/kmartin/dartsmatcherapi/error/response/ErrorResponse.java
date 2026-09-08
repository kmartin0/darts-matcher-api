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
 * Contains:
 * • {@code type} - the application error classification
 * • {@code description} - the human-readable error description
 * • {@code status} - the numeric status associated with the error
 * • {@code targetErrors} - optional target-specific errors
 */
@Getter
@ToString
public class ErrorResponse implements Serializable {
    private final ApiErrorCode type;
    private final String description;
    private final int status;

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
        this.status = apiErrorCode.getHttpStatus().value();
        this.description = description;
        this.type = apiErrorCode;
        this.targetErrors = TargetErrorUtil.targetErrorsToMap(targetErrors);
    }

    /**
     * Creates an error response without target-specific errors.
     *
     * @param apiErrorCode the API error code
     * @param description  the error description
     */
    public ErrorResponse(ApiErrorCode apiErrorCode, String description) {
        this.status = apiErrorCode.getHttpStatus().value();
        this.description = description;
        this.type = apiErrorCode;
        this.targetErrors = new HashMap<>();
    }
}