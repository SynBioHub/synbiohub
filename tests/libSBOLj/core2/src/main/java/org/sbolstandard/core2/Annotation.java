package org.sbolstandard.core2;

import static org.sbolstandard.core.datatree.Datatree.NamedProperties;
import static org.sbolstandard.core.datatree.Datatree.NamedProperty;
import static org.sbolstandard.core.datatree.Datatree.NestedDocument;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.sbolstandard.core.datatree.Literal.BooleanLiteral;
import org.sbolstandard.core.datatree.Literal.DoubleLiteral;
import org.sbolstandard.core.datatree.Literal.IntegerLiteral;
import org.sbolstandard.core.datatree.Literal.StringLiteral;
import org.sbolstandard.core.datatree.Literal.UriLiteral;
import org.sbolstandard.core.datatree.NamedProperty;
import org.sbolstandard.core.datatree.NestedDocument;

/**
 * Represents an Annotation object in the cSBOL data model.
 * 
 * @author Zhen Zhang
 * @author Matthew Pocock
 * @author Goksel Misirli
 * @author Chris Myers
 * @version 2.1
 */

public class Annotation implements Comparable<Annotation>  {

	//private NamedProperty<QName> value;
	private String namespaceURI = null;
	private String localPart = null;
	private String prefix = null;
	private String type = null;
	private Boolean boolValue = null;
	private Double doubleValue = null;
	private Integer intValue = null;
	private String stringValue = null;
	private URI URIValue = null;
	private String nestedNamespaceURI = null;
	private String nestedLocalPart = null;
	private String nestedPrefix = null;
	private URI nestedURI = null;
	private List<Annotation> nestedAnnotations = null;

	/**
	 * Constructs an annotation using the given qName and the string type literal.
	 *
	 * @param qName the QName of this annotation
	 * @param literal a string type value
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 12201, 12203 
	 */
	public Annotation(QName qName, String literal) throws SBOLValidationException {
		setQName(qName);
		setStringValue(literal);
	}

	/**
	 * Constructs an annotation using the given qName and the integer type literal.
	 *
	 * @param qName the QName of this annotation
	 * @param literal an integer type value
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 12201, 12203 
	 */
	public Annotation(QName qName, int literal) throws SBOLValidationException {
		setQName(qName);
		setIntegerValue(literal);
	}

	/**
	 * Constructs an annotation using the given qName and the double type literal.
	 *
	 * @param qName the QName of this annotation
	 * @param literal a double type value
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 12201, 12203  
	 */
	public Annotation(QName qName, double literal) throws SBOLValidationException {
		setQName(qName);
		setDoubleValue(literal);
	}

	/**
	 * Constructs an annotation using the given qName and the boolean type literal.
	 *
	 * @param qName the QName of this annotation
	 * @param literal a boolean type value
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 12201, 12203  
	 */
	public Annotation(QName qName, boolean literal) throws SBOLValidationException {
		setQName(qName);
		setBooleanValue(literal);
	}

	/**
	 * Constructs an annotation using the given qName and the {@code URI} type literal.
	 *
	 * @param qName the QName of this annotation
	 * @param literal a URI type value
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 12201, 12203  
	 */
	public Annotation(QName qName, URI literal) throws SBOLValidationException {
		setQName(qName);
		setURIValue(literal);
	}

	/**
	 * Constructs a nested annotation using the given qName, nested qName,
	 * nested URI, and list of annotations.
	 *
	 * @param qName the QName of this annotation
	 * @param nestedQName the QName of the nested annotation
	 * @param nestedURI the identity URI for the nested annotation
	 * @param annotations the list of annotations to construct the nested annotation
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 12201, 12203, 12204, 12205, 12206
	 */
	Annotation(QName qName, QName nestedQName, URI nestedURI, List<Annotation> annotations) throws SBOLValidationException {
		setQName(qName);
		setNestedQName(nestedQName);
		setNestedIdentity(nestedURI);
		setAnnotations(annotations);
	}
	
