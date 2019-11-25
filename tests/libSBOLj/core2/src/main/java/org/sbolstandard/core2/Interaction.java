
package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;
import static org.sbolstandard.core2.URIcompliance.extractDisplayId;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an Interaction object in the SOBL data model.
 * 
 * @author Zhen Zhang
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */

public class Interaction extends Measured {

	private Set<URI> types;
	private HashMap<URI, Participation> participations;
	private ModuleDefinition moduleDefinition = null;

	/**
	 *
	 * @param identity the identity URI for the interaction to be created
	 * @param types the set of types for the interaction to be created
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in 
	 * either of the following constructor or method:
	 * <ul>
	 * <li>{@link Identified#Identified(URI)}, or</li>
	 * <li>{@link #setTypes(Set)}.</li>
	 * </ul> 
	 */
	Interaction(URI identity, Set<URI> types) throws SBOLValidationException {
		super(identity);
		this.types = new HashSet<>();
		this.participations = new HashMap<>();
		setTypes(types);
	}

	/**
	 * @param interaction
	 * @throws SBOLValidationException
	 */
	private Interaction(Interaction interaction) throws SBOLValidationException {
		super(interaction);
		this.types = new HashSet<>();
		this.participations = new HashMap<>();
		Set<URI> type = new HashSet<>();
		for (URI typeElement : interaction.getTypes()) {
			type.add(URI.create(typeElement.toString()));
		}
		this.setTypes(type);
		Set<Participation> participations = new HashSet<>();
		for (Participation participation : interaction.getParticipations()) {
			participations.add(participation.deepCopy());
		}
		this.setParticipations(participations);
	}
	
	void copy(Interaction interaction) throws SBOLValidationException {
		((Measured)this).copy(interaction);
		for (Participation participation : interaction.getParticipations()) {
			String displayId = URIcompliance.findDisplayId(participation);
			String participantDisplayId = URIcompliance.findDisplayId(participation.getParticipant());
			Participation newParticipation = this.createParticipation(displayId, 
					participantDisplayId, participation.getRoles());
			newParticipation.copy(participation);
		}		
	}

	/**
	 * Adds the given type URI to this interaction's set of type URIs.
	 *
	 * @param typeURI the given type URI to be added
	 * @return {@code true} if this set did not contain the given type, {@code false} otherwise
	 */
	public boolean addType(URI typeURI) {
		return types.add(typeURI);
	}

	/**
	 * Removes the given type URI from this interation's the set of type URIs.
	 *
	 * @param typeURI the type URI to be removed
	 * @return {@code true} if the matching type URI was removed successfully, {@code false} otherwise
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 11902.  
	 */
	public boolean removeType(URI typeURI) throws SBOLValidationException {
		if (types.size()==1 && types.contains(typeURI)) {
			throw new SBOLValidationException("sbol-11902", this);
		}
		return types.remove(typeURI);
	}

	/**
	 * Clears the existing set of type URIs first, then adds the given
	 * set of the type URIs to this interaction.
	 *
	 * @param types the given set of type URIs to set to
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 11902.
	 */
	public void setTypes(Set<URI> types) throws SBOLValidationException {
		if (types==null || types.size()==0) {
			throw new SBOLValidationException("sbol-11902", this);
		}
		clearTypes();
		for (URI type : types) {
			addType(type);
		}
	}

	/**
	 * Returns the set of type URIs owned by this Interaction object.
	 *
	 * @return the set of type URIs owned by this Interaction object.
	 */
	public Set<URI> getTypes() {
		return types;
	}

	/**
	 * Checks if the given type URI is included in this interaction's set of type URIs.
	 *
	 * @param typeURI the given type URI to be checked for existence
	 * @return {@code true} if this set contains the given URI, {@code false} otherwise
	 */
	public boolean containsType(URI typeURI) {
		return types.contains(typeURI);
	}

	/**
	 * Removes all entries this interation's list of types. 
	 * The list will be empty after this call returns.
	 */
	private void clearTypes() {
		types.clear();
	}

	//	/**
	//	 * Test if the optional field variable <code>participations</code> is set.
	//	 * @return <code>true</code> if the field variable is not an empty list
	//	 */
	//	public boolean isSetParticipations() {
	//		return !(participations == null || participations.isEmpty());
	//	}

