package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a VariableComponent in the SBOL data model
 * 
 * @author Zach Zundel
 * @author Igor Durovic
 * @version 2.1
 */

public class VariableComponent extends Identified {
	private HashSet<URI> variants;
	private HashSet<URI> variantCollections;
	private HashSet<URI> variantDerivations;
	private URI variable;
	private OperatorType operator;

	/**
	 * Parent combinatorial derivation of this variable component
	 */
	private CombinatorialDerivation combinatorialDerivation = null;

	/**
	 * @param identity
	 * @param operator
	 * @param variable
	 *            the referenced component
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 * <li>{@link Identified#Identified(URI)}</li>
	 * <li>{@link #setVariable(URI)}, or</li>
     * <li>{@link #setOperator(URI)}</li>
	 * </ul>	
	 */
	VariableComponent(URI identity, OperatorType operator, URI variable) throws SBOLValidationException {
		super(identity);
		setVariable(variable);
		setOperator(operator);
		this.variants = new HashSet<>();
		this.variantCollections = new HashSet<>();
		this.variantDerivations = new HashSet<>();
	}

	/**
	 * @param variableComponent
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in any of the
	 *             following constructors or methods:
	 * <li>{@link Identified#Identified(Identified)}</li>
	 * <li>{@link #setVariable(URI)},</li>
     * <li>{@link #setOperator(URI)},</li>
     * <li>{@link #setVariants(HashSet)},</li>
     * <li>{@link #setVariantCollections(HashSet)},</li>
     * <li>{@link #setVariantDerivations(HashSet)},</li>
	 * </ul>	
	 */
	private VariableComponent(VariableComponent variableComponent) throws SBOLValidationException {
		super(variableComponent.getIdentity());

		setVariable(variableComponent.variable);
		setOperator(variableComponent.operator);
		setVariants(variableComponent.variants);
		setVariantCollections(variableComponent.variantCollections);
		setVariantDerivations(variableComponent.variantDerivations);
	}

	/**
	 * Sets the parent combinatorial derivation to the given one.
	 *
	 * @param combinatorialDerivation
	 *            the given combinatorial derivation to set to
	 */
	public void setCombinatorialDerivation(CombinatorialDerivation combinatorialDerivation) {
		this.combinatorialDerivation = combinatorialDerivation;
	}