	/**
	 * Creates an annotation with nested annotations using the given arguments, and then adds to this instance's list of annotations.
	 * 
	 * @param qName the QName of the annotation to be created
	 * @param nestedQName the QName of the nested annotation
	 * @param nestedId the id for the nested annotation
	 * @param annotations the list of annotations used to construct the nested annotation
	 * @return the created annotation
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated: 
	 * 10401, 10501, 10701, 10801, 10901, 11101, 11201, 11301, 11401, 11501, 11601, 11701, 11801, 11901, 12001, 12101, 12301.
	 */
	public Annotation createAnnotation(QName qName,QName nestedQName, String nestedId, List<Annotation> annotations) throws SBOLValidationException {
		if (isNestedAnnotations() && nestedURI != null) {
			URI nestednestedURI = URIcompliance.createCompliantURI(URIcompliance.extractPersistentId(nestedURI),
					TopLevel.ANNOTATION, nestedId, URIcompliance.extractVersion(nestedURI), false);
			Annotation annotation = new Annotation(qName, nestedQName, nestednestedURI, annotations);
			nestedAnnotations.add(annotation);
			return annotation;
		} else {
			// TODO: perhaps not the best error message
			throw new SBOLValidationException("sbol-12205");
		}
	}

	Annotation(NamedProperty<QName> value) throws SBOLValidationException {
		if (value.getName().getNamespaceURI().equals(Sbol2Terms.sbol2.getNamespaceURI()) ||
				value.getName().getNamespaceURI().equals(Sbol1Terms.sbol1.getNamespaceURI())) {
			if (value.getName().equals(Sbol2Terms.Identified.timeStamp)) {
				System.err.println("Warning: sbol:timeStamp is deprecated");
			}
		}
		setQName(value.getName());
		if ((value.getValue() instanceof BooleanLiteral<?>)) {
			setBooleanValue(((BooleanLiteral<QName>) value.getValue()).getValue());
		} else if ((value.getValue() instanceof DoubleLiteral<?>)) {
			setDoubleValue(((DoubleLiteral<QName>) value.getValue()).getValue());
		} else if ((value.getValue() instanceof IntegerLiteral<?>)) {
			setIntegerValue(((IntegerLiteral<QName>) value.getValue()).getValue());
		} else if ((value.getValue() instanceof StringLiteral<?>)) {
			setStringValue(((StringLiteral<QName>) value.getValue()).getValue());
		} else if ((value.getValue() instanceof UriLiteral<?>)) {
			setURIValue(((UriLiteral<QName>) value.getValue()).getValue());
		} else if (value.getValue() instanceof NestedDocument<?>) {
			setNestedQName(((NestedDocument<QName>) value.getValue()).getType());
			setNestedIdentity(((NestedDocument<QName>) value.getValue()).getIdentity());
			List<Annotation> annotations = new ArrayList<>();
			for (NamedProperty<QName> namedProperty : ((NestedDocument<QName>) value.getValue()).getProperties()) {
				annotations.add(new Annotation(namedProperty));
			}
			setAnnotations(annotations);
		} else {
			throw new SBOLValidationException("sbol-12203");
		}
	}

	private Annotation(Annotation annotation) throws SBOLValidationException {
		setQName(annotation.getQName());
		if (annotation.isBooleanValue()) {
			setBooleanValue(annotation.getBooleanValue());
		} else if (annotation.isDoubleValue()) {
			setDoubleValue(annotation.getDoubleValue());
		} else if (annotation.isIntegerValue()) {
			setIntegerValue(annotation.getIntegerValue());
		} else if (annotation.isStringValue()) {
			setStringValue(annotation.getStringValue());
		} else if (annotation.isURIValue()) {
			setURIValue(annotation.getURIValue());
		} else if (annotation.isNestedAnnotations()) {
			setNestedQName(annotation.getNestedQName());
			setNestedIdentity(annotation.getNestedIdentity());
			setAnnotations(annotation.getAnnotations());
		} else {
			throw new SBOLValidationException("sbol-12203");
		}
	}
		
	@Override
	public int compareTo(Annotation annotation) {
		int result = this.getQName().getNamespaceURI().compareTo(annotation.getQName().getNamespaceURI());
		if (result==0) {
			result = this.getQName().getLocalPart().compareTo(annotation.getQName().getLocalPart());
		}
		if (result==0) {
			result = this.hashCode() - annotation.hashCode();
		}
		return result;
	}

	/**
	 * Returns the QName of this Annotation instance.
	 *
	 * @return the QName of this Annotation instance
	 */
	public QName getQName() {
		return new QName(namespaceURI, localPart, prefix);
	}
	
