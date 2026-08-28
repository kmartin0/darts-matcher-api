package nl.kmartin.dartsmatcherapi.error.exception;

import lombok.Getter;
import lombok.Setter;
import nl.kmartin.dartsmatcherapi.error.response.TargetError;

import java.util.ArrayList;
import java.util.Arrays;

@Setter
@Getter
public class InvalidArgumentsException extends RuntimeException {
	// Errors map where the key is the target and the value is the error message.
	private ArrayList<TargetError> errors;

	public InvalidArgumentsException() {
		super("Invalid arguments have been supplied.");
		this.errors = new ArrayList<>();
	}

	public InvalidArgumentsException(ArrayList<TargetError> errors) {
		super("Invalid arguments have been supplied.");
		this.errors = errors;
	}

	public InvalidArgumentsException(TargetError... errors) {
		super("Invalid arguments have been supplied.");
		this.errors = new ArrayList<>(Arrays.asList(errors));
	}
}