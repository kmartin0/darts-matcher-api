package nl.kmartin.dartsmatcherapi.websocket;

import jakarta.validation.ConstraintViolationException;
import nl.kmartin.dartsmatcherapi.error.util.ErrorUtil;
import nl.kmartin.dartsmatcherapi.error.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.error.response.ApiErrorCode;
import nl.kmartin.dartsmatcherapi.error.response.TargetError;
import nl.kmartin.dartsmatcherapi.i18n.MessageKeys;
import nl.kmartin.dartsmatcherapi.i18n.MessageResolver;
import nl.kmartin.dartsmatcherapi.utils.StringUtils;
import nl.kmartin.dartsmatcherapi.websocket.event.IWebSocketEventPublisher;
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

    // Handler for all unhandled exceptions.
    @MessageExceptionHandler(Exception.class)
    public void handleRunTimeException(
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

    // Handler for bean validation errors thrown in controllers.
    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    public void handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            StompHeaderAccessor stompHeaderAccessor
    ) {
        ArrayList<TargetError> errors = ErrorUtil.extractFieldErrors(e);

        publishError(
                e,
                ApiErrorCode.INVALID_ARGUMENTS,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
                stompHeaderAccessor,
                errors.toArray(new TargetError[0])
        );
    }

    // Handler for bean validation errors in services.
    @MessageExceptionHandler(ConstraintViolationException.class)
    public void handleConstraintViolationException(
            ConstraintViolationException e,
            StompHeaderAccessor stompHeaderAccessor
    ) {
        ArrayList<TargetError> errors = ErrorUtil.extractTargetErrors(e);

        publishError(
                e,
                ApiErrorCode.INVALID_ARGUMENTS,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
                stompHeaderAccessor,
                errors.toArray(new TargetError[0])
        );
    }

    // Handler for custom invalid arguments.
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

    // Handler for resources that are not found.
    @MessageExceptionHandler(ResourceNotFoundException.class)
    public void handleResourceNotFoundException(
            ResourceNotFoundException e,
            StompHeaderAccessor stompHeaderAccessor
    ) {
        String resourceSimpleName = e.getResourceClass().getSimpleName();
        String userResourceType = messageResolver.getMessage(
                MessageKeys.forResourceType(e.getResourceClass())
        );
        String userMessage = messageResolver.getMessage(
                MessageKeys.MESSAGE_RESOURCE_NOT_FOUND,
                userResourceType
        );

        publishError(
                e,
                ApiErrorCode.RESOURCE_NOT_FOUND,
                messageResolver.getMessage(
                        MessageKeys.EXCEPTION_RESOURCE_NOT_FOUND,
                        resourceSimpleName,
                        e.getIdentifier()
                ),
                stompHeaderAccessor,
                new TargetError(
                        StringUtils.pascalToCamelCase(resourceSimpleName),
                        userMessage
                )
        );
    }

    // Handler for sending malformed data or invalid data types.
    @MessageExceptionHandler(MethodArgumentTypeMismatchException.class)
    public void handleHttpMessageNotReadableException(
            MethodArgumentTypeMismatchException e,
            StompHeaderAccessor stompHeaderAccessor
    ) {
        publishError(
                e,
                ApiErrorCode.MESSAGE_NOT_READABLE,
                messageResolver.getMessage(MessageKeys.EXCEPTION_BODY_NOT_READABLE),
                stompHeaderAccessor
        );
    }

    // Handler for messages that cannot be deserialized to the corresponding object.
    @MessageExceptionHandler({MessageConversionException.class, ConversionFailedException.class})
    public void handleMessageConversionException(
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

    // Handler for message handling errors, such as missing required STOMP headers.
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

    // Handler for when the database is down.
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

    // Handler for optimistic locking conflicts.
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
     * Logs an exception and publishes a WebSocket error event for the originating session.
     *
     * @param exception           the exception that occurred
     * @param apiErrorCode        the API error code
     * @param description         the error description
     * @param stompHeaderAccessor accessor for the incoming STOMP message
     * @param targetErrors        target-specific errors
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