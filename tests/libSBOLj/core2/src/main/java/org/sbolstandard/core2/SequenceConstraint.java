package org.sbolstandard.core2;

import java.net.URI;

import static org.sbolstandard.core2.URIcompliance.*;

/**
 * Represents a SequenceConstraint object in the SBOL data model.
 * 
 * @author Zhen Zhang
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */

public class SequenceConstraint extends Identified {

	//private RestrictionType restriction;
	private URI restriction;
	private URI subject;
	private URI object;
	/**
	 * the parent component definition of this sequence constraint
	 */
	private ComponentDefinition componentDefinition = null;
	
	/**
	 * @param identity
	 * @param restriction
	 * @param subject
	 * @param object
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of the following constructors or methods:
	 * <ul>
	 * <li>{@link Identified#Identified(URI)},</li>
	 * <li>{@link #setRestriction(URI)},</li>
	 * <li>{@link #setSubject(URI)}, or</li>
	 * <li>{@link #setObject(URI)},</li>
	 * </ul>
	 */
	SequenceConstraint(URI identity, URI restriction, URI subject, URI object) throws SBOLValidationException {
		super(identity);
		setRestriction(restriction);
		setSubject(subject);
		setObject(object);
	}
	
	/**
	 * @param identity
	 * @param restriction
	 * @param subject
	 * @param object
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of 
	 * the following methods:
	 * <ul>
	 * <li>{@link Identified#Identified(URI)},</li>
	 * <li>{@link #setRestriction(RestrictionType)},</li>
	 * <li>{@link #setSubject(URI)}, or</li>
	 * <li>{@link #setObject(URI)}.</li>
	 * </ul>
	 */
	SequenceConstraint(URI identity, RestrictionType restriction, URI subject, URI object) throws SBOLValidationException {
		super(identity);
		setRestriction(restriction);
		setSubject(subject);
		setObject(object);
	}
	
	/**
	 * @param sequenceConstraint
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of 
	 * the following constructors or methods:
	 * <ul>
	 * <li>{@link Identified#Identified(Identified)},</li>
	 * <li>{@link #setSubject(URI)}, or </li>
	 * <li>{@link #setObject(URI)}</li>
	 * </ul>
	 */
	private SequenceConstraint(SequenceConstraint sequenceConstraint) throws SBOLValidationException {
		super(sequenceConstraint);
		this.setRestriction(sequenceConstraint.getRestrictionURI());
		this.setSubject(sequenceConstraint.getSubjectURI());
		this.setObject(sequenceConstraint.getObjectURI());
	}
	
	void copy(SequenceConstraint sequenceConstraint) throws SBOLValidationException {
		((Identified)this).copy((Identified)sequenceConstraint);
	}
	
	/**
	 * Returns the restriction property of this sequence constraint.
	 * 
	 * @return the restriction property of this sequence constraint
	 */
	public RestrictionType getRestriction() {
		try {
			return RestrictionType.convertToRestrictionType(restriction);
		}
		catch (SBOLValidationException e) {
			return null;
		}
	}
	
	
	/**
	 * Returns the restriction property of this sequence constraint.
	 * 
	 * @return the restriction property of this sequence constraint
	 */
	public URI getRestrictionURI() {
		return restriction;
	}


	/**
	 * Sets the restriction property to the given one.
	 * 
	 * @param restriction the restriction type to set to
 	 * @throws SBOLValidationException if either of the following SBOL validation rule was violated: 11407, 11412.
	 */
	public void setRestriction(RestrictionType restriction) throws SBOLValidationException {
		if (restriction==null) {
			throw new SBOLValidationException("sbol-11407",this);
		}
		if (restriction.equals(RestrictionType.DIFFERENT_FROM)) {
			if (componentDefinition != null && subject != null && object != null) {
				if (componentDefinition.getComponent(object).getDefinitionURI()
						.equals(componentDefinition.getComponent(subject).getDefinitionURI())) {
					throw new SBOLValidationException("sbol-11413", this);
				}
			}
		}

		this.restriction = RestrictionType.convertToURI(restriction);
	}
	
