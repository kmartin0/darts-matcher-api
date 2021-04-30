package com.dartsmatcher.dartsmatcherapi.exceptionhandler.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
public class TargetError {
	// Key of the field.
	private final String target;

	// Error description describing the error to an end user.
	private final String error;
}
