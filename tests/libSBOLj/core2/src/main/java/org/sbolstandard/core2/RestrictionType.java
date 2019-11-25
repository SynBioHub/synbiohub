package org.sbolstandard.core2;

import java.net.URI;

/**
 * Represents the relationship between a sequence constraint's subject and object components.
 * 
 * @author Zhen Zhang
 * @author Chris Myers
 * @version 2.1
 */

public enum RestrictionType {
	
	/**
	 * The position of the subject Component MUST precede that of the object Component. 
	 * If each one is associated with a SequenceAnnotation, then the SequenceAnnotation 
	 * associated with the subject Component MUST specify a region that starts before 
	 * the region specified by the SequenceAnnotation associated with the object Component.
	 */
	PRECEDES("precedes"),
	/**
	 * The subject and object Component objects MUST have the same orientation. 
	 * If each one is associated with a SequenceAnnotation, then the orientation URIs
	 * of the Location objects of the first SequenceAnnotation MUST be represented
	 * among those of the second SequenceAnnotation, and vice versa.  
	 */
	SAME_ORIENTATION_AS("sameOrienationAs"),
	/**
	 * The subject and object Component objects MUST have opposite orientations.
	 * If each one is associated with a SequenceAnnotation, then the orientation URIs
	 * of the Location objects of one SequenceAnnotation MUST NOT be represented
	 * among those of the other SequenceAnnotation.  
	 */
	OPPOSITE_ORIENTATION_AS("oppositeOrienationAs"),
	/**
	 * The definition property of the subject Component MUST NOT refer to the same 
	 * ComponentDefinition as that of the object Component.
	 */
	DIFFERENT_FROM("differentFrom");
	
	private final String restrictionType;

	RestrictionType(String restrictionType) {
		this.restrictionType = restrictionType;
	}

	@Override
	public String toString() {
		return restrictionType;
	}
	
			
	/**
	 * Convert the specified URI to its corresponding RestrictionType instance.
	 * @return the corresponding RestrictionType instance.
	 * @throws SBOLValidationException 
	 */
	static RestrictionType convertToRestrictionType(URI restriction) throws SBOLValidationException {
		if (restriction!=null) {
			if (restriction.equals(precedes)) {
				return RestrictionType.PRECEDES;
			} else if (restriction.equals(sameOrientationAs)) {
				return RestrictionType.SAME_ORIENTATION_AS;
			} else if (restriction.equals(oppositeOrientationAs)) {
				return RestrictionType.OPPOSITE_ORIENTATION_AS;
			} else if (restriction.equals(differentFrom)) {
				return RestrictionType.DIFFERENT_FROM;
			} 
			else {
				throw new SBOLValidationException("sbol-11412");
			}
		} else {
			throw new SBOLValidationException("sbol-11412");
		}
	}
	
	/**
	 * Returns the restriction type in URI.
	 * @return restriction type in URI
	 */
	static URI convertToURI(RestrictionType restriction) {
		if (restriction != null) {
			if (restriction.equals(RestrictionType.PRECEDES)) {
				return precedes;
			} else if (restriction.equals(RestrictionType.SAME_ORIENTATION_AS)) {
				return sameOrientationAs;
			} else if (restriction.equals(RestrictionType.OPPOSITE_ORIENTATION_AS)) {
				return oppositeOrientationAs;
			} else if (restriction.equals(RestrictionType.DIFFERENT_FROM)) {
				return differentFrom;
			} 
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}
	
	private static final URI precedes = URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "precedes");
	private static final URI sameOrientationAs = URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "sameOrientationAs");
	private static final URI oppositeOrientationAs = URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "oppositeOrientationAs");
	private static final URI differentFrom = URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "differentFrom");

}