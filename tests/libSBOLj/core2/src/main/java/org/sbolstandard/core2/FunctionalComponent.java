package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;
import static org.sbolstandard.core2.URIcompliance.extractDisplayId;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a FunctionalComponent object in the SBOL data model.
 * 
 * @author Zhen Zhang
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */

public class FunctionalComponent extends ComponentInstance {

	private DirectionType direction;
	private HashMap<URI, MapsTo> mapsTos;
	private ModuleDefinition moduleDefinition = null;

	/**
	 * @param identity
	 * @param access
	 * @param definitionURI
	 * @param direction
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul>
	 * <li>an SBOL validation rule violation occurred in {@link ComponentInstance#ComponentInstance(URI, AccessType, URI)}; or</li>
	 * <li>an SBOL validation rule violation occurred in {@link #setDirection(DirectionType)}.</li>
	 * </ul>
	 */
	FunctionalComponent(URI identity, AccessType access, URI definitionURI,
			DirectionType direction) throws SBOLValidationException {
		super(identity, access, definitionURI);
		this.mapsTos = new HashMap<>();
		setDirection(direction);
	}

	/**
	 * @param functionalComponent
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of the following
	 * constructors or methods:
	 * <ul>
	 * <li>{@link ComponentInstance#ComponentInstance(ComponentInstance)},</li>
	 * <li>{@link #setDirection(DirectionType)},</li>
	 * <li>{@link MapsTo#deepCopy()}, or</li>
	 * <li>{@link #setMapsTos(Set)}.</li>
	 * </ul>
	 */
	private FunctionalComponent(FunctionalComponent functionalComponent) throws SBOLValidationException {
		super(functionalComponent);
		this.setDirection(functionalComponent.getDirection());
		this.mapsTos = new HashMap<>();
		if (!functionalComponent.getMapsTos().isEmpty()) {
			Set<MapsTo> mapsTos = new HashSet<>();
			for (MapsTo mapsTo : functionalComponent.getMapsTos()) {
				mapsTos.add(mapsTo.deepCopy());
			}
			this.setMapsTos(mapsTos);
		}
	}
	
	void copy(FunctionalComponent functionalComponent) throws SBOLValidationException {
		((ComponentInstance)this).copy((ComponentInstance)functionalComponent);
		this.mapsTos = new HashMap<>();
		// TODO: moved this to later, since need to copy functionalComponents first
//		if (!functionalComponent.getMapsTos().isEmpty()) {
//			for (MapsTo mapsTo : functionalComponent.getMapsTos()) {
//				String displayId = URIcompliance.findDisplayId(mapsTo);
//				String localDisplayId = URIcompliance.findDisplayId(mapsTo.getLocal());
//				MapsTo newMapsTo = this.createMapsTo(displayId, mapsTo.getRefinement(), localDisplayId, 
//						mapsTo.getRemoteURI());
//				newMapsTo.copy(mapsTo);
//			}
//		}
	}

	/**
	 * Returns the direction property of this functional component.
	 *
	 * @return the direction property of this functional component
	 */
	public DirectionType getDirection() {
		return direction;
	}

	/**
	 * Sets the direction property of this functional component to the given one.
	 *
	 * @param direction The direction for the FunctionalComponent
	 * @throws SBOLValidationException if the following SBOL validation rule violation occurred: 11802.
	 *
	 */
	public void setDirection(DirectionType direction) throws SBOLValidationException {
		if (direction==null) {
			throw new SBOLValidationException("sbol-11802",this);
		}
		this.direction = direction;
	}
	
