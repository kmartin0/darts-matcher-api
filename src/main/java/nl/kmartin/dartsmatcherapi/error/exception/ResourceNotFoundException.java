package nl.kmartin.dartsmatcherapi.error.exception;

import lombok.Getter;

/**
 * Thrown when a requested resource cannot be found.
 *
 * Stores the resource type and lookup identifier so the exception handler can
 * create an appropriate API error response.
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final Class<?> resourceClass;
    private final Object identifier;

    /**
     * Creates a resource-not-found exception for the given resource and lookup identifier.
     *
     * @param resourceClass the type of resource that could not be found
     * @param identifier    the identifier used to locate the resource
     */
    public ResourceNotFoundException(Class<?> resourceClass, Object identifier) {
        super("%s with identifier %s was not found".formatted(resourceClass.getSimpleName(), identifier));
        this.resourceClass = resourceClass;
        this.identifier = identifier;
    }
}