	/**
	 * Sets the QName of this annotation.
	 * @param qName the QName for this annotation.
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 12201
	 */
	public void setQName(QName qName) throws SBOLValidationException {
		if (qName==null) {
			throw new SBOLValidationException("sbol-12201");			
		}
		namespaceURI = qName.getNamespaceURI();
		localPart = qName.getLocalPart();
		prefix = qName.getPrefix();
	}

	/**
	 * Sets the boolean representation of the value property.
	 * @param literal the boolean representation of the value property
	 */
	public void setBooleanValue(boolean literal) {
		type = "Boolean";
		boolValue = literal;
	}

	/**
	 * Checks if the annotation has a boolean value property.
	 *
	 * @return {@code true} if the value property is a boolean, {@code false} otherwise. 
	 */
	public boolean isBooleanValue() {
		if (type.equals("Boolean")) {
			return true;
		}
		return false;
	}

	/**
	 * Returns a Boolean representation of the value property.
	 *
	 * @return a Boolean representation of the value property if its
	 * value is a Boolean, {@code null} otherwise.
	 */
	public Boolean getBooleanValue() {
		if (isBooleanValue()) {
			return boolValue;
		}
		return null;
	}

	/**
	 * Sets the double representation of the value property.
	 * @param literal the double representation of the value property
	 */
	public void setDoubleValue(double literal) {
		type = "Double";
		doubleValue = literal;
	}

	/**
	 * Checks if the annotation has a double value property.
	 *
	 * @return true if the value property is a double integer, {@code false} otherwise
	 */
	public boolean isDoubleValue() {
		if (type.equals("Double")) {
			return true;
		}
		return false;
	}

	/**
	 * Returns a Double representation of the value property.
	 *
	 * @return a Double integer representation of the value property if its
	 * value is a Double integer, {@code null} otherwise.
	 */
	public Double getDoubleValue() {
		if (isDoubleValue()) {
			return doubleValue;
		}
		return null;
	}

	/**
	 * Sets the integer representation of the value property.
	 * @param literal the integer representation of the value property
	 */
	public void setIntegerValue(int literal) {
		type = "Integer";
		intValue = literal;
	}

	/**
	 * Checks if the annotation has an integer value property.
	 *
	 * @return {@code true} if the value property is an integer, {@code false} otherwise
	 */
	public boolean isIntegerValue() {
		if (type.equals("Integer")) {
			return true;
		}
		return false;
	}

	/**
	 * Returns an Integer representation of the value property.
	 *
	 * @return an Integer representation of the value property if its
	 * value is an Integer, {@code null} otherwise.
	 */
	public Integer getIntegerValue() {
		if (isIntegerValue()) {
			return intValue;
		}
		return null;
	}

	/**
	 * Sets the string representation of the value property.
	 * @param literal the string representation of the value property
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 12203  
	 */
	public void setStringValue(String literal) throws SBOLValidationException {
		if (literal==null) {
			throw new SBOLValidationException("sbol-12203");			
		}
		type = "String";
		stringValue = literal;
	}

	/**
	 * Checks if the annotation has a string value property.
	 *
	 * @return true if the value property is string type, {@code false} otherwise
	 */
	public boolean isStringValue() {
		if (type.equals("String")) {
			return true;
		}
		return false;
	}

	/**
	 * Returns a string representation of the value property.
	 *
	 * @return a string representation of the value property if it is a string, 
	 * {@code null} otherwise.
	 */
	public String getStringValue() {
		if (isStringValue()) {
			return stringValue;
		}
		return null;
	}

	/**
	 * Sets the string representation of the value property.
	 * @param literal the URI representation of the value property
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 12203  
	 */
	public void setURIValue(URI literal) throws SBOLValidationException {
		if (literal==null) {
			throw new SBOLValidationException("sbol-12203");			
		}
		type = "URI";
		URIValue = literal;
	}
	
	/**
	 * Checks if the annotation is a URI {@code value} property.
	 *
	 * @return true if the annotation is a URI {@code value} property.
	 */
	public boolean isURIValue() {
		if (type.equals("URI")) {
			return true;
		}
		return false;
	}


