package org.sbolstandard.core2;

import java.net.URI;

/**
 * Represents the orientation type for extended classes of the Location class.
 * 
 * @author Zhen Zhang
 * @author Chris Myers
 * @version 2.1
 */

public enum OrientationType {
	/**
	 * The region specified by this Location MUST be on the elements of a Sequence.
	 */
	INLINE("inline"), 
	/**
	 * The region specified by this Location MUST be on the reverse-complement translation of the elements of a Sequence. 
	 * The exact nature of this translation depends on the encoding of the Sequence.
	 */
	REVERSECOMPLEMENT("reverseComplement");
	private final String orientationType;

	private OrientationType(String orientationType) {
		this.orientationType = orientationType;
	}

	@Override
	public String toString() {
		return orientationType;
	}

	/**
	 * Convert the specified URI to its corresponding OrientationType instance. 
	 * @return the corresponding OrientationType instance
	 * @throws SBOLValidationException 
	 */
	static OrientationType convertToOrientationType(URI orientation) throws SBOLValidationException {
		if (orientation != null) {
			if (orientation.equals(inline)) {
				return OrientationType.INLINE;
			} 
			else if (orientation.equals(reverseComplement)) {
				return OrientationType.REVERSECOMPLEMENT;
			}
			else {
				throw new SBOLValidationException("sbol-11002");
			}
		} else {
			throw new SBOLValidationException("sbol-11002");
		}
	}

	/**
	 * Returns the orientation type in URI.
	 * @return orientation type in URI
	 */
	static URI convertToURI(OrientationType orientation) {
		if (orientation != null) {
			if (orientation.equals(OrientationType.INLINE)) {
				return inline;
			}
			else if (orientation.equals(OrientationType.REVERSECOMPLEMENT)) {
				return reverseComplement;
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}
	

	private static final URI inline 		  = URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "inline");
	private static final URI reverseComplement = URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "reverseComplement");
}