	/**
	 * Sets the reference subject component's identity URI to the given one.
	 * 
	 * @param restrictionURI the identity URI of the restriction to set to
 	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 11407.
	 */
	public void setRestriction(URI restrictionURI) throws SBOLValidationException {
		if (restrictionURI==null) {
			throw new SBOLValidationException("sbol-11407",this);
		}
		this.restriction = restrictionURI;
	}

	/**
	 * Returns the subject component's identity URI that this sequence constraint refers to.
	 * 
	 * @return the subject component's identity URI that this sequence constraint refers to
	 */
	public URI getSubjectURI() {
		return subject;
	}
	
	/**
	 * Returns the subject component identity this sequence constraint refers to.
	 * <p>
	 * If this sequence constraint's parent component definition is {@code null}, this method returns {@code null}.
	 * Otherwise, it returns its child component which is also referenced by this sequence constraint.
	 * @return the subject component identity this sequence constraint refers to
	 */
	public URI getSubjectIdentity() {
		if (componentDefinition==null) return null;
		if (componentDefinition.getComponent(subject)==null) return null;
		return componentDefinition.getComponent(subject).getIdentity();
	}

	/**
	 * Returns the subject component this sequence constraint refers to.
	 * <p>
	 * If this sequence constraint's parent component definition is {@code null}, this method returns {@code null}.
	 * Otherwise, it returns its child component which is also referenced by this sequence constraint.
	 * @return the subject component this sequence constraint refers to
	 */
	public Component getSubject() {
		if (componentDefinition==null) return null;
		return componentDefinition.getComponent(subject);
	}
	
	/**
	 * Returns the component definition that defines the subject component of this sequence constraint.
	 * <p>
	 * If this sequence constraint's parent component definition is {@code null}, this method returns {@code null}.
	 * Otherwise, it returns the component definition that defines the subject component of this sequence constraint.
	 * 
	 * @return the component definition that defines the subject component of this sequence constraint
	 */
	public ComponentDefinition getSubjectDefinition() {
		if (componentDefinition!=null) {
			return componentDefinition.getComponent(subject).getDefinition();
		}
		return null;
	}

	/**
	 * Sets the reference subject Component URI to the given {@code subjectURI}.
	 * 
	 * @param subjectURI the reference subject's identity URI of the subject component
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated: 11402, 11403, 11406.
	 */
	public void setSubject(URI subjectURI) throws SBOLValidationException {
		if (componentDefinition != null) {
			if (componentDefinition.getComponent(subjectURI)==null) {
				throw new SBOLValidationException("sbol-11403",this);
			}
		}
		if (subjectURI==null) {
			throw new SBOLValidationException("sbol-11402", this);
		}
		if (subjectURI.equals(object)) {
			throw new SBOLValidationException("sbol-11406", this);
		}
		if (RestrictionType.convertToRestrictionType(restriction).equals(RestrictionType.DIFFERENT_FROM)) {
			if (componentDefinition != null && object != null) {
				if (componentDefinition.getComponent(subjectURI).getDefinitionURI()
						.equals(componentDefinition.getComponent(object).getDefinitionURI())) {
					throw new SBOLValidationException("sbol-11413", this);
				}
			}
		}
		this.subject = subjectURI;
	}

	/**
	 * Returns the object component's identity URI that this sequence constraint refers to.
	 * 
	 * @return the object component's identity URI that this sequence constraint refers to
	 */
	public URI getObjectURI() {
		return object;
	}
	
	/**
	 * Returns the object component identity this sequence constraint refers to.
	 * <p>
	 * If this sequence constraint's parent component definition is {@code null}, this method returns {@code null}.
	 * Otherwise, it returns its child component which is also referenced by this sequence constraint.
	 * 
	 * @return the object component identity this sequence constraint refers to
	 */
	public URI getObjectIdentity() {
		if (componentDefinition==null) return null;
		if (componentDefinition.getComponent(object)==null) return null;
		return componentDefinition.getComponent(object).getIdentity();
	}
	
