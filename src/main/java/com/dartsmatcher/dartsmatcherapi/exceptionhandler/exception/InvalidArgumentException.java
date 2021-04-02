package com.dartsmatcher.dartsmatcherapi.exceptionhandler.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class InvalidArgumentException extends RuntimeException {

	// the field that causes the exception.
	private String target;

	// the error message.
	private String error;

}