	/**
	 * Calls the Participation constructor to create a new participation using the given arguments,
	 * then adds to the list of participations owned by this interaction.
	 * 
	 * @param identity the URI identity of the participation to be created
	 * @param participant the functional component the participation to be created refers to
	 * @param roles the roles property of the participation to be created
	 * @return the created participation
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul>
	 * <li>an SBOL validation rule violation occurred in {@link Participation#Participation(URI, URI, Set)}; or</li>
	 * <li>an SBOL validation rule violation occurred in {@link #addParticipation(Participation)}.</li>
	 * </ul>
	 */
	private Participation createParticipation(URI identity, URI participant, Set<URI> roles) throws SBOLValidationException {
		Participation participation = new Participation(identity, participant, roles);
		addParticipation(participation);
		return participation;
	}

	/**
	 * Creates a child participation for this interaction with the given arguments, 
	 * and then adds to this interaction's list of participations.
	 * <p>
	 * This method creates a new set of roles, and adds to it the given role. It then calls
	 * {@link #createParticipation(String, String, Set)} with the given display IDs for the participation 
	 * and its reference participant, and the set of roles created in this method.
	 *
	 * @param displayId the display ID of the participation to be created
	 * @param participantId the display ID of the participant functional component referenced by the participation to be created
	 * @param role the role of the participation to be created 
	 * @return the created participation
	 * @throws SBOLValidationException if an SBOL validation rule was violated in {@link #createParticipation(String, String, Set)}. 
	 */
	public Participation createParticipation(String displayId, String participantId, URI role) throws SBOLValidationException {
		HashSet<URI> roles = new HashSet<URI>();
		roles.add(role);
		return createParticipation(displayId,participantId,roles);
	}


	//	/**
	//	 * Creates a child participation for this Interaction
	//	 * object with the given arguments, and then adds to this Interaction's list of participations.
	//	 * <p>
	//	 * If this ComponentDefinition object belongs to an SBOLDocument instance, then
	//	 * the SBOLDocument instance is checked for compliance first. Only a compliant SBOLDocument instance
	//	 * is allowed to be edited.
	//	 * <p>
	//	 * This method creates a compliant Participation URI with this Interaction object's
	//	 * persistent identity URI, the given {@code paricipantId}, and this Interaction object's version.
	//	 *It then calls {@link #createParticipation(String, URI, Set<URI>)}
	//	 * with this component definition URI.
	//	 *
	//	 * @param displayId The displayId identifier for this
	//	 * @param participantId specify precisely one FunctionalComponent object that plays the designated role in its parent Interaction object
	//	 * @param roles describes the behavior of a Participation
	//	 * @return a participation
	//	 * @throws SBOLValidationException if the associated SBOLDocument is not compliant
	//	 */

	/**
	 * Creates a child participation for this interaction with the given arguments, and then adds to this interaction's 
	 * list of participations.
	 * <p>
	 * This first method creates a compliant URI for the reference functional component of the participation, i.e., the participant
	 * property of the participation. This participant URI starts with the persistent identity of the module definition that hosts this interaction,
	 * followed by the particpant's display ID, and ends with the hosting module definition's version.
	 * <p>
	 * If no functional component exists with the provided participantId and isCreateDefaults is true,
	 * it creates a new functionalComponent instantiation for the componentDefinition with the displayId
	 * that matches the participantId.
	 * <p>
	 * This method then calls {@link #createParticipation(String, URI, Set)} with the given display ID, the 
	 * created compliant URI for the participant, and the set of roles.
	 * 
	 * @param displayId the display ID of the participation to be created
	 * @param participantId the display ID of the participant functional component 
	 * @param roles the roles of the participation to be created
	 * @return the created participation
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul>
	 * <li>if either of the following SBOL validation rules was violated: 10204, 10206;</li>
	 * <li>an SBOL validation rule violation occurred in {@link ModuleDefinition#createFunctionalComponent(String, AccessType, String, String, DirectionType)}</li>
	 * <li>an SBOL validation rule violation occurred in {@link #createParticipation(String, URI, Set)}.</li>
	 * </ul>
	 */
	public Participation createParticipation(String displayId, String participantId, Set<URI> roles) throws SBOLValidationException {
		URI participantURI = URIcompliance.createCompliantURI(moduleDefinition.getPersistentIdentity().toString(),
				participantId, moduleDefinition.getVersion());
		if (this.getSBOLDocument()!=null && this.getSBOLDocument().isCreateDefaults() && moduleDefinition!=null &&
				moduleDefinition.getFunctionalComponent(participantURI)==null) {
			moduleDefinition.createFunctionalComponent(participantId,AccessType.PUBLIC,participantId,"",
					DirectionType.INOUT);
		}
		return createParticipation(displayId,participantURI,roles);
	}

