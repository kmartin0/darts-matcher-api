package nl.kmartin.dartsmatcherapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configures scheduled task execution for the application.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

    /**
     * Creates the scheduler used for application scheduled tasks.
     *
     * @return application task scheduler
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("scheduledTask-");
        scheduler.initialize();
        return scheduler;
    }

    /**
     * Creates the scheduler used by the simple message broker for STOMP heartbeats.
     *
     * @return heartbeat task scheduler
     */
    @Bean
    public TaskScheduler webSocketHeartbeatTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("webSocketHeartbeat-");
        scheduler.initialize();
        return scheduler;
    }
}