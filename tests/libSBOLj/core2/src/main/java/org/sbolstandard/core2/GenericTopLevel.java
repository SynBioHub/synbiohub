package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;

import java.net.URI;

import javax.xml.namespace.QName;

/**
 * Represents a GenericTopLevel object in the SBOL data model.
 * 
 * @author Zhen Zhang
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */

public class GenericTopLevel extends TopLevel{

	private QName rdfType;

	/**
	 * @param identity
	 * @param rdfType
	 * @throws SBOLValidationException if either of the following condition is satisfied: 
	 * <ul>
	 * <li>if an SBOL validation rule violation occurred in {@link TopLevel#TopLevel(URI)}, or</li>
	 * <li>the following SBOL validation rule was violated: 12302.</li>
	 * </ul>
	 */
	GenericTopLevel(URI identity, QName rdfType) throws SBOLValidationException {
		super(identity);
		if (rdfType.getNamespaceURI().equals(Sbol2Terms.sbol2.getNamespaceURI())/* ||
				rdfType.getNamespaceURI().equals(Sbol1Terms.sbol1.getNamespaceURI())*/) {
			throw new SBOLValidationException("sbol-12303",this);
		}
		// TODO: should update based on documents namespaces, i.e., use prefix for this document,
		// generate new prefix if overlaps existing one.
		this.rdfType = rdfType;
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
	private GenericTopLevel(GenericTopLevel genericTopLevel) throws SBOLValidationException {
		super(genericTopLevel);
		this.setRDFType(genericTopLevel.getRDFType());
	}
	
	void copy(GenericTopLevel genericTopLevel) throws SBOLValidationException {
		((TopLevel)this).copy((TopLevel)genericTopLevel);
	}

	/**
	 * Returns the RDF type property of this GenericTopLevel instance.
	 *
	 * @return the RDF type property of this GenericTopLevel instance
	 */
	public QName getRDFType() {
		return rdfType;
	}

	/**
	 * Set the RDF type property of this GenericTopLevel instance to the specified one.
	 * 
	 * @param rdfType the RDF type property
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 12302.
	 */
	public void setRDFType(QName rdfType) throws SBOLValidationException {
		if (rdfType == null) {
			throw new SBOLValidationException("sbol-12302", this);
		}
		this.rdfType = rdfType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((rdfType == null) ? 0 : rdfType.hashCode());
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
		GenericTopLevel other = (GenericTopLevel) obj;
		if (rdfType == null) {
			if (other.rdfType != null)
				return false;
		} else if (!rdfType.equals(other.rdfType))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.TopLevel#deepCopy()
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule was violated in 
	 * {@link #GenericTopLevel(GenericTopLevel)}.
	 */
	@Override
	GenericTopLevel deepCopy() throws SBOLValidationException {
		return new GenericTopLevel(this);
	}

	//	/**
	//	 * @param newDisplayId
	//	 * @return
	//	 */
	//	public GenericTopLevel copy(String newDisplayId) {
	//		GenericTopLevel cloned = (GenericTopLevel) this.deepCopy();
	//		cloned.updateCompliantURI(newDisplayId);
	//		return cloned;
	//	}
	//
	//	/**
	//	 * Get a deep copy of the object first, and set its major version to the specified value, and minor version to "0".
	//	 * @param newVersion
	//	 * @return the copied {@link ComponentDefinition} instance with the specified major version.
	//	 */
	//	public GenericTopLevel newVersion(String newVersion) {
	//		GenericTopLevel cloned = (GenericTopLevel) super.newVersion(newVersion);
	//		cloned.updateVersion(newVersion);
	//		return cloned;
	//	}

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
	GenericTopLevel copy(String URIprefix, String displayId, String version) throws SBOLValidationException {
		GenericTopLevel cloned = this.deepCopy();
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
		return "GenericTopLevel ["
				+ super.toString()
				+ ", rdfType=" + rdfType 
				+ "]";
	}

}
