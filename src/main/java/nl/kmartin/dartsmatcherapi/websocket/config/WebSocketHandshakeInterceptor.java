package nl.kmartin.dartsmatcherapi.websocket.config;

import jakarta.servlet.http.HttpSession;
import nl.kmartin.dartsmatcherapi.logging.MdcKeys;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Propagates the HTTP session correlation ID to the WebSocket session during the handshake.
 *
 * This allows the same correlation ID to be used for logging throughout subsequent
 * WebSocket message processing.
 */
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    /**
     * Copies the correlation ID from the existing HTTP session to the WebSocket session attributes.
     *
     * @param request    the handshake request
     * @param response   the handshake response
     * @param wsHandler  the target WebSocket handler
     * @param attributes the WebSocket session attributes
     * @return true to continue the handshake
     */
    @Override
    public boolean beforeHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes
    ) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return true;
        }

        HttpSession session = servletRequest.getServletRequest().getSession(false);
        if (session == null) {
            return true;
        }

        Object correlationId = session.getAttribute(MdcKeys.CORRELATION_ID);
        if (correlationId != null) {
            attributes.put(MdcKeys.CORRELATION_ID, correlationId.toString());
        }

        return true;
    }

    /**
     * Performs no additional processing after the WebSocket handshake completes.
     */
    @Override
    public void afterHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            Exception exception
    ) {
    }
}