package nl.kmartin.dartsmatcherapi.websocket;

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
 * Intercepts the native WebSocket handshake process before it completes.
 *
 * Copies the correlationId from the initial HTTP
 * session to the attributes of the WebSocket session, enabling
 * consistent logging context across the connection's lifecycle.
 */
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request,
                                   @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler,
                                   @NonNull Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpSession session = servletRequest.getServletRequest().getSession(false);
            if (session != null) {
                Object correlationId = session.getAttribute(MdcKeys.CORRELATION_ID);
                if (correlationId != null) {
                    attributes.put(MdcKeys.CORRELATION_ID, correlationId.toString());
                }
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request,
                               @NonNull ServerHttpResponse response,
                               @NonNull WebSocketHandler wsHandler,
                               Exception exception) {
        // No action needed
    }
}