	/**
	 * Creates a child participation for this interaction with the given arguments, and then adds to its list of participations.
	 * <p>
	 * This method creates a new set of roles, and adds to it the given role. It then calls
	 * {@link #createParticipation(String, URI, Set)} with the given display ID for the participation, 
	 * the URI identity of its reference participant, and the set of roles created in this method.
	 *	 
	 * @param displayId the display ID of the participation to be created
	 * @param participant the participant functional component for the participation to be created
	 * @param role the role of the participation to be created
	 * @return the created participation
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #createParticipation(String, URI, Set)}.
	 */
	public Participation createParticipation(String displayId, URI participant, URI role) throws SBOLValidationException {
		HashSet<URI> roles = new HashSet<URI>();
		roles.add(role);
		return createParticipation(displayId,participant,roles);
	}

	/**
	 * Creates a child participation for this interaction with the given arguments, 
	 * and then adds to this interaction's list of participations.
	 * <p>
	 * This method first creates a compliant URI for the participation to be created. It starts with this Interaction object's
	 * persistent identity URI, followed by the given display ID, and ends with this interaction's version.
	 *
	 *
	 * @param displayId The displayId identifier for this
	 * @param participant specify precisely one FunctionalComponent object that plays the designated 3 role in its parent Interaction object
	 * @param roles describes the behavior of a Participation
	 * @return the created participation
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated: 
	 * 10201, 10202, 10204, 10206, 12002, 12003, 12004, 
	 */
	public Participation createParticipation(String displayId, URI participant, Set<URI> roles) throws SBOLValidationException {
		String parentPersistentIdStr = this.getPersistentIdentity().toString();
		String version = this.getVersion();
		if(parentPersistentIdStr == null) {
			throw new IllegalStateException(
					"Cannot create a child on a parent that has the non-standard compliant identity " +
							this.getIdentity());
		}
		//validateIdVersion(displayId, version);
		Participation p = createParticipation(
				createCompliantURI(parentPersistentIdStr, displayId, version), participant,roles);
		p.setPersistentIdentity(createCompliantURI(parentPersistentIdStr, displayId, ""));
		p.setDisplayId(displayId);
		p.setVersion(version);
		return p;
	}

	/**
	 * Adds the given participation to the list of participations owned by this interaction.
	 *
	 * @param participation the participation to be added
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul>
	 * <li>the following SBOL validation rule was violated: 12003; or</li>
	 * <li>an SBOL validation rule violation occurred in {@link Identified#addChildSafely(Identified, java.util.Map, String, java.util.Map...)}</li>
	 * </ul>
	 */
	private void addParticipation(Participation participation) throws SBOLValidationException {
		if (moduleDefinition != null && moduleDefinition.getFunctionalComponent(participation.getParticipantURI())==null) {
			throw new SBOLValidationException("sbol-12003", participation);
		}
		addChildSafely(participation, participations, "participation");
		participation.setSBOLDocument(this.getSBOLDocument());
		participation.setModuleDefinition(moduleDefinition);
	}

	/**
	 * Removes the given participation from this interaction's list of participations.
	 *
	 * @param participation the given participation to be removed
	 * @return {@code true} if the matching participation was removed successfully,
	 *         {@code false} otherwise
	 */
	public boolean removeParticipation(Participation participation) {
		return removeChildSafely(participation,participations);
	}

