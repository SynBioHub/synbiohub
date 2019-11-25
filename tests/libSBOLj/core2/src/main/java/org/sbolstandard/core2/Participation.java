package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Participation object in the SBOL data model.
 * 
 * @author Zhen Zhang
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */

public class Participation extends Measured {

	private Set<URI> roles;
	private URI participant;
	private ModuleDefinition moduleDefinition = null;

	/**
	 * @param identity
	 * @param participant
	 * @param roles
	 * @throws SBOLValidationException if any of the following condition is satisfied:
	 * <ul>
	 * <li>an SBOL validation rule violation occurred in {@link Identified#Identified(URI)};</li>
	 * <li>an SBOL validation rule violation occurred in {@link #setParticipant(URI)}; or </li>
	 * <li>an SBOL validation rule violation occurred in {@link #setRoles(Set)}.</li>
	 * </ul>
	 */
	Participation(URI identity, URI participant, Set<URI> roles) throws SBOLValidationException {
		super(identity);
		this.roles = new HashSet<>();
		setParticipant(participant);
		setRoles(roles);
	}

	/**
	 * @param participation
	 * @throws SBOLValidationException
	 */
	private Participation(Participation participation) throws SBOLValidationException {
		super(participation);
		this.roles = new HashSet<>();
		for (URI role : participation.getRoles()) {
			this.addRole(URI.create(role.toString()));
		}
		this.setParticipant(participation.getParticipantURI());
	}

	void copy(Participation participation) throws SBOLValidationException {
		((Measured)this).copy((Measured)participation);
	}
	
	/**
	 * Returns the FunctionalComponent URI that this participation refers to.
	 *
	 * @return the FunctionalComponent URI that this participation refers to
	 */
	public URI getParticipantURI() {
		return participant;
	}
	
	/**
	 * Returns the functional component identity this participation refers to.
	 *
	 * @return the functional component identity this participation refers to
	 */
	public URI getParticipantIdentity() {
		if (moduleDefinition==null) return null;
		if (moduleDefinition.getFunctionalComponent(participant)==null) return null;
		return moduleDefinition.getFunctionalComponent(participant).getIdentity();
	}

	/**
	 * Returns the functional component this participation refers to.
	 *
	 * @return the functional component this participation refers to
	 */
	public FunctionalComponent getParticipant() {
		if (moduleDefinition==null) return null;
		return moduleDefinition.getFunctionalComponent(participant);
	}

	/**
	 * Returns the component definition referenced by this participation's participant.
	 * 
	 * @return the component definition referenced by this participation's participant
	 */
	public ComponentDefinition getParticipantDefinition() {
		if (moduleDefinition!=null) {
			return moduleDefinition.getFunctionalComponent(participant).getDefinition();
		}
		return null;
	}

	/**
	 * Sets the participant property of this object to the given one.
	 *
	 * @param participant the participant property to set to
	 * @throws SBOLValidationException if either of the following SBOL validation rules was violated: 12002, 12003.
	 */
	public void setParticipant(URI participant) throws SBOLValidationException {
		if (participant == null) {
			throw new SBOLValidationException("sbol-12002",this);
		}
		if (moduleDefinition != null && moduleDefinition.getFunctionalComponent(participant)==null) {
			throw new SBOLValidationException("sbol-12003",this);
		}
		this.participant = participant;
	}

	/**
	 * Adds the given role to this participation's set of roles.
	 * 	
	 * @param roleURI the given role to be added
	 * @return {@code true} if this set did not already contain the given role, 
	 * or {@code false} otherwise
	 */
	public boolean addRole(URI roleURI) {
		return roles.add(roleURI);
	}

	/**
	 * Removes the given role from the set of roles.
	 *
	 * @param roleURI the given role to be removed
	 * @return {@code true} if the matching role reference was removed successfully, or {@code false} otherwise.
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 12004.
	 */
	public boolean removeRole(URI roleURI) throws SBOLValidationException {
		if (roles.size()==1 && roles.contains(roleURI)) {
			throw new SBOLValidationException("sbol-12004", this);
		}
		return roles.remove(roleURI);
	}

	/**
	 * Clears the existing set of roles first, and then adds the given
	 * set of the roles to this participation.
	 *
	 * @param roles the set of roles to set to
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 12004.
	 */
	public void setRoles(Set<URI> roles) throws SBOLValidationException {
		if (roles==null || roles.size()==0) {
			throw new SBOLValidationException("sbol-12004", this);
		}
		clearRoles();
		for (URI role : roles) {
			addRole(role);
		}
	}

	/**
	 * Returns the set of role URIs owned by this participation.
	 *
	 * @return the set of role URIs owned by this participation
	 */
	public Set<URI> getRoles() {
		return roles;
	}

	/**
	 * Checks if the given role is included in this participation's set of roles.
	 *
	 * @param roleURI the given role to be checked 
	 * @return {@code true} if this set contains the specified URI, or {@code false} otherwise
	 */
	public boolean containsRole(URI roleURI) {
		return roles.contains(roleURI);
	}

	/**
	 * Removes all entries of this Participation object's set of role URIs.
	 * The set will be empty after this call returns.
	 * <p>
	 * If this Participation object belongs to an SBOLDocument instance,
	 * then the SBOLDocument instance is checked for compliance first. Only a compliant SBOLDocument instance
	 * is allowed to be edited.
	 *
	 */
	private void clearRoles() {
		roles.clear();
	}

	@Override
	Participation deepCopy() throws SBOLValidationException {
		return new Participation(this);
	}

	/**
	 * Updates this participation with a compliant URI.
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

	/**
	 * Sets the module definition that hosts this participation's parent interaction.
	 * 
	 * @param moduleDefinition the module definition that hosts this participation's parent interaction
	 */
	void setModuleDefinition(ModuleDefinition moduleDefinition) {
		this.moduleDefinition = moduleDefinition;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Participation other = (Participation) obj;
		if (participant == null) {
			if (other.participant != null)
				return false;
		} else if (!participant.equals(other.participant)) {
			if (getParticipantIdentity() == null || other.getParticipantIdentity() == null 
					|| !getParticipantIdentity().equals(other.getParticipantIdentity())) {
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((participant == null) ? 0 : participant.hashCode());
		result = prime * result + ((roles == null) ? 0 : roles.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "Participation ["
				+ super.toString()
				+ ", roles=" + roles 
				+ ", participant=" + participant 
				+ "]";
	}

}