	/**
	 * Returns a URI representation of the value property.
	 *
	 * @return a URI representation of the value property if it is a URI, 
	 * {@code null} otherwise.
	 */
	public URI getURIValue() {
		if (isURIValue()) {
			return URIValue;
		}
		return null;
	}

	/**
	 * Sets the nested QName for this annotation.
	 * 
	 * @param qName the nested QName for this annotation.
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 12204  
	 */
	public void setNestedQName(QName qName) throws SBOLValidationException {
		if (qName==null) {
			throw new SBOLValidationException("sbol-12204");			
		}
		nestedNamespaceURI = qName.getNamespaceURI();
		nestedLocalPart = qName.getLocalPart();
		nestedPrefix = qName.getPrefix();
	}

	/**
	 * Returns the nested QName of the nested Annotation.
	 *
	 * @return the nested QName if its value is nested Annotations, {@code null} otherwise.
	 */
	public QName getNestedQName() {
		if (isNestedAnnotations()) {
			return new QName(nestedNamespaceURI,nestedLocalPart,nestedPrefix);
		}
		return null;
	}

	/**
	 * Returns the nested identity URI of the nested Annotation.
	 *
	 * @return the nested identity URI of the nested nested Annotations if its value is nested Annotations, {@code null} otherwise.
	 */
	public URI getNestedIdentity() {
		if (isNestedAnnotations()) {
			return nestedURI;
		}
		return null;
	}

	/**
	 * Sets the nested URI for this annotation.
	 * 
	 * @param uri the nested uri for this annotation.
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 12205  
	 */
	public void setNestedIdentity(URI uri) throws SBOLValidationException {
		if (uri==null) {
			throw new SBOLValidationException("sbol-12205");			
		}
		nestedURI = uri;
	}

	/**
	 * Checks if the annotation has a nested value property.
	 *
	 * @return true if the value property is nested Annotations, {@code false} otherwise
	 */
	public boolean isNestedAnnotations() {
		if (type.equals("NestedAnnotation")) {
			return true;
		}
		return false;
	}

	/**
	 * Sets the list of Annotations of the nested value property.
	 *
	 * @param annotations the list of Annotations for the nested value property.
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 12206
	 */
	public void setAnnotations(List<Annotation> annotations) throws SBOLValidationException {
		if (annotations==null) {
			throw new SBOLValidationException("sbol-12206");			
		}
		type = "NestedAnnotation";
		nestedAnnotations = new ArrayList<>();
		for(Annotation a : annotations)
		{
			nestedAnnotations.add(a);
		}
	}
	
	/**
	 * Returns the list of Annotations of the nested value property.
	 *
	 * @return the list of Annotations if its value is nested Annotations, {@code null} otherwise.
	 */
	public List<Annotation> getAnnotations() {
		if (isNestedAnnotations()) {
			return nestedAnnotations;
		}
		return null;
	}

	/**
	 * Returns the value of this Annotation instance.
	 *
	 * @return the value of this Annotation instance.
	 */
	NamedProperty<QName> getValue() {
		if (isBooleanValue()) {
			return NamedProperty(getQName(),getBooleanValue());
		} else if (isDoubleValue()) {
			return NamedProperty(getQName(),getDoubleValue());
		} else if (isIntegerValue()) {
			return NamedProperty(getQName(),getIntegerValue());
		} else if (isStringValue()) {
			return NamedProperty(getQName(),getStringValue());
		} else if (isURIValue()) {
			return NamedProperty(getQName(),getURIValue());
		} else if (isNestedAnnotations()) {
			List<NamedProperty<QName>> list = new ArrayList<>();
			for(Annotation a : getAnnotations())
			{
				list.add(a.getValue());
			}
			return NamedProperty(getQName(), NestedDocument(getNestedQName(), getNestedIdentity(), NamedProperties(list)));
		}
		return null;
	}

	/**
	 * Makes a deep copy of this Annotation instance.
	 * @return an Annotation instance that is the exact copy of this instance.
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 12201, 12203, 12204, 12205, 12206  
	 */
	private Annotation deepCopy() throws SBOLValidationException {
		return new Annotation(this);
	}

