package nl.kmartin.dartsmatcherapi.logging;

import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Propagates the WebSocket session correlation ID to the MDC.
 *
 * The correlation ID is added before a message is sent through the channel
 * and cleared when channel processing is complete.
 */
@Component
public class MdcChannelInterceptor implements ChannelInterceptor {

    /**
     * Adds the WebSocket session correlation ID to the MDC when available.
     *
     * @param message the WebSocket message
     * @param channel the message channel
     * @return the message to continue processing
     */
    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes != null && sessionAttributes.containsKey(MdcKeys.CORRELATION_ID)) {
            MDC.put(
                    MdcKeys.CORRELATION_ID,
                    (String) sessionAttributes.get(MdcKeys.CORRELATION_ID)
            );
        }

        return message;
    }

    /**
     * Clears the MDC after channel processing completes.
     */
    @Override
    public void afterSendCompletion(@NonNull Message<?> message, @NonNull MessageChannel channel, boolean sent, Exception exception) {
        MDC.clear();
    }
}