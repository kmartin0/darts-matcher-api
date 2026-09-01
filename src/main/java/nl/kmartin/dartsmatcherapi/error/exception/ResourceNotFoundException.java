package nl.kmartin.dartsmatcherapi.error.exception;

import lombok.Getter;

/**
 * Thrown when a requested resource cannot be found.
 *
 * Stores the resource type and identifier so the exception handler can create
 * an appropriate API error response.
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {
    // the type of the requested resource.
    private final Class<?> resourceClass;

    // The id of the requested resource.
    private final Object identifier;

    /**
     * Creates a resource not found exception for the given resource and identifier.
     *
     * @param resourceClass the type of resource that could not be found
     * @param identifier the identifier used to find the resource
     */
    public ResourceNotFoundException(Class<?> resourceClass, Object identifier) {
        super("%s with id %s was not found".formatted(resourceClass.getSimpleName(), identifier));
        this.resourceClass = resourceClass;
        this.identifier = identifier;
    }
}