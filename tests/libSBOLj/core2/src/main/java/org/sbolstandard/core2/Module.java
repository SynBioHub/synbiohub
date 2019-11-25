package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;
import static org.sbolstandard.core2.URIcompliance.extractDisplayId;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Module object in the SBOL data model.
 * 
 * @author Zhen Zhang
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */

public class Module extends Measured {

	private HashMap<URI, MapsTo> mapsTos;
	private URI definition;
	private ModuleDefinition moduleDefinition = null;

	/**
	 * @param identity
	 * @param moduleDefinition
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in either of the
	 * following constructor or method:
	 * <ul>
	 * <li>{@link Identified#Identified(URI)}, or</li>
	 * <li>{@link #setDefinition(URI)}.</li>
	 * </ul>
	 */
	Module(URI identity, URI moduleDefinition) throws SBOLValidationException {
		super(identity);
		this.mapsTos = new HashMap<>();
		setDefinition(moduleDefinition);
	}

	/**
	 * @param module
	 * @throws SBOLValidationException
	 */
	private Module(Module module) throws SBOLValidationException {
		super(module);
		this.mapsTos = new HashMap<>();
		this.setDefinition(module.getDefinitionURI());
		for (MapsTo mapping : module.getMapsTos()) {
			this.addMapsTo(mapping.deepCopy());
		}
	}
	
	void copy(Module module) throws SBOLValidationException {
		((Measured)this).copy((Measured)module);
		if (!module.getMapsTos().isEmpty()) {
			for (MapsTo mapsTo : module.getMapsTos()) {
				String displayId = URIcompliance.findDisplayId(mapsTo);
				String localDisplayId = URIcompliance.findDisplayId(mapsTo.getLocal());
				MapsTo newMapsTo = this.createMapsTo(displayId, mapsTo.getRefinement(), localDisplayId, 
						mapsTo.getRemoteURI());
				newMapsTo.copy(mapsTo);
			}
		}
	}

	/**
	 * Returns the module definition URI that this module refers to.
	 *
	 * @return the module definition URI that this module refers to
	 */
	public URI getDefinitionURI() {
		return definition;
	}
	
	/**
	 * Returns the module definition identity that this module refers to.
	 *
	 * @return the the module definition identity that this module refers to
	 */
	public URI getDefinitionIdentity() {
		if (this.getSBOLDocument()==null) return null;
		if (this.getSBOLDocument().getModuleDefinition(definition)==null) return null;
		return this.getSBOLDocument().getModuleDefinition(definition).getIdentity();
	}

	/**
	 * Returns the module definition that this module refers to.
	 *
	 * @return the the module definition that this module refers to
	 */
	public ModuleDefinition getDefinition() {
		if (this.getSBOLDocument()==null) return null;
		return this.getSBOLDocument().getModuleDefinition(definition);
	}

	/**
	 * Sets the reference definition URI to this module.
	 * <p>
	 * This method creates a compliant local and a compliant remote URIs.
	 * They are created with this Module object's persistent ID,
	 * the given {@code localId} or {@code remoteId}, and this Module object's version.
	 * It then calls {@link #createMapsTo(String, RefinementType, URI, URI)} to create
	 * a MapsTo instance.
	 *
	 * @param definitionURI The definition URI for this Module.
	 * @throws SBOLValidationException if either of the following SBOL validation rule was violated: 11702, 11703, 11704, 11705.
	 */
	public void setDefinition(URI definitionURI) throws SBOLValidationException {
		if (definitionURI==null) {
			throw new SBOLValidationException("sbol-11702",this);
		}
		if (this.getSBOLDocument() != null) {
			if (this.getSBOLDocument().isComplete()) {
				if (this.getSBOLDocument().getModuleDefinition(definitionURI)==null) {
					throw new SBOLValidationException("sbol-11703",this);
				}
			}
			if (moduleDefinition!=null) {
				ModuleDefinition md = this.getSBOLDocument().getModuleDefinition(definitionURI);
				if (md!=null &&	moduleDefinition.getIdentity().equals(md.getIdentity())) {
					throw new SBOLValidationException("sbol-11704", this);
				}
				Set<URI> visited = new HashSet<>();
				visited.add(moduleDefinition.getIdentity());
				try { 
					SBOLValidate.checkModuleDefinitionCycle(this.getSBOLDocument(), md, visited);
				} catch (SBOLValidationException e) {
					throw new SBOLValidationException("sbol-11705", this);
				}
			}
		}
		this.definition = definitionURI;
	}