	/**
	 * Makes a deep copy of this Annotation instance.
	 * @return an Annotation instance that is the exact copy of this instance.
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 12201, 12203, 12204, 12205, 12206 
	 */
	Annotation copy() throws SBOLValidationException {
		return this.deepCopy();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((URIValue == null) ? 0 : URIValue.hashCode());
		result = prime * result + ((boolValue == null) ? 0 : boolValue.hashCode());
		result = prime * result + ((doubleValue == null) ? 0 : doubleValue.hashCode());
		result = prime * result + ((intValue == null) ? 0 : intValue.hashCode());
		result = prime * result + ((stringValue == null) ? 0 : stringValue.hashCode());
		result = prime * result + ((localPart == null) ? 0 : localPart.hashCode());
		result = prime * result + ((namespaceURI == null) ? 0 : namespaceURI.hashCode());
		result = prime * result + ((nestedAnnotations == null) ? 0 : nestedAnnotations.hashCode());
		result = prime * result + ((nestedLocalPart == null) ? 0 : nestedLocalPart.hashCode());
		result = prime * result
				+ ((nestedNamespaceURI == null) ? 0 : nestedNamespaceURI.hashCode());
		// TODO: removed, not needed to be equal
		//result = prime * result + ((nestedPrefix == null) ? 0 : nestedPrefix.hashCode());
		result = prime * result + ((nestedURI == null) ? 0 : nestedURI.hashCode());
		// TODO: removed, not needed to be equal
		//result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Annotation other = (Annotation) obj;
		if (URIValue == null) {
			if (other.URIValue != null)
				return false;
		}
		else if (!URIValue.equals(other.URIValue))
			return false;
		if (boolValue == null) {
			if (other.boolValue != null)
				return false;
		}
		else if (!boolValue.equals(other.boolValue))
			return false;
		if (doubleValue == null) {
			if (other.doubleValue != null)
				return false;
		}
		else if (!doubleValue.equals(other.doubleValue))
			return false;
		if (intValue == null) {
			if (other.intValue != null)
				return false;
		}
		else if (!intValue.equals(other.intValue))
			return false;
		if (localPart == null) {
			if (other.localPart != null)
				return false;
		}
		else if (!localPart.equals(other.localPart))
			return false;
		if (namespaceURI == null) {
			if (other.namespaceURI != null)
				return false;
		}
		else if (!namespaceURI.equals(other.namespaceURI))
			return false;
		if (nestedAnnotations == null) {
			if (other.nestedAnnotations != null)
				return false;
		}
		else if (!nestedAnnotations.containsAll(other.nestedAnnotations))
			return false;
		if (nestedLocalPart == null) {
			if (other.nestedLocalPart != null)
				return false;
		}
		else if (!nestedLocalPart.equals(other.nestedLocalPart))
			return false;
		if (nestedNamespaceURI == null) {
			if (other.nestedNamespaceURI != null)
				return false;
		}
		else if (!nestedNamespaceURI.equals(other.nestedNamespaceURI))
			return false;
		// TODO: removed since do not need to be equal
		/*
		if (nestedPrefix == null) {
			if (other.nestedPrefix != null)
				return false;
		}
		else if (!nestedPrefix.equals(other.nestedPrefix))
			return false;
			*/
		if (nestedURI == null) {
			if (other.nestedURI != null)
				return false;
		}
		else if (!nestedURI.equals(other.nestedURI))
			return false;
		// TODO: removed since do not need to be equal
		/*
		if (prefix == null) {
			if (other.prefix != null)
				return false;
		}
		else if (!prefix.equals(other.prefix))
			return false;
			*/
		if (stringValue == null) {
			if (other.stringValue != null)
				return false;
		}
		else if (!stringValue.equals(other.stringValue))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		}
		else if (!type.equals(other.type))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Annotation [(" + prefix + ":" + namespaceURI + ":" + localPart + ")"
				+ ", type=" + type + ", value=" 
				+ (isBooleanValue()?boolValue:"") 
				+ (isDoubleValue()?doubleValue:"") 
				+ (isIntegerValue()?intValue:"") 
				+ (isStringValue()?stringValue:"")
				+ (isURIValue()?URIValue:"")
				+ (isNestedAnnotations()?("("+nestedPrefix+":"+nestedNamespaceURI+":"+nestedLocalPart+":"+
						nestedURI + ")" + nestedAnnotations.toString()):"")
				+ "]";
	}

}