	/**
	 * Sets the definition property to the given one.
	 *
	 * @param definition the given definition URI to set to 
	 * @throws SBOLValidationException if either of the following SBOL validation rules was violated: 10604.
	 */
	public void setDefinition(URI definition) throws SBOLValidationException {
		if (this.getSBOLDocument() != null) {
			ComponentDefinition cd = this.getSBOLDocument().getComponentDefinition(definition);
			if (this.getSBOLDocument().isComplete()) {
				if (cd==null) {
					throw new SBOLValidationException("sbol-10604",this);
				}
			}
		}
		super.setDefinition(definition);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + ((mapsTos == null) ? 0 : mapsTos.hashCode());
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
		FunctionalComponent other = (FunctionalComponent) obj;
		if (direction != other.direction)
			return false;
		if (mapsTos == null) {
			if (other.mapsTos != null)
				return false;
		} else if (!mapsTos.equals(other.mapsTos))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.ComponentInstance#deepCopy()
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link FunctionalComponent#FunctionalComponent(FunctionalComponent)}.
	 */
	@Override
	FunctionalComponent deepCopy() throws SBOLValidationException {
		return new FunctionalComponent(this);
	}

	/**
	 * Update the URI of this functional component and its list of mapsTos.
	 *  
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of the following
	 * methods:
	 * <ul>
	 * <li>{@link URIcompliance#createCompliantURI(String, String, String)}</li>
	 * <li>{@link Identified#setWasDerivedFrom(URI)}</li>
	 * <li>{@link #setIdentity(URI)}</li>
	 * <li>{@link #setDisplayId(String)}</li>
	 * <li>{@link #setVersion(String)}</li>
	 * <li>{@link MapsTo#updateCompliantURI(String, String, String)}</li>
	 * <li>{@link #addMapsTo(MapsTo)}</li>
	 * <li>{@link MapsTo#setLocal(URI)}</li>
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
		for (MapsTo mapsTo : this.getMapsTos()) {
			mapsTo.updateCompliantURI(this.getPersistentIdentity().toString(),
					mapsTo.getDisplayId(), version);
			this.removeChildSafely(mapsTo, this.mapsTos);
			this.addMapsTo(mapsTo);
			String localId = extractDisplayId(mapsTo.getLocalURI());
			mapsTo.setLocal(createCompliantURI(URIprefix,localId,version));
		}
	}

	/**
	 * Calls the MapsTo constructor {@link MapsTo#MapsTo(URI, RefinementType, URI, URI)} to create a new 
	 * mapsTo using the given arguments, and then adds to its list of mapsTos.
	 *
	 * @return the created MapsTo instance
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul>
	 * <li>an SBOL validation rule violation occurred in {@link MapsTo#MapsTo(URI, RefinementType, URI, URI)}</li>
	 * <li>an SBOL validation rule violation occurred in {@link #addMapsTo(MapsTo)}</li>
	 * </ul>
	 */
	private MapsTo createMapsTo(URI identity, RefinementType refinement, URI local, URI remote) throws SBOLValidationException {
		MapsTo mapping = new MapsTo(identity, refinement, local, remote);
		addMapsTo(mapping);
		return mapping;
	}

	/**
	 * Creates a child MapsTo instance for this module with the given arguments, and then adds to this module's list of mapsTos.
	 * <p>
	 * This method creates compliant local and remote URIs first.
	 * The compliant local URI is created with this module's persistent identity URI, followed by
	 * the given local component's display ID, followed by this module's version. 
	 * The compliant remote URI is created following the same pattern.
	 * It then calls {@link #createMapsTo(String, RefinementType, URI, URI)} to create
	 * a MapsTo instance.
	 * <p>
	 * This method automatically creates a local functional component if all of the following conditions are satisfied:
	 * <ul>
	 * <li>the associated SBOLDocument instance for this module is not {@code null};</li>
	 * <li>if default functional components should be automatically created when not present for the associated SBOLDocument instance,
	 * i.e., {@link SBOLDocument#isCreateDefaults} returns {@code true};</li>
	 * <li>if this module's parent module definition exists; and</li>
	 * <li>if this module's parent module definition does not already have a functional component
	 * with the created compliant local functional component URI.</li> 
	 * </ul>
	 * @param displayId the display ID of the mapsTo to be created 
	 * @param refinement the relationship between the local and remote functional components
	 * @param localId the display ID of the local functional component
	 * @param remoteId the display ID of the remote functional component
	 * @return the created mapsTo
	 * @throws SBOLValidationException if any of the following conditions is satisfied:
	 * <ul>
	 * <li>if either of the following SBOL validation rules was violated: 10204, 10206;</li>
	 * <li>an SBOL validation rule violation occurred in {@link ModuleDefinition#createFunctionalComponent(String, AccessType, String, String, DirectionType)}; or</li>
	 * <li>an SBOL validation rule violation occurred in {@link #createMapsTo(String, RefinementType, URI, URI)}.</li>
	 * </ul>
	 */
	public MapsTo createMapsTo(String displayId, RefinementType refinement, String localId, String remoteId) throws SBOLValidationException {
		URI localURI = URIcompliance.createCompliantURI(moduleDefinition.getPersistentIdentity().toString(),
				localId, moduleDefinition.getVersion());
		if (this.getSBOLDocument()!=null && this.getSBOLDocument().isCreateDefaults() && moduleDefinition!=null &&
				moduleDefinition.getFunctionalComponent(localURI)==null) {
			moduleDefinition.createFunctionalComponent(localId,AccessType.PUBLIC,localId,"",DirectionType.INOUT);
		}
		URI remoteURI = URIcompliance.createCompliantURI(getDefinition().getPersistentIdentity().toString(),
				remoteId, getDefinition().getVersion());
		return createMapsTo(displayId,refinement,localURI,remoteURI);
	}
	
	MapsTo createMapsTo(String displayId, RefinementType refinement, String localId, URI remoteURI) throws SBOLValidationException {
		URI localURI = URIcompliance.createCompliantURI(moduleDefinition.getPersistentIdentity().toString(),
				localId, moduleDefinition.getVersion());
		if (this.getSBOLDocument()!=null && this.getSBOLDocument().isCreateDefaults() && moduleDefinition!=null &&
				moduleDefinition.getFunctionalComponent(localURI)==null) {
			moduleDefinition.createFunctionalComponent(localId,AccessType.PUBLIC,localId,"",DirectionType.INOUT);
		}
		return createMapsTo(displayId,refinement,localURI,remoteURI);
	}

	/**
	 * Creates a child mapsTo for this functional component with the given arguments,
	 * and then adds it to this functional compoennt's list of mapsTos.
	 * <p>
	 * The compliant mapsTo URI is created with this functional component's persistent identity URI, followed by
	 * the given mapsTo's display ID, followed by this functional component's version. 
	 *
	 * @param displayId the display ID of the mapsTo to be created
	 * @param refinement the refinement property of the mapsTo to be created
	 * @param local the URI of the referenced local component instance 
	 * @param remote the URI of the referenced remote component instance
	 * @return the created mapsTo
	 * @throws SBOLValidationException if any of the following SBOL validation rule was violated:
	 * 10201, 10202, 10204, 10206, 10802, 10803, 10804, 10805, 10807, 10808, 10809, 10811. 
	 */
	public MapsTo createMapsTo(String displayId, RefinementType refinement, URI local, URI remote) throws SBOLValidationException {
		String parentPersistentIdStr = this.getPersistentIdentity().toString();
		String version = this.getVersion();
		MapsTo m = createMapsTo(createCompliantURI(parentPersistentIdStr, displayId, version),
				refinement, local, remote);
		m.setPersistentIdentity(createCompliantURI(parentPersistentIdStr, displayId, ""));
		m.setDisplayId(displayId);
		m.setVersion(version);
		return m;
	}

	/**
	 * Adds the specified instance to the list of references.
	 * @throws SBOLValidationException if any of the following is satisfied:
	 * <ul>
	 * <li>if any of the following SBOL validation rule was violated: 10804, 10807, 10808, 10811.</li>
	 * <li>an SBOL validation rule exception occurred in {@link Identified#addChildSafely(Identified, java.util.Map, String, java.util.Map...)} </li>
	 * </ul>
	 */
	private void addMapsTo(MapsTo mapsTo) throws SBOLValidationException {
		mapsTo.setSBOLDocument(this.getSBOLDocument());
		mapsTo.setModuleDefinition(moduleDefinition);
		mapsTo.setComponentInstance(this);
		if (this.getSBOLDocument() != null) {
			if (moduleDefinition.getFunctionalComponent(mapsTo.getLocalURI())==null) {
				//throw new SBOLValidationException("Functional component '" + mapsTo.getLocalURI() + "' does not exist.");
				throw new SBOLValidationException("sbol-10804", mapsTo);
			}
		}
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (getDefinition().getComponent(mapsTo.getRemoteURI())==null) {
				//throw new SBOLValidationException("Component '" + mapsTo.getRemoteURI() + "' does not exist.");
				throw new SBOLValidationException("sbol-10808", mapsTo);
			}
			if (getDefinition().getComponent(mapsTo.getRemoteURI()).getAccess().equals(AccessType.PRIVATE)) {
				//throw new SBOLValidationException("Component '" + mapsTo.getRemoteURI() + "' is private.");
				throw new SBOLValidationException("sbol-10807", mapsTo);
			}
			if (mapsTo.getRefinement().equals(RefinementType.VERIFYIDENTICAL)) {
				if (!mapsTo.getLocal().getDefinitionURI().equals(mapsTo.getRemote().getDefinitionURI())) {
					//throw new SBOLValidationException("MapsTo '" + mapsTo.getIdentity() + "' have non-identical local and remote Functional Component");
					throw new SBOLValidationException("sbol-10811", mapsTo);
				}
			}
		}
		addChildSafely(mapsTo, mapsTos, "mapsTo");
	}

