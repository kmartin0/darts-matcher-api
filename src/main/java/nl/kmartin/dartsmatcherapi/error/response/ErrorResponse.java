package nl.kmartin.dartsmatcherapi.error.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import nl.kmartin.dartsmatcherapi.error.util.ErrorUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class ErrorResponse implements Serializable {
    // The Status
    private String error;

    // A developer-facing human-readable error description in English.
    private String description;

    // The HTTP Status Code
    private int code;

    // (Optional) Additional user-friendly error information that the client code can use to handle
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> targetErrors;

    public ErrorResponse(ApiErrorCode apiErrorCode, String description, TargetError... targetErrors) {
        this.code = apiErrorCode.getHttpStatus().value();
        this.description = description;
        this.error = apiErrorCode.name();
        this.targetErrors = ErrorUtil.targetErrorsToMap(targetErrors);
    }

    public ErrorResponse(ApiErrorCode apiErrorCode, String description) {
        this.code = apiErrorCode.getHttpStatus().value();
        this.description = description;
        this.error = apiErrorCode.name();
        this.targetErrors = new HashMap<>();
    }
}
