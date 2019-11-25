package org.oboparser.obo.annotations.parsers;

public class OBOTermValueException extends RuntimeException {

	public OBOTermValueException(String message) {
		super(message);
	}

	public OBOTermValueException(Throwable cause) {
		super(cause);
	}
}
