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
 * Represents a SequenceAnnotation object in the SBOL data model.
 * 
 * @author Zhen Zhang
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */

public class SequenceAnnotation extends Identified implements Comparable<SequenceAnnotation> {

	private HashMap<URI, Location> locations;
	private URI component;
	private Set<URI> roles;
//	private RoleIntegrationType roleIntegration;
	/**
	 * The parent component definition for this sequence annotation.
	 */
	private ComponentDefinition componentDefinition = null;
	
	/**
	 * @param identity
	 * @param locations
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <li>an SBOL validation rule violation occurred in {@link Identified#Identified(URI)}</li>
	 * <li>an SBOL validation rule violation occurred in {@link #setLocations(Set)}</li>
	 */
	SequenceAnnotation(URI identity, Set<Location> locations) throws SBOLValidationException {
		super(identity);
		this.locations = new HashMap<>();
		this.setLocations(locations);	
		this.roles = new HashSet<>();
	}
	
	/**
	 * @param sequenceAnnotation
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of the 
	 * following constructors or methods:
	 * <ul>
	 * <li>{@link Identified#Identified(Identified)},</li>
	 * <li>{@link Location#deepCopy()},</li>
	 * <li>{@link #addLocation(Location)}, or</li>
	 * <li>{@link #setComponent(URI)}.</li>
	 * </ul>
	 */
	private SequenceAnnotation(SequenceAnnotation sequenceAnnotation) throws SBOLValidationException {
		super(sequenceAnnotation);
		this.locations = new HashMap<>();
		for (Location location : sequenceAnnotation.getLocations()) {
			addLocation(location.deepCopy());
		}
		if (sequenceAnnotation.isSetComponent()) {
			this.setComponent(sequenceAnnotation.getComponentURI());
		}
		this.roles = new HashSet<>();
		for (URI role : sequenceAnnotation.getRoles()) {
			this.addRole(URI.create(role.toString()));
		}
	}
	
