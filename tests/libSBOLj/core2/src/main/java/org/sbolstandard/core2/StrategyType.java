package org.sbolstandard.core2;

import java.net.URI;

/**
 * Represents all strategy types for a {@link CombinatorialDerivation} object.
 * 
 * @author Igor Durovic
 */

public enum StrategyType {
	/**
	 * A user SHOULD derive all possible ComponentDefinition objects specified by 
	 * the Component objects contained by the template ComponentDefinition and the 
	 * VariableComponent objects contained by the CombinatorialDerivation
	 */
	ENUMERATE("enumerate"),
	
	/**
	 * A user SHOULD derive a subset of all possible ComponentDefinition objects
	 * specified by the Component objects contained by the template ComponentDefinition
	 * and the VariableComponent objects contained by the CombinatorialDerivation.T
	 * he manner in which this subset is chosen is for the user to decide.
	 */
	SAMPLE("sample");
	
	private final String strategyType;

	StrategyType(String strategyType) {
		this.strategyType = strategyType;
	}

	@Override
	public String toString() {
		return strategyType;
	}

	/**
	 * Convert the specified URI to its corresponding StrategyType instance.
	 * @param strategy
	 * @return the corresponding StrategyType instance
	 * @throws SBOLValidationException if either of the following SBOL validation rule was violated: 11802
	 */
	static StrategyType convertToStrategyType(URI strategy) throws SBOLValidationException {
		if (strategy != null) {
			if (strategy.equals(enumerate)) {
				return StrategyType.ENUMERATE;
			} else if (strategy.equals(sample)) {
				return StrategyType.SAMPLE;
			}
			else {
				throw new SBOLValidationException("sbol-12902");
			}
		} else {
			throw new SBOLValidationException("sbol-12902");
		}
	}
	
	/**
	 * Returns the strategy type in URI.
	 * @return strategy type in URI
	 */
	static URI convertToURI(StrategyType strategy) {
		if (strategy != null) {
			if (strategy.equals(StrategyType.ENUMERATE)) {
				return enumerate;
			}
			else if (strategy.equals(StrategyType.SAMPLE)) {
				return sample;
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}

	private static final URI	enumerate	= URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "enumerate");
	private static final URI	sample		= URI.create(Sbol2Terms.sbol2.getNamespaceURI() + "sample");

}

