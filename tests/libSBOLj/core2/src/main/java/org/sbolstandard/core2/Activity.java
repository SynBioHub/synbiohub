package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.joda.time.DateTime;

/**
 * Represents a Activity object in the SBOL data model.
 * 
 * @author Chris Myers
 * @version 2.2
 */

public class Activity extends TopLevel{

	private Set<URI> types;
	private DateTime startedAtTime;
	private DateTime endedAtTime;
	private Set<URI> wasInformedBys;
	private HashMap<URI, Association> associations;
	private HashMap<URI, Usage> usages;

	/**
	 * @param identity
	 * @throws SBOLValidationException if either of the following condition is satisfied: 
	 * <ul>
	 * <li>if an SBOL validation rule violation occurred in {@link TopLevel#TopLevel(URI)}, or</li>
	 * <li>the following SBOL validation rule was violated: XXXXX.</li>
	 * </ul>
	 */
	Activity(URI identity) throws SBOLValidationException {
		super(identity);
		this.types = new HashSet<>();
		startedAtTime = null;
		endedAtTime = null;
		wasInformedBys = new HashSet<>();
		associations = new HashMap<>();
		usages = new HashMap<>();
	}

	/**
	 * @param genericTopLevel
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in
	 * the following constructor or method:
	 * <ul>
	 * <li>{@link TopLevel#TopLevel(TopLevel)}, or</li>
	 * <li>{@link #setRDFType(QName)}.</li>
	 * </ul>
	 */
	private Activity(Activity activity) throws SBOLValidationException {
		super(activity);
		//this.setRDFType(genericTopLevel.getRDFType());
	}
	
	void copy(Activity activity) throws SBOLValidationException {
		((TopLevel)this).copy((TopLevel)activity);
		for (URI type : activity.getTypes()) {
			this.addType(URI.create(type.toString()));
		}
		if (activity.isSetStartedAtTime()) {
			this.setStartedAtTime(activity.getStartedAtTime());
		}
		if (activity.isSetEndedAtTime()) {
			this.setEndedAtTime(activity.getEndedAtTime());
		}
		for (Association association : activity.getAssociations()) {
			String displayId = URIcompliance.findDisplayId(association);
			Association newAssociation = this.createAssociation(displayId, association.getAgentURI());
			newAssociation.copy(association);
		}
		for (Usage usage : activity.getUsages()) {
			String displayId = URIcompliance.findDisplayId(usage);
			Usage newUsage = this.createUsage(displayId, usage.getEntityURI());
			newUsage.copy(usage);
		}
		for (URI wasInformedBy : activity.getWasInformedByURIs()) {
			this.addWasInformedBy(URI.create(wasInformedBy.toString()));
		}
	}
	
	/**
	 * Adds the given type URI to this activity's set of type URIs.
	 *
	 * @param typeURI the given type URI
	 * @return {@code true} if this set did not already contain the given type URI, {@code false} otherwise.
	 */
	public boolean addType(URI typeURI) {
		return types.add(typeURI);
	}

	/**
	 * Removes the given type URI from the set of types.
	 *
	 * @param typeURI the specified type URI
	 * @return {@code true} if the matching type reference was removed successfully, {@code false} otherwise.
	 */
	public boolean removeType(URI typeURI) {
		return types.remove(typeURI);
	}

	/**
	 * Clears the existing set of types first, then adds the given 
	 * set of the types to this activity.
	 *
	 * @param types the set of types to set to
	 */
	public void setTypes(Set<URI> types) {
		clearTypes();
		for (URI type : types) {
			addType(type);
		}
	}

	/**
	 * Returns the set of type URIs owned by this activity.
	 *
	 * @return the set of type URIs owned by this actviity
	 */
	public Set<URI> getTypes() {
		Set<URI> result = new HashSet<>();
		result.addAll(types);
		return result;
	}

	/**
	 * Checks if the given type URI is included in this activity's
	 * set of type URIs.
	 *
	 * @param typeURI the type URI to be checked
	 * @return {@code true} if this set contains the given type URI, {@code false} otherwise.
	 */
	public boolean containsType(URI typeURI) {
		return types.contains(typeURI);
	}

	/**
	 * Removes all entries of the list of <code>type</code> instances owned by this instance.
	 * The list will be empty after this call returns.
	 */
	private void clearTypes() {
		types.clear();
	}