	/**
	 * Removes the given mapsTo.
	 *
	 * @param mapsTo The mapsTo to be removed
	 * @return {@code true} if the matching mapsTo was removed successfully,
	 *         {@code false} otherwise.
	 *
	 */
	public boolean removeMapsTo(MapsTo mapsTo) {
		return removeChildSafely(mapsTo,mapsTos);
	}

	/**
	 * Returns the mapsTo that matches the given display ID.
	 *
	 * @param displayId the display ID of the mapsTo to be retrieved
	 * @return the mapsTo that matches the given display ID, or {@code null} otherwise
	 */
	public MapsTo getMapsTo(String displayId) {
		try {
			return mapsTos.get(createCompliantURI(this.getPersistentIdentity().toString(),displayId,this.getVersion()));
		}
		catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the mapsTo that matches the given URI.
	 *
	 * @param mapsToURI the URI for the mapsTo to be retrieved
	 * @return the matching mapsTo, or {@code null} otherwise
	 */
	public MapsTo getMapsTo(URI mapsToURI) {
		return mapsTos.get(mapsToURI);
	}

	/**
	 * Returns the set of mapsTos owned by this object.
	 *
	 * @return the set of mapsTos owned by this object.
	 */
	public Set<MapsTo> getMapsTos() {
		return new HashSet<>(mapsTos.values());
	}

	/**
	 * Removes all entries of this object's list of
	 * mapsTos. The list will be empty after this call returns.
	 *
	 */
	public void clearMapsTos() {
		Object[] valueSetArray = mapsTos.values().toArray();
		for (Object mapsTo : valueSetArray) {
			removeMapsTo((MapsTo)mapsTo);
		}
	}

	/**
	 * Clears the existing list of reference instances, then appends all of the elements in the specified collection to the end of this list.
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #addMapsTo(MapsTo)}.
	 */
	void setMapsTos(Set<MapsTo> mapsTos) throws SBOLValidationException {
		clearMapsTos();
		for (MapsTo reference : mapsTos) {
			addMapsTo(reference);
		}
	}

	void setModuleDefinition(ModuleDefinition moduleDefinition) {
		this.moduleDefinition = moduleDefinition;
	}

	@Override
	public String toString() {
		return "FunctionalComponent ["
				+ super.toString()
				+ ", direction=" + direction 
				+ "]";
	}
}