	/**
	 * Returns the object component this sequence constraint refers to.
	 * <p>
	 * If this sequence constraint's parent component definition is {@code null}, this method returns {@code null}.
	 * Otherwise, it returns its child component which is also referenced by this sequence constraint.
	 * 
	 * @return the object component this sequence constraint refers to
	 */
	public Component getObject() {
		if (componentDefinition==null) return null;
		return componentDefinition.getComponent(object);
	}
	
	/**
	 * Returns the component definition that defines the object component of this sequence constraint.
	 * <p>
	 * If this sequence constraint's parent component definition is {@code null}, this method returns {@code null}.
	 * Otherwise, it returns the component definition that defines the object component of this sequence constraint.
	 * 
	 * @return the component definition that defines the object component of this sequence constraint
	 */
	public ComponentDefinition getObjectDefinition() {
		if (componentDefinition!=null) {
			return componentDefinition.getComponent(object).getDefinition();
		}
		return null;
	}

	/**
	 * Sets the reference object component's identity URI to the given one.
	 * 
	 * @param objectURI the reference object component's identity URI to set to 
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated: 11402, 11404, 11405. 
	 */
	public void setObject(URI objectURI) throws SBOLValidationException {
		if (componentDefinition != null) {
			if (componentDefinition.getComponent(objectURI)==null) {
				throw new SBOLValidationException("sbol-11405",this);
			}
		}
		if (objectURI==null) {
			throw new SBOLValidationException("sbol-11404", this);
		}
		if (objectURI==subject) {
			throw new SBOLValidationException("sbol-11402", this);
		}
		if (RestrictionType.convertToRestrictionType(restriction).equals(RestrictionType.DIFFERENT_FROM)) {
			if (componentDefinition != null && subject != null) {
				if (componentDefinition.getComponent(objectURI).getDefinitionURI()
						.equals(componentDefinition.getComponent(subject).getDefinitionURI())) {
					throw new SBOLValidationException("sbol-11413", this);
				}
			}
		}
		this.object = objectURI;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result + ((restriction == null) ? 0 : restriction.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
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
		SequenceConstraint other = (SequenceConstraint) obj;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject)) {
			if (getSubjectIdentity() == null || other.getSubjectIdentity() == null 
					|| !getSubjectIdentity().equals(other.getSubjectIdentity())) {
				return false;
			}
		}
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object)) {
			if (getObjectIdentity() == null || other.getObjectIdentity() == null 
					|| !getObjectIdentity().equals(other.getObjectIdentity())) {
				return false;
			}
		}
		if (!restriction.equals(other.restriction))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.Identified#deepCopy()
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in
	 * {@link SequenceConstraint#SequenceConstraint(SequenceConstraint)}. 
	 */
	@Override
	SequenceConstraint deepCopy() throws SBOLValidationException {		
		return new SequenceConstraint(this);
	}

	/**
	 *  
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of the following constructors or methods:
	 * <ul>
	 * <li>{@link URIcompliance#createCompliantURI(String, String, String)},</li>
	 * <li>{@link #setWasDerivedFrom(URI)},</li>
	 * <li>{@link #setIdentity(URI)},</li>
	 * <li>{@link #setDisplayId(String)},</li>
	 * <li>{@link #setVersion(String)},</li>
	 * <li>{@link #setSubject(URI)}, or</li>
	 * <li>{@link #setObject(URI)}.</li>
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
		String subjectId = extractDisplayId(subject);
		this.setSubject(createCompliantURI(URIprefix,subjectId,version));
		String objectId = extractDisplayId(object);
		this.setObject(createCompliantURI(URIprefix,objectId,version));
	}

	/**
	 * Sets this sequence constraint's parent component definition to the given one. 
	 * 
	 * @param componentDefinition the component definition to set to
	 */
	void setComponentDefinition(ComponentDefinition componentDefinition) {
		this.componentDefinition = componentDefinition;
	}

	@Override
	public String toString() {
		return "SequenceConstraint ["
				+ super.toString()
				//+ ", restriction=" + restriction
				+ ", restriction=" + this.getRestriction().toString()
				+ ", subject=" + subject
				+ ", object=" + object 
				+ "]";
	}
}
