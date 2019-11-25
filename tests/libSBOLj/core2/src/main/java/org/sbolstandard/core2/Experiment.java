package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an Collection object in the SBOL data model.
 * 
 * @author Zhen Zhang
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */

public class Experiment extends TopLevel{

	private Set<URI> experimentalData;

	/**
	 * @param identity
	 * @throws SBOLValidationException if an SBOL validation rule was violated in the following constructor:
	 * {@link TopLevel#TopLevel(URI)}. 
	 */
	Experiment(URI identity) throws SBOLValidationException {
		super(identity);
		this.experimentalData = new HashSet<>();
	}

	/**
	 * @param experiment
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in the following 
	 * constructor or method:
	 * <ul>
	 * <li>{@link TopLevel#TopLevel(TopLevel)}, or</li>
	 * <li>{@link #setExpermentalData(Set)}.</li>
	 * </ul>
	 */
	private Experiment(Experiment experiment) throws SBOLValidationException {
		//super(collection.getIdentity());
		super(experiment);
		this.experimentalData = new HashSet<>();
		for (URI experimentalDataURI : experiment.getExperimentalDataURIs()) {
			this.addExperimentalData(experimentalDataURI);
		}
	}
	
	void copy(Experiment experiment) throws SBOLValidationException {
		((TopLevel)this).copy((TopLevel)experiment);
		for (URI experimentalDataURI : experiment.getExperimentalDataURIs()) {
			this.addExperimentalData(experimentalDataURI);
		}
	}

	/**
	 * Adds the given experimentalData URI to this Experiment's
	 * set of reference experimentalData URIs.
	 *
	 * @param experimentalDataURI References to a ExperimentalData instance
	 * @return {@code true} if the matching experimentalData reference has been added successfully,
	 *         {@code false} otherwise.
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 1xxxx.
	 */
	public boolean addExperimentalData(URI experimentalDataURI) throws SBOLValidationException {
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (this.getSBOLDocument().getExperimentalData(experimentalDataURI)==null) {
				throw new SBOLValidationException("sbol-13403", this);
			}
		}
		return experimentalData.add(experimentalDataURI);
	}

	/**
	 * Removes the given experimentalData from this Experiment's set of experimentalData.
	 *
	 * @param experimentalDataURI the experimentalData identity URI to be removed from this Experiment's experimentalData
	 * @return {@code true} if the matching experimentalData is removed successfully,
	 *         {@code false} otherwise.
	 */
	public boolean removeExperimentalData(URI experimentalDataURI) {
		return experimentalData.remove(experimentalDataURI);
	}

	/**
	 * Clears the existing set of experimentalData of this Experiment instance first, then adds the given
	 * set of the experimentalData references to it.
	 * 
	 * @param experimentalData a set of identity URIs of zero or more TopLevel instances to be added
	 * @throws SBOLValidationException see SBOL validation rule violation at {@link Experiment#addExperimentalData(URI)}
	 */
	public void setExperimentalData(Set<URI> experimentalData) throws SBOLValidationException {
		clearExperimentalData();
		for (URI experimentalDataURI : experimentalData) {
			addExperimentalData(experimentalDataURI);
		}
	}

	/**
	 * Returns the set of experimentalData URIs referenced by this Experiment instance.
	 *
	 * @return the set of experimentalData URIs referenced by this Experiment instance.
	 */
	public Set<URI> getExperimentalDataURIs() {
		Set<URI> result = new HashSet<>();
		result.addAll(experimentalData);
		return result;
	}

	/**
	 * Returns the set of experimentalData identities referenced by this Experiment instance.
	 *
	 * @return the set of experimentalData identities referenced by this Experiment instance.
	 */
	public Set<URI> getExperimentalDataIdentities() {
		Set<URI> result = new HashSet<>();
		for (URI experimentalDataURI : experimentalData) {
			ExperimentalData experimentalData = this.getSBOLDocument().getExperimentalData(experimentalDataURI);
			if(experimentalData != null) {
				result.add(experimentalData.getIdentity());
			}
		}
		return result;
	}

	/**
	 * Returns the set of objects referenced by this Experiment's experimentalData.
	 *
	 * @return the set of objects referenced by this Experiment's experimentalData.
	 */
	public Set<ExperimentalData> getExperimentalData() {
		Set<ExperimentalData> result = new HashSet<>();
		for (URI experimentalDataURI : experimentalData) {
			ExperimentalData experimentalData = this.getSBOLDocument().getExperimentalData(experimentalDataURI);
			if(experimentalData != null) {
				result.add(experimentalData);
			}
		}
		return result;
	}

	/**
	 * Checks if the given URI is included in this Experiment
	 * instance's set of experimentalData URIs.
	 *
	 * @param experimentalDataURI a URI that is checked against this Experiment's list of experimentalData URIs 
	 * @return {@code true} if its experimentalData contain the given URI, {@code false} otherwise.
	 */
	public boolean containsExperimentalData(URI experimentalDataURI) {
		return experimentalData.contains(experimentalDataURI);
	}

	/**
	 * Removes all entries of this Experiment's set of reference
	 * experimentalData URIs. The set will be empty after this call returns.
	 */
	public void clearExperimentalData() {
		experimentalData.clear();
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.abstract_classes.Documented#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((experimentalData == null) ? 0 : experimentalData.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.abstract_classes.Documented#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Experiment other = (Experiment) obj;
		if (experimentalData == null) {
			if (other.experimentalData != null)
				return false;
		} else if (!experimentalData.equals(other.experimentalData)) {
			if (getExperimentalDataIdentities().size()!=getExperimentalDataURIs().size() ||
					other.getExperimentalDataIdentities().size()!=other.getExperimentalDataURIs().size() ||
					!getExperimentalDataIdentities().equals(other.getExperimentalDataIdentities())) {
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.abstract_classes.TopLevel#deepCopy()
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #Collection(Experiment)}.
	 */
	@Override
	Experiment deepCopy() throws SBOLValidationException {
		return new Experiment(this);
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.abstract_classes.TopLevel#copy(java.lang.String, java.lang.String, java.lang.String)
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of the following methods:
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
	Experiment copy(String URIprefix, String displayId, String version) throws SBOLValidationException {
		Experiment cloned = this.deepCopy();
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

	@Override
	public String toString() {
		return "Experiment ["
				+ super.toString()
				+ (experimentalData.size()>0?", experimentalData=" + experimentalData:"")  
				+ "]";
	}

}
