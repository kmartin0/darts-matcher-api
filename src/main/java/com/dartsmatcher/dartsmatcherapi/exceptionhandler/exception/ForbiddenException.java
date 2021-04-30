package com.dartsmatcher.dartsmatcherapi.exceptionhandler.exception;

import lombok.Getter;

@Getter
public class ForbiddenException extends RuntimeException {

	// Description
	private final String description;


	public ForbiddenException(String description) {
		this.description = description;
	}

}
