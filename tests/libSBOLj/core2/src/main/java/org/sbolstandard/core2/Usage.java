package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an Usage object in the SBOL data model.
 * 
 * @author Chris Myers
 * @version 2.2
 */

public class Usage extends Identified {

	private URI entity;
	private Set<URI> roles;

	/**
	 * @param identity
	 * @param entity
	 * @param roles
	 * @throws SBOLValidationException if any of the following condition is satisfied:
	 * <ul>
	 * <li>an SBOL validation rule violation occurred in {@link #setEntity(URI)}.</li>
	 * <li>an SBOL validation rule violation occurred in {@link #setRoles(URI)}.</li>
	 * </ul>
	 */
	Usage(URI identity, URI entity) throws SBOLValidationException {
		super(identity);
		setEntity(entity);
		this.roles = new HashSet<>();
		setRoles(roles);
	}

	/**
	 * @param usage
	 * @throws SBOLValidationException
	 */
	private Usage(Usage usage) throws SBOLValidationException {
		super(usage);
		this.setEntity(usage.getEntityURI());
		this.setRoles(usage.getRoles());
	}

	void copy(Usage usage) throws SBOLValidationException {
		((Identified)this).copy((Identified)usage);
		for (URI role : usage.getRoles()) {
			this.addRole(URI.create(role.toString()));
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result + ((roles == null) ? 0 : roles.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Usage other = (Usage) obj;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entity.equals(other.entity)) {
			if (getEntityIdentity() == null || other.getEntityIdentity() == null 
					|| !getEntityIdentity().equals(other.getEntityIdentity())) {
				return false;
			}
		}
		if (roles == null) {
			if (other.roles != null)
				return false;
		} else if (!roles.equals(other.roles))
			return false;
		return true;
	}
		
	/**
	 * Returns the reference entity URI.
	 *
	 * @return the reference entity URI
	 */
	public URI getEntityURI() {
		return entity;
	}
	
	/**
	 * Returns the entity identity referenced by this usage.
	 *
	 * @return {@code null} if the associated SBOLDocument instance is {@code null} or no matching
	 * entity identity referenced by this usage exists; 
	 * or the matching entity otherwise.
	 */
	public URI getEntityIdentity() {
		if (this.getSBOLDocument()==null) return null;
		if (this.getSBOLDocument().getTopLevel(entity)==null) return null;
		return this.getSBOLDocument().getTopLevel(entity).getIdentity();
	}

	/**
	 * Returns the entity referenced by this usage.
	 *
	 * @return {@code null} if the associated SBOLDocument instance is {@code null} or no matching
	 * entity referenced by this usage exists; 
	 * or the matching entity otherwise.
	 */
	public TopLevel getEntity() {
		if (this.getSBOLDocument()==null) return null;
		return this.getSBOLDocument().getTopLevel(entity);
	}

	/**
	 * @param entity the entity to set
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 12502.
	 */
	public void setEntity(URI entity) throws SBOLValidationException {
		if (entity==null) {
			throw new SBOLValidationException("sbol-12502",this);
		}
		this.entity = entity;
	}

	/**
	 * Adds the given role URI to this usage's set of role URIs.
	 *
	 * @param roleURI the role URI to be added
	 * @return {@code true} if this set did not already contain the specified role, {@code false} otherwise.
	 */
	public boolean addRole(URI roleURI) {
		return roles.add(roleURI);
	}

	/**
	 * Adds the given role to this usage's set of roles.
	 *
	 * @param role the role to be added
	 * @return {@code true} if this set did not already contain the specified role, {@code false} otherwise.
	 */
	public boolean addRole(ActivityRoleType role) {
		return addRole(ActivityRoleType.convertToURI(role));
	}
	
	/**
	 * Removes the given role URI from the set of roles.
	 *
	 * @param roleURI the given role URI to be removed
	 * @return {@code true} if the matching role reference was removed successfully, {@code false} otherwise.
	 */
	public boolean removeRole(URI roleURI) {
		return roles.remove(roleURI);
	}

	/**
	 * Returns the set of role URIs owned by this usage.
	 *
	 * @return the set of role URIs owned by this usage.
	 */
	public Set<URI> getRoles() {
		Set<URI> result = new HashSet<>();
		result.addAll(roles);
		return result;
	}

	/**
	 * Clears the existing set of roles first, and then adds the given
	 * set of the roles to this usage.
	 *
	 * @param roles the set of roles to set to
	 */
	public void setRoles(Set<URI> roles) {
		clearRoles();
		if (roles==null) return;
		for (URI role : roles) {
			addRole(role);
		}
	}
	
	/**
	 * Checks if the given role URI is included in this usage's set of role URIs.
	 *
	 * @param roleURI the role URI to be checked
	 * @return {@code true} if this set contains the given role URI, {@code false} otherwise.
	 */
	public boolean containsRole(URI roleURI) {
		return roles.contains(roleURI);
	}
	
	/**
	 * Removes all entries of this usage's set of role URIs.
	 * The set will be empty after this call returns.	 
	 */
	public void clearRoles() {
		roles.clear();
	}

	@Override
	Usage deepCopy() throws SBOLValidationException {
		return new Usage(this);
	}

	/**
	 * Updates this usage with a compliant URI.
	 * 
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of the following methods:
	 * <ul>
	 * <li>{@link URIcompliance#createCompliantURI(String, String, String)},</li>
	 * <li>{@link #setWasDerivedFrom(URI)},</li>
	 * <li>{@link #setIdentity(URI)},</li>
	 * <li>{@link #setDisplayId(String)}, or</li>
	 * <li>{@link #setVersion(String)}.</li>
	 * </ul>
	 */
	void updateCompliantURI(String URIprefix, String displayId, String version) throws SBOLValidationException {
		if (!this.getIdentity().equals(createCompliantURI(URIprefix,displayId,version))) {
			this.addWasDerivedFrom(this.getIdentity());
		}
		this.setIdentity(createCompliantURI(URIprefix,displayId,version));
		this.setPersistentIdentity(createCompliantURI(URIprefix,displayId,""));
		this.setDisplayId(displayId);
		this.setVersion(version);
	}

	@Override
	public String toString() {
		return "Usage ["
				+ super.toString()
				+ ", entity=" + entity
				+ ", roles=" + roles 
				+ "]";
	}

}
