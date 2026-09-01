package nl.kmartin.dartsmatcherapi.logging;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Propagates the MDC context to tasks executed on another thread.
 *
 * This preserves logging information such as the correlation ID during
 * asynchronous WebSocket message processing.
 */
@Component
public class MdcTaskDecorator implements TaskDecorator {

    /**
     * Wraps a task so it executes with a copy of the current MDC context.
     *
     * @param runnable the task to execute
     * @return the decorated task
     */
    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return () -> {
            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }

                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}