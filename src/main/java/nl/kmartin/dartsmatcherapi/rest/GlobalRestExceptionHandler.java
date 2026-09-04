package nl.kmartin.dartsmatcherapi.rest;

import jakarta.validation.ConstraintViolationException;
import nl.kmartin.dartsmatcherapi.error.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.error.response.ApiErrorCode;
import nl.kmartin.dartsmatcherapi.error.response.ErrorResponse;
import nl.kmartin.dartsmatcherapi.error.response.TargetError;
import nl.kmartin.dartsmatcherapi.error.util.TargetErrorUtil;
import nl.kmartin.dartsmatcherapi.i18n.MessageKeys;
import nl.kmartin.dartsmatcherapi.i18n.MessageResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;

/**
 * Handles exceptions raised by REST requests and converts them into consistent API error responses.
 *
 * Maps validation, resource, request, database and unexpected errors to the appropriate
 * API error code, HTTP status and optional target-specific errors.
 *
 * Target errors are exposed only for {@link ApiErrorCode#INVALID_ARGUMENTS} responses
 * and identify client-correctable targets of the request that triggered the error.
 * Internal state, persisted data and service-level validation failures are never
 * exposed as target-specific errors.
 */
@RestControllerAdvice
public class GlobalRestExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalRestExceptionHandler.class);
    private final MessageResolver messageResolver;

    @Autowired
    public GlobalRestExceptionHandler(MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    /**
     * Handles Bean Validation failures on REST request objects.
     *
     * Field and object validation errors are exposed as client-correctable target errors.
     *
     * @param e the method argument validation exception
     * @return the invalid-arguments error response
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleMethodArgumentsInvalidException(
            MethodArgumentNotValidException e
    ) {
        ArrayList<TargetError> errors = TargetErrorUtil.extractFieldErrors(e);

        return createErrorResponse(
                e,
                ApiErrorCode.INVALID_ARGUMENTS,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
                errors.toArray(new TargetError[0])
        );
    }

    /**
     * Handles Bean Validation failures on REST controller method parameters.
     *
     * Argument validation errors are exposed as client-correctable target errors.
     * Return-value validation failures represent an internal API error and are not
     * exposed as target errors.
     *
     * @param e the handler method validation exception
     * @return the corresponding API error response
     */
    @ExceptionHandler({HandlerMethodValidationException.class})
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(
            HandlerMethodValidationException e
    ) {
        // A controller return-value violation represents an API implementation error.
        if (e.isForReturnValue()) {
            return createErrorResponse(
                    e,
                    ApiErrorCode.INTERNAL,
                    messageResolver.getMessage(MessageKeys.EXCEPTION_INTERNAL)
            );
        }

        ArrayList<TargetError> errors = TargetErrorUtil.extractFieldErrors(e);

        return createErrorResponse(
                e,
                ApiErrorCode.INVALID_ARGUMENTS,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
                errors.toArray(new TargetError[0])
        );
    }

    /**
     * Handles missing required REST request parameters.
     *
     * @param e the missing request parameter exception
     * @return the invalid-arguments error response
     */
    @ExceptionHandler({MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e
    ) {
        return createErrorResponse(
                e,
                ApiErrorCode.INVALID_ARGUMENTS,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
                new TargetError(
                        e.getParameterName(),
                        messageResolver.getMessage(MessageKeys.VALIDATION_NOT_NULL)
                )
        );
    }

    /**
     * Handles explicitly reported client-correctable argument errors.
     *
     * @param e the invalid-arguments exception
     * @return the invalid-arguments error response
     */
    @ExceptionHandler({InvalidArgumentsException.class})
    public ResponseEntity<ErrorResponse> handleInvalidArgumentException(InvalidArgumentsException e) {
        return createErrorResponse(
                e,
                ApiErrorCode.INVALID_ARGUMENTS,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
                e.getErrors().toArray(new TargetError[0])
        );
    }

    /**
     * Handles request values that cannot be read or converted to the expected type.
     *
     * These errors indicate that the request does not conform to the API contract and
     * therefore are not exposed as client-correctable target errors.
     *
     * @param e the request value exception
     * @return the message-not-readable error response
     */
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            ConversionFailedException.class
    })
    public ResponseEntity<ErrorResponse> handleRequestValueNotReadableException(Exception e) {
        return createErrorResponse(
                e,
                ApiErrorCode.MESSAGE_NOT_READABLE,
                messageResolver.getMessage(MessageKeys.EXCEPTION_BODY_NOT_READABLE)
        );
    }

    /**
     * Handles requests using an unsupported HTTP media type.
     *
     * @param e the HTTP media type exception
     * @return the unsupported-media-type error response
     */
    @ExceptionHandler({HttpMediaTypeException.class})
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeException(HttpMediaTypeException e) {
        return createErrorResponse(
                e,
                ApiErrorCode.UNSUPPORTED_MEDIA_TYPE,
                e.getMessage()
        );
    }

    /**
     * Handles requests using an unsupported HTTP method.
     *
     * @param e the HTTP request method exception
     * @return the method-not-allowed error response
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e
    ) {
        return createErrorResponse(
                e,
                ApiErrorCode.METHOD_NOT_ALLOWED,
                e.getMessage()
        );
    }

    /**
     * Handles requests for URLs that do not have a matching handler or resource.
     *
     * @param e the no-handler or no-resource exception
     * @return the URI-not-found error response
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleNoMappingFoundException(Exception e) {
        // Resolve the missing request path from the concrete routing exception.
        String requestUrl = e instanceof NoHandlerFoundException ex
                ? ex.getRequestURL()
                : ((NoResourceFoundException) e).getResourcePath();

        // Normalize the path so both exception types produce the same response format.
        if (!requestUrl.startsWith("/")) {
            requestUrl = "/" + requestUrl;
        }

        return createErrorResponse(
                e,
                ApiErrorCode.URI_NOT_FOUND,
                messageResolver.getMessage(MessageKeys.EXCEPTION_URI_NOT_FOUND, requestUrl)
        );
    }

    /**
     * Handles requested domain resources that could not be found.
     *
     * Resource lookup failures are not automatically associated with a request
     * target because the resource type does not necessarily correspond to a
     * client-correctable request field.
     *
     * @param e the resource-not-found exception
     * @return the resource-not-found error response
     */
    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e) {
        String resourceSimpleName = e.getResourceClass().getSimpleName();

        return createErrorResponse(
                e,
                ApiErrorCode.RESOURCE_NOT_FOUND,
                messageResolver.getMessage(
                        MessageKeys.EXCEPTION_RESOURCE_NOT_FOUND,
                        resourceSimpleName,
                        e.getIdentifier()
                )
        );
    }

    /**
     * Handles optimistic locking conflicts while persisting application state.
     *
     * @param e the optimistic locking exception
     * @return the conflict error response
     */
    @ExceptionHandler({OptimisticLockingFailureException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailureException(
            OptimisticLockingFailureException e
    ) {
        return createErrorResponse(
                e,
                ApiErrorCode.CONFLICT,
                messageResolver.getMessage(MessageKeys.EXCEPTION_CONFLICT)
        );
    }

    /**
     * Handles failures to access the persistence layer.
     *
     * @param e the data-access resource failure exception
     * @return the service-unavailable error response
     */
    @ExceptionHandler({DataAccessResourceFailureException.class})
    public ResponseEntity<ErrorResponse> handleDataAccessResourceFailureException(
            DataAccessResourceFailureException e
    ) {
        return createErrorResponse(
                e,
                ApiErrorCode.UNAVAILABLE,
                messageResolver.getMessage(MessageKeys.EXCEPTION_SERVICE_UNAVAILABLE)
        );
    }

    /**
     * Handles unexpected Bean Validation failures raised below the API boundary.
     *
     * Service validation errors can originate from persisted or internal state and
     * therefore are not exposed as client-correctable target errors.
     *
     * @param e the constraint violation exception
     * @return the internal error response
     */
    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException e
    ) {
        return createErrorResponse(
                e,
                ApiErrorCode.INTERNAL,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INTERNAL)
        );
    }

    /**
     * Handles all exceptions that are not handled by a more specific exception handler.
     *
     * @param e the unhandled exception
     * @return the internal error response
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> handleUnhandledException(Exception e) {
        return createErrorResponse(
                e,
                ApiErrorCode.INTERNAL,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INTERNAL)
        );
    }

    /**
     * Creates an API error response.
     *
     * @param exception    the exception being handled
     * @param apiErrorCode the API error code
     * @param description  the human-readable error description
     * @return the error response with the corresponding HTTP status
     */
    private ResponseEntity<ErrorResponse> createErrorResponse(
            Exception exception,
            ApiErrorCode apiErrorCode,
            String description
    ) {
        logger.error(exception.getClass().getSimpleName(), exception);

        ErrorResponse responseBody = new ErrorResponse(apiErrorCode, description);

        return new ResponseEntity<>(
                responseBody,
                apiErrorCode.getHttpStatus()
        );
    }

    /**
     * Creates an API error response with target-specific errors.
     *
     * @param exception    the exception being handled
     * @param apiErrorCode the API error code
     * @param description  the human-readable error description
     * @param targetErrors the target-specific errors
     * @return the error response with the corresponding HTTP status
     */
    private ResponseEntity<ErrorResponse> createErrorResponse(
            Exception exception,
            ApiErrorCode apiErrorCode,
            String description,
            TargetError... targetErrors
    ) {
        logger.error(exception.getClass().getSimpleName(), exception);

        ErrorResponse responseBody = new ErrorResponse(apiErrorCode, description, targetErrors);

        return new ResponseEntity<>(
                responseBody,
                apiErrorCode.getHttpStatus()
        );
    }
}