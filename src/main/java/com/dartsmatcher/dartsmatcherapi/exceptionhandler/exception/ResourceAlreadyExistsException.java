package com.dartsmatcher.dartsmatcherapi.exceptionhandler.exception;

import lombok.Getter;

@Getter
public class ResourceAlreadyExistsException extends RuntimeException {

	// the type of the requested resource.
	private String resourceType;

	// the field that causes the exception.
	private String target;

	// the value that caused the exception.
	private Object value;

	public ResourceAlreadyExistsException(Class<?> resourceType, String target, Object value) {
		this.resourceType = resourceType.getSimpleName();
		this.target = target;
		this.value = value;
	}

}