	/**
	 * Adds the given variant URI to the list of variant URIs.
	 * 
	 * @param variant
	 * 			variant to be added
	 * @throws SBOLValidationException 
	 *              if the following SBOL validation rule was violated: 13008
	 */
	public void addVariant(URI variant) throws SBOLValidationException {
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (this.getSBOLDocument().getComponentDefinition(variant) == null) {
				throw new SBOLValidationException("sbol-13008", this);
			}
		}
		variants.add(variant);
	}

	/**
	 * Adds the given variant collection URI to the list of variant collection URIs.
	 * 
	 * @param variantCollection
	 * 			variant collection to be added
	 * @throws SBOLValidationException 
	 *              if the following SBOL validation rule was violated: 13010
	 */
	public void addVariantCollection(URI variantCollection) throws SBOLValidationException {
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			Collection collection = this.getSBOLDocument().getCollection(variantCollection);
			if (collection == null) {
				throw new SBOLValidationException("sbol-13010", this);
			}
			if (collection.getMemberURIs().size()==0) {
				throw new SBOLValidationException("sbol-13011", this);
			}
			for (TopLevel topLevel : collection.getMembers()) {
				if (!(topLevel instanceof ComponentDefinition)) {
					throw new SBOLValidationException("sbol-13012", this);
				}
			}
		}
		variantCollections.add(variantCollection);
	}

	/**
	 * Adds the given variant derivation URI to the list of variant derivations URIs.
	 * 
	 * @param variantDerivation
	 * 			variant derivation to be added
	 * @throws SBOLValidationException 
	 *              if the following SBOL validation rule was violated: 13014
	 */
	public void addVariantDerivation(URI variantDerivation) throws SBOLValidationException {
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (this.getSBOLDocument().getCombinatorialDerivation(variantDerivation) == null) {
				throw new SBOLValidationException("sbol-13014", this);
			}
		}
		variantDerivations.add(variantDerivation);
	}

	/**
	 * Returns the instance matching the given variable component's variable.
	 *
	 * @return the matching component if present, or {@code null} otherwise.
	 */
	public Component getVariable() {
		if (combinatorialDerivation==null) return null;
		if (combinatorialDerivation.getTemplate()==null) return null;
		return combinatorialDerivation.getTemplate().getComponent(variable);
	}
	
	/**
	 * Returns the URI of the instance matching the given variable component's variable.
	 *
	 * @return the matching component if present, or {@code null} otherwise.
	 */
	public URI getVariableURI() {
		return this.variable;
	}

	/**
	 * Sets the variable property to the given one.
	 *
	 * @param variable
	 *            the given component to set to
	 * @throws SBOLValidationException 
	 *              if the following SBOL validation rule was violated: 13004, 13005
	 */
	public void setVariable(URI variable) throws SBOLValidationException {
		if (variable == null) {
			throw new SBOLValidationException("sbol-13004", this);
		}
		if (combinatorialDerivation != null) {
			ComponentDefinition template = combinatorialDerivation.getTemplate();
			if (template !=null) {
				if (template.getComponent(variable)==null) {
					throw new SBOLValidationException("sbol-13005",this);
				}
			}
		}
		this.variable = variable;
	}

	/**
	 * Returns the operator of the given variable component.
	 *
	 * @return the matching operator if present, or {@code null} otherwise.
	 */
	public OperatorType getOperator() {
		return this.operator;
	}

	/**
	 * Sets the operator property to the given one.
	 *
	 * @param operator
	 *            the given operator type to set to
	 * @throws SBOLValidationException
	 *             if the following SBOL validation rule was violated: 13002
	 */
	public void setOperator(OperatorType operator) throws SBOLValidationException {
		if (operator == null) {
			throw new SBOLValidationException("sbol-13002", this);
		}
		if (combinatorialDerivation != null && combinatorialDerivation.isSetStrategy() &&
				combinatorialDerivation.getStrategy().equals(StrategyType.ENUMERATE)) {
			if (operator.equals(OperatorType.ZEROORMORE) ||
					operator.equals(OperatorType.ONEORMORE)) {
				throw new SBOLValidationException("sbol-12903",this);
			}
		}
		this.operator = operator;
	}

	/**
	 * Returns a set of component definitions belonging to the variable component.
	 *
	 * @return the matching set of component definitions
	 */
	public Set<ComponentDefinition> getVariants() {
		HashSet<ComponentDefinition> tempVariants = new HashSet<>();

		for (URI variantURI : variants) {
			tempVariants.add(this.getSBOLDocument().getComponentDefinition(variantURI));
		}

		return tempVariants;
	}

	/**
	 * Returns a set of component definition URIs belonging to the variable component.
	 *
	 * @return the matching set of component definition URIs
	 */
	public Set<URI> getVariantURIs() {
		return new HashSet<URI>(this.variants);
	}

	/**
	 * Returns a set of collections belonging to the variable component.
	 *
	 * @return the matching set of collections
	 */
	public Set<Collection> getVariantCollections() {
		HashSet<Collection> tempVariantCollections = new HashSet<>();

		for (URI variantCollectionURI : variantCollections) {
			tempVariantCollections.add(this.getSBOLDocument().getCollection(variantCollectionURI));
		}

		return tempVariantCollections;
	}
	
	/**
	 * Returns a set of collection URIs belonging to the variable component.
	 *
	 * @return the matching set of collection URIs
	 */
	public Set<URI> getVariantCollectionURIs() {
		return new HashSet<URI>(this.variantCollections);
	}

	/**
	 * Returns a set of combinatorial derivations belonging to the variable component.
	 *
	 * @return the matching set of combinatorial derivations
	 */
	public Set<CombinatorialDerivation> getVariantDerivations() {
		HashSet<CombinatorialDerivation> tempVariantDerivations = new HashSet<>();

		for (URI variantDerivationURI : variantDerivations) {
			tempVariantDerivations.add(this.getSBOLDocument().getCombinatorialDerivation(variantDerivationURI));
		}

		return tempVariantDerivations;
	}
	
	/**
	 * Returns a set of combinatorial derivation URIs belonging to the variable component.
	 *
	 * @return the matching set of combinatorial derivation URIs
	 */
	public Set<URI> getVariantDerivationURIs() {
		return new HashSet<URI>(this.variantDerivations);
	}

	/**
	 * Clears the existing set of variants first, and then adds the given
	 * set of the variants to this variable component.
	 * 
	 * @param variants The set of variants
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of the following methods:
	 * <ul>
	 * <li>{@link #clearVariants()}</li>
	 * <li>{@link #addVariant(URI)}, or</li>
	 * </ul>
	 */
	public void setVariants(Set<URI> variants) throws SBOLValidationException {
		clearVariants();
		for (URI uri : variants) {
			addVariant(uri);
		}
	}

	/**
	 * Clears the existing set of variant collections first, and then adds the given
	 * set of the variant collections to this variable component.
	 * 
	 * @param variantCollections The set of variant collections
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of the following methods:
	 * <ul>
	 * <li>{@link #clearVariantCollections()}</li>
	 * <li>{@link #addVariantCollection(URI)}, or</li>
	 * </ul>
	 */
	public void setVariantCollections(Set<URI> variantCollections) throws SBOLValidationException {
		clearVariantCollections();
		for (URI uri : variantCollections) {
			addVariantCollection(uri);
		}	}

	/**
	 * Clears the existing set of variant derivations first, and then adds the given
	 * set of the variant derivations to this variable component.
	 * 
	 * @param variantDerivations The set of variant derivations
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of the following methods:
	 * <ul>
	 * <li>{@link #clearVariantDerivations()}</li>
	 * <li>{@link #addVariantDerivation(URI)}, or</li>
	 * </ul>
	 */
	public void setVariantDerivations(Set<URI> variantDerivations) throws SBOLValidationException {
		clearVariantDerivations();
		for (URI uri : variantDerivations) {
			addVariantDerivation(uri);
		}
	}

	/**
	 * Adds the given variant to the list of variants.
	 * 
	 * @param uriPrefix
	 * 			URI prefix for variant
	 * @param displayId
	 * 			display id for variant
	 * @param version
	 * 			version for variant
	 * @throws SBOLValidationException if the following SBOL validation rule was violated:
	 */
	public void addVariant(String uriPrefix, String displayId, String version) throws SBOLValidationException {
		URI uri = URIcompliance.createCompliantURI(uriPrefix, displayId, version);

		ComponentDefinition componentDefinition = this.getSBOLDocument().getComponentDefinition(uri);
		addVariant(componentDefinition.getIdentity());
	}

	/**
	 * Adds the given variant collection to the list of variant collections.
	 * 
	 * @param uriPrefix
	 * 			URI prefix for variant collection
	 * @param displayId
	 * 	        display id for variant collection
	 * @param version
	 *          version for variant collection
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 
	 */
	public void addVariantCollection(String uriPrefix, String displayId, String version)
			throws SBOLValidationException {
		URI uri = URIcompliance.createCompliantURI(uriPrefix, displayId, version);

		Collection collection = this.getSBOLDocument().getCollection(uri);
		addVariantCollection(collection.getIdentity());
	}

	/**
	 * Adds the given variant derivation to the list of variant derivations.
	 * 
	 * @param uriPrefix
	 * 			URI prefix for variant derivation
	 * @param displayId
	 *          display id for variant derivation 
	 * @param version
	 *          version for variant derivation
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 
	 */
	public void addVariantDerivation(String uriPrefix, String displayId, String version)
			throws SBOLValidationException {
		URI uri = URIcompliance.createCompliantURI(uriPrefix, displayId, version);

		CombinatorialDerivation combinatorialDerivation = this.getSBOLDocument().getCombinatorialDerivation(uri);
		addVariantDerivation(combinatorialDerivation.getIdentity());
	}

	/**
	 * Removes the given component definition from the list of variants.
	 *
	 * @param variant
	 *            a component definition be removed
	 * @return {@code true} if the matching component definition is removed
	 *         successfully, {@code false} otherwise.
	 */
	public boolean removeVariant(ComponentDefinition variant) {
		return variants.remove(variant.getIdentity());
	}

	/**
	 * Removes the component definition with the given URI from the list of
	 * variants.
	 *
	 * @param variantURI
	 *            a component definition URI be removed
	 * @return {@code true} if the matching component definition is removed
	 *         successfully, {@code false} otherwise.
	 */
	public boolean removeVariant(URI variantURI) {
		return variants.remove(variantURI);
	}

	/**
	 * removes all entries of this variable component's set of variant URIs.
	 *
	 */
	public void clearVariants() {
		variants.clear();
	}

	/**
	 * Removes the given collection from the list of variantCollections.
	 *
	 * @param variantCollection
	 *            a collection to be removed
	 * @return {@code true} if the matching collection is removed successfully,
	 *         {@code false} otherwise.
	 */
	public boolean removeVariantCollection(Collection variantCollection) {
		return variantCollections.remove(variantCollection.getIdentity());
	}

	/**
	 * Removes the collection with the given URI from the list of
	 * variantCollections.
	 *
	 * @param variantCollectionURI
	 *            a collection URI to be removed
	 * @return {@code true} if the matching collection is removed successfully,
	 *         {@code false} otherwise.
	 */
	public boolean removeVariantCollection(URI variantCollectionURI) {
		return variantCollections.remove(variantCollectionURI);
	}

	/**
	 * removes all entries of this variable component's set of variant collection
	 * URIs.
	 *
	 */
	public void clearVariantCollections() {
		variantCollections.clear();
	}

	/**
	 * Removes the given combinatorial derivation from the list of variant
	 * derivations.
	 *
	 * @param variantDerivation
	 *            a combinatorial derivation to be removed
	 * @return {@code true} if the matching combinatorial derivation is removed
	 *         successfully, {@code false} otherwise.
	 */
	public boolean removeVariantDerivation(CombinatorialDerivation variantDerivation) {
		return variantDerivations.remove(variantDerivation.getIdentity());
	}

	/**
	 * Removes the combinatorial derivation with the given URI from the list of
	 * variant derivations.
	 *
	 * @param variantDerivationURI
	 *            a combinatorial derivation URI to be removed
	 * @return {@code true} if the matching combinatorial derivation is removed
	 *         successfully, {@code false} otherwise.
	 */
	public boolean removeVariantDerivation(URI variantDerivationURI) {
		return variantDerivations.remove(variantDerivationURI);
	}

	/**
	 * removes all entries of this variable component's set of variant derivation
	 * URIs.
	 *
	 */
	public void clearVariantDerivations() {
		variantDerivations.clear();
	}

	/**
	 * Updates this variable component's and each of its member identity URIs with
	 * compliant URIs.
	 * 
	 * @throws SBOLValidationException
	 *             if any of the following SBOL validation rules was violated:
	 *             <ul>
	 *             <li>{@link URIcompliance#createCompliantURI(String, String, String)};</li>
	 *             <li>{@link #setWasDerivedFrom(URI)};</li>
	 *             <li>{@link #setIdentity(URI)};</li>
	 *             <li>{@link #setDisplayId(String)};</li>
	 *             <li>{@link #setVersion(String)};</li>
	 *             </ul>
	 */
	void updateCompliantURI(String URIprefix, String displayId, String version) throws SBOLValidationException {
		if (!this.getIdentity().equals(createCompliantURI(URIprefix, displayId, version))) {
			this.addWasDerivedFrom(this.getIdentity());
		}
		this.setIdentity(createCompliantURI(URIprefix, displayId, version));
		this.setPersistentIdentity(createCompliantURI(URIprefix, displayId, ""));
		this.setDisplayId(displayId);
		this.setVersion(version);
	}
	
	void copy(VariableComponent variableComponent) throws SBOLValidationException {
		((Identified)this).copy((Identified)variableComponent);
		for (URI variant : variableComponent.getVariantURIs()) {
			this.addVariant(URI.create(variant.toString()));
		}
		for (URI variantCollection : variableComponent.getVariantCollectionURIs()) {
			this.addVariantCollection(URI.create(variantCollection.toString()));
		}
		for (URI variantDerivation : variableComponent.getVariantDerivationURIs()) {
			this.addVariantDerivation(URI.create(variantDerivation.toString()));
		}
	}

	/**
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #VariableComponent(VariableComponent)}.
	 */
	VariableComponent deepCopy() throws SBOLValidationException {
		return new VariableComponent(this);
	}

	@Override
	public String toString() {
		return "VariableComponent [" +
				super.toString() + ", operator=" + this.getOperator() + ", variable=" + this.getVariableURI()
				+ (variants.size() > 0 ? ", variants=" + variants : "")
				+ (variantCollections.size() > 0 ? ", variantCollections=" + variantCollections : "")
				+ (variantDerivations.size() > 0 ? ", variantDeriviations=" + variantDerivations : "")
				+ "]" + (combinatorialDerivation==null? combinatorialDerivation.getIdentity() + " " + combinatorialDerivation.getTemplateURI():"");
	}
}