	private Association createAssociation(URI identity, URI agent) throws SBOLValidationException {
		Association association = new Association(identity, agent);
		addAssociation(association);
		return association;
	}
	
	/**
	 * Creates a child association for this activity with the given arguments, 
	 * and then adds to this activity's list of associations.
	 * <p>
	 * This method first creates a compliant URI for the child association to be created. 
	 * This URI starts with this activity's persistent identity, 
	 * followed by the given display ID and ends with this activity's version. 
	 * 
	 * @param displayId the display ID for the association to be created
	 * @param agent URI for the agent associated with this activity
	 * @return the created association
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 12602, 12604, 12605, 12606
	 */
	public Association createAssociation(String displayId, URI agent) throws SBOLValidationException {
		String URIprefix = this.getPersistentIdentity().toString();
		String version = this.getVersion();
		Association a = createAssociation(createCompliantURI(URIprefix, displayId, version),agent);
		a.setPersistentIdentity(createCompliantURI(URIprefix, displayId, ""));
		a.setDisplayId(displayId);
		a.setVersion(version);
		return a;
	}

	private Usage createUsage(URI identity, URI entity) throws SBOLValidationException {
		Usage usage = new Usage(identity, entity);
		addUsage(usage);
		return usage;
	}

	/**
	 * Creates a child usage for this activity with the given arguments, 
	 * and then adds to this activity's list of usages.
	 * <p>
	 * This method first creates a compliant URI for the child usage to be created. 
	 * This URI starts with this activity's persistent identity, 
	 * followed by the given display ID and ends with this activity's version. 
	 * 
	 * @param displayId the display ID for the usage to be created
	 * @param entity URI reference to the entity used 
	 * @return the created usage
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 12502, 12503
	 */
	public Usage createUsage(String displayId, URI entity) throws SBOLValidationException {
		String URIprefix = this.getPersistentIdentity().toString();
		String version = this.getVersion();
		Usage u = createUsage(createCompliantURI(URIprefix, displayId, version),entity);
		u.setPersistentIdentity(createCompliantURI(URIprefix, displayId, ""));
		u.setDisplayId(displayId);
		u.setVersion(version);
		return u;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((startedAtTime == null) ? 0 : startedAtTime.hashCode());
		result = prime * result + ((endedAtTime == null) ? 0 : endedAtTime.hashCode());
		result = prime * result + ((wasInformedBys == null) ? 0 : wasInformedBys.hashCode());
		result = prime * result + ((associations == null) ? 0 : associations.hashCode());
		result = prime * result + ((usages == null) ? 0 : usages.hashCode());
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
		Activity other = (Activity) obj;
		if (startedAtTime == null) {
			if (other.startedAtTime != null)
				return false;
		} else if (!startedAtTime.equals(other.startedAtTime))
			return false;
		if (endedAtTime == null) {
			if (other.endedAtTime != null)
				return false;
		} else if (!endedAtTime.equals(other.endedAtTime))
			return false;
		if (wasInformedBys == null) {
			if (other.wasInformedBys != null)
				return false;
		} else if (!wasInformedBys.equals(other.wasInformedBys)) {
			if (getWasInformedByIdentities().size()!=getWasInformedByURIs().size() ||
					other.getWasInformedByIdentities().size()!=other.getWasInformedByURIs().size() ||
					!getWasInformedByIdentities().equals(other.getWasInformedByIdentities())) {
				return false;
			}
		}	
		if (associations == null) {
			if (other.associations != null)
				return false;
		} else if (!associations.equals(other.associations))
			return false;
		if (usages == null) {
			if (other.usages != null)
				return false;
		} else if (!usages.equals(other.usages))
			return false;
		if (types == null) {
			if (other.types != null)
				return false;
		} else if (!types.equals(other.types))
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.TopLevel#deepCopy()
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule was violated in 
	 * {@link #GenericTopLevel(Activity)}.
	 */
	@Override
	Activity deepCopy() throws SBOLValidationException {
		return new Activity(this);
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.abstract_classes.TopLevel#copy(java.lang.String, java.lang.String, java.lang.String)
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in 
	 * any of the following methods:
	 * <ul>
	 * <li>{@link #deepCopy()},</li>
	 * <li>{@link URIcompliance#createCompliantURI(String, String, String)},</li>
	 * <li>{@link #setDisplayId(String)},</li>
	 * <li>{@link #setVersion(String)},</li>
	 * <li>{@link #setWasDerivedFrom(URI)}, or</li>
	 * <li>{@link #setIdentity(URI)}.</li>
	 * </ul>
	 */
	@Override
	Activity copy(String URIprefix, String displayId, String version) throws SBOLValidationException {
		Activity cloned = this.deepCopy();
		cloned.setPersistentIdentity(createCompliantURI(URIprefix,displayId,""));
		cloned.setDisplayId(displayId);
		cloned.setVersion(version);
		URI newIdentity = createCompliantURI(URIprefix,displayId,version);
		if (!this.getIdentity().equals(newIdentity)) {
			cloned.addWasDerivedFrom(this.getIdentity());
		} else {
			cloned.setWasDerivedFroms(this.getWasDerivedFroms());
		}
		cloned.setIdentity(newIdentity);
		return cloned;
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.abstract_classes.TopLevel#checkDescendantsURIcompliance()
	 */
	@Override
	void checkDescendantsURIcompliance() {//throws SBOLValidationException {
		//URIcompliance.isTopLevelURIformCompliant(this.getIdentity());
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.Identified#toString()
	 */
	@Override
	public String toString() {
		return "Activity ["
				+ super.toString()
				+ (this.isSetStartedAtTime()?", startedAtTime =" + startedAtTime:"")
				+ (this.isSetEndedAtTime()?", endedAtTime =" + endedAtTime:"")
				+ (associations.size()>0?", associations=" + associations:"")	  
				+ (usages.size()>0?", usages=" + usages:"")	  
				+ (wasInformedBys.size()>0?", wasInformedBys=" + wasInformedBys:"")	  
				+ "]";
	}

	/**
	 * Test if the startedAtTime is set.
	 *
	 * @return {@code true} if it is not {@code null}, or {@code false} otherwise
	 */
	public boolean isSetStartedAtTime() {
		return startedAtTime != null;
	}

	/**
	 * @return the startedAtTime
	 */
	public DateTime getStartedAtTime() {
		return startedAtTime;
	}

	/**
	 * @param startedAtTime the startedAtTime to set
	 */
	public void setStartedAtTime(DateTime startedAtTime) {
		this.startedAtTime = startedAtTime;
	}
	
	/**
	 * Test if the endedAtTime is set.
	 *
	 * @return {@code true} if it is not {@code null}, or {@code false} otherwise
	 */
	public boolean isSetEndedAtTime() {
		return endedAtTime != null;
	}

	/**
	 * @return the endedAtTime
	 */
	public DateTime getEndedAtTime() {
		return endedAtTime;
	}

	/**
	 * @param endedAtTime the endedAtTime to set
	 */
	public void setEndedAtTime(DateTime endedAtTime) {
		this.endedAtTime = endedAtTime;
	}
	
	/**
	 * Adds the URI of the given Activity instance to this Activity's 
	 * set of wasInformdBy URIs. This method calls {@link #addWasInformedBy(URI)} with this Activity URI.
	 *
	 * @param activity the Activity instance whose identity URI to be added
	 * @return {@code true} if this set did not already contain the identity URI of the given Activity, {@code false} otherwise.
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 12407.
	 */
	public boolean addWasInformedBy(Activity activity) throws SBOLValidationException {
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (this.getSBOLDocument().getActivity(activity.getIdentity())==null) {
				throw new SBOLValidationException("sbol-12407", this);
			}
		}
		return this.addWasInformedBy(activity.getIdentity());
	}

	/**
	 * Adds the given activity URI to this Activity's set of wasInformedBy URIs.
	 *
	 * @param activityURI the identity URI of the Activity to be added
	 * @return {@code true} if this set did not already contain the given activity's URI, {@code false} otherwise.
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 12407. 
	 */
	public boolean addWasInformedBy(URI activityURI) throws SBOLValidationException {
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (this.getSBOLDocument().getActivity(activityURI)==null) {
				throw new SBOLValidationException("sbol-12407",this);
			}
		}
		return wasInformedBys.add(activityURI);
	}

	/**
	 * Constructs a compliant activity URI with the given display ID and version, and then adds this URI
	 * to this activity's set of wasInformedBy URIs.
	 * <p>
	 * This method creates a compliant activity URI with the default
	 * URI prefix, which was set in the SBOLDocument instance hosting this activity, the given 
	 * display ID and version. It then calls {@link #addWasInformedBy(URI)} with this Activity URI.
	 *
	 * @param displayId the display ID of the activity whose identity URI is to be added
	 * @param version version of the activity whose identity URI is to be added
	 * @return {@code true} if this set did not already contain the given activity's URI, {@code false} otherwise. 
	 * @throws SBOLValidationException see {@link #addWasInformedBy(URI)} 
	 */
	public boolean addWasInformedBy(String displayId,String version) throws SBOLValidationException {
		URI activityURI = URIcompliance.createCompliantURI(this.getSBOLDocument().getDefaultURIprefix(),
				TopLevel.SEQUENCE, displayId, version, this.getSBOLDocument().isTypesInURIs());
		return addWasInformedBy(activityURI);
	}

	/**
	 * Constructs a compliant activity URI using the given activity display ID, and then adds this URI to 
	 * this activity's set of wasInformedBy URIs. This method calls {@link #addWasInformedBy(String, String)} with
	 * the given sequence display ID and an empty string as its version. 
	 *
	 * @param displayId the display ID of the activity whose identity URI is to be added
	 * @return {@code true} if this set did not already contain the given activity's URI, {@code false} otherwise.
	 * @throws SBOLValidationException see {@link #addWasInformedBy(String, String)}
	 */
	public boolean addWasInformedBy(String displayId) throws SBOLValidationException {
		return addWasInformedBy(displayId,"");
	}

	/**
	 * Returns the set of wasInformedBy URIs referenced by this activity.
	 *
	 * @return the set of wasInformedBy URIs referenced by this activity
	 */
	public Set<URI> getWasInformedByURIs() {
		Set<URI> result = new HashSet<>();
		result.addAll(wasInformedBys);
		return result;
	}
	
	/**
	 * Returns the set of wasInformedBys identities referenced by this activity.
	 *
	 * @return the set of wasInformedBys identities referenced by this activity
	 */
	public Set<URI> getWasInformedByIdentities() {
		if (this.getSBOLDocument()==null) return null;
		Set<URI> resolved = new HashSet<>();
		for(URI wib : wasInformedBys) {
			Activity activity = this.getSBOLDocument().getActivity(wib);
			if(activity != null) {
				resolved.add(activity.getIdentity());
			}
		}
		return resolved;
	}

	/**
	 * Returns the set of wasInformedBys referenced by this activity.
	 *
	 * @return the set of wasInformedBys referenced by this activity
	 */
	public Set<Activity> getWasInformedBys() {
		if (this.getSBOLDocument()==null) return null;
		Set<Activity> resolved = new HashSet<>();
		for(URI wib : wasInformedBys) {
			Activity activity = this.getSBOLDocument().getActivity(wib);
			if(activity != null) {
				resolved.add(activity);
			}
		}
		return resolved;
	}
	
	/**
	 * Removes all entries of this activity's set of reference
	 * wasInformedBy URIs. The set will be empty after this call returns.
	 */
	public void clearWasInformedBys() {
		wasInformedBys.clear();
	}


	/**
	 * @param wasInformedBys the wasInformedBys to set
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #addWasInformedBy(URI)}
	 */
	public void setWasInformedBys(Set<URI> wasInformedBys) throws SBOLValidationException {
		clearWasInformedBys();
		if (wasInformedBys==null) return;
		for (URI wasInformedBy : wasInformedBys) {
			addWasInformedBy(wasInformedBy);
		}
		this.wasInformedBys = wasInformedBys;
	}
	
	/**
	 * Returns the association matching the given association's display ID.
	 * <p>
	 * This method first creates a compliant URI for the association to be retrieved. It starts with
	 * this activity's persistent identity, followed by the given association's display ID,
	 * and ends with this activity's version.
	 * 
	 * @param displayId the display ID of the association to be retrieved
	 * @return the matching association if present, or {@code null} otherwise.
	 */
	public Association getAssociation(String displayId) {
		try {
			return associations.get(createCompliantURI(this.getPersistentIdentity().toString(),displayId,this.getVersion()));
		}
		catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the instance matching the given association's identity URI.
	 *
	 * @param associationURI the identity URI of the association to be retrieved
	 * @return the matching association if present, or {@code null} otherwise.
	 */
	public Association getAssociation(URI associationURI) {
		return associations.get(associationURI);
	}

	/**
	 * Returns the set of associations owned by this activity.
	 *
	 * @return the set of associations owned by this activity.
	 */
	public Set<Association> getAssociations() {
		Set<Association> associations = new HashSet<>();
		associations.addAll(this.associations.values());
		return associations;
	}
	
	/**
	 * Adds the given association to the list of associations.
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated: 10604, 10605, 10803</li>
	 * <li>an SBOL validation rule violation occurred in {@link Identified#addChildSafely(Identified, java.util.Map, String, java.util.Map...)}</li>
	 * </ul>
	 */
	private void addAssociation(Association association) throws SBOLValidationException {
		association.setSBOLDocument(this.getSBOLDocument());
		addChildSafely(association, associations, "association", usages);
	}
	
	/**
	 * Removes the given association from the list of associations.
	 * 
	 * @param association the given association
	 * @return {@code true} if the matching association was removed successfully,
	 *         {@code false} otherwise.
	 */
	public boolean removeAssociation(Association association) {
		return removeChildSafely(association, associations);
	}

	/**
	 * Removes all entries of this activity's list of associations.
	 * The list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeAssociation(Association association)} to iteratively remove
	 * each association.
	 *
	 */
	public void clearAssociations() {
		Object[] valueSetArray = associations.values().toArray();
		for (Object association : valueSetArray) {
			removeAssociation((Association)association);
		}
	}
	
	/**
	 * @param associations the associations to set
	 */
	void setAssociations(Set<Association> associations) throws SBOLValidationException {
		clearAssociations();
		for (Association association : associations) {
			addAssociation(association);
		}
	}

	/**
	 * Returns the usage matching the given usage's display ID.
	 * <p>
	 * This method first creates a compliant URI for the usage to be retrieved. It starts with
	 * this activity's persistent identity, followed by the given usage's display ID,
	 * and ends with this activity's version.
	 * 
	 * @param displayId the display ID of the usage to be retrieved
	 * @return the matching usage if present, or {@code null} otherwise.
	 */
	public Usage getUsage(String displayId) {
		try {
			return usages.get(createCompliantURI(this.getPersistentIdentity().toString(),displayId,this.getVersion()));
		}
		catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the instance matching the given usage's identity URI.
	 *
	 * @param usageURI the identity URI of the usage to be retrieved
	 * @return the matching usage if present, or {@code null} otherwise.
	 */
	public Usage getUsage(URI usageURI) {
		return usages.get(usageURI);
	}
	
	/**
	 * Returns the set of usages owned by this activity.
	 *
	 * @return the set of usages owned by this activity.
	 */
	public Set<Usage> getUsages() {
		Set<Usage> usages = new HashSet<>();
		usages.addAll(this.usages.values());
		return usages;
	}
	
	/**
	 * Adds the given usage to the list of usages.
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated: 10604, 10605, 10803</li>
	 * <li>an SBOL validation rule violation occurred in {@link Identified#addChildSafely(Identified, java.util.Map, String, java.util.Map...)}</li>
	 * </ul>
	 */
	private void addUsage(Usage usage) throws SBOLValidationException {
		usage.setSBOLDocument(this.getSBOLDocument());
		addChildSafely(usage, usages, "usage", associations);
	}
	
	/**
	 * Removes the given usage from the list of usages.
	 * 
	 * @param usage the given usage
	 * @return {@code true} if the matching usage was removed successfully,
	 *         {@code false} otherwise.
	 */
	public boolean removeUsages(Usage usage) {
		return removeChildSafely(usage, usages);
	}
	
	/**
	 * Removes all entries of this activity's list of usages.
	 * The list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeUsages(Usage usage)} to iteratively remove
	 * each usage.
	 *
	 */
	public void clearUsages() {
		Object[] valueSetArray = usages.values().toArray();
		for (Object usage : valueSetArray) {
			removeUsages((Usage)usage);
		}
	}
	
	/**
	 * @param usages the usages to set
	 */
	void setUsages(Set<Usage> usages) throws SBOLValidationException {
		clearUsages();
		for (Usage usage : usages) {
			addUsage(usage);
		}
	}

}
