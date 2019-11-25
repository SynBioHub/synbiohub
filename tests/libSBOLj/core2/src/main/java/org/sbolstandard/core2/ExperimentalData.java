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

public class ExperimentalData extends TopLevel{

	/**
	 * @param identity
	 * @throws SBOLValidationException if an SBOL validation rule was violated in the following constructor:
	 * {@link TopLevel#TopLevel(URI)}. 
	 */
	ExperimentalData(URI identity) throws SBOLValidationException {
		super(identity);
	}

	/**
	 * @param collection
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in the following 
	 * constructor or method:
	 * <ul>
	 * <li>{@link TopLevel#TopLevel(TopLevel)}, or</li>
	 * <li>{@link #setMembers(Set)}.</li>
	 * </ul>
	 */
	private ExperimentalData(ExperimentalData collection) throws SBOLValidationException {
		//super(collection.getIdentity());
		super(collection);
	}
	
	void copy(ExperimentalData collection) throws SBOLValidationException {
		((TopLevel)this).copy((TopLevel)collection);
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.abstract_classes.Documented#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
		ExperimentalData other = (ExperimentalData) obj;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.abstract_classes.TopLevel#deepCopy()
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #Collection(ExperimentalData)}.
	 */
	@Override
	ExperimentalData deepCopy() throws SBOLValidationException {
		return new ExperimentalData(this);
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
	ExperimentalData copy(String URIprefix, String displayId, String version) throws SBOLValidationException {
		ExperimentalData cloned = this.deepCopy();
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
		return "ExpermentalData ["
				+ super.toString()
				+ "]";
	}

}
