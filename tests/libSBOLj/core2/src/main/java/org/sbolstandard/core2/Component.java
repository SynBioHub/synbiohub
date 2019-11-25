package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;
import static org.sbolstandard.core2.URIcompliance.extractDisplayId;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a Component object in the SBOL data model.
 * 
 * @author Zhen Zhang
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */

public class Component extends ComponentInstance{

	private HashMap<URI, MapsTo> mapsTos;
	private Set<URI> roles;
	private RoleIntegrationType roleIntegration;
	private HashMap<URI, Location> locations;
	private HashMap<URI, Location> sourceLocations;
	/**
	 * Parent component definition of this component
	 */
	private ComponentDefinition componentDefinition = null;

	/**
	 * @param identity
	 * @param access
	 * @param definition the referenced component definition
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred 
	 * in {@link ComponentInstance#ComponentInstance(URI, AccessType, URI)}
	 */
	Component(URI identity, AccessType access, URI definition) throws SBOLValidationException {
		super(identity, access, definition);
		this.mapsTos = new HashMap<>();
		this.roles = new HashSet<>();
		this.locations = new HashMap<>();
		this.sourceLocations = new HashMap<>();
	}

	/**
	 * 
	 * @param component
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of 
	 * the following constructors or methods:
	 * <ul>
	 * <li>{@link ComponentInstance#ComponentInstance(ComponentInstance)},</li>
	 * <li>{@link MapsTo#deepCopy()}, or</li>
	 * <li>{@link #setMapsTos(Set)}.</li>
	 * </ul>
	 */
	private Component(Component component) throws SBOLValidationException {
		super(component);
		this.roles = new HashSet<>();
		this.mapsTos = new HashMap<>();
		if (!component.getMapsTos().isEmpty()) {
			Set<MapsTo> mapsTos = new HashSet<>();
			for (MapsTo mapsTo : component.getMapsTos()) {
				mapsTos.add(mapsTo.deepCopy());
			}
			this.setMapsTos(mapsTos);
		}
		this.roles = new HashSet<>();
		for (URI role : component.getRoles()) {
			this.addRole(URI.create(role.toString()));
		}
		this.locations = new HashMap<>();
		for (Location location : component.getLocations()) {
			addLocation(location.deepCopy());
		}
		this.sourceLocations = new HashMap<>();
		for (Location location : component.getSourceLocations()) {
			addSourceLocation(location.deepCopy());
		}
	}
	