	/**
	 * Returns the participation matching the given display ID from
	 * this interaction's list of participations.
	 * <p>
	 * This method first creates a compliant URI for the participation to be retrieved. It starts with this
	 * interaction's persistent identity, followed by the given display ID, and ends with this interation's version.
	 *
	 * @param displayId the display ID of the participation to be retrieved 
	 * @return the matching participation if present, or {@code null} otherwise
	 */
	public Participation getParticipation(String displayId) {
		try {
			return participations.get(createCompliantURI(this.getPersistentIdentity().toString(),displayId,this.getVersion()));
		}
		catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the participation matching the given participation URI from this
	 * interaction's list of participations.
	 *
	 * @param participationURI the identity URI for the participation to be retrieved
	 * @return the matching participation if present, or
	 *         {@code null} otherwise
	 */
	public Participation getParticipation(URI participationURI) {
		return participations.get(participationURI);
	}

	/**
	 * Returns the set of participations owned by this
	 * interaction.
	 *
	 * @return the set of the set of participations owned by this
	 * interaction
	 */
	public Set<Participation> getParticipations() {
		return new HashSet<>(participations.values());
	}

	/**
	 * Removes all entries of this interaction list of participations.
	 * The list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeParticipation(Participation)} to iteratively remove
	 * each participation owned by this object.
	 *
	 */
	public void clearParticipations() {
		Object[] valueSetArray = participations.values().toArray();
		for (Object participation : valueSetArray) {
			removeParticipation((Participation)participation);
		}

	}

	/**
	 * Clears the existing list of participations, then adds the given set of participations to this list.
	 * 
	 * @param participations the given set of participations to set to
	 * @throws SBOLValidationException an SBOL validation rule violation occurred in {@link #addParticipation(Participation)}
	 */	
	void setParticipations(Set<Participation> participations) throws SBOLValidationException {
		clearParticipations();
		for (Participation participation : participations) {
			addParticipation(participation);
		}
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((participations == null) ? 0 : participations.hashCode());
		result = prime * result + ((types == null) ? 0 : types.hashCode());
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
		Interaction other = (Interaction) obj;
		if (participations == null) {
			if (other.participations != null)
				return false;
		} else if (!participations.equals(other.participations))
			return false;
		if (types == null) {
			if (other.types != null)
				return false;
		} else if (!types.equals(other.types))
			return false;
		return true;
	}

	@Override
	Interaction deepCopy() throws SBOLValidationException {
		return new Interaction(this);
	}

	/**
	 * Updates this interaction's and each of its child participation's identity URI with a compliant URI. 
	 *
	 * @param URIprefix the URI prefix of this interaction used to created its compliant URI
	 * @param displayId the display ID of this interaction used to created its compliant URI
	 * @param version the version of this interaction used to created its compliant URI
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of the following methods:
	 * <ul>
	 * <li>{@link URIcompliance#createCompliantURI(String, String, String)},</li>
	 * <li>{@link #setWasDerivedFrom(URI)},</li>
	 * <li>{@link Identified#setIdentity(URI)},</li>
	 * <li>{@link Identified#setDisplayId(String)}, </li>
	 * <li>{@link Identified#setVersion(String)}, </li>
	 * <li>{@link Participation#updateCompliantURI(String, String, String)},</li>
	 * <li>{@link #addParticipation(Participation)}, or</li>
	 * <li>{@link Participation#setParticipant(URI)}.</li>
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
		int count = 0;
		for (Participation participation : this.getParticipations()) {
			if (!participation.isSetDisplayId()) participation.setDisplayId("participation"+ ++count);
			participation.updateCompliantURI(this.getPersistentIdentity().toString(),
					participation.getDisplayId(), version);
			this.removeChildSafely(participation, this.participations);
			this.addParticipation(participation);
			String participantId = extractDisplayId(participation.getParticipantURI());
			participation.setParticipant(createCompliantURI(URIprefix,participantId,version));
		}
	}

	/**
	 * Sets the given module definition to be the parent of this interaction. 
	 * 
	 * @param moduleDefinition the parent module definition
	 */
	void setModuleDefinition(ModuleDefinition moduleDefinition) {
		this.moduleDefinition = moduleDefinition;
	}

	@Override
	public String toString() {
		return "Interaction ["
				+ super.toString()
				+ ", types=" + types 
				+ (participations.size()>0?", participations=" + participations:"") 
				+ "]";
	}

}
