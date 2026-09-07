package nl.kmartin.dartsmatcherapi.websocket.error;

import jakarta.validation.ConstraintViolationException;
import nl.kmartin.dartsmatcherapi.error.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.error.response.ApiErrorCode;
import nl.kmartin.dartsmatcherapi.error.response.TargetError;
import nl.kmartin.dartsmatcherapi.error.util.TargetErrorUtil;
import nl.kmartin.dartsmatcherapi.i18n.MessageKeys;
import nl.kmartin.dartsmatcherapi.i18n.MessageResolver;
import nl.kmartin.dartsmatcherapi.websocket.destination.WebSocketHeaders;
import nl.kmartin.dartsmatcherapi.websocket.event.publisher.IWebSocketEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;

/**
 * Handles exceptions raised during WebSocket message processing and converts them into consistent error responses.
 *
 * Maps validation, resource, message, database and unexpected errors to the appropriate
 * API error code and optional target-specific errors.
 *
 * Target errors are exposed only for {@link ApiErrorCode#INVALID_ARGUMENTS} responses
 * and identify client-correctable targets of the message that triggered the error.
 * Internal state, persisted data and service-level validation failures are never
 * exposed as target-specific errors.
 */
@ControllerAdvice
public class GlobalWebSocketExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalWebSocketExceptionHandler.class);

    private final MessageResolver messageResolver;
    private final IWebSocketEventPublisher webSocketEventPublisher;

    public GlobalWebSocketExceptionHandler(
            MessageResolver messageResolver,
            IWebSocketEventPublisher webSocketEventPublisher
    ) {
        this.messageResolver = messageResolver;
        this.webSocketEventPublisher = webSocketEventPublisher;
    }

    /**
     * Handles Bean Validation failures on WebSocket message payloads.
     *
     * Field and object validation errors are exposed as client-correctable target errors.
     *
     * @param e                   the method argument validation exception
     * @param stompHeaderAccessor accessor for the incoming STOMP message
     */
    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    public void handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            StompHeaderAccessor stompHeaderAccessor
    ) {
        ArrayList<TargetError> errors = TargetErrorUtil.extractFieldErrors(e);

        publishError(
                e,
                ApiErrorCode.INVALID_ARGUMENTS,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
                stompHeaderAccessor,
                errors.toArray(new TargetError[0])
        );
    }

    /**
     * Handles explicitly reported client-correctable argument errors.
     *
     * @param e                   the invalid-arguments exception
     * @param stompHeaderAccessor accessor for the incoming STOMP message
     */
    @MessageExceptionHandler(InvalidArgumentsException.class)
    public void handleInvalidArgumentException(
            InvalidArgumentsException e,
            StompHeaderAccessor stompHeaderAccessor
    ) {
        publishError(
                e,
                ApiErrorCode.INVALID_ARGUMENTS,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
                stompHeaderAccessor,
                e.getErrors().toArray(new TargetError[0])
        );
    }

    /**
     * Handles message values that cannot be read or converted to the expected type.
     *
     * These errors indicate that the message does not conform to the API contract and
     * therefore are not exposed as client-correctable target errors.
     *
     * @param e                   the message value exception
     * @param stompHeaderAccessor accessor for the incoming STOMP message
     */
    @MessageExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MessageConversionException.class,
            ConversionFailedException.class
    })
    public void handleMessageValueNotReadableException(
            Exception e,
            StompHeaderAccessor stompHeaderAccessor
    ) {
        publishError(
                e,
                ApiErrorCode.MESSAGE_NOT_READABLE,
                messageResolver.getMessage(MessageKeys.EXCEPTION_BODY_NOT_READABLE),
                stompHeaderAccessor
        );
    }

    /**
     * Handles message processing failures such as missing required STOMP headers.
     *
     * @param e                   the message handling exception
     * @param stompHeaderAccessor accessor for the incoming STOMP message
     */
    @MessageExceptionHandler(MessageHandlingException.class)
    public void handleMessageHandlingException(
            MessageHandlingException e,
            StompHeaderAccessor stompHeaderAccessor
    ) {
        publishError(
                e,
                ApiErrorCode.MESSAGE_NOT_READABLE,
                messageResolver.getMessage(MessageKeys.EXCEPTION_BODY_NOT_READABLE),
                stompHeaderAccessor
        );
    }

    /**
     * Handles requested domain resources that could not be found.
     *
     * Resource lookup failures are not automatically associated with a message
     * target because the resource type does not necessarily correspond to a
     * client-correctable message field.
     *
     * @param e                   the resource-not-found exception
     * @param stompHeaderAccessor accessor for the incoming STOMP message
     */
    @MessageExceptionHandler(ResourceNotFoundException.class)
    public void handleResourceNotFoundException(
            ResourceNotFoundException e,
            StompHeaderAccessor stompHeaderAccessor
    ) {
        String resourceSimpleName = e.getResourceClass().getSimpleName();

        publishError(
                e,
                ApiErrorCode.RESOURCE_NOT_FOUND,
                messageResolver.getMessage(
                        MessageKeys.EXCEPTION_RESOURCE_NOT_FOUND,
                        resourceSimpleName,
                        e.getIdentifier()
                ),
                stompHeaderAccessor
        );
    }

    /**
     * Handles optimistic locking conflicts while persisting application state.
     *
     * @param e                   the optimistic locking exception
     * @param stompHeaderAccessor accessor for the incoming STOMP message
     */
    @MessageExceptionHandler(OptimisticLockingFailureException.class)
    public void handleOptimisticLockingFailureException(
            OptimisticLockingFailureException e,
            StompHeaderAccessor stompHeaderAccessor
    ) {
        publishError(
                e,
                ApiErrorCode.CONFLICT,
                messageResolver.getMessage(MessageKeys.EXCEPTION_CONFLICT),
                stompHeaderAccessor
        );
    }

    /**
     * Handles failures to access the persistence layer.
     *
     * @param e                   the data-access resource failure exception
     * @param stompHeaderAccessor accessor for the incoming STOMP message
     */
    @MessageExceptionHandler(DataAccessResourceFailureException.class)
    public void handleDataAccessResourceFailureException(
            DataAccessResourceFailureException e,
            StompHeaderAccessor stompHeaderAccessor
    ) {
        publishError(
                e,
                ApiErrorCode.UNAVAILABLE,
                messageResolver.getMessage(MessageKeys.EXCEPTION_SERVICE_UNAVAILABLE),
                stompHeaderAccessor
        );
    }

    /**
     * Handles unexpected Bean Validation failures raised below the WebSocket API boundary.
     *
     * Service validation errors can originate from persisted or internal state and
     * therefore are not exposed as client-correctable target errors.
     *
     * @param e                   the constraint violation exception
     * @param stompHeaderAccessor accessor for the incoming STOMP message
     */
    @MessageExceptionHandler(ConstraintViolationException.class)
    public void handleConstraintViolationException(
            ConstraintViolationException e,
            StompHeaderAccessor stompHeaderAccessor
    ) {
        publishError(
                e,
                ApiErrorCode.INTERNAL,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INTERNAL),
                stompHeaderAccessor
        );
    }

    /**
     * Handles all exceptions that are not handled by a more specific exception handler.
     *
     * @param e                   the unhandled exception
     * @param stompHeaderAccessor accessor for the incoming STOMP message
     */
    @MessageExceptionHandler(Exception.class)
    public void handleUnhandledException(
            Exception e,
            StompHeaderAccessor stompHeaderAccessor
    ) {
        publishError(
                e,
                ApiErrorCode.INTERNAL,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INTERNAL),
                stompHeaderAccessor
        );
    }

    /**
     * Logs an exception and publishes a WebSocket error event for the originating session.
     *
     * @param exception           the exception that occurred
     * @param apiErrorCode        the API error code
     * @param description         the human-readable error description
     * @param stompHeaderAccessor accessor for the incoming STOMP message
     * @param targetErrors        the target-specific errors
     */
    private void publishError(
            Exception exception,
            ApiErrorCode apiErrorCode,
            String description,
            StompHeaderAccessor stompHeaderAccessor,
            TargetError... targetErrors
    ) {
        logger.error(exception.getClass().getSimpleName(), exception);

        webSocketEventPublisher.sendError(
                stompHeaderAccessor.getDestination(),
                apiErrorCode,
                description,
                targetErrors,
                stompHeaderAccessor.getSessionId(),
                stompHeaderAccessor.getFirstNativeHeader(WebSocketHeaders.PUBLISH_ID)
        );
    }
}