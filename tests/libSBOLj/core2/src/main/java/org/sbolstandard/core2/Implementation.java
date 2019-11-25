package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;

import java.net.URI;

/**
 * Represents an Implementation object in the SBOL data model.
 * 
 * @author Igor Durovic
 * @author Chris Myers
 * @version 2.3
 */

public class Implementation extends TopLevel {
	
	private URI built;

	/**
	 * @param identity
	 *            identity of the implementation
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in the following
	 *             constructor or method:
	 *             <ul>
	 *             <li>{@link TopLevel#TopLevel(URI)}, or</li>
	 *             </ul>
	 */
	Implementation(URI identity) throws SBOLValidationException {
		super(identity);
	}
	
	/**
	 * @param implementation
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in any of the
	 *             following constructors or methods:
	 *             <ul>
	 *             <li>{@link TopLevel#TopLevel(TopLevel)},</li>
	 *             </ul>
	 */
	private Implementation(Implementation implementation) throws SBOLValidationException {
		super(implementation);

		this.built = implementation.getBuiltURI();
	}
	
	/**
	 * Checks if the built property is set.
	 * 
	 * @return {@code true} if it is not {@code null}, {@code false} otherwise
	 */
	public boolean isSetBuilt() {
		return built != null;
	}
	
	/**
	 * Returns the URI of the component definition or module definition built
	 *
	 * @return the URI of built
	 */
	public URI getBuiltURI() {
		return this.built;
	}
	
	/**
	 * Returns the component definition or module definition referenced by the built field.
	 *
	 * @return {@code null} if the associated SBOLDocument instance is {@code null}
	 *         or no matching component definition or module definition referenced;
	 *         or the matching component definition or module definition otherwise.
	 */
	public TopLevel getBuilt() {
		if (this.getSBOLDocument() == null)
			return null;
		
		if(this.getSBOLDocument().getComponentDefinition(built) == null) {
			return this.getSBOLDocument().getModuleDefinition(built);
		}
		
		return this.getSBOLDocument().getComponentDefinition(built);
	}

	/**
	 * Sets the URI of the built property to the given one.
	 *
	 * @param builtURI
	 *            the given URI to set to
	 * @throws SBOLValidationException 
	 * 				on SBOL validation rule violation XXXXX.
	 */
	public void setBuilt(URI builtURI) throws SBOLValidationException {
		if(this.getSBOLDocument() != null && this.getSBOLDocument().isComplete() &&
				this.getSBOLDocument().getComponentDefinition(builtURI) == null &&
				this.getSBOLDocument().getModuleDefinition(builtURI) == null) {
			throw new SBOLValidationException("sbol-13103", this);
		}
		
		this.built = builtURI;
	}
	
	/**
	 * Sets the the built property to the given one.
	 *
	 * @param built
	 *            the given component definition or module definition to set to
	 * @throws SBOLValidationException 
	 * 				on SBOL validation rule violation XXXXX.
	 */
	public void setBuilt(TopLevel built) throws SBOLValidationException {
		if(!(built instanceof ComponentDefinition) && !(built instanceof ModuleDefinition)) {
			throw new SBOLValidationException("sbol-XXXXX", this);
		}
		
		this.built = built.getIdentity();
	}

	/**
	 * Sets the built property of the Implementation to {@code null}.
	 */
	public void unsetBuilt() {
		this.built = null;
	}

	@Override
	Implementation deepCopy() throws SBOLValidationException {
		return new Implementation(this);
	}

	void copy(Implementation implementation) throws SBOLValidationException {
		((TopLevel) this).copy((TopLevel) implementation);
		
		if (implementation.isSetBuilt()) {
			this.setBuilt(implementation.getBuiltURI());
		}
	}
	
	/**
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in any of the
	 *             following constructors or methods:
	 *             <ul>
	 *             <li>{@link #deepCopy()},</li>
	 *             <li>{@link URIcompliance#createCompliantURI(String, String, String)},</li>
	 *             <li>{@link #setDisplayId(String)},</li>
	 *             <li>{@link #setVersion(String)},</li>
	 *             <li>{@link #setWasDerivedFrom(URI)},</li>
	 *             <li>{@link #setIdentity(URI)}</li>
	 *             </ul>
	 */
	@Override
	Implementation copy(String URIprefix, String displayId, String version) throws SBOLValidationException {
		Implementation cloned = this.deepCopy();
		cloned.setPersistentIdentity(createCompliantURI(URIprefix, displayId, ""));
		cloned.setDisplayId(displayId);
		cloned.setVersion(version);
		URI newIdentity = createCompliantURI(URIprefix, displayId, version);

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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode() * prime;

		result = prime * result + (this.isSetBuilt() ? this.built.hashCode() : 0);

		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbolstandard.core2.abstract_classes.Documented#equals(java.lang.Instance)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Implementation other = (Implementation) obj;
		if (built == null) {
			if (other.built != null)
				return false;
		} else if (!built.equals(other.built))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Implementation [" + super.toString()
				+ (this.isSetBuilt() ? ", built=" + this.getBuiltURI() : "")
				+ "]";
	}

}
