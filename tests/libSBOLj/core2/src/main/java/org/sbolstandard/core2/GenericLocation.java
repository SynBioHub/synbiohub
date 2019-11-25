package org.sbolstandard.core2;

import java.net.URI;

/**
 * Represents a GenericLocation extension object of the SBOL Location class.
 * 
 * @author Zhen Zhang
 * @author Chris Myers
 * @version 2.1
 */

public class GenericLocation extends Location{
	
	/**
	 * @param identity
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link Location#Location(URI)}.
	 */
	GenericLocation(URI identity) throws SBOLValidationException {
		super(identity);
	}
	
	/**
	 * @param genericLocation
	 * @throws SBOLValidationException SBOLValidationException if an SBOL validation rule violation occurred in {@link Location#Location(Location)}.
	 */
	private GenericLocation(GenericLocation genericLocation) throws SBOLValidationException {
		super(genericLocation);
	}
	
	void copy(GenericLocation genericLocation) throws SBOLValidationException {
		((Location)this).copy((Location)genericLocation);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.getOrientation() == null) ? 0 : this.getOrientation().hashCode());
		result = prime * result + ((this.isSetSequence()) ? this.getSequenceURI().hashCode() : 0);
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
		GenericLocation other = (GenericLocation) obj;
		if (!this.isSetSequence()) {
			if (other.isSetSequence())
				return false;
		} else if (!this.getSequence().equals(other.getSequence())) {
			return false;
		}
		return this.getOrientation() == other.getOrientation();
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.Location#deepCopy()
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #GenericLocation(GenericLocation)}.
	 */
	@Override
	GenericLocation deepCopy() throws SBOLValidationException {
		return new GenericLocation(this);
	}

	@Override
	public String toString() {
		return "GenericLocation ["
				+ super.toString()
				+ "]";
	}

	@Override
	public int compareTo(Location location) {
		return Integer.MAX_VALUE;
	}
}
