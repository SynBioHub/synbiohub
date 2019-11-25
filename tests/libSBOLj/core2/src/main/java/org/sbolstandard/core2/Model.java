package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;
import java.net.URI;

/**
 * Represents a Model object in the SBOL data model.
 * 
 * @author Zhen Zhang
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */

public class Model extends TopLevel {

	private URI source;
	private URI language;
	private URI framework;

	/**
	 * @param identity
	 * @param source
	 * @param language
	 * @param framework
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in either of the following
	 * constructors or methods:
	 * <ul>
	 * <li>{@link TopLevel#TopLevel(URI)},</li>
	 * <li>{@link #setSource(URI)},</li>
	 * <li>{@link #setLanguage(URI)}, or</li>
	 * <li>{@link #setFramework(URI)}.</li>
	 * </ul>
	 */
	Model(URI identity,URI source, URI language, URI framework) throws SBOLValidationException {
		super(identity);
		setSource(source);
		setLanguage(language);
		setFramework(framework);
	}

	/**
	 * @param model
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of 
	 * the following constructors or methods:
	 * <ul>
	 * <li>{@link TopLevel#TopLevel(TopLevel)},</li>
	 * <li>{@link #setSource(URI)},</li>
	 * <li>{@link #setLanguage(URI)}, or</li>
	 * <li>{@link #setFramework(URI)}.</li>
	 * </ul>
	 */
	private Model(Model model) throws SBOLValidationException {
		super(model);
		this.setSource(model.getSource());
		this.setLanguage(model.getLanguage());
		this.setFramework(model.getFramework());
	}
	
	void copy(Model model) throws SBOLValidationException {
		((TopLevel)this).copy((TopLevel)model);
	}

	/**
	 * Returns the this model's source property.
	 *
	 * @return the this model's source property
	 */
	public URI getSource() {
		return source;
	}

	/**
	 * Sets the source property to the given one.
	 * 
	 * @param source the source property to set
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 11502.
	 */
	public void setSource(URI source) throws SBOLValidationException {
		if (source==null) {
			throw new SBOLValidationException("sbol-11502", this);
		}
		this.source = source;
	}

	/**
	 * Returns this model's language property.
	 *
	 * @return this model's language property
	 */
	public URI getLanguage() {
		return language;
	}

	/**
	 * Sets the language property to the given one.
	 *
	 * @param language the language property to set to
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 11504.
	 * 
	 */
	public void setLanguage(URI language) throws SBOLValidationException {
		if (language==null) {
			throw new SBOLValidationException("sbol-11504",this);
		}
		this.language = language;
	}

	/**
	 * Returns the URI of the framework property of this Model object.
	 *
	 * @return the URI of the framework property of this Model object
	 */
	public URI getFramework() {
		return framework;
	}

	/**
	 * Sets the framework property to the given one.
	 *
	 * @param framework the framework to set to
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 11508.
	 */
	public void setFramework(URI framework) throws SBOLValidationException {
		if (framework==null) {
			throw new SBOLValidationException("sbol-11508", this);
		}
		this.framework = framework;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((framework == null) ? 0 : framework.hashCode());
		result = prime * result + ((language == null) ? 0 : language.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
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
		Model other = (Model) obj;
		if (framework == null) {
			if (other.framework != null)
				return false;
		} else if (!framework.equals(other.framework))
			return false;
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.TopLevel#deepCopy()
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #Model(Model)}.
	 */
	@Override
	Model deepCopy() throws SBOLValidationException {
		return new Model(this);
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
	Model copy(String URIprefix, String displayId, String version) throws SBOLValidationException {
		Model cloned = this.deepCopy();
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
		return "Model ["
				+ "identity=" + this.getIdentity()
				+ (this.isSetDisplayId()?", displayId=" + this.getDisplayId():"") 
				+ (this.isSetName()?", name=" + this.getName():"")
				+ (this.isSetDescription()?", description=" + this.getDescription():"") 
				+ ", source=" + source 
				+ ", language=" + language 
				+ ", framework=" + framework
				+ "]";
	}

}