	//	/**
	//	 * Test if optional field variable <code>references</code> is set.
	//	 * @return <code>true</code> if it is not an empty list
	//	 */
	//	public boolean isSetMappings() {
	//		if (mapsTos.isEmpty())
	//			return false;
	//		else
	//			return true;
	//	}

	/**
	 * Calls the MapsTo constructor to create a new instance using the specified parameters,
	 * then adds to the list of MapsTo instances owned by this component.
	 *
	 * @param identity the identity URI for the mapsTo to be created
	 * @param refinement the relationship between the local and remote components
	 * @param local the identity URI of the local functional component
	 * @param remote the identity URI of the remote functional component
	 * @return the created mapsTo
 	 * @throws SBOLValidationException if either of the following conditions is satisfied:
 	 * <ul>
	 * <li>an SBOL validation exception occurred in {@link MapsTo#MapsTo(URI, RefinementType, URI, URI)};</li>
	 * <li>an SBOL validation exception occurred in {@link #addMapsTo(MapsTo)};</li>
	 * </ul>
	 */
	MapsTo createMapsTo(URI identity, RefinementType refinement,
			URI local, URI remote) throws SBOLValidationException {
		MapsTo mapping = new MapsTo(identity, refinement, local, remote);
		addMapsTo(mapping);
		return mapping;
	}

	/**
	 * Creates a child mapsTo for this module with the given arguments, and then adds it to its list of mapsTos.
	 * <p>
	 * This method creates compliant local and remote URIs first.
	 * The compliant local URI is created with this module's persistent identity URI, followed by
	 * the given local module's display ID, followed by this module's version. 
	 * The compliant remote URI is created following the same pattern.
	 * It then calls {@link #createMapsTo(String, RefinementType, URI, URI)} with the given mapsTo's display ID, its refinement type,
	 * and the created compliant local and remote functional components' URIs.
	 * <p>
	 * This method calls {@link ModuleDefinition#createFunctionalComponent(String, AccessType, String, String, DirectionType)}
	 * to automatically create a local functional component with the given display ID of referenced local component definition, 
	 * {@link AccessType#PUBLIC}, an empty version string, and {@link DirectionType#INOUT}, if all of the following conditions are satisfied:
	 * <ul>
	 * <li>the associated SBOLDocument instance for this module is not {@code null};</li>
	 * <li>if default functional components should be automatically created when not present in the associated SBOLDocument instance,
	 * i.e., {@link SBOLDocument#isCreateDefaults} returns {@code true};</li>
	 * <li>if this module's parent ModuleDefinition instance exists; and</li>
	 * <li>if this module's parent ModuleDefinition instance does not already have a functional component
	 * with the created compliant local URI.</li> 
	 * </ul>
	 * This automatically created created functional component has the same display ID as its referenced component definition. 
	 *
	 * @param displayId the display ID of the mapsTo to be created 
	 * @param refinement the relationship between the local and remote components
	 * @param localId the display ID of the local functional component
	 * @param remoteId the display ID of the remote functional component
	 * @return the created mapsTo 
	 * @throws SBOLValidationException if any of the following condition is satisfied:
	 * <ul>
	 * <li>if either of the following SBOL validation rules was violated: 10204, 10206;</li>
	 * <li>an SBOL validation exception occurred in {@link ModuleDefinition#createFunctionalComponent(String, AccessType, String, String, DirectionType)}; or</li>
	 * <li>an SBOL validation exception occurred in {@link #createMapsTo(String, RefinementType, URI, URI)}.</li>
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
	 * Creates a child mapsTo for this module with the given arguments, and then adds it to its list of mapsTos.
	 * <p>
	 * This method creates a compliant URI for the mapsTo to be created. It starts with this module's persistent
	 * identity, followed by the mapsTo's display ID, and ends with this module's version.
	 * 
	 * @param displayId the display ID of the mapsTo to be created
	 * @param refinement the relationship between the local and remote components
	 * @param local the identity URI of the local functional component
	 * @param remote the identity URI of the remote functional component
	 * @return the created mapsTo
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated: 
 	 * 10201, 10202, 10204, 10206, 10802, 10803, 10804, 10805, 10807, 10808, 10809, 10811, 11609.
	 */
	public MapsTo createMapsTo(String displayId, RefinementType refinement, URI local, URI remote) throws SBOLValidationException {
		String parentPersistentIdStr = this.getPersistentIdentity().toString();
		String version = this.getVersion();
		URI newMapsToURI = createCompliantURI(parentPersistentIdStr, displayId, version);
		MapsTo m = createMapsTo(newMapsToURI, refinement, local, remote);
		m.setPersistentIdentity(createCompliantURI(parentPersistentIdStr, displayId, ""));
		m.setDisplayId(displayId);
		m.setVersion(version);
		return m;
	}

	/**
	 * Adds the specified instance to the list of references.
	 * @throws SBOLValidationException if any of the following conditions is satisfied:
	 * <ul> 
	 * <li>any of the following SBOL validation rules was violated: 10804, 10807, 10809, 10811;</li>
	 * <li>an SBOL validation exception occurred in {@link SBOLValidate#checkModuleDefinitionMapsTos(ModuleDefinition, MapsTo)}; or</li>
	 * <li>an SBOL validation exception occurred in {@link Identified#addChildSafely(Identified, java.util.Map, String, java.util.Map...)}.</li>
	 * </ul>
	 */
	private void addMapsTo(MapsTo mapsTo) throws SBOLValidationException {
		mapsTo.setSBOLDocument(this.getSBOLDocument());
		mapsTo.setModuleDefinition(moduleDefinition);
		mapsTo.setModule(this);
		if (this.getSBOLDocument() != null) {
			if (mapsTo.getLocal()==null) {
				//throw new SBOLValidationException("Functional component '" + mapsTo.getLocalURI() + "' does not exist.");
				throw new SBOLValidationException("sbol-10804", mapsTo);
			}
		}
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (mapsTo.getRemote()==null) {
				//throw new SBOLValidationException("Functional component '" + mapsTo.getRemoteURI() + "' does not exist.");
				throw new SBOLValidationException("sbol-10809", mapsTo);
			}
			if (mapsTo.getRemote().getAccess().equals(AccessType.PRIVATE)) {
				//throw new SBOLValidationException("Functional Component '" + mapsTo.getRemoteURI() + "' is private.");
				throw new SBOLValidationException("sbol-10807", mapsTo);
			}
			if (mapsTo.getRefinement().equals(RefinementType.VERIFYIDENTICAL)) {
				if (!mapsTo.getLocal().getDefinitionURI().equals(mapsTo.getRemote().getDefinitionURI())) {
					//throw new SBOLValidationException("MapsTo '" + mapsTo.getIdentity() + "' have non-identical local and remote Functional Component");
					throw new SBOLValidationException("sbol-10811", mapsTo);
				}
			}
		}
		if (moduleDefinition!=null) {
			SBOLValidate.checkModuleDefinitionMapsTos(moduleDefinition, mapsTo);
		}
		addChildSafely(mapsTo, mapsTos, "mapsTo");
	}

	/**
	 * Removes the given mapsTo from the list of mapsTos owned by this module.
	 *
	 * @param mapsTo the mapsTo to be removed
	 * @return {@code true} if the matching mapsTo is removed successfully, {@code false} otherwise
	 */
	public boolean removeMapsTo(MapsTo mapsTo) {
		return removeChildSafely(mapsTo,mapsTos);
	}

 	/**
	 * Returns the mapsTo that matches the given display ID.
	 * <p>
	 * This method first creates a compliant URI for the mapsTo to be retrieved. It starts with this module's 
	 * persistent identity, followed by the given display ID, and ends with this module's version.
	 *
	 * @param displayId the display ID of the mapsTo to be retrieved
	 * @return the matching mapsTo
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
	 * Returns the MapsTo instance owned by this Module object that matches the given {@code referenceURI}
	 *
	 * @param referenceURI The MapsTo URI object
	 * @return the matching MapsTo instance URI
	 */
	public MapsTo getMapsTo(URI referenceURI) {
		return mapsTos.get(referenceURI);
	}

	/**
	 * Returns the set of MapsTo instances referenced by this Module object.
	 *
	 * @return the set of MapsTo instances referenced by this Module object.
	 */
	public Set<MapsTo> getMapsTos() {
		return new HashSet<>(mapsTos.values());
	}

	/**
	 * Removes all entries of this module's list of mapsTos.
	 * The set will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeMapsTo(MapsTo)} iteratively to remove each mapsTo at a time.
	 */
	public void clearMapsTos() {
		Object[] valueSetArray = mapsTos.values().toArray();
		for (Object mapsTo : valueSetArray) {
			removeMapsTo((MapsTo)mapsTo);
		}
	}

	/**
	 * Clears the existing list of mapsTos first, and then adds the given set to this module.
	 * 
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #addMapsTo(MapsTo)}. 
	 */
	void setMapsTos(Set<MapsTo> mappings) throws SBOLValidationException {
		clearMapsTos();
		for (MapsTo mapping : mappings) {
			addMapsTo(mapping);
		}
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((definition == null) ? 0 : definition.hashCode());
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
		Module other = (Module) obj;
		if (definition == null) {
			if (other.definition != null)
				return false;
		} else if (!definition.equals(other.definition)) {
			if (getDefinitionIdentity() == null || other.getDefinitionIdentity() == null 
					|| !getDefinitionIdentity().equals(other.getDefinitionIdentity())) {
				return false;
			}
		}
		if (mapsTos == null) {
			if (other.mapsTos != null)
				return false;
		} else if (!mapsTos.equals(other.mapsTos))
			return false;
		return true;
	}


	@Override
	Module deepCopy() throws SBOLValidationException {
		return new Module(this);
	}

	/**
	 * Updates this module's and each of its child mapsTo's identity URIs with compliant URIs.
	 *  
  	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
 	 * <ul> 
	 * <li>{@link URIcompliance#createCompliantURI(String, String, String)};</li>
	 * <li>{@link #setWasDerivedFrom(URI)};</li>
	 * <li>{@link #setIdentity(URI)};</li>
	 * <li>{@link #setDisplayId(String)};</li>
	 * <li>{@link #setVersion(String)};</li>
	 * <li>{@link MapsTo#updateCompliantURI(String, String, String)};</li>
	 * <li>{@link #addMapsTo(MapsTo)};</li>
	 * <li>{@link MapsTo#setLocal(URI)};</li>
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

	ModuleDefinition getModuleDefinition() {
		return moduleDefinition;
	}

	void setModuleDefinition(ModuleDefinition moduleDefinition) {
		this.moduleDefinition = moduleDefinition;
	}

	@Override
	public String toString() {
		return "Module ["
				+ super.toString()
				+ ", definition=" + definition 
				+ (this.getMapsTos().size()>0?", mapsTos=" + this.getMapsTos():"") 
				+ "]";
	}

}

