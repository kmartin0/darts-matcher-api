package nl.kmartin.dartsmatcherapi.websocket.config;

import nl.kmartin.dartsmatcherapi.logging.MdcChannelInterceptor;
import nl.kmartin.dartsmatcherapi.logging.MdcTaskDecorator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration class for WebSocket STOMP message broker.
 * =
 * Client to server (message) prefix:                       /app
 * Server to client (single response subscription) prefix:  /app
 * Server to client (Broadcast subscription) prefix:        /topic
 * Server to client queue prefix:                           /queue
 * Server to user (Publish response) prefix:                /user
 * WebSocket connection URL:                                ws://localhost:8080/darts-matcher-websocket
 * =
 * Example destinations:
 * STOMP subscribe destination (Single Response):           /app/x01/matches/67dd4a0746cdab5415620e01
 * STOMP subscribe destination (Broadcast Response):        /topic/x01/matches/67dd4a0746cdab5415620e01
 * STOMP subscribe destination (publish responses):         /user/queue/responses
 * STOMP subscribe destination (publish errors):            /user/queue/errors
 * STOMP publish destination:                               /app/x01/matches/67dd4a0746cdab5415620e01/turn/add
 * STOMP message content (add turn):                        {"score": 60, "checkoutDartsUsed": null, "doublesMissed": null}
 * =
 * WebSocket Debug Tool: https://jiangxy.github.io/websocket-debug-tool/
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    public static final String APP_PREFIX = "/app";
    public static final String BROADCAST_PREFIX = "/topic";
    public static final String QUEUE_PREFIX = "/queue";
    public static final String USER_PREFIX = "/user";
    public static final String WEBSOCKET_ENDPOINT = "/darts-matcher-websocket";

    private static final long HEARTBEAT_INTERVAL_MS = 5000;

    private final MdcChannelInterceptor mdcChannelInterceptor;
    private final WebSocketHandshakeInterceptor handshakeInterceptor;
    private final ThreadPoolTaskExecutorBuilder taskExecutorBuilder;
    private final MdcTaskDecorator mdcTaskDecorator;
    private final TaskScheduler webSocketHeartbeatTaskScheduler;

    public WebSocketConfig(
            MdcChannelInterceptor mdcChannelInterceptor,
            WebSocketHandshakeInterceptor handshakeInterceptor,
            ThreadPoolTaskExecutorBuilder taskExecutorBuilder,
            MdcTaskDecorator mdcTaskDecorator,
            @Qualifier("webSocketHeartbeatTaskScheduler") TaskScheduler webSocketHeartbeatTaskScheduler) {
        this.mdcChannelInterceptor = mdcChannelInterceptor;
        this.handshakeInterceptor = handshakeInterceptor;
        this.taskExecutorBuilder = taskExecutorBuilder;
        this.mdcTaskDecorator = mdcTaskDecorator;
        this.webSocketHeartbeatTaskScheduler = webSocketHeartbeatTaskScheduler;
    }

    /**
     * Configures the STOMP application, user, broadcast and queue destination prefixes.
     *
     * @param config the message broker registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes(APP_PREFIX);
        config.setUserDestinationPrefix(USER_PREFIX);
        config.enableSimpleBroker(BROADCAST_PREFIX, QUEUE_PREFIX)
                .setTaskScheduler(webSocketHeartbeatTaskScheduler)
                .setHeartbeatValue(new long[]{HEARTBEAT_INTERVAL_MS, HEARTBEAT_INTERVAL_MS});
    }

    /**
     * Registers the WebSocket STOMP endpoint and handshake interceptor.
     *
     * @param registry the STOMP endpoint registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(WEBSOCKET_ENDPOINT)
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor);
    }

    /**
     * Configures the inbound WebSocket channel with MDC propagation and correlation logging.
     *
     * @param registration the inbound channel registration
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        ThreadPoolTaskExecutor mdcTaskExecutor = taskExecutorBuilder
                .taskDecorator(mdcTaskDecorator)
                .threadNamePrefix("clientInboundChannel-")
                .build();

        registration.interceptors(mdcChannelInterceptor);
        registration.taskExecutor(mdcTaskExecutor);
    }

    /**
     * Configures the outbound WebSocket channel with MDC propagation.
     *
     * @param registration the outbound channel registration
     */
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        ThreadPoolTaskExecutor mdcTaskExecutor = taskExecutorBuilder
                .taskDecorator(mdcTaskDecorator)
                .threadNamePrefix("clientOutboundChannel-")
                .build();

        registration.taskExecutor(mdcTaskExecutor);
    }
}