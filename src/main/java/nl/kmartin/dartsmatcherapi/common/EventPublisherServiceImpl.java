package nl.kmartin.dartsmatcherapi.common;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class EventPublisherServiceImpl implements IEventPublisherService {
    private final ApplicationEventPublisher eventPublisher;

    public EventPublisherServiceImpl(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publishes an application event through Spring's application event publisher.
     *
     * @param event The event to publish.
     */
    @Override
    public void publish(Object event) {
        eventPublisher.publishEvent(event);
    }
}
