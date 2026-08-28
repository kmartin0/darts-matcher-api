package nl.kmartin.dartsmatcherapi.exceptionhandler;

import jakarta.validation.ConstraintViolationException;
import nl.kmartin.dartsmatcherapi.common.*;
import nl.kmartin.dartsmatcherapi.exceptionhandler.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapi.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.exceptionhandler.response.ApiErrorCode;
import nl.kmartin.dartsmatcherapi.exceptionhandler.response.TargetError;
import nl.kmartin.dartsmatcherapi.exceptionhandler.response.WebSocketErrorResponse;
import nl.kmartin.dartsmatcherapi.utils.ErrorUtil;
import nl.kmartin.dartsmatcherapi.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@ControllerAdvice
public class GlobalWebsocketExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalWebsocketExceptionHandler.class);

    private final MessageResolver messageResolver;
    private final IEventPublisherService eventPublisherService;

    @Autowired
    public GlobalWebsocketExceptionHandler(
            MessageResolver messageResolver,
            IEventPublisherService eventPublisherService) {
        this.messageResolver = messageResolver;
        this.eventPublisherService = eventPublisherService;
    }

    // Handler for all unhandled exceptions.
    @MessageExceptionHandler(Exception.class)
    public void handleRunTimeException(Exception e, StompHeaderAccessor stompHeaderAccessor) {
        publishError(
                e,
                new WebSocketErrorResponse(
                        ApiErrorCode.INTERNAL,
                        messageResolver.getMessage(MessageKeys.EXCEPTION_INTERNAL),
                        stompHeaderAccessor.getDestination()
                ),
                stompHeaderAccessor
        );
    }

    // Handler for bean validation errors thrown in controllers.
    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    public void handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            StompHeaderAccessor stompHeaderAccessor) {

        ArrayList<TargetError> errors = ErrorUtil.extractFieldErrors(e);

        publishError(
                e,
                new WebSocketErrorResponse(
                        ApiErrorCode.INVALID_ARGUMENTS,
                        messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
                        stompHeaderAccessor.getDestination(),
                        errors.toArray(new TargetError[0])
                ),
                stompHeaderAccessor
        );
    }

    // Handler for bean validation errors in services.
    @MessageExceptionHandler(ConstraintViolationException.class)
    public void handleConstraintViolationException(
            ConstraintViolationException e,
            StompHeaderAccessor stompHeaderAccessor) {

        ArrayList<TargetError> errors = ErrorUtil.extractTargetErrors(e);

        publishError(
                e,
                new WebSocketErrorResponse(
                        ApiErrorCode.INVALID_ARGUMENTS,
                        messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
                        stompHeaderAccessor.getDestination(),
                        errors.toArray(new TargetError[0])
                ),
                stompHeaderAccessor
        );
    }

    // Handler for custom invalid arguments.
    @MessageExceptionHandler(InvalidArgumentsException.class)
    public void handleInvalidArgumentException(
            InvalidArgumentsException e,
            StompHeaderAccessor stompHeaderAccessor) {

        publishError(
                e,
                new WebSocketErrorResponse(
                        ApiErrorCode.INVALID_ARGUMENTS,
                        messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
                        stompHeaderAccessor.getDestination(),
                        e.getErrors().toArray(new TargetError[0])
                ),
                stompHeaderAccessor
        );
    }

    // Handler for resources that are not found.
    @MessageExceptionHandler(ResourceNotFoundException.class)
    public void handleResourceNotFoundException(
            ResourceNotFoundException e,
            StompHeaderAccessor stompHeaderAccessor) {

        String resourceSimpleName = e.getResourceClass().getSimpleName();
        String userResourceType = messageResolver.getMessage(MessageKeys.forResourceType(e.getResourceClass()));
        String userMessage = messageResolver.getMessage(MessageKeys.MESSAGE_RESOURCE_NOT_FOUND, userResourceType);

        publishError(
                e,
                new WebSocketErrorResponse(
                        ApiErrorCode.RESOURCE_NOT_FOUND,
                        messageResolver.getMessage(
                                MessageKeys.EXCEPTION_RESOURCE_NOT_FOUND,
                                resourceSimpleName,
                                e.getIdentifier()
                        ),
                        stompHeaderAccessor.getDestination(),
                        new TargetError(StringUtils.pascalToCamelCase(resourceSimpleName), userMessage)
                ),
                stompHeaderAccessor
        );
    }

    // Handler for sending malformed data or invalid data types (e.g. invalid json, using array instead of string).
    @MessageExceptionHandler(MethodArgumentTypeMismatchException.class)
    public void handleHttpMessageNotReadableException(
            MethodArgumentTypeMismatchException e,
            StompHeaderAccessor stompHeaderAccessor) {

        publishError(
                e,
                new WebSocketErrorResponse(
                        ApiErrorCode.MESSAGE_NOT_READABLE,
                        messageResolver.getMessage(MessageKeys.EXCEPTION_BODY_NOT_READABLE),
                        stompHeaderAccessor.getDestination()
                ),
                stompHeaderAccessor
        );
    }

    // Handler for when a Message can't be deserialized to the corresponding object (e.g. object requires int but gets an array).
    @MessageExceptionHandler({MessageConversionException.class, ConversionFailedException.class})
    public void handleMessageConversionException(
            Exception e,
            StompHeaderAccessor stompHeaderAccessor) {

        publishError(
                e,
                new WebSocketErrorResponse(
                        ApiErrorCode.MESSAGE_NOT_READABLE,
                        messageResolver.getMessage(MessageKeys.EXCEPTION_BODY_NOT_READABLE),
                        stompHeaderAccessor.getDestination()
                ),
                stompHeaderAccessor
        );
    }

    // Handler for message handling errors, such as missing required STOMP headers.
    @MessageExceptionHandler(MessageHandlingException.class)
    public void handleMessageHandlingException(
            MessageHandlingException e,
            StompHeaderAccessor stompHeaderAccessor) {

        publishError(
                e,
                new WebSocketErrorResponse(
                        ApiErrorCode.MESSAGE_NOT_READABLE,
                        messageResolver.getMessage(MessageKeys.EXCEPTION_BODY_NOT_READABLE),
                        stompHeaderAccessor.getDestination()
                ),
                stompHeaderAccessor
        );
    }

    // Handler for when the database is down.
    @MessageExceptionHandler(DataAccessResourceFailureException.class)
    public void handleDataAccessResourceFailureException(
            DataAccessResourceFailureException e,
            StompHeaderAccessor stompHeaderAccessor) {

        publishError(
                e,
                new WebSocketErrorResponse(
                        ApiErrorCode.UNAVAILABLE,
                        messageResolver.getMessage(MessageKeys.EXCEPTION_SERVICE_UNAVAILABLE),
                        stompHeaderAccessor.getDestination()
                ),
                stompHeaderAccessor
        );
    }

    // Handler for optimistic locking conflicts.
    @MessageExceptionHandler(OptimisticLockingFailureException.class)
    public void handleOptimisticLockingFailureException(
            OptimisticLockingFailureException e,
            StompHeaderAccessor stompHeaderAccessor) {

        publishError(
                e,
                new WebSocketErrorResponse(
                        ApiErrorCode.CONFLICT,
                        messageResolver.getMessage(MessageKeys.EXCEPTION_CONFLICT),
                        stompHeaderAccessor.getDestination()
                ),
                stompHeaderAccessor
        );
    }

    /**
     * Logs an exception and publishes its WebSocket error response for the originating session.
     *
     * @param exception - Exception that occurred.
     * @param errorResponse - Error response to send.
     * @param stompHeaderAccessor - Accessor for the incoming STOMP message.
     */
    private void publishError(
            Exception exception,
            WebSocketErrorResponse errorResponse,
            StompHeaderAccessor stompHeaderAccessor) {

        logger.error(exception.getClass().getSimpleName(), exception);

        eventPublisherService.publish(
                new WebSocketExceptionEvent(
                        errorResponse,
                        stompHeaderAccessor.getSessionId(),
                        stompHeaderAccessor.getFirstNativeHeader(Constants.PUBLISH_ID_HEADER)
                )
        );
    }
}