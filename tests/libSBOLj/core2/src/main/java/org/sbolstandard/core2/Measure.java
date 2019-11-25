package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an Measure object in the SBOL data model.
 * 
 * @author Chris Myers
 * @version 2.3
 */

public class Measure extends Identified {

	private Double hasNumericalValue;
	private URI hasUnit;
	private Set<URI> types;

	/**
	 * @param identity
	 * @param hasNumericalValue
	 * @param hasUnit
	 * @throws SBOLValidationException if any of the following condition is satisfied:
	 * <ul>
	 * <li>an SBOL validation rule violation occurred in {@link #setNumericalValue(doulbe)}.</li>
	 * <li>an SBOL validation rule violation occurred in {@link #setUnit(URI)}.</li>
	 * </ul>
	 */
	Measure(URI identity, double hasNumericalValue, URI hasUnit) throws SBOLValidationException {
		super(identity);
		setNumericalValue(hasNumericalValue);
		setUnit(hasUnit);
		this.types = new HashSet<>();
	}

	/**
	 * @param measure
	 * @throws SBOLValidationException
	 */
	private Measure(Measure measure) throws SBOLValidationException {
		super(measure);
		this.setNumericalValue(measure.getNumericalValue());
		this.setUnit(measure.getUnitURI());
		this.setTypes(measure.getTypes());
	}

	void copy(Measure measure) throws SBOLValidationException {
		((Identified)this).copy((Identified)measure);
		for (URI type : measure.getTypes()) {
			this.addType(URI.create(type.toString()));
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((hasNumericalValue == null) ? 0 : hasNumericalValue.hashCode());
		result = prime * result + ((hasUnit == null) ? 0 : hasUnit.hashCode());
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
		Measure other = (Measure) obj;
		if (hasNumericalValue == null) {
			if (other.hasNumericalValue != null)
				return false;
		} else if (!hasNumericalValue.equals(other.hasNumericalValue)) {
			return false;
		}
		if (hasUnit == null) {
			if (other.hasUnit != null)
				return false;
		} else if (!hasUnit.equals(other.hasUnit)) {
			return false;
		}
		if (types == null) {
			if (other.types != null)
				return false;
		} else if (!types.equals(other.types))
			return false;
		return true;
	}
		
	/**
	 * Returns the numerical value.
	 *
	 * @return the numerical value
	 */
	public Double getNumericalValue() {
		return hasNumericalValue;
	}

	/**
	 * @param hasNumericalValue the numerical value to set
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 13502.
	 */
	public void setNumericalValue(Double hasNumericalValue) throws SBOLValidationException {
		if (hasNumericalValue==null) {
			throw new SBOLValidationException("sbol-13502",this);
		}
		this.hasNumericalValue = hasNumericalValue;
	}

	/**
	 * Returns the unit URI.
	 *
	 * @return the unit URI
	 */
	public URI getUnitURI() {
		return hasUnit;
	}
	
	/**
	 * Returns the unit identity referenced by this measure.
	 *
	 * @return {@code null} if the associated SBOLDocument instance is {@code null} or no matching
	 * unity identity referenced by this measure exists; 
	 * or the matching unit otherwise.
	 */
	public URI getUnitIdentity() {
		if (this.getSBOLDocument()==null) return null;
		if (this.getSBOLDocument().getTopLevel(hasUnit)==null) return null;
		return this.getSBOLDocument().getTopLevel(hasUnit).getIdentity();
	}

	/**
	 * Returns the unit referenced by this measure.
	 *
	 * @return {@code null} if the associated SBOLDocument instance is {@code null} or no matching
	 * unit referenced by this measure exists; 
	 * or the matching unit otherwise.
	 */
	public TopLevel getUnit() {
		if (this.getSBOLDocument()==null) return null;
		return this.getSBOLDocument().getTopLevel(hasUnit);
	}

	/**
	 * @param hasUnit the unit to set
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 13503.
	 */
	public void setUnit(URI hasUnit) throws SBOLValidationException {
		if (hasUnit==null) {
			throw new SBOLValidationException("sbol-13503",this);
		}
		this.hasUnit = hasUnit;
	}

	/**
	 * Adds the given type URI to this measure's set of type URIs.
	 *
	 * @param typeURI the type URI to be added
	 * @return {@code true} if this set did not already contain the specified type, {@code false} otherwise.
	 */
	public boolean addType(URI typeURI) {
		return types.add(typeURI);
	}
	
	/**
	 * Removes the given type URI from the set of types.
	 *
	 * @param typeURI the given type URI to be removed
	 * @return {@code true} if the matching type reference was removed successfully, {@code false} otherwise.
	 */
	public boolean removeType(URI typeURI) {
		return types.remove(typeURI);
	}

	/**
	 * Returns the set of type URIs owned by this measure.
	 *
	 * @return the set of type URIs owned by this measure.
	 */
	public Set<URI> getTypes() {
		Set<URI> result = new HashSet<>();
		result.addAll(types);
		return result;
	}

	/**
	 * Clears the existing set of types first, and then adds the given
	 * set of the types to this measure.
	 *
	 * @param types the set of types to set to
	 */
	public void setTypes(Set<URI> types) {
		clearTypes();
		if (types==null) return;
		for (URI type : types) {
			addType(type);
		}
	}
	
	/**
	 * Checks if the given type URI is included in this measure's set of type URIs.
	 *
	 * @param typeURI the type URI to be checked
	 * @return {@code true} if this set contains the given type URI, {@code false} otherwise.
	 */
	public boolean containsType(URI typeURI) {
		return types.contains(typeURI);
	}
	
	/**
	 * Removes all entries of this measure's set of type URIs.
	 * The set will be empty after this call returns.	 
	 */
	public void clearTypes() {
		types.clear();
	}

	@Override
	Measure deepCopy() throws SBOLValidationException {
		return new Measure(this);
	}

	/**
	 * Updates this measure with a compliant URI.
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
		return "Measure ["
				+ super.toString()
				+ ", hasNumericalValue=" + hasNumericalValue
				+ ", hasUnit=" + hasUnit
				+ ", types=" + types 
				+ "]";
	}

}
