package org.sbolstandard.core2;

import java.net.URI;

/**
 * Represents the refinement relationship between a MapsTo instance's local and remote components.
 *
 * @author Zhen Zhang
 * @author Chris Myers
 * @version 2.1
 */

public enum RefinementType {
	/**
	 * The definition properties of the local and remote ComponentInstance objects 
	 * MUST refer to the same ComponentDefinition.
	 */
	VERIFYIDENTICAL("verifyIdentical"),
	/**
	 * In the context of the ComponentDefinition or ModuleDefinition that contains 
	 * the owner of the MapsTo, all references to the definition property
	 * of the remote ComponentInstance MUST dereference to that of the local 
	 * ComponentInstance instead. 
	 */
	USELOCAL("useLocal"), 
	/**
	 * All references to the definition property of the local ComponentInstance 
	 * MUST dereference to that of the remote ComponentInstance instead.
	 */
	USEREMOTE("useRemote"),
	/**
	 * In the context of the ComponentDefinition or ModuleDefinition that contains 
	 * the owner of the MapsTo, all references to the definition property 
	 * of the local ComponentInstance or that of the remote ComponentInstance MUST 
	 * dereference to both properties.
	 */
	MERGE("merge");
	private final String refinementType;

	RefinementType(String refinementType) {
		this.refinementType = refinementType;
	}

	@Override
	public String toString() {
		return refinementType;
	}

	/**
	 * Convert the specified URI to its corresponding RefinementType instance. 
	 * @return the corresponding RefinementType instance
	 * @throws SBOLValidationException 
	 */
	static RefinementType convertToRefinementType(URI refinement) throws SBOLValidationException {
		if (refinement != null) {
			if (refinement.equals(merge)) {
				return RefinementType.MERGE;
			} 
			else if (refinement.equals(useLocal)) {
				return RefinementType.USELOCAL;
			}
			else if (refinement.equals(useRemote)) {
				return RefinementType.USEREMOTE;
			}
			else if (refinement.equals(verifyIdentical)) {
				return RefinementType.VERIFYIDENTICAL;
			}
			else {
				throw new SBOLValidationException("sbol-10810");
			}
		} else {
			throw new SBOLValidationException("sbol-10810");
		}
	}

	/**
	 * Returns the refinement type in URI.
	 * @return refinement type in URI
	 */
	static URI convertToURI(RefinementType refinement) {
		if (refinement != null) {
			if (refinement.equals(RefinementType.MERGE)) {
				return merge;
			}
			else if (refinement.equals(RefinementType.USELOCAL)) {
				return useLocal;
			}
			else if (refinement.equals(RefinementType.USEREMOTE)) {
				return useRemote;
			}
			else if (refinement.equals(RefinementType.VERIFYIDENTICAL)) {
				return verifyIdentical;
			}				
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}
	
	private static final URI merge = URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "merge");
	private static final URI useLocal = URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "useLocal");
	private static final URI useRemote = URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "useRemote");
	private static final URI verifyIdentical = URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "verifyIdentical");

}