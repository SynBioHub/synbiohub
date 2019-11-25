package org.sbolstandard.core2;

import java.net.URI;

/**
 * Represents all operator types for a {@link VariableComponent} object.
 * 
 * @author Igor Durovic
 */

public enum OperatorType {
	/**
	 * No more than one Component in the derived ComponentDefinition SHOULD have a
	 * wasDerivedFrom property that refers to the template Component
	 */
	ZEROORONE("zeroOrOne"),
	
	/**
	 * Exactly one Component in the derived ComponentDefinition SHOULD have a
	 * wasDerivedFrom property that refers to the template Component.
	 */
	ONE("one"),
	
	/**
	 * Any number of Component objects in the derived ComponentDefinition
	 * MAY have wasDerivedFrom properties that refer to the template Component.
	 */
	ZEROORMORE("zeroOrMore"),
	
	/**
	 * At least one Component in the derived ComponentDefinition SHOULD have
	 * a wasDerivedFrom property that refers to the template Component.
	 */
	ONEORMORE("oneOrMore");
	
	private final String operatorType;

	OperatorType(String operatorType) {
		this.operatorType = operatorType;
	}

	@Override
	public String toString() {
		return operatorType;
	}

	/**
	 * Convert the specified URI to its corresponding OperatorType instance.
	 * @param operator
	 * @return the corresponding OperatorType instance
	 * @throws SBOLValidationException if either of the following SBOL validation rule was violated: 11802
	 */
	static OperatorType convertToOperatorType(URI operator) throws SBOLValidationException {
		if (operator != null) {
			if (operator.equals(zeroOrOne)) {
				return OperatorType.ZEROORONE;
			} else if (operator.equals(one)) {
				return OperatorType.ONE;
			}
			else if (operator.equals(zeroOrMore)) {
				return OperatorType.ZEROORMORE;
			} else if (operator.equals(oneOrMore)) {
				return OperatorType.ONEORMORE;
			}
			else {
				throw new SBOLValidationException("sbol-13003");
			}
		} else {
			throw new SBOLValidationException("sbol-13003");
		}
	}
	
	/**
	 * Returns the operator type in URI.
	 * @return operator type in URI
	 */
	static URI convertToURI(OperatorType operator) {
		if (operator != null) {
			if (operator.equals(OperatorType.ZEROORONE)) {
				return zeroOrOne;
			}
			else if (operator.equals(OperatorType.ONE)) {
				return one;
			}
			else if (operator.equals(OperatorType.ZEROORMORE)) {
				return zeroOrMore;
			}
			else if (operator.equals(OperatorType.ONEORMORE)) {
				return oneOrMore;
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}

	private static final URI	zeroOrOne	= URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "zeroOrOne");
	private static final URI	one			= URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "one");
	private static final URI	zeroOrMore	= URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "zeroOrMore");
	private static final URI	oneOrMore	= URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "oneOrMore");

}

