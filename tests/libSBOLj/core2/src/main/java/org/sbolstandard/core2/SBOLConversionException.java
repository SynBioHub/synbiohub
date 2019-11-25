package org.sbolstandard.core2;

/**
 * Signals that an exception related to conversion between SBOL and other file formats has occurred.
 * 
 * @author Chris Myers
 * @author Zhen Zhang
 * @version 2.1
 */

public class SBOLConversionException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception instance with the given message and objects causing the problem.
	 * @param message
	 * @param objects
	 */
	SBOLConversionException(String message) {
		super(message);
	}

	/**
	 * Creates a new exception instance with the given cause but no specific objects for the problem.
	 * 
	 * @param cause
	 */	
	SBOLConversionException(Throwable cause) {
		super(cause);
	}

}

