package org.sbolstandard.core2;

import java.net.URI;

import javax.xml.namespace.QName;

/**
 * Represents a Range extension object of the SBOL Location class.
 * 
 * @author Zhen Zhang
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */

public class Range extends Location {

	private int start = 0;
	private int end = 0;

	/**
	 * @param identity
	 * @param start
	 * @param end
	 * @throws SBOLValidationException if if an SBOL validation rule violation occurred 
	 * in any of the following constructors or methods:
	 * <ul>
	 * <li>{@link Location#Location(URI)}, </li>
	 * <li>{@link #setEnd(int)}, or </li>
	 * <li>{@link #setStart(int)}.</li>
	 * </ul>
	 */
	Range(URI identity, int start, int end) throws SBOLValidationException {
		super(identity);
		setEnd(end);
		setStart(start);
	}

	/**
	 * @param range
	 * @throws SBOLValidationException if an SBOL validation rule violation 
	 * occurred in any of the following constructors or methods:
	 * <ul>
	 * <li>{@link Location#Location(Location)},</li>
	 * <li>{@link #setEnd(int)}, or</li>
	 * <li>{@link #setStart(int)}.</li>
	 * </ul>
	 */
	private Range(Range range) throws SBOLValidationException {
		super(range);
		this.setEnd(range.getEnd());
		this.setStart(range.getStart());
	}
	
	void copy(Range range) throws SBOLValidationException {
		((Location)this).copy((Location)range);
	}

	/**
	 * Sets the start position of this range.
	 * 
	 * @param value the start position of this range
	 * @throws SBOLValidationException if either of the following SBOL validation rules was violated: 11102, 11104. 
	 */
	public void setStart(int value) throws SBOLValidationException {
		if (value<=0) {
			throw new SBOLValidationException("sbol-11102", this);
		}
		if (value > end) {
			throw new SBOLValidationException("sbol-11104", this);
		}
		start = value;
	}

	/**
	 * Returns the start position of this range.
	 *
	 * @return the start position of this range
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Returns the end position of this range.
	 *
	 * @return the end position of this range 
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * Sets the end position of this range.
	 * 
	 * @param value the end position to set to
	 * @throws SBOLValidationException if either of the following SBOL validation rules was violated: 11103, 11104.
	 */
	public void setEnd(int value) throws SBOLValidationException {
		if (value<=0) {
			throw new SBOLValidationException("sbol-11103", this);
		}
		if (value < start) {
			throw new SBOLValidationException("sbol-11104", this);
		}
		end = value;
	}


	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.Location#deepCopy()
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in 
	 * {@link #Range(Range)}.
	 */
	@Override
	Location deepCopy() throws SBOLValidationException {
		return new Range(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + end;
		result = prime * result + ((this.getOrientation() == null) ? 0 : this.getOrientation().hashCode());
		result = prime * result + ((this.isSetSequence()) ? this.getSequenceURI().hashCode() : 0);
		result = prime * result + start;
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
		Range other = (Range) obj;
		if (end != other.end)
			return false;
		if (this.getOrientation() != other.getOrientation())
			return false;
		if (!this.isSetSequence()) {
			if (other.isSetSequence())
				return false;
		} else if (!this.getSequence().equals(other.getSequence())) {
			return false;
		}
		return start == other.start;
	}

	@Override
	public String toString() {
		return "Range [" 
				+ super.toString() 
				+ ", start=" + start 
				+ ", end=" + end
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
			int result = this.start - ((Range)location).getStart();
			if (result==0) {
				result = this.end - ((Range)location).getEnd();
			}
			return result;
		} else if (location instanceof Cut) {
			int result = this.start - ((Cut)location).getAt();
			if (result==0) {
				result = this.end - ((Cut)location).getAt(); 
			}
			return result;
		}
		return -1*(Integer.MAX_VALUE);
		//return this.start;
	}
}