	void copy(SequenceAnnotation sequenceAnnotation) throws SBOLValidationException {
		((Identified)this).copy((Identified)sequenceAnnotation);
		for (Location location : sequenceAnnotation.getLocations()) {
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
		if (sequenceAnnotation.isSetComponent()) {
			String componentDisplayId = URIcompliance.findDisplayId(sequenceAnnotation.getComponent());
			this.setComponent(componentDisplayId);
		}
		this.roles = new HashSet<>();
		for (URI role : sequenceAnnotation.getRoles()) {
			this.addRole(URI.create(role.toString()));
		}
	}
	
	/**
	 * Creates a generic location with the given arguments and then adds it to this sequence annotation's
	 * list of locations.
	 * <p>
	 * This method first creates a compliant URI for the generic location to be created. 
	 * It starts with this sequence annotation's persistent identity URI, 
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
	 * Creates a generic location with the given arguments and then adds it to this sequence annotation's
	 * list of locations.
	 * <p>
	 * This method first creates a compliant URI for the generic location to be created. 
	 * It starts with this sequence annotation's persistent identity URI, 
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
	 * Creates a cut with the given arguments and then adds it to this sequence annotation's
	 * list of locations.
	 * <p>
	 * This method first creates a compliant URI for the cut to be created. 
	 * It starts with this sequence annotation's persistent identity URI, 
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
	 * Creates a cut with the given arguments and then adds it to this sequence annotation's
	 * list of locations.
	 * <p>
	 * This method first creates a compliant URI for the cut to be created. 
	 * It starts with this sequence annotation's persistent identity URI, 
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
	 * Creates a range with the given arguments and then adds it to this sequence annotation's
	 * list of locations.
	 * <p>
	 * This method first creates a compliant URI for the range to be created. 
	 * It starts with this sequence annotation's persistent identity URI, 
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
	 * Creates a range with the given arguments and then adds it to this sequence annotation's
	 * list of locations.
	 * <p>
	 * This method first creates a compliant URI for the range to be created. 
	 * It starts with this sequence annotation's persistent identity URI, 
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
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 10902
	 */	
	public boolean removeLocation(Location location) throws SBOLValidationException {
		if (locations.size()==1 && locations.containsValue(location)) {
			throw new SBOLValidationException("sbol-10902", this);
		}
		return removeChildSafely(location,locations);
	}
	
	/**
	 * Returns the location owned by this sequence annotation 
	 * that matches the given display ID.
	 * <p>
	 * This method first creates a compliant URI. It starts with the sequence annotation's persistent identity URI,
	 * followed by the given display ID, and ends with this sequence annotation's version. This compliant URI is used
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
	 * Returns the location owned by this sequence annotation 
	 * that matches the given identity URI.
	 * 
	 * @param locationURI the URI identity of the location to be retrieved
	 * @return the matching location 
	 */
	public Location getLocation(URI locationURI) {
		return locations.get(locationURI);
	}
	
	/**
	 * Returns the set of locations referenced by this sequence annotation.
	 * 
	 * @return the set of locations referenced by this sequence annotation
	 */
	public Set<Location> getLocations() {
		return new HashSet<>(locations.values());
	}
	
	/**
	 * Returns the set of Range/Cut locations referenced by this sequence annotation.
	 * 
	 * @return the set of Range/Cut locations referenced by this sequence annotation
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
	 * Returns a sorted list of locations owned by this sequence annotation.
	 * 
	 * @return a sorted list of locations owned by this sequence annotation
	 */
	public List<Location> getSortedLocations() {
		List<Location> sortedLocations = new ArrayList<Location>();
		sortedLocations.addAll(this.getLocations());
		Collections.sort(sortedLocations);
		return sortedLocations;
	}

	/**
	 * Removes all entries of this sequence annotation's list of locations.
	 * The set will be empty after this call returns.
 	 * 
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #removeLocation(Location)}.
	 */
	void clearLocations() throws SBOLValidationException {
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
	private void setLocations(Set<Location> locations) throws SBOLValidationException {
		clearLocations();	
		if (locations==null || locations.size()==0) {
			throw new SBOLValidationException("sbol-10902", this);
		}
		for (Location location : locations) {
			addLocation(location);
		}
	}

	/*
	public void addRange(int start,int end,OrientationType orientation) {
		if (sbolDocument!=null) sbolDocument.checkReadOnly();
		if (location instanceof MultiRange) {
			int numRanges = ((MultiRange)location).getRanges().size();
			Range range = new Range(URIcompliance.createCompliantURI(this.getPersistentIdentity().toString()+"/multiRange","range"+numRanges,this.getVersion()),start,end);
			range.setPersistentIdentity(URI.create(this.getPersistentIdentity().toString()+"/multiRange/range"+numRanges));
			range.setDisplayId("range"+numRanges);
			range.setVersion(this.getVersion());
			if (orientation!=null) range.setOrientation(orientation);
			((MultiRange)location).addRange(range);
		} else if (location instanceof Range) {
			List<Range> ranges = new ArrayList<>();
			location.setIdentity(URIcompliance.createCompliantURI(this.getPersistentIdentity().toString()+"/multiRange","range0",this.getVersion()));
			location.setPersistentIdentity(URI.create(this.getPersistentIdentity().toString()+"/multiRange/range0"));
			location.setDisplayId("range0");
			ranges.add((Range)location);
			Range range = new Range(URIcompliance.createCompliantURI(this.getPersistentIdentity().toString()+"/multiRange","range1",this.getVersion()),start,end);
			range.setPersistentIdentity(URI.create(this.getPersistentIdentity().toString()+"/multiRange/range1"));
			range.setDisplayId("range1");
			range.setVersion(this.getVersion());
			if (orientation!=null) range.setOrientation(orientation);
			ranges.add(range);
			MultiRange multiRange = new MultiRange(URIcompliance.createCompliantURI(this.getPersistentIdentity().toString(),"multiRange",this.getVersion()),ranges);
			multiRange.setPersistentIdentity(URI.create(this.getPersistentIdentity().toString()+"/multiRange"));
			multiRange.setDisplayId("multiRange");
			multiRange.setVersion(this.getVersion());
			location = multiRange;
		} else {
			location = new Range(URIcompliance.createCompliantURI(this.getPersistentIdentity().toString(),"range",this.getVersion()),start,end);
			location.setPersistentIdentity(URI.create(this.getPersistentIdentity().toString()+"/range"));
			location.setDisplayId("range");
			location.setVersion(this.getVersion());
			if (orientation!=null) ((Range)location).setOrientation(orientation);
		}
	}
	
	void removeRange(Range range) {
		if (sbolDocument!=null) sbolDocument.checkReadOnly();
		if (location instanceof MultiRange) {
			try {
				((MultiRange)location).removeRange(range);
			} catch (Exception e) {
				Set<Range> ranges = ((MultiRange)location).getRanges();
				if (ranges.size()!=2) {
					throw new SBOLValidationException("Sequence annotation " + this.getIdentity() + 
							" is required to have a location.");
				}
				for (Range otherRange : ranges) {
					if (otherRange.getIdentity().equals(range)) continue;
					location = new Range(URIcompliance.createCompliantURI(this.getPersistentIdentity().toString(), 
							"range", this.getVersion()),otherRange.getStart(),otherRange.getEnd());
					if (otherRange.isSetOrientation()) {
						((Range)location).setOrientation(otherRange.getOrientation());
					}
				}
			}
		}
	}
	*/
		
	/**
	 * Test if the reference component is set.
	 *  
	 * @return {@code true} if this sequence annotation refers to a component, {@code false} otherwise
	 */
	public boolean isSetComponent() {
		return component != null;
	}

	/**
	 * Returns the component that this sequence annotation refers to.
	 * 
	 * @return the component that this sequence annotation refers to
	 */
	public URI getComponentURI() {
		return component;
	}
	
	/**
	 * Returns the component identity this sequence annotation refers to.
	 * <p>
	 * If this sequence annotation's parent component definition is {@code null}, this method returns {@code null}.
	 * Otherwise, it returns its child component which is also referenced by this sequence annotation.   
	 * 
	 * @return the component identity this sequence annotation refers to  
	 */
	public URI getComponentIdentity() {
		if (componentDefinition==null) return null;
		if (componentDefinition.getComponent(component)==null) return null;
		return componentDefinition.getComponent(component).getIdentity();
	}

	/**
	 * Returns the component this sequence annotation refers to.
	 * <p>
	 * If this sequence annotation's parent component definition is {@code null}, this method returns {@code null}.
	 * Otherwise, it returns its child component which is also referenced by this sequence annotation.   
	 * 
	 * @return the component this sequence annotation refers to  
	 */
	public Component getComponent() {
		if (componentDefinition==null) return null;
		return componentDefinition.getComponent(component);
	}
	
	/**
	 * Sets this sequence annotation's reference component (its identity URI) to the one matching
	 * the given display ID. 
	 * <p>
	 * This method first creates a compliant URI for the reference component. It starts with this sequence
	 * annotation's parent component defintion's persistent identity URI, followed by the given display ID,
	 * and ends with this sequence annotation's parent component defintion's version.
	 * 
	 * @param displayId the given display ID for the reference component
 	 * @throws SBOLValidationException if either of the following conditions is satisfied:
 	 * <ul>
 	 * <li>either of the following SBOL validation rules was violated: 10204, 10206; or</li>
 	 * <li>if an SBOL validation rule violation occurred in any of the following constructors or methods:
 	 * 	<ul>
 	 * 		<li>{@link ComponentDefinition#createComponent(String, AccessType, String, String)}, or</li>
 	 * 		<li>{@link #setComponent(URI)}.</li>
 	 * 	</ul>
 	 * </li>
 	 * </ul> 
 	 */
	public void setComponent(String displayId) throws SBOLValidationException {
		URI componentURI = URIcompliance.createCompliantURI(componentDefinition.getPersistentIdentity().toString(), 
				displayId, componentDefinition.getVersion());
		if (this.getSBOLDocument()!=null && this.getSBOLDocument().isCreateDefaults() && componentDefinition!=null &&
				componentDefinition.getComponent(componentURI)==null) {
			componentDefinition.createComponent(displayId,AccessType.PUBLIC,displayId,"");
		}
		setComponent(componentURI);
	}

	/**
	 * Sets the reference component's identity URI to the given URI.
	 * 
	 * @param componentURI the given component identity URI
 	 * @throws SBOLValidationException if either of the following SBOL validation rules was violated:
 	 * 10522, 10905, 10909.
	 */
	public void setComponent(URI componentURI) throws SBOLValidationException {
		if (!roles.isEmpty()) {
			throw new SBOLValidationException("sbol-10909", this);
		}
		if (componentDefinition!=null) {
			if (componentDefinition.getComponent(componentURI)==null) {
				throw new SBOLValidationException("sbol-10905",this);
				
			}
			for (SequenceAnnotation sa : componentDefinition.getSequenceAnnotations()) {
				if (sa.getIdentity().equals(this.getIdentity())) continue;
				if (sa.isSetComponent() && sa.getComponentURI().equals(componentURI)) {
					throw new SBOLValidationException("sbol-10522", this);
				}
			}
		}
		this.component = componentURI;
	}
	
	/**
	 * Sets the reference component to {@code null}.
	 */
	public void unsetComponent() {
		component = null;
	}
	
	/**
	 * Adds the given role to this sequence annotation's set of roles.
	 *
	 * @param roleURI the role to be added
	 * @return {@code true} if this set did not already contain the specified role, {@code false} otherwise
	 * @throws SBOLValidationException if component property is already set
	 */
	public boolean addRole(URI roleURI) throws SBOLValidationException {
		if (isSetComponent()) {
			throw new SBOLValidationException("sbol-10909", this);
		}
		return roles.add(roleURI);
	}

	/**
	 * Removes the given role from this sequence annotation's set of roles.
	 *
	 * @param roleURI the given role to be removed
	 * @return {@code true} if the matching role was removed successfully, {@code false} otherwise
	 */
	public boolean removeRole(URI roleURI) {
		return roles.remove(roleURI);
	}

	/**
	 * Clears the existing set of roles first, and then adds the given
	 * set of the roles to this sequence annotation.
	 *
	 * @param roles the set of roles to be set
	 * @throws SBOLValidationException if component property is already set
	 */
	public void setRoles(Set<URI> roles) throws SBOLValidationException {
		clearRoles();
		if (roles==null) return;
		for (URI role : roles) {
			addRole(role);
		}
	}

	/**
	 * Returns this sequence annotation's set of roles.
	 *
	 * @return this sequence annotation's set of roles
	 */
	public Set<URI> getRoles() {
		Set<URI> result = new HashSet<>();
		result.addAll(roles);
		return result;
	}

	/**
	 * Checks if the given role is included in this sequence annotation's set of roles.
	 *
	 * @param roleURI the role to be checked
	 * @return {@code true} if this set contains the given role, {@code false} otherwise
	 */
	public boolean containsRole(URI roleURI) {
		return roles.contains(roleURI);
	}

	/**
	 * Removes all entries of this sequence annotation's set of roles.
	 * The set will be empty after this call returns.	 
	 */
	public void clearRoles() {
		roles.clear();
	}
	
//	/**
//	 * Test if the roleIntegration property is set.
//	 * 
//	 * @return {@code true} if this sequence annotation's ruleIntegration property is not {@code null}, 
//	 * {@code false} otherwise 
//	 */
//	public boolean isSetRoleIntegration() {
//		return roleIntegration != null;
//	}
//
//	/**
//	 * Returns the roleIntegration property of this sequence annotation.
//	 * 
//	 * @return the roleIntegration property of this sequence annotation
//	 */
//	public RoleIntegrationType getRoleIntegration() {
//		return this.roleIntegration;
//	}
//
//	/**
//	 * Sets the roleIntegration property to the given one.
//	 * 
//	 * @param roleIntegration the given roleIntegration type
//	 */
//	public void setRoleIntegration(RoleIntegrationType roleIntegration) {
//		this.roleIntegration = roleIntegration;
//	}
//
//	/**
//	 * Sets the roleIntegration property of this sequence annotation to {@code null}.
//	 *
//	 */
//	public void unsetRoleIntegration() {
//		roleIntegration = null;
//	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((roles == null) ? 0 : roles.hashCode());
		result = prime * result + ((component == null) ? 0 : component.hashCode());
		result = prime * result + ((locations == null) ? 0 : locations.hashCode());
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
		SequenceAnnotation other = (SequenceAnnotation) obj;
		if (component == null) {
			if (other.component != null)
				return false;
		} else if (!component.equals(other.component)) {
			if (getComponentIdentity() == null || other.getComponentIdentity() == null 
					|| !getComponentIdentity().equals(other.getComponentIdentity())) {
				return false;
			}
		}
		if (roles == null) {
			if (other.roles != null)
				return false;
		} else if (!roles.equals(other.roles))
			return false;
		if (locations == null) {
			if (other.locations != null)
				return false;
		} else if (!locations.equals(other.locations))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.Identified#deepCopy()
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in
	 * {@link SequenceAnnotation#SequenceAnnotation(SequenceAnnotation)}.
	 */
	@Override
	SequenceAnnotation deepCopy() throws SBOLValidationException {
		return new SequenceAnnotation(this);
	}

	/**
	 * Assume this sequence annotation has compliant URI, and all given parameters have compliant forms.
	 * This method is called by {@link ComponentDefinition#copy(String, String, String)}.
	 * @param URIprefix
	 * @param parentDisplayId
	 * @param version
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of the following methods:
	 * <ul>
	 * <li>{@link URIcompliance#createCompliantURI(String, String, String)},</li>
	 * <li>{@link #setWasDerivedFrom(URI)},</li>
	 * <li>{@link #setIdentity(URI)},</li>
	 * <li>{@link #setDisplayId(String)},</li>
	 * <li>{@link #setVersion(String)},</li>
	 * <li>{@link #setComponent(URI)},</li>
	 * <li>{@link Location#setDisplayId(String)},</li>
	 * <li>{@link Location#updateCompliantURI(String, String, String)}, or</li>
	 * <li>{@link #addLocation(Location)}.</li>
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
		if (component!=null) {
			String componentId = extractDisplayId(component);
			this.setComponent(createCompliantURI(URIprefix,componentId,version));
		}
		int count = 0;
		for (Location location : this.getLocations()) {
			if (!location.isSetDisplayId()) location.setDisplayId("location"+ ++count);
			location.updateCompliantURI(this.getPersistentIdentity().toString(),location.getDisplayId(),version);
			this.removeChildSafely(location, this.locations);
			this.addLocation(location);
		}
	}

	/**
	 * Returns the component definition for the component annotated by this sequence annotation.
	 * <p>
	 * If the parent component definition for this sequence annotation is not set, then this method
	 * returns {@code null}. The component definition returned is the definition of the component that 
	 * is referenced by the "component" field of this sequence annotation.
	 * 
	 * @return the component definition for the component annotated by this sequence annotation
	 */
	public ComponentDefinition getComponentDefinition() {
		if (componentDefinition!=null && isSetComponent()) {
			return componentDefinition.getComponent(component).getDefinition();
		}
		return null;
	}

	/**
	 * @param componentDefinition
	 */
	void setComponentDefinition(ComponentDefinition componentDefinition) {
		this.componentDefinition = componentDefinition;
	}

	@Override
	public String toString() {
		return "SequenceAnnotation ["
				+ super.toString()
				+ (roles.size()>0?", roles=" + roles:"")  
				+ ", locations=" + this.getLocations() 
				+ (this.isSetComponent()?", component=" + component:"")
				+ "]";
	}
	
	@Override
	public int compareTo(SequenceAnnotation sa) {
		List<Location> sortedLocations1 = this.getSortedLocations();
		List<Location> sortedLocations2 = sa.getSortedLocations();
		if (sortedLocations1.size()>0 && sortedLocations2.size()>0) {
			return sortedLocations1.get(0).compareTo(sortedLocations2.get(0));
		} 
		return 0;
	}
}
