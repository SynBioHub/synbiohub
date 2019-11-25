
package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Measured object in the SBOL data model.
 * 
 * @author Zhen Zhang
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */

public abstract class Measured extends Identified {

	private HashMap<URI, Measure> measures;

	/**
	 * @param identity
	 * @throws SBOLValidationException if any of the following condition is satisfied:
	 * <ul>
	 * <li>an SBOL validation rule violation occurred in {@link Identified#Identified(URI)};</li>
	 * </ul>
	 */
	Measured(URI identity) throws SBOLValidationException {
		super(identity);
		this.measures = new HashMap<>();
	}

	/**
	 * @param measured
	 * @throws SBOLValidationException if any of the following condition is satisfied:
	 * <ul>
	 * <li>an SBOL validation rule violation occurred in {@link Identified#Identified(Identified)};</li>
	 * </ul>
	 */
	Measured(Measured measured) throws SBOLValidationException {
		super(measured);
		setMeasures(measured.getMeasures());
	}
	
	void copy(Measured measured) throws SBOLValidationException {
		((Identified)this).copy(measured);
		for (Measure measure : measured.getMeasures()) {
			String displayId = URIcompliance.findDisplayId(measure);
			Measure newMeasure = this.createMeasure(displayId, measure.getNumericalValue(), measure.getUnitURI());
			newMeasure.copy(measure);
		}		
	}

	@Override
	abstract Measured deepCopy() throws SBOLValidationException;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((measures == null) ? 0 : measures.hashCode());
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
		Measured other = (Measured) obj;
		if (measures == null) {
			if (other.measures != null)
				return false;
		} else if (!measures.equals(other.measures))
			return false;
		return true;
	}

	/**
	 * Calls the Measure constructor to create a new measure using the given arguments,
	 * then adds to the list of measures owned by this measured object.
	 * 
	 * @param identity the URI identity of the measure to be created
	 * @param numericalValue the numerical value for this measure
	 * @param unit the unit of this measure
	 * @return the created measure
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul>
	 * <li>an SBOL validation rule violation occurred in {@link Measure#Measure(URI, URI, Set)}; or</li>
	 * <li>an SBOL validation rule violation occurred in {@link #addMeasure(Measure)}.</li>
	 * </ul>
	 */
	private Measure createMeasure(URI identity, Double numericalValue, URI unit) throws SBOLValidationException {
		Measure measure = new Measure(identity, numericalValue, unit);
		addMeasure(measure);
		return measure;
	}
	
	/**
	 * Creates a child measure for this measured object with the given arguments, 
	 * and then adds to this measured object's list of measures.
	 * <p>
	 * This method first creates a compliant URI for the measure to be created. It starts with this measured object's
	 * persistent identity URI, followed by the given display ID, and ends with this measured object's version.
	 *
	 *
	 * @param displayId The displayId identifier for this
	 * @param numericalValue the numerical value for this measure
	 * @param unit the unit of this measure
	 * @return the created measure
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated: 
	 * 10201, 10202, 10204, 10206, 13501, 13502, 13503, 13504, 13505, 
	 */
	public Measure createMeasure(String displayId, Double numericalValue, URI unit) throws SBOLValidationException {
		String parentPersistentIdStr = this.getPersistentIdentity().toString();
		String version = this.getVersion();
		if(parentPersistentIdStr == null) {
			throw new IllegalStateException(
					"Cannot create a child on a parent that has the non-standard compliant identity " +
							this.getIdentity());
		}
		//validateIdVersion(displayId, version);
		Measure m = createMeasure(
				createCompliantURI(parentPersistentIdStr, displayId, version), numericalValue, unit);
		m.setPersistentIdentity(createCompliantURI(parentPersistentIdStr, displayId, ""));
		m.setDisplayId(displayId);
		m.setVersion(version);
		return m;
	}

	/**
	 * Adds the given measure to the list of measures owned by this measured object.
	 *
	 * @param measure the measure to be added
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul>
	 * <li>the following SBOL validation rule was violated:</li>
	 * <li>an SBOL validation rule violation occurred in {@link Identified#addChildSafely(Identified, java.util.Map, String, java.util.Map...)}</li>
	 * </ul>
	 */
	private void addMeasure(Measure measure) throws SBOLValidationException {
		addChildSafely(measure, measures, "measure");
		measure.setSBOLDocument(this.getSBOLDocument());
	}

	/**
	 * Removes the given measure from this measured object's list of measures.
	 *
	 * @param measure the given measure to be removed
	 * @return {@code true} if the matching measure was removed successfully,
	 *         {@code false} otherwise
	 */
	public boolean removeMeasure(Measure measure) {
		return removeChildSafely(measure,measures);
	}

	/**
	 * Returns the measure matching the given display ID from
	 * this measured object's list of measures.
	 * <p>
	 * This method first creates a compliant URI for the measure to be retrieved. It starts with this
	 * measured object's persistent identity, followed by the given display ID, and ends with this measured object's version.
	 *
	 * @param displayId the display ID of the measure to be retrieved 
	 * @return the matching measure if present, or {@code null} otherwise
	 */
	public Measure getMeasure(String displayId) {
		try {
			return measures.get(createCompliantURI(this.getPersistentIdentity().toString(),displayId,this.getVersion()));
		}
		catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the measure matching the given measure URI from this
	 * measured object's list of measures.
	 *
	 * @param measureURI the identity URI for the measure to be retrieved
	 * @return the matching measure if present, or
	 *         {@code null} otherwise
	 */
	public Measure getMeasure(URI measureURI) {
		return measures.get(measureURI);
	}

	/**
	 * Returns the set of measures owned by this
	 * measured object.
	 *
	 * @return the set of the set of measures owned by this
	 * measured object
	 */
	public Set<Measure> getMeasures() {
		return new HashSet<>(measures.values());
	}

	/**
	 * Removes all entries of this measured object list of measures.
	 * The list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeMeasure(Measure)} to iteratively remove
	 * each measure owned by this object.
	 *
	 */
	public void clearMeasures() {
		Object[] valueSetArray = measures.values().toArray();
		for (Object measure : valueSetArray) {
			removeMeasure((Measure)measure);
		}
	}

	/**
	 * Clears the existing list of measures, then adds the given set of measures to this list.
	 * 
	 * @param measures the given set of measures to set to
	 * @throws SBOLValidationException an SBOL validation rule violation occurred in {@link #addMeasure(Measure)}
	 */	
	void setMeasures(Set<Measure> measures) throws SBOLValidationException {
		clearMeasures();
		for (Measure measure : measures) {
			addMeasure(measure);
		}
	}

	@Override
	public String toString() {
		return super.toString() 
				+ (measures.size()>0?", measures=" + measures:"");
	}

}
