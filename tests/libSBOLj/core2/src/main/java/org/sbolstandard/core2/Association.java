package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an Association object in the SBOL data model.
 * 
 * @author Chris Myers
 * @version 2.2
 */

public class Association extends Identified {

	private Set<URI> roles;
	private URI agent;
	private URI plan;

	/**
	 * @param identity
	 * @param agent
	 * @throws SBOLValidationException if any of the following condition is satisfied:
	 * <ul>
	 * <li>an SBOL validation rule violation occurred in {@link #setRole(URI)}.</li>
	 * <li>an SBOL validation rule violation occurred in {@link #setAgent(URI)}.</li>
	 * </ul>
	 */
	Association(URI identity, URI agent) throws SBOLValidationException {
		super(identity);
		this.roles = new HashSet<>();
		setAgent(agent);
		plan = null;
	}

	/**
	 * @param association
	 * @throws SBOLValidationException
	 */
	private Association(Association association) throws SBOLValidationException {
		super(association);
		this.setRoles(association.getRoles());
		this.setAgent(association.getAgentURI());
		this.setPlan(association.getPlanURI());
	}

	void copy(Association association) throws SBOLValidationException {
		((Identified)this).copy((Identified)association);
		this.setPlan(association.getPlanURI());
		for (URI role : association.getRoles()) {
			this.addRole(URI.create(role.toString()));
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((agent == null) ? 0 : agent.hashCode());
		result = prime * result + ((plan == null) ? 0 : plan.hashCode());
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
		Association other = (Association) obj;
		if (agent == null) {
			if (other.agent != null)
				return false;
		} else if (!agent.equals(other.agent)) {
			if (getAgentIdentity() == null || other.getAgentIdentity() == null 
					|| !getAgentIdentity().equals(other.getAgentIdentity())) {
				return false;
			}
		}
		if (plan == null) {
			if (other.plan != null)
				return false;
		} else if (!plan.equals(other.plan)) {
			if (getPlanIdentity() == null || other.getPlanIdentity() == null 
					|| !getPlanIdentity().equals(other.getPlanIdentity())) {
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
	 * Adds the given role URI to this association's set of role URIs.
	 *
	 * @param roleURI the role URI to be added
	 * @return {@code true} if this set did not already contain the specified role, {@code false} otherwise.
	 */
	public boolean addRole(URI roleURI) {
		return roles.add(roleURI);
	}

	/**
	 * Adds the given role to this association's set of roles.
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
	 * Returns the set of role URIs owned by this association.
	 *
	 * @return the set of role URIs owned by this association.
	 */
	public Set<URI> getRoles() {
		Set<URI> result = new HashSet<>();
		result.addAll(roles);
		return result;
	}

	/**
	 * Clears the existing set of roles first, and then adds the given
	 * set of the roles to this association.
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
	 * Checks if the given role URI is included in this association's set of role URIs.
	 *
	 * @param roleURI the role URI to be checked
	 * @return {@code true} if this set contains the given role URI, {@code false} otherwise.
	 */
	public boolean containsRole(URI roleURI) {
		return roles.contains(roleURI);
	}
	
	/**
	 * Removes all entries of this associations's set of role URIs.
	 * The set will be empty after this call returns.	 
	 */
	public void clearRoles() {
		roles.clear();
	}
	
	/**
	 * Returns the reference agent URI.
	 *
	 * @return the reference agent URI
	 */
	public URI getAgentURI() {
		return agent;
	}

	/**
	 * Returns the agent identity referenced by this association.
	 *
	 * @return {@code null} if the associated SBOLDocument instance is {@code null} or no matching
	 * agent referenced by this association exists; 
	 * or the matching agent otherwise.
	 */
	public URI getAgentIdentity() {
		if (this.getSBOLDocument()==null) return null;
		if (this.getSBOLDocument().getAgent(agent)==null) return null;
		return this.getSBOLDocument().getAgent(agent).getIdentity();
	}
	
	/**
	 * Returns the agent referenced by this association.
	 *
	 * @return {@code null} if the associated SBOLDocument instance is {@code null} or no matching
	 * agent referenced by this association exists; 
	 * or the matching agent otherwise.
	 */
	public Agent getAgent() {
		if (this.getSBOLDocument()==null) return null;
		return this.getSBOLDocument().getAgent(agent);
	}
	
	/**
	 * @param agent the agent to set
	 * @throws SBOLValidationException if either of the following SBOL validation rules was violated: 12605, 12606. 
	 */
	public void setAgent(URI agent) throws SBOLValidationException {
		if (agent==null) {
			throw new SBOLValidationException("sbol-12605",this);
		}
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (this.getSBOLDocument().getAgent(agent)==null) {
				throw new SBOLValidationException("sbol-12606",this);
			}
		}
		this.agent = agent;
	}
	
	/**
	 * Test if the plan is set.
	 *
	 * @return {@code true} if it is not {@code null}, or {@code false} otherwise
	 */
	public boolean isSetPlan() {
		return plan != null;
	}
	
	/**
	 * Returns the reference plan URI.
	 *
	 * @return the reference plan URI
	 */
	public URI getPlanURI() {
		return plan;
	}
	
	/**
	 * Returns the plan identity referenced by this association.
	 *
	 * @return {@code null} if the associated SBOLDocument instance is {@code null} or no matching
	 * plan referenced by this association exists; 
	 * or the matching plan otherwise.
	 */
	public URI getPlanIdentity() {
		if (this.getSBOLDocument()==null) return null;
		if (this.getSBOLDocument().getPlan(plan)==null) return null;
		return this.getSBOLDocument().getPlan(plan).getIdentity();
	}

	/**
	 * Returns the plan referenced by this association.
	 *
	 * @return {@code null} if the associated SBOLDocument instance is {@code null} or no matching
	 * plan referenced by this association exists; 
	 * or the matching plan otherwise.
	 */
	public Plan getPlan() {
		if (this.getSBOLDocument()==null) return null;
		return this.getSBOLDocument().getPlan(plan);
	}

	/**
	 * @param plan the plan to set
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 12604.
	 */
	public void setPlan(URI plan) throws SBOLValidationException {
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (this.getSBOLDocument().getPlan(plan)==null) {
				throw new SBOLValidationException("sbol-12604",this);
			}
		}
		this.plan = plan;
	}

	@Override
	Association deepCopy() throws SBOLValidationException {
		return new Association(this);
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

	@Override
	public String toString() {
		return "Association ["
				+ super.toString()
				+ ", roles=" + roles 
				+ ", agent=" + agent
				+ ", plan=" + plan
				+ "]";
	}

}
