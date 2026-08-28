package nl.kmartin.dartsmatcherapi.exceptionhandler;

import jakarta.validation.ConstraintViolationException;
import nl.kmartin.dartsmatcherapi.common.MessageKeys;
import nl.kmartin.dartsmatcherapi.common.MessageResolver;
import nl.kmartin.dartsmatcherapi.exceptionhandler.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapi.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.exceptionhandler.response.ApiErrorCode;
import nl.kmartin.dartsmatcherapi.exceptionhandler.response.ErrorResponse;
import nl.kmartin.dartsmatcherapi.exceptionhandler.response.TargetError;
import nl.kmartin.dartsmatcherapi.utils.ErrorUtil;
import nl.kmartin.dartsmatcherapi.utils.StringUtils;
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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final MessageResolver messageResolver;

    @Autowired
    public GlobalExceptionHandler(MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    /**
     * Handler for all unhandled exceptions.
     *
     * @param e Exception The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> handleRunTimeException(Exception e) {
        return createErrorResponse(
                e,
                ApiErrorCode.INTERNAL,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INTERNAL)
        );
    }

    /**
     * Handler for all method arguments invalid exceptions.
     *
     * @param e MethodArgumentNotValidException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleMethodArgumentsInvalidException(MethodArgumentNotValidException e) {
        ArrayList<TargetError> errors = ErrorUtil.extractFieldErrors(e);

        return createErrorResponse(
                e,
                ApiErrorCode.INVALID_ARGUMENTS,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
                errors.toArray(new TargetError[0])
        );
    }

    /**
     * Handler for bean validation errors in services.
     *
     * @param e ConstraintViolationException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        ArrayList<TargetError> errors = ErrorUtil.extractTargetErrors(e);

        return createErrorResponse(
                e,
                ApiErrorCode.INVALID_ARGUMENTS,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
                errors.toArray(new TargetError[0])
        );
    }

    /**
     * Handler for custom invalid arguments.
     *
     * @param e InvalidArgumentsException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
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
     * Handler for missing request parameters.
     *
     * @param e MissingServletRequestParameterException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return createErrorResponse(
                e,
                ApiErrorCode.INVALID_ARGUMENTS,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
                new TargetError(e.getParameterName(), messageResolver.getMessage(MessageKeys.VALIDATION_NOT_NULL))
        );
    }

    /**
     * Handler for accessing url that don't support the Http media type (e.g. using form url encoded where only application/json is supported).
     *
     * @param e HttpMediaTypeException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
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
     * Handler for accessing url that don't support the Http method (e.g. using HTTP POST where only HTTP GET is supported).
     *
     * @param e HttpRequestMethodNotSupportedException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return createErrorResponse(
                e,
                ApiErrorCode.METHOD_NOT_ALLOWED,
                e.getMessage()
        );
    }

    /**
     * Handler for accessing url that doesn't exist
     *
     * @param e Exception The exception that was thrown: NoHandlerFoundException or NoResourceFoundException
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleNoMappingFoundException(Exception e) {
        // Get request URL from exception
        String requestUrl = (e instanceof NoHandlerFoundException ex) ? ex.getRequestURL()
                : (e instanceof NoResourceFoundException ex) ? ex.getResourcePath()
                : "";

        // noHandler will prefix with forward slash, check for consistency.
        if (!requestUrl.startsWith("/")) requestUrl = "/" + requestUrl;

        return createErrorResponse(
                e,
                ApiErrorCode.URI_NOT_FOUND,
                messageResolver.getMessage(MessageKeys.EXCEPTION_URI_NOT_FOUND, requestUrl)
        );
    }

    /**
     * Handler for accessing url that doesn't exist
     *
     * @param e ResourceNotFoundException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e) {
        String resourceSimpleName = e.getResourceClass().getSimpleName();

        String userResourceType = messageResolver.getMessage(MessageKeys.forResourceType(e.getResourceClass()));
        String userMessage = messageResolver.getMessage(MessageKeys.MESSAGE_RESOURCE_NOT_FOUND, userResourceType);

        return createErrorResponse(
                e,
                ApiErrorCode.RESOURCE_NOT_FOUND,
                messageResolver.getMessage(
                        MessageKeys.EXCEPTION_RESOURCE_NOT_FOUND,
                        resourceSimpleName,
                        e.getIdentifier()
                ),
                new TargetError(
                        StringUtils.pascalToCamelCase(resourceSimpleName),
                        userMessage
                )
        );
    }

    /**
     * Handler for sending malformed data or invalid data types (e.g. invalid json, using array instead of string).
     *
     * @param e HttpMessageNotReadableException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, ConversionFailedException.class})
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(Exception e) {
        return createErrorResponse(
                e,
                ApiErrorCode.MESSAGE_NOT_READABLE,
                messageResolver.getMessage(MessageKeys.EXCEPTION_BODY_NOT_READABLE)
        );
    }

    /**
     * Handler for when the database is down.
     *
     * @param e DataAccessResourceFailureException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({DataAccessResourceFailureException.class})
    public ResponseEntity<ErrorResponse> handleDataAccessResourceFailureException(DataAccessResourceFailureException e) {
        return createErrorResponse(
                e,
                ApiErrorCode.UNAVAILABLE,
                messageResolver.getMessage(MessageKeys.EXCEPTION_SERVICE_UNAVAILABLE)
        );
    }

    /**
     * Handler for optimistic locking conflicts.
     *
     * @param e OptimisticLockingFailureException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({OptimisticLockingFailureException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailureException(OptimisticLockingFailureException e) {
        return createErrorResponse(
                e,
                ApiErrorCode.CONFLICT,
                messageResolver.getMessage(MessageKeys.EXCEPTION_CONFLICT)
        );
    }

    /**
     * Creates an API error response.
     *
     * @param exception    the exception being handled
     * @param apiErrorCode API error code for the response
     * @param description  human-readable error description
     * @return ResponseEntity containing the error response and corresponding HTTP status
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
     * @param apiErrorCode API error code for the response
     * @param description  human-readable error description
     * @param targetErrors target-specific errors
     * @return ResponseEntity containing the error response and corresponding HTTP status
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
