package nl.kmartin.dartsmatcherapi.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Adds request-specific logging information to the MDC for incoming HTTP requests.
 *
 * Stores the client IP address and a generated correlation ID for the duration
 * of the request. The correlation ID is also stored in the HTTP session so it
 * can be propagated to WebSocket messages.
 */
@Component
public class MdcFilter extends OncePerRequestFilter {

    /**
     * Adds request logging information to the MDC and clears it after processing.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response
     * @param filterChain the request filter chain
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // Store the originating client IP address for request logging.
        String clientIp = getClientIpAddress(request);
        MDC.put(MdcKeys.CLIENT_IP, clientIp);

        // Generate a correlation ID and make it available to HTTP and WebSocket processing.
        String correlationId = UUID.randomUUID().toString();
        MDC.put(MdcKeys.CORRELATION_ID, correlationId);
        request.getSession().setAttribute(MdcKeys.CORRELATION_ID, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Prevent request logging information from leaking into another request.
            MDC.clear();
        }
    }

    /**
     * Resolves the originating client IP address.
     *
     * Uses the first address in the X-Forwarded-For header when the request passed
     * through a proxy, otherwise the direct connection address is returned.
     *
     * @param request the incoming HTTP request
     * @return the resolved client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");

        // The first X-Forwarded-For entry represents the original client.
        if (xForwardedForHeader != null && !xForwardedForHeader.isBlank()) {
            return xForwardedForHeader.split(",", 2)[0].trim();
        }

        // When no proxy header is present, use the direct connection address.
        return request.getRemoteAddr();
    }
}