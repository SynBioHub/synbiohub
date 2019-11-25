package org.sbolstandard.core2;

import java.net.URI;

import javax.xml.namespace.QName;

/**
 * Represents the Cut extension of the SBOL Location class.
 * 
 * @author Zhen Zhang
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */

public class Cut extends Location{

	private int at;
	
	/**
	 * @param identity
	 * @param at
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in either of 
	 * the following constructor or method:
	 * {@link Location#Location(URI)} or {@link #setAt(int)}.
	 */
	Cut(URI identity, int at) throws SBOLValidationException {
		super(identity);
		setAt(at);
	}

	/**
	 * @param cut
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in 
	 * either of the following constructors or methods:
	 * <ul>
	 * <li>{@link Location#Location(Location)}, or</li>
	 * <li>{@link #setAt(int)}.</li>
	 * </ul>
	 */
	private Cut(Cut cut) throws SBOLValidationException {
		super(cut);
		this.setAt(cut.getAt());
	}
	
	void copy(Cut cut) throws SBOLValidationException {
		((Location)this).copy((Location)cut);
	}

	/**
	 * Returns the at property of this Cut instance.
	 *
	 * @return the at property of this Cut instance
	 */
	public int getAt() {
		return at;
	}

	/**
	 * Sets the at property of this Cut instance to the given one.
	 *
	 * @param at the given at property
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 11202. 
	 */
	public void setAt(int at) throws SBOLValidationException {
		if (at<0) {
			throw new SBOLValidationException("sbol-11202", this);
		}
		this.at = at;
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.Location#deepCopy()
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #Cut(Cut)}.
	 */
	@Override
	Cut deepCopy() throws SBOLValidationException {
		return new Cut(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + at;
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
		Cut other = (Cut) obj;
		if (at != other.at)
			return false;
		if (!this.isSetSequence()) {
			if (other.isSetSequence())
				return false;
		} else if (!this.getSequence().equals(other.getSequence())) {
			return false;
		}
		return this.getOrientation() == other.getOrientation();
	}

	@Override
	public String toString() {
		return "Cut ["
				+ super.toString()
				+ ", at=" + at 
				+ "]";
	}

	@Override
	public int compareTo(Location location) {
		int thisPos = -1;
		Annotation annotation = this.getAnnotation(new QName(GenBank.GBNAMESPACE,GenBank.POSITION,GenBank.GBPREFIX));
		if (annotation!=null) {
			thisPos = Integer.parseInt(annotation.getStringValue().replace("position",""));
		}
		int otherPos = -1;
		annotation = location.getAnnotation(new QName(GenBank.GBNAMESPACE,GenBank.POSITION,GenBank.GBPREFIX));
		if (annotation!=null) {
			otherPos = Integer.parseInt(annotation.getStringValue().replace("position",""));
		}
		if (thisPos != -1 && otherPos != -1) {
			int result = thisPos - otherPos;
			return result;
		}
		if (location instanceof Range) {
			int result = this.at - ((Range)location).getStart();
			if (result==0) {
				result = this.at - ((Range)location).getEnd();
			}
			return result;
		} else if (location instanceof Cut) {
			return this.at - ((Cut)location).getAt();
		} 
		return -1*(Integer.MAX_VALUE);
		//return this.at;
	}
}
