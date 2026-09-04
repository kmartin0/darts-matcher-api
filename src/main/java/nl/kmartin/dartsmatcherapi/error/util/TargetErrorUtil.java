package nl.kmartin.dartsmatcherapi.error.util;

import nl.kmartin.dartsmatcherapi.error.response.ErrorTargets;
import nl.kmartin.dartsmatcherapi.error.response.TargetError;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class TargetErrorUtil {
    private TargetErrorUtil() {
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
     * Extracts target errors from REST controller method validation errors.
     *
     * Nested request-object errors use their affected field as the target.
     * Direct method-parameter errors use the client-facing parameter name.
     * Cross-parameter errors target the request body as a whole.
     *
     * @param ex the handler method validation exception
     * @return the extracted target errors
     */
    public static ArrayList<TargetError> extractFieldErrors(HandlerMethodValidationException ex) {
        ArrayList<TargetError> errors = new ArrayList<>();

        for (ParameterValidationResult result : ex.getParameterValidationResults()) {
            if (result instanceof ParameterErrors parameterErrors) {
                errors.addAll(extractErrors(parameterErrors));
                continue;
            }

            String target = getParameterTarget(result.getMethodParameter());

            for (MessageSourceResolvable error : result.getResolvableErrors()) {
                errors.add(new TargetError(target, error.getDefaultMessage()));
            }
        }

        for (MessageSourceResolvable error : ex.getCrossParameterValidationResults()) {
            errors.add(new TargetError(null, error.getDefaultMessage()));
        }

        return errors;
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
     * Extracts target errors from validation errors.
     *
     * Field errors use the affected field as their target. Object-level errors
     * target the request body as a whole.
     *
     * @param validationErrors the validation errors
     * @return the extracted target errors
     */
    private static ArrayList<TargetError> extractErrors(Errors validationErrors) {
        ArrayList<TargetError> errors = new ArrayList<>();

        for (ObjectError error : validationErrors.getAllErrors()) {
            if (error instanceof FieldError fieldError) {
                errors.add(new TargetError(fieldError.getField(), error.getDefaultMessage()));
            } else {
                errors.add(new TargetError(null, error.getDefaultMessage()));
            }
        }

        return errors;
    }

    /**
     * Resolves the client-facing target name of a controller method parameter.
     *
     * @param parameter the controller method parameter
     * @return the client-facing parameter name
     */
    private static String getParameterTarget(MethodParameter parameter) {
        PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (pathVariable != null && !pathVariable.name().isBlank()) {
            return pathVariable.name();
        }

        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null && !requestParam.name().isBlank()) {
            return requestParam.name();
        }

        return parameter.getParameterName();
    }

    /**
     * Converts target errors to a map of target paths and error messages.
     *
     * Errors without a target are mapped to the {@code root} target.
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
                    : ErrorTargets.ROOT;

            errors.put(target, targetError.error());
        }

        return errors;
    }
}