	void copy(Component component) throws SBOLValidationException {
		((ComponentInstance)this).copy((ComponentInstance)component);
		this.mapsTos = new HashMap<>();
		// TODO: moved up a level since need to copy components before mapstos
//		if (!component.getMapsTos().isEmpty()) {
//			for (MapsTo mapsTo : component.getMapsTos()) {
//				String displayId = URIcompliance.findDisplayId(mapsTo);
//				String localDisplayId = URIcompliance.findDisplayId(mapsTo.getLocal());
//				MapsTo newMapsTo = this.createMapsTo(displayId, mapsTo.getRefinement(), localDisplayId, 
//						mapsTo.getRemoteURI());
//				newMapsTo.copy(mapsTo);
//			}
//		}
		this.setRoleIntegration(component.getRoleIntegration());
		for (URI role : component.getRoles()) {
			this.addRole(URI.create(role.toString()));
		}
		for (Location location : component.getLocations()) {
			String displayId = URIcompliance.findDisplayId(location);
			if (location instanceof Range) {
				Range range = (Range)location;
				Range newRange;
				if (range.isSetOrientation()) {
					newRange = this.addRange(displayId, range.getStart(), range.getEnd(), 
							range.getOrientation());
				} else {
					newRange = this.addRange(displayId, range.getStart(), range.getEnd());
				}
				if (range.isSetSequence()) {
					newRange.setSequence(range.getSequenceURI());
				}
				newRange.copy(range);
			} else if (location instanceof Cut) {
				Cut cut = (Cut)location;
				Cut newCut;
				if (cut.isSetOrientation()) {
					newCut = this.addCut(displayId, cut.getAt(), cut.getOrientation());
				} else {
					newCut = this.addCut(displayId, cut.getAt());
				}
				if (cut.isSetSequence()) {
					newCut.setSequence(cut.getSequenceURI());
				}
				newCut.copy(cut);
			} else if (location instanceof GenericLocation) {
				GenericLocation genericLocation = (GenericLocation)location;
				GenericLocation newGenericLocation;
				if (genericLocation.isSetOrientation()) {
					newGenericLocation = this.addGenericLocation(displayId,
							genericLocation.getOrientation());
				} else {
					newGenericLocation = this.addGenericLocation(displayId);
				}
				if (genericLocation.isSetSequence()) {
					newGenericLocation.setSequence(genericLocation.getSequenceURI());
				}
				newGenericLocation.copy(genericLocation);
			}
		}
		Location location = this.getLocation("DUMMY__LOCATION");
		if (location!=null) {
			this.removeLocation(location);
		}
		for (Location sourceLocation : component.getSourceLocations()) {
			String displayId = URIcompliance.findDisplayId(sourceLocation);
			if (sourceLocation instanceof Range) {
				Range range = (Range)sourceLocation;
				Range newRange;
				if (range.isSetOrientation()) {
					newRange = this.addSourceRange(displayId, range.getStart(), range.getEnd(), 
							range.getOrientation());
				} else {
					newRange = this.addSourceRange(displayId, range.getStart(), range.getEnd());
				}
				if (range.isSetSequence()) {
					newRange.setSequence(range.getSequenceURI());
				}
				newRange.copy(range);
			} else if (sourceLocation instanceof Cut) {
				Cut cut = (Cut)sourceLocation;
				Cut newCut;
				if (cut.isSetOrientation()) {
					newCut = this.addSourceCut(displayId, cut.getAt(), cut.getOrientation());
				} else {
					newCut = this.addSourceCut(displayId, cut.getAt());
				}
				if (cut.isSetSequence()) {
					newCut.setSequence(cut.getSequenceURI());
				}
				newCut.copy(cut);
			} else if (sourceLocation instanceof GenericLocation) {
				GenericLocation genericLocation = (GenericLocation)sourceLocation;
				GenericLocation newGenericLocation;
				if (genericLocation.isSetOrientation()) {
					newGenericLocation = this.addGenericSourceLocation(displayId,
							genericLocation.getOrientation());
				} else {
					newGenericLocation = this.addGenericSourceLocation(displayId);
				}
				if (genericLocation.isSetSequence()) {
					newGenericLocation.setSequence(genericLocation.getSequenceURI());
				}
				newGenericLocation.copy(genericLocation);
			}
		}
		Location sourceLocation = this.getLocation("DUMMY__LOCATION");
		if (sourceLocation!=null) {
			this.removeLocation(sourceLocation);
		}
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.ComponentInstance#deepCopy()
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #Component(Component)}.
	 */
	@Override
	Component deepCopy() throws SBOLValidationException {
		return new Component(this);
	}

	/**
	 * Updates this component's and each of its mapsTo's identity URIs with compliant URIs. 
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
		int count = 0;
		for (Location location : this.getLocations()) {
			if (!location.isSetDisplayId()) location.setDisplayId("location"+ ++count);
			location.updateCompliantURI(this.getPersistentIdentity().toString(),location.getDisplayId(),version);
			this.removeChildSafely(location, this.locations);
			this.addLocation(location);
		}
		count = 0;
		for (Location sourceLocation : this.getSourceLocations()) {
			if (!sourceLocation.isSetDisplayId()) sourceLocation.setDisplayId("sourceLocation"+ ++count);
			sourceLocation.updateCompliantURI(this.getPersistentIdentity().toString(),sourceLocation.getDisplayId(),version);
			this.removeChildSafely(sourceLocation, this.sourceLocations);
			this.addSourceLocation(sourceLocation);
		}
	}
	
	/**
	 * Adds the given role URI to this component's set of role URIs.
	 *
	 * @param roleURI the role URI to be added
	 * @return {@code true} if this set did not already contain the specified role, {@code false} otherwise.
	 * @throws SBOLValidationException if RoleIntegration is not set (10709).
	 */
	public boolean addRole(URI roleURI) throws SBOLValidationException {
		if (!isSetRoleIntegration()) {
			throw new SBOLValidationException("sbol-10709", this);
		}
		return roles.add(roleURI);
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
	 * Clears the existing set of roles first, and then adds the given
	 * set of the roles to this component.
	 *
	 * @param roles the set of roles to be set
	 * @throws SBOLValidationException if RoleIntegration is not set (10709).
	 */
	public void setRoles(Set<URI> roles) throws SBOLValidationException {
		clearRoles();
		if (roles==null) return;
		for (URI role : roles) {
			addRole(role);
		}
	}

	/**
	 * Returns the set of role URIs owned by this component. 
	 *
	 * @return the set of role URIs owned by this component.
	 */
	public Set<URI> getRoles() {
		Set<URI> result = new HashSet<>();
		result.addAll(roles);
		return result;
	}

	/**
	 * Checks if the given role URI is included in this component's set of role URIs.
	 *
	 * @param roleURI the role URI to be checked
	 * @return {@code true} if this set contains the given role URI, {@code false} otherwise.
	 */
	public boolean containsRole(URI roleURI) {
		return roles.contains(roleURI);
	}

	/**
	 * Removes all entries of this component's set of role URIs.
	 * The set will be empty after this call returns.	 
	 */
	public void clearRoles() {
		roles.clear();
	}
	
	/**
	 * Test if the roleIntegration property is set.
	 * @return {@code true} if it is not {@code null}
	 */
	public boolean isSetRoleIntegration() {
		return roleIntegration != null;
	}

	/**
	 * Returns the roleIntegration property of this object.
	 * @return the roleIntegration property of this object.
	 */
	public RoleIntegrationType getRoleIntegration() {
		return this.roleIntegration;
	}

	/**
	 * Sets the roleIntegration property of this object to the given one.
	 *
	 * @param roleIntegration indicates how role is to be integrated with related roles.
	 * @throws SBOLValidationException if there are roles on this Component (10709).
	 */
	public void setRoleIntegration(RoleIntegrationType roleIntegration) throws SBOLValidationException {
		if (roleIntegration==null && !roles.isEmpty()) {
			throw new SBOLValidationException("sbol-10709", this);
		}
		this.roleIntegration = roleIntegration;
	}

	/**
	 * Sets the roleIntegration property of this object to {@code null}.
	 * @throws SBOLValidationException if there are roles on this Component (10709).
	 *
	 */
	public void unsetRoleIntegration() throws SBOLValidationException {
		if (!roles.isEmpty()) {
			throw new SBOLValidationException("sbol-10709", this);
		}
		roleIntegration = null;
	}

	/**
	 * Calls the MapsTo constructor to create a new instance using the specified parameters,
	 * then adds to the list of mapsTos owned by this component.
	 *
	 * @return the created mapsTo.
 	 * @throws SBOLValidationException if either of the following condition is satisfied:
  	 * <ul>
	 * <li>an SBOL validation exception occurred in {@link MapsTo#MapsTo(URI, RefinementType, URI, URI)};</li>
	 * <li>an SBOL validation exception occurred in {@link #addMapsTo(MapsTo)};</li>
	 * </ul>
	 */
	private MapsTo createMapsTo(URI identity, RefinementType refinement, URI local, URI remote) throws SBOLValidationException {
		MapsTo mapping = new MapsTo(identity, refinement, local, remote);
		addMapsTo(mapping);
		return mapping;
	}

	/**
	 * Creates a child mapsTo for this component with the given arguments, and then adds it to its list of mapsTos.
	 * <p>
	 * This method creates compliant local and remote URIs first.
	 * The compliant local URI is created with this component's persistent identity URI, followed by
	 * the given local component's display ID, followed by this component's version. 
	 * The compliant remote URI is created following the same pattern.
	 * It then calls {@link #createMapsTo(String, RefinementType, URI, URI)} with the given mapsTo's display ID, its refinement type,
	 * and the created compliant local and remote components' URIs.
	 * <p>
	 * This method calls {@link ComponentDefinition#createComponent(String, AccessType, String, String)} to automatically 
	 * create a local component with the given display ID of referenced local component definition, 
	 * {@link AccessType#PUBLIC}, and an empty version string, if all of the following conditions are satisfied:
	 * <ul>
	 * <li>the associated SBOLDocument instance for this component is not {@code null};</li>
	 * <li>if default components should be automatically created when not present in the associated SBOLDocument instance,
	 * i.e., {@link SBOLDocument#isCreateDefaults} returns {@code true};</li>
	 * <li>if this component's parent ComponentDefinition instance exists; and</li>
	 * <li>if this component's parent ComponentDefinition instance does not already have a component
	 * with the created compliant local URI.</li> 
	 * </ul>
	 * This automatically created created component has the same display ID as its referenced component definition.
	 * 
	 * @param displayId the display ID of the mapsTo to be created 
	 * @param refinement the relationship between the local and remote components
	 * @param localId the display ID of the local component
	 * @param remoteId the display ID of the remote component
	 * @return the created mapsTo 
	 * @throws SBOLValidationException if any of the following condition is satisfied:
	 * <ul>
	 * <li>if either of the following SBOL validation rules was violated: 10204, 10206;</li>
	 * <li>an SBOL validation exception occurred in {@link ComponentDefinition#createComponent(String, AccessType, String, String)}; or</li>
	 * <li>an SBOL validation exception occurred in {@link #createMapsTo(String, RefinementType, URI, URI)}.</li>
	 * </ul>
	 */
	public MapsTo createMapsTo(String displayId, RefinementType refinement, String localId, String remoteId) throws SBOLValidationException {
		URI localURI = URIcompliance.createCompliantURI(componentDefinition.getPersistentIdentity().toString(),
				localId, componentDefinition.getVersion());
		if (this.getSBOLDocument() !=null && this.getSBOLDocument().isCreateDefaults() && componentDefinition!=null &&
				componentDefinition.getComponent(localURI)==null) {
			componentDefinition.createComponent(localId,AccessType.PUBLIC,localId,"");
		}
		URI remoteURI = URIcompliance.createCompliantURI(getDefinition().getPersistentIdentity().toString(),
				remoteId, getDefinition().getVersion());
		return createMapsTo(displayId,refinement,localURI,remoteURI);
	}
	
	MapsTo createMapsTo(String displayId, RefinementType refinement, String localId, URI remoteURI) throws SBOLValidationException {
		URI localURI = URIcompliance.createCompliantURI(componentDefinition.getPersistentIdentity().toString(),
				localId, componentDefinition.getVersion());
		if (this.getSBOLDocument() !=null && this.getSBOLDocument().isCreateDefaults() && componentDefinition!=null &&
				componentDefinition.getComponent(localURI)==null) {
			componentDefinition.createComponent(localId,AccessType.PUBLIC,localId,"");
		}
		return createMapsTo(displayId,refinement,localURI,remoteURI);
	}

	/**
	 * Creates a child mapsTo for this component with the given arguments,
	 * and then adds it to its list of mapsTos.
	 * <p>
	 * This method creates a compliant URI for the mapsTo to be created. It starts with this component's persistent
	 * identity, followed by the mapsTo's display ID, and ends with this component's version.
	 * 
	 * @param displayId the display ID of the mapsTo to be created
	 * @param refinement the relationship between the local and remote components
	 * @param local the identity URI of the local component
	 * @param remote the identity URI of the remote component
	 * @return the created mapsTo 
 	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated: 
 	 * 10201, 10202, 10204, 10206, 10526, 10802, 10803, 10804, 10805, 10807, 10808, 10809, 10811.
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
	 * @throws SBOLValidationException if any of the following conditions is satisfied:
	 * <ul> 
	 * <li>any of the following SBOL validation rules was violated: 10803, 10807, 10808, 10811;</li>
	 * <li>an SBOL validation exception occurred in {@link SBOLValidate#checkComponentDefinitionMapsTos(ComponentDefinition, MapsTo)}; or</li>
	 * <li>an SBOL validation exception occurred in {@link Identified#addChildSafely(Identified, java.util.Map, String, java.util.Map...)}.</li>
	 * </ul>
	 */
	private void addMapsTo(MapsTo mapsTo) throws SBOLValidationException {
		mapsTo.setSBOLDocument(this.getSBOLDocument());
		mapsTo.setComponentDefinition(componentDefinition);
		mapsTo.setComponentInstance(this);
		if (this.getSBOLDocument() != null) {
			if (componentDefinition.getComponent(mapsTo.getLocalURI())==null) {
				//throw new SBOLValidationException("Component '" + mapsTo.getLocalURI() + "' does not exist.");
				throw new SBOLValidationException("sbol-10803", mapsTo);
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
		if (componentDefinition!=null) {
			SBOLValidate.checkComponentDefinitionMapsTos(componentDefinition, mapsTo);
		}
		addChildSafely(mapsTo, mapsTos, "mapsTo");
	}

	/**
	 * Removes the given mapsTo from the list of
	 * mapsTos.
	 *
	 * @param mapsTo a mapsTo to be removed
	 * @return {@code true} if the matching mapsTo is removed successfully,
	 *         {@code false} otherwise.
	 */
	public boolean removeMapsTo(MapsTo mapsTo) {
		return removeChildSafely(mapsTo,mapsTos);
	}

	/**
	 * Returns the mapsTo that matches the given display ID.
	 *› <p>
	 * This method first creates a compliant URI for the mapsTo to be retrieved. It starts with this component's 
	 * persistent identity, followed by the given display ID, and ends with this component's version.
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
	 * Returns the mapsTo that matches the given URI.
	 *
	 * @param mapsToURI The URI of the mapsTo to be retrieved
	 * @return the matching mapsTo
	 */
	public MapsTo getMapsTo(URI mapsToURI) {
		return mapsTos.get(mapsToURI);
	}

	/**
	 * Returns the set of mapsTos owned by this instance.
	 *
	 * @return the set of mapsTos owned by this instance.
	 */
	public Set<MapsTo> getMapsTos() {
		return new HashSet<>(mapsTos.values());
	}

	/**
	 * Removes all entries the list of mapsTos. The list will be empty after this call returns.
	 */
	public void clearMapsTos() {
		Object[] valueSetArray = mapsTos.values().toArray();
		for (Object mapsTo : valueSetArray) {
			removeMapsTo((MapsTo)mapsTo);
		}
	}

	/**
	 * Clears the existing list of reference instances, then appends all of the elements in the specified collection to the end of this list.
	 * 
 	 * @throws SBOLValidationException if any of the following is true:
 	 * <ul> 
	 * <li>any of the following SBOL validation rules was violated: 10803, 10807, 10808, 10811;</li>
	 * <li>an SBOL validation exception occurred in {@link SBOLValidate#checkComponentDefinitionMapsTos(ComponentDefinition, MapsTo)};</li>
	 * <li>an SBOL validation exception occurred in {@link Identified#addChildSafely(Identified, java.util.Map, String, java.util.Map...)};</li>
	 * </ul>
	 */
	void setMapsTos(Set<MapsTo> mapsTos) throws SBOLValidationException {
		clearMapsTos();
		for (MapsTo reference : mapsTos) {
			addMapsTo(reference);
		}
	} 

/*	ComponentDefinition getComponentDefinition() {
		return componentDefinition;
	}
*/

	void setComponentDefinition(ComponentDefinition componentDefinition) {
		this.componentDefinition = componentDefinition;
	}
	
	/**
	 * Sets the definition property to the given one.
	 *
	 * @param definition the given definition URI to set to 
	 * @throws SBOLValidationException if either of the following SBOL validation rules was violated: 10604, 10605.
	 */
	public void setDefinition(URI definition) throws SBOLValidationException {
		if (this.getSBOLDocument() != null) {
			ComponentDefinition cd = this.getSBOLDocument().getComponentDefinition(definition);
			if (this.getSBOLDocument().isComplete()) {
				if (cd==null) {
					throw new SBOLValidationException("sbol-10604",this);
				}
			}
			if (componentDefinition!=null) {
				if (cd!=null &&	componentDefinition.getIdentity().equals(cd.getIdentity())) {
					throw new SBOLValidationException("sbol-10605", this);
				}
				Set<URI> visited = new HashSet<>();
				visited.add(componentDefinition.getIdentity());
				try {
					SBOLValidate.checkComponentDefinitionCycle(this.getSBOLDocument(), cd, visited);
				} catch (SBOLValidationException e) {
					throw new SBOLValidationException("sbol-10605",this);
				}
			}
		}
		super.setDefinition(definition);
	}
	
	/**
	 * Creates a generic location with the given arguments and then adds it to this components's
	 * list of source locations.
	 * <p>
	 * This method first creates a compliant URI for the generic location to be created. 
	 * It starts with this component's persistent identity URI, 
	 * followed by the given display ID, and ends an empty string for version.
	 *  
	 * @param displayId the display ID for the generic location to be created
	 * @return the created generic location instance
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10201, 10202, 10204, 10206.
	 */
	public GenericLocation addGenericSourceLocation(String displayId) throws SBOLValidationException {
		URI identity = createCompliantURI(this.getPersistentIdentity().toString(),displayId,this.getVersion());
		GenericLocation genericLocation = new GenericLocation(identity);
		genericLocation.setPersistentIdentity(createCompliantURI(this.getPersistentIdentity().toString(),displayId,""));
		genericLocation.setDisplayId(displayId);
		genericLocation.setVersion(this.getVersion());
		addSourceLocation(genericLocation);
		return genericLocation;
	}
	
	/**
	 * Creates a generic location with the given arguments and then adds it to this component's
	 * list of source locations.
	 * <p>
	 * This method first creates a compliant URI for the generic location to be created. 
	 * It starts with this component's persistent identity URI, 
	 * followed by the given display ID, and ends an empty string for version.
	 *  
	 * @param displayId the display ID for the generic location to be created
	 * @param orientation the orientation type
	 * @return the created generic location instance
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10201, 10202, 10204, 10206. 
 	 */
	public GenericLocation addGenericSourceLocation(String displayId,OrientationType orientation) throws SBOLValidationException {
		URI identity = createCompliantURI(this.getPersistentIdentity().toString(),displayId,this.getVersion());
		GenericLocation genericLocation = new GenericLocation(identity);
		genericLocation.setPersistentIdentity(createCompliantURI(this.getPersistentIdentity().toString(),displayId,""));
		genericLocation.setDisplayId(displayId);
		genericLocation.setVersion(this.getVersion());
		genericLocation.setOrientation(orientation);
		addSourceLocation(genericLocation);
		return genericLocation;
	}
	
	/**
	 * Creates a cut with the given arguments and then adds it to this component's
	 * list of source locations.
	 * <p>
	 * This method first creates a compliant URI for the cut to be created. 
	 * It starts with this component's persistent identity URI, 
	 * followed by the given display ID, and ends an empty string for version.
	 *  
	 * @param displayId the display ID for the cut to be created
	 * @param at the at property for the cut to be created
	 * @return the created cut
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10201, 10202, 10204, 10206, 11202.
	 */
	public Cut addSourceCut(String displayId,int at) throws SBOLValidationException {
		URI identity = createCompliantURI(this.getPersistentIdentity().toString(),displayId,this.getVersion());
		Cut cut = new Cut(identity,at);
		cut.setPersistentIdentity(createCompliantURI(this.getPersistentIdentity().toString(),displayId,""));
		cut.setDisplayId(displayId);
		cut.setVersion(this.getVersion());
		addSourceLocation(cut);
		return cut;
	}
	
	/**
	 * Creates a cut with the given arguments and then adds it to this component's
	 * list of source locations.
	 * <p>
	 * This method first creates a compliant URI for the cut to be created. 
	 * It starts with this component's persistent identity URI, 
	 * followed by the given display ID, and ends an empty string for version.
	 *  
	 * @param displayId the display ID for the cut to be created
	 * @param at the at property for the cut to be created
	 * @param orientation the orientation type
	 * @return the created cut
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10201, 10202, 10204, 10206, 11202. 
	 */
	public Cut addSourceCut(String displayId,int at,OrientationType orientation) throws SBOLValidationException {
		URI identity = createCompliantURI(this.getPersistentIdentity().toString(),displayId,this.getVersion());
		Cut cut = new Cut(identity,at);
		cut.setPersistentIdentity(createCompliantURI(this.getPersistentIdentity().toString(),displayId,""));
		cut.setDisplayId(displayId);
		cut.setVersion(this.getVersion());
		cut.setOrientation(orientation);
		addSourceLocation(cut);
		return cut;
	}

	/**
	 * Creates a range with the given arguments and then adds it to this component's
	 * list of source locations.
	 * <p>
	 * This method first creates a compliant URI for the range to be created. 
	 * It starts with this component's persistent identity URI, 
	 * followed by the given display ID, and ends an empty string for version.
	 *  
	 * @param displayId the display ID for the range to be created
 	 * @param start the start index for the range to be created
	 * @param end the end index for the range to be created
	 * @return the created range
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10201, 10202, 10204, 10206, 11102, 11103, 11104. 
	 */
	public Range addSourceRange(String displayId,int start,int end) throws SBOLValidationException {
		URI identity = createCompliantURI(this.getPersistentIdentity().toString(),displayId,this.getVersion());
		Range range = new Range(identity,start,end);
		range.setPersistentIdentity(createCompliantURI(this.getPersistentIdentity().toString(),displayId,""));
		range.setDisplayId(displayId);
		range.setVersion(this.getVersion());
		addSourceLocation(range);
		return range;
	}
	
	/**
	 * Creates a range with the given arguments and then adds it to this component's
	 * list of source locations.
	 * <p>
	 * This method first creates a compliant URI for the range to be created. 
	 * It starts with this component's persistent identity URI, 
	 * followed by the given display ID, and ends an empty string for version.
	 * 
	 * @param displayId the display ID for the range to be created
 	 * @param start the start index for the range to be created
	 * @param end the end index for the range to be created
	 * @param orientation the orientation type
	 * @return the created range
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10201, 10202, 10204, 10206, 11102, 11103, 11104.
	 */
	public Range addSourceRange(String displayId,int start,int end,OrientationType orientation) throws SBOLValidationException {
		URI identity = createCompliantURI(this.getPersistentIdentity().toString(),displayId,this.getVersion());
		Range range = new Range(identity,start,end);
		range.setPersistentIdentity(createCompliantURI(this.getPersistentIdentity().toString(),displayId,""));
		range.setDisplayId(displayId);
		range.setVersion(this.getVersion());
		range.setOrientation(orientation);
		addSourceLocation(range);
		return range;
	}
	
	/**
	 * @param sourceLocation
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link Identified#addChildSafely(Identified, java.util.Map, String, java.util.Map...)}
	 */
	void addSourceLocation(Location sourceLocation) throws SBOLValidationException {
		addChildSafely(sourceLocation, sourceLocations, "sourceLocation");
		sourceLocation.setSBOLDocument(this.getSBOLDocument());
		sourceLocation.setComponentDefinition(componentDefinition);
		if (sourceLocation.isSetSequence() && getDefinition() != null && 
				!getDefinition().getSequenceURIs().contains(sourceLocation.getSequenceURI())) {
			throw new SBOLValidationException("sbol-11003",this);
		}
	}
	
	/**
	 * Removes the given location from the list of source locations.
	 * 
	 * @param sourceLocation the location to be removed
	 * @return {@code true} if the matching location was removed successfully, {@code false} otherwise
	 */	
	public boolean removeSourceLocation(Location sourceLocation) {
		return removeChildSafely(sourceLocation,sourceLocations);
	}
	
	/**
	 * Returns a source location owned by this component 
	 * that matches the given display ID.
	 * <p>
	 * This method first creates a compliant URI. It starts with the component's persistent identity URI,
	 * followed by the given display ID, and ends with this component's version. This compliant URI is used
	 * to look up for the location to be retrieved.
	 * 
	 * @param displayId the display ID of the location to be retrieved
	 * @return the matching location
	 */
	public Location getSourceLocation(String displayId) {
		try {
			return sourceLocations.get(createCompliantURI(this.getPersistentIdentity().toString(),displayId,this.getVersion()));
		}
		catch (SBOLValidationException e) {
			return null;
		}
	}
	
	/**
	 * Returns a sourceLocation owned by this component 
	 * that matches the given identity URI.
	 * 
	 * @param sourceLocationURI the URI identity of the location to be retrieved
	 * @return the matching location 
	 */
	public Location getSourceLocation(URI sourceLocationURI) {
		return sourceLocations.get(sourceLocationURI);
	}
	
	/**
	 * Returns the set of sourceLocations referenced by this component.
	 * 
	 * @return the set of sourceLocations referenced by this component
	 */
	public Set<Location> getSourceLocations() {
		return new HashSet<>(sourceLocations.values());
	}
	
	/**
	 * Returns the set of Range/Cut sourceLocations referenced by this component.
	 * 
	 * @return the set of Range/Cut sourceLocations referenced by this component
	 */
	public Set<Location> getPreciseSourceLocations() {
		HashSet<Location> preciseSourceLocations = new HashSet<>();
		for (Location sourceLocation : sourceLocations.values()) {
			if (!(sourceLocation instanceof GenericLocation)) {
				preciseSourceLocations.add(sourceLocation);
			}
		}
		return preciseSourceLocations;
	}
	
	/**
	 * Returns a sorted list of sourceLocations owned by this component.
	 * 
	 * @return a sorted list of sourceLocations owned by this component
	 */
	public List<Location> getSortedSourceLocations() {
		List<Location> sortedSourceLocations = new ArrayList<Location>();
		sortedSourceLocations.addAll(this.getSourceLocations());
		Collections.sort(sortedSourceLocations);
		return sortedSourceLocations;
	}

	/**
	 * Removes all entries of this component's list of sourceLocations.
	 * The set will be empty after this call returns.
	 */
	void clearSourceLocations() {
		Object[] valueSetArray = sourceLocations.values().toArray();
		for (Object sourceLocation : valueSetArray) {
			removeSourceLocation((Location)sourceLocation);
		}
	}
		
	/**
	 * Clears the existing list of sourceLocation instances first, 
	 * then adds the given set of sourceLocations.
	 * 
	 * @throws SBOLValidationException if any of the following condition is satisfied:
	 * <li>the following SBOL validation rule was violated: 10902;</li>
	 * <li>an SBOL validation rule violation occurred in {@link #clearSourceLocations()}; or </li>
	 * <li>an SBOL validation rule violation occurred in {@link #addSourceLocation(Location)}.</li>
	 */
	void setSourceLocations(Set<Location> sourceLocations) throws SBOLValidationException {
		clearSourceLocations();	
		for (Location location : sourceLocations) {
			addSourceLocation(location);
		}
	}
	
	/**
	 * Creates a generic location with the given arguments and then adds it to this component's
	 * list of locations.
	 * <p>
	 * This method first creates a compliant URI for the generic location to be created. 
	 * It starts with this component's persistent identity URI, 
	 * followed by the given display ID, and ends an empty string for version.
	 *  
	 * @param displayId the display ID for the generic location to be created
	 * @return the created generic location instance
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10201, 10202, 10204, 10206.
	 */
	public GenericLocation addGenericLocation(String displayId) throws SBOLValidationException {
		URI identity = createCompliantURI(this.getPersistentIdentity().toString(),displayId,this.getVersion());
		GenericLocation genericLocation = new GenericLocation(identity);
		genericLocation.setPersistentIdentity(createCompliantURI(this.getPersistentIdentity().toString(),displayId,""));
		genericLocation.setDisplayId(displayId);
		genericLocation.setVersion(this.getVersion());
		addLocation(genericLocation);
		return genericLocation;
	}
	
	/**
	 * Creates a generic location with the given arguments and then adds it to this component's
	 * list of locations.
	 * <p>
	 * This method first creates a compliant URI for the generic location to be created. 
	 * It starts with this component's persistent identity URI, 
	 * followed by the given display ID, and ends an empty string for version.
	 *  
	 * @param displayId the display ID for the generic location to be created
	 * @param orientation the orientation type
	 * @return the created generic location instance
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10201, 10202, 10204, 10206. 
 	 */
	public GenericLocation addGenericLocation(String displayId,OrientationType orientation) throws SBOLValidationException {
		URI identity = createCompliantURI(this.getPersistentIdentity().toString(),displayId,this.getVersion());
		GenericLocation genericLocation = new GenericLocation(identity);
		genericLocation.setPersistentIdentity(createCompliantURI(this.getPersistentIdentity().toString(),displayId,""));
		genericLocation.setDisplayId(displayId);
		genericLocation.setVersion(this.getVersion());
		genericLocation.setOrientation(orientation);
		addLocation(genericLocation);
		return genericLocation;
	}
	
	/**
	 * Creates a cut with the given arguments and then adds it to this component's
	 * list of locations.
	 * <p>
	 * This method first creates a compliant URI for the cut to be created. 
	 * It starts with this component's persistent identity URI, 
	 * followed by the given display ID, and ends an empty string for version.
	 *  
	 * @param displayId the display ID for the cut to be created
	 * @param at the at property for the cut to be created
	 * @return the created cut
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10201, 10202, 10204, 10206, 11202.
	 */
	public Cut addCut(String displayId,int at) throws SBOLValidationException {
		URI identity = createCompliantURI(this.getPersistentIdentity().toString(),displayId,this.getVersion());
		Cut cut = new Cut(identity,at);
		cut.setPersistentIdentity(createCompliantURI(this.getPersistentIdentity().toString(),displayId,""));
		cut.setDisplayId(displayId);
		cut.setVersion(this.getVersion());
		addLocation(cut);
		return cut;
	}
	
	/**
	 * Creates a cut with the given arguments and then adds it to this component's
	 * list of locations.
	 * <p>
	 * This method first creates a compliant URI for the cut to be created. 
	 * It starts with this component's persistent identity URI, 
	 * followed by the given display ID, and ends an empty string for version.
	 *  
	 * @param displayId the display ID for the cut to be created
	 * @param at the at property for the cut to be created
	 * @param orientation the orientation type
	 * @return the created cut
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10201, 10202, 10204, 10206, 11202. 
	 */
	public Cut addCut(String displayId,int at,OrientationType orientation) throws SBOLValidationException {
		URI identity = createCompliantURI(this.getPersistentIdentity().toString(),displayId,this.getVersion());
		Cut cut = new Cut(identity,at);
		cut.setPersistentIdentity(createCompliantURI(this.getPersistentIdentity().toString(),displayId,""));
		cut.setDisplayId(displayId);
		cut.setVersion(this.getVersion());
		cut.setOrientation(orientation);
		addLocation(cut);
		return cut;
	}

	/**
	 * Creates a range with the given arguments and then adds it to this component's
	 * list of locations.
	 * <p>
	 * This method first creates a compliant URI for the range to be created. 
	 * It starts with this component's persistent identity URI, 
	 * followed by the given display ID, and ends an empty string for version.
	 *  
	 * @param displayId the display ID for the range to be created
 	 * @param start the start index for the range to be created
	 * @param end the end index for the range to be created
	 * @return the created range
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10201, 10202, 10204, 10206, 11102, 11103, 11104. 
	 */
	public Range addRange(String displayId,int start,int end) throws SBOLValidationException {
		URI identity = createCompliantURI(this.getPersistentIdentity().toString(),displayId,this.getVersion());
		Range range = new Range(identity,start,end);
		range.setPersistentIdentity(createCompliantURI(this.getPersistentIdentity().toString(),displayId,""));
		range.setDisplayId(displayId);
		range.setVersion(this.getVersion());
		addLocation(range);
		return range;
	}
	
	/**
	 * Creates a range with the given arguments and then adds it to this component's
	 * list of locations.
	 * <p>
	 * This method first creates a compliant URI for the range to be created. 
	 * It starts with this component's persistent identity URI, 
	 * followed by the given display ID, and ends an empty string for version.
	 * 
	 * @param displayId the display ID for the range to be created
 	 * @param start the start index for the range to be created
	 * @param end the end index for the range to be created
	 * @param orientation the orientation type
	 * @return the created range
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10201, 10202, 10204, 10206, 11102, 11103, 11104.
	 */
	public Range addRange(String displayId,int start,int end,OrientationType orientation) throws SBOLValidationException {
		URI identity = createCompliantURI(this.getPersistentIdentity().toString(),displayId,this.getVersion());
		Range range = new Range(identity,start,end);
		range.setPersistentIdentity(createCompliantURI(this.getPersistentIdentity().toString(),displayId,""));
		range.setDisplayId(displayId);
		range.setVersion(this.getVersion());
		range.setOrientation(orientation);
		addLocation(range);
		return range;
	}
	
	/**
	 * @param location
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link Identified#addChildSafely(Identified, java.util.Map, String, java.util.Map...)}
	 */
	void addLocation(Location location) throws SBOLValidationException {
		addChildSafely(location, locations, "location");
		location.setSBOLDocument(this.getSBOLDocument());
		location.setComponentDefinition(componentDefinition);
		if (location.isSetSequence() && componentDefinition != null && 
				!componentDefinition.getSequenceURIs().contains(location.getSequenceURI())) {
			throw new SBOLValidationException("sbol-11003",this);
		}
	}
	
	/**
	 * Removes the given location from the list of locations.
	 * 
	 * @param location the location to be removed
	 * @return {@code true} if the matching location was removed successfully, {@code false} otherwise
	 */	
	public boolean removeLocation(Location location) {
		return removeChildSafely(location,sourceLocations);
	}
	
	/**
	 * Returns the location owned by this component 
	 * that matches the given display ID.
	 * <p>
	 * This method first creates a compliant URI. It starts with the component's persistent identity URI,
	 * followed by the given display ID, and ends with this component's version. This compliant URI is used
	 * to look up for the location to be retrieved.
	 * 
	 * @param displayId the display ID of the location to be retrieved
	 * @return the matching location
	 */
	public Location getLocation(String displayId) {
		try {
			return locations.get(createCompliantURI(this.getPersistentIdentity().toString(),displayId,this.getVersion()));
		}
		catch (SBOLValidationException e) {
			return null;
		}
	}
	
	/**
	 * Returns the location owned by this component 
	 * that matches the given identity URI.
	 * 
	 * @param locationURI the URI identity of the location to be retrieved
	 * @return the matching location 
	 */
	public Location getLocation(URI locationURI) {
		return locations.get(locationURI);
	}
	
	/**
	 * Returns the set of locations referenced by this component.
	 * 
	 * @return the set of locations referenced by this component
	 */
	public Set<Location> getLocations() {
		return new HashSet<>(locations.values());
	}
	
	/**
	 * Returns the set of Range/Cut locations referenced by this component.
	 * 
	 * @return the set of Range/Cut locations referenced by this component
	 */
	public Set<Location> getPreciseLocations() {
		HashSet<Location> preciseLocations = new HashSet<>();
		for (Location location : locations.values()) {
			if (!(location instanceof GenericLocation)) {
				preciseLocations.add(location);
			}
		}
		return preciseLocations;
	}
	
	/**
	 * Returns a sorted list of locations owned by this component.
	 * 
	 * @return a sorted list of locations owned by this component
	 */
	public List<Location> getSortedLocations() {
		List<Location> sortedLocations = new ArrayList<Location>();
		sortedLocations.addAll(this.getLocations());
		Collections.sort(sortedLocations);
		return sortedLocations;
	}

	/**
	 * Removes all entries of this component's list of locations.
	 * The set will be empty after this call returns.
	 */
	void clearLocations() {
		Object[] valueSetArray = locations.values().toArray();
		for (Object location : valueSetArray) {
			removeLocation((Location)location);
		}
	}
		
	/**
	 * Clears the existing list of location instances first, 
	 * then adds the given set of locations.
	 * 
	 * @throws SBOLValidationException if any of the following condition is satisfied:
	 * <li>the following SBOL validation rule was violated: 10902;</li>
	 * <li>an SBOL validation rule violation occurred in {@link #clearLocations()}; or </li>
	 * <li>an SBOL validation rule violation occurred in {@link #addLocation(Location)}.</li>
	 */
	void setLocations(Set<Location> locations) throws SBOLValidationException {
		clearLocations();	
		for (Location location : locations) {
			addLocation(location);
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((roles == null) ? 0 : roles.hashCode());
		result = prime * result + ((mapsTos == null) ? 0 : mapsTos.hashCode());
		result = prime * result + ((locations == null) ? 0 : locations.hashCode());
		result = prime * result + ((sourceLocations == null) ? 0 : sourceLocations.hashCode());
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
		Component other = (Component) obj;
		if (roles == null) {
			if (other.roles != null)
				return false;
		} else if (!roles.equals(other.roles))
			return false;
		if (mapsTos == null) {
			if (other.mapsTos != null)
				return false;
		} else if (!mapsTos.equals(other.mapsTos))
			return false;
		if (locations == null) {
			if (other.locations != null)
				return false;
		} else if (!locations.equals(other.locations))
			return false;
		if (sourceLocations == null) {
			if (other.sourceLocations != null)
				return false;
		} else if (!sourceLocations.equals(other.sourceLocations))
			return false;
		return true;
	}

	@Override
	// TODO: Remove until roles. Add roleIntegration.
	public String toString() {
		return "Component ["
				+ super.toString()
				+ (roles.size()>0?", roles=" + roles:"")  
				+ (locations.size()>0?", locations=" + this.getLocations():"")
				+ (sourceLocations.size()>0?", sourcelocations=" + this.getSourceLocations():"")
				+ (this.getMapsTos().size()>0?", mapsTos=" + this.getMapsTos():"") 
				+ "]";
	}
}
