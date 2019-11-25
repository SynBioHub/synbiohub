package org.sbolstandard.core2;

import java.net.URI;

/**
 * Represents the role integration relationship between roles specified in related objects.
 * It indicates how role is to be integrated with related roles.
 *
 * @author Zhen Zhang
 * @author Chris Myers
 * @version 2.1
 */

public enum RoleIntegrationType {
	/**
	 * Ignore any role(s) given for the child object. 
	 * Instead use only the set of zero or more roles given for this object.
	 */
	OVERRIDEROLES("overrideRoles"),
	/**
	 * Use the union of the two sets: both the set of zero or more roles given for 
	 * this object as well as the set of zero or more roles given for the child 
	 * object.
	 */
	MERGEROLES("mergeRoles"); 
	private final String roleIntegrationType;

	RoleIntegrationType(String roleIntegrationType) {
		this.roleIntegrationType = roleIntegrationType;
	}

	@Override
	public String toString() {
		return roleIntegrationType;
	}

	// TODO: Need proper validation error numbers
	/**
	 * Convert the specified URI to its corresponding RoleIntegrationType instance. 
	 * @return the corresponding RoleIntegrationType instance
	 * @throws SBOLValidationException 
	 */
	static RoleIntegrationType convertToRoleIntegrationType(URI roleIntegration) throws SBOLValidationException {
		if (roleIntegration != null) {
			if (roleIntegration.equals(mergeRoles)) {
				return RoleIntegrationType.MERGEROLES;
			} 
			else if (roleIntegration.equals(overrideRoles)) {
				return RoleIntegrationType.OVERRIDEROLES;
			}
			else {
				throw new SBOLValidationException("sbol-10708");
			}
		} else {
			throw new SBOLValidationException("sbol-10708");
		}
	}

	/**
	 * Returns the roleIntegration type in URI.
	 * @return roleIntegration type in URI
	 */
	static URI convertToURI(RoleIntegrationType refinement) {
		if (refinement != null) {
			if (refinement.equals(RoleIntegrationType.MERGEROLES)) {
				return mergeRoles;
			}
			else if (refinement.equals(RoleIntegrationType.OVERRIDEROLES)) {
				return overrideRoles;
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}
	
	private static final URI mergeRoles = URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "mergeRoles");
	private static final URI overrideRoles = URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "overrideRoles");

}