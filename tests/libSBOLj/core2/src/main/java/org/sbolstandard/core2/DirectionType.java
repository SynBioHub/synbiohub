package org.sbolstandard.core2;

import java.net.URI;

/**
 * Represents all direction types for a {@link FunctionalComponent} object.
 * 
 * @author Zhen Zhang
 * @author Chris Myers
 * @version 2.1
 */

public enum DirectionType {
	/**
	 * The "in" direction type indicates a FunctionalComponent can be used as input.
	 */
	IN("in"), 
	/**
	 * The "out" direction type indicates a FunctionalComponent can be used as output.
	 */
	OUT("out"), 
	/**
	 * The "inout" direction type indicates a FunctionalComponent can be used as both input or output.
	 */
	INOUT("inout"), 
	/**
	 * The "none" direction type indicates a FunctionalComponent is neither input nor output.
	 */
	NONE("none");
	private final String directionType;

	DirectionType(String directionType) {
		this.directionType = directionType;
	}

	@Override
	public String toString() {
		return directionType;
	}

	/**
	 * Convert the specified URI to its corresponding DirectionType instance.
	 * @param direction
	 * @return the corresponding DirectionType instance
	 * @throws SBOLValidationException if either of the following SBOL validation rule was violated: 11802
	 */
	static DirectionType convertToDirectionType(URI direction) throws SBOLValidationException {
		if (direction != null) {
			if (direction.equals(inout)) {
				return DirectionType.INOUT;
			} else if (direction.equals(in)) {
				return DirectionType.IN;
			} else if (direction.equals(none)) {
				return DirectionType.NONE;
			} else if (direction.equals(out)) {
				return DirectionType.OUT;
			} else {
				throw new SBOLValidationException("sbol-11802");
			}
		} else {
			throw new SBOLValidationException("sbol-11802");
		}
	}
	
	/**
	 * Returns the direction type in URI.
	 * @return direction type in URI
	 */
	static URI convertToURI(DirectionType direction) {
		if (direction != null) {
			if (direction.equals(DirectionType.INOUT)) {
				return inout;
			}
			else if (direction.equals(DirectionType.IN)) {
				return in;
			}
			else if (direction.equals(DirectionType.OUT)) {
				return out;
			}
			else if (direction.equals(DirectionType.NONE)) {
				return none;
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}

	private static final URI	in		= URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "in");
	private static final URI	out		= URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "out");
	private static final URI	inout	= URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "inout");
	private static final URI	none	= URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "none");

}