package nl.kmartin.dartsmatcherapi.error.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import nl.kmartin.dartsmatcherapi.error.response.TargetError;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides utility methods for extracting and mapping validation target errors.
 */
public final class ErrorUtil {
    private ErrorUtil() {
    }

    /**
     * Extracts target errors from constraint violations.
     *
     * The constraint violation property path is converted to the target path used by the API error response.
     *
     * @param e the constraint violation exception
     * @return the extracted target errors
     */
    public static ArrayList<TargetError> extractTargetErrors(ConstraintViolationException e) {
        ArrayList<TargetError> errors = new ArrayList<>();

        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            ArrayList<Path.Node> violationPropertyPaths = new ArrayList<>();
            violation.getPropertyPath().iterator().forEachRemaining(violationPropertyPaths::add);

            StringBuilder errorPath = new StringBuilder();
            int startIndex = violationPropertyPaths.size() > 2 ? 2 : 0;

            for (int i = startIndex; i < violationPropertyPaths.size(); i++) {
                errorPath.append(violationPropertyPaths.get(i));
                if (i < violationPropertyPaths.size() - 1) {
                    errorPath.append(".");
                }
            }

            errors.add(new TargetError(errorPath.toString(), violation.getMessage()));
        }

        return errors;
    }

    /**
     * Extracts target errors from REST request validation errors.
     *
     * @param ex the REST method argument validation exception
     * @return the extracted target errors
     */
    public static ArrayList<TargetError> extractFieldErrors(
            org.springframework.web.bind.MethodArgumentNotValidException ex
    ) {
        return extractErrors(ex.getBindingResult());
    }

    /**
     * Extracts target errors from WebSocket message validation errors.
     *
     * @param ex the WebSocket method argument validation exception
     * @return the extracted target errors, or an empty list when no binding result is available
     */
    public static ArrayList<TargetError> extractFieldErrors(
            org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException ex
    ) {
        return ex.getBindingResult() != null
                ? extractErrors(ex.getBindingResult())
                : new ArrayList<>();
    }

    /**
     * Extracts target errors from a validation binding result.
     *
     * Field errors use the affected field as their target. Object-level errors use the validation
     * error code as their target.
     *
     * @param bindingResult the validation binding result
     * @return the extracted target errors
     */
    private static ArrayList<TargetError> extractErrors(BindingResult bindingResult) {
        ArrayList<TargetError> errors = new ArrayList<>();

        if (bindingResult != null) {
            for (ObjectError error : bindingResult.getAllErrors()) {
                if (error instanceof FieldError fieldError) {
                    errors.add(new TargetError(fieldError.getField(), error.getDefaultMessage()));
                } else {
                    errors.add(new TargetError(error.getCode(), error.getDefaultMessage()));
                }
            }
        }

        return errors;
    }

    /**
     * Converts target errors to a map of target paths and error messages.
     *
     * Errors without a target are mapped to the {@code body} target.
     *
     * @param targetErrors the target errors to convert
     * @return the mapped target errors, or {@code null} when the supplied errors are {@code null}
     */
    public static Map<String, String> targetErrorsToMap(TargetError... targetErrors) {
        if (targetErrors == null) {
            return null;
        }

        Map<String, String> errors = new HashMap<>();

        for (TargetError targetError : targetErrors) {
            String target = targetError.target() != null
                    ? targetError.target()
                    : "body";

            errors.put(target, targetError.error());
        }

        return errors;
    }
}