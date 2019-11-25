
package org.sbolstandard.core2;
import static org.sbolstandard.core2.URIcompliance.createCompliantURI;
import static org.sbolstandard.core2.URIcompliance.isChildURIcompliant;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Represents a ComponentDefinition object in the SBOL data model.
 * 
 * @author Zhen Zhang
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */
public class ComponentDefinition extends TopLevel {

	private Set<URI> types;
	private Set<URI> roles;
	private Set<URI> sequences;
	private HashMap<URI, Component> components;
	private HashMap<URI, SequenceAnnotation> sequenceAnnotations;
	private HashMap<URI, SequenceConstraint> sequenceConstraints;

	/* Types */
	/**
	 * A physical entity consisting of a sequence of deoxyribonucleotide monophosphates; a deoxyribonucleic acid.
     * Usage: DNA should be used for pools of individual DNA molecules. For describing subregions on those molecules 
     * use DNARegion.  Examples: a chromosome, a plasmid. A specific example is chromosome 7 of Homo sapiens.
	 * (<a href="http://www.biopax.org/release/biopax-level3-documentation.pdf">DNA</a>).
	 * Aspects of the state of the DNA region, including cellular location and features, are defined here,
	 * using properties inherited from PhysicalEntity.
	 * Deprecated - please use DNA_REGION instead.
	 */
	@Deprecated
	public static final URI DNA = URI.create("http://www.biopax.org/release/biopax-level3.owl#DnaRegion");
	
	/**
	 * A physical entity consisting of a sequence of deoxyribonucleotide monophosphates; a deoxyribonucleic acid.
     * Usage: DNA should be used for pools of individual DNA molecules. For describing subregions on those molecules 
     * use DNARegion.  Examples: a chromosome, a plasmid. A specific example is chromosome 7 of Homo sapiens.
	 * (<a href="http://www.biopax.org/release/biopax-level3-documentation.pdf">DNA</a>).
	 * Aspects of the state of the DNA region, including cellular location and features, are defined here,
	 * using properties inherited from PhysicalEntity.
	 */
	public static final URI DNA_REGION = URI.create("http://www.biopax.org/release/biopax-level3.owl#DnaRegion");
	
	/**
	 * A physical entity consisting of a sequence of deoxyribonucleotide monophosphates; a deoxyribonucleic acid
	 * (<a href="http://www.biopax.org/release/biopax-level3-documentation.pdf">DNA</a>).
	 * Aspects of the state of the DNA molecule, including cellular location and features, are defined here,
	 * using properties inherited from PhysicalEntity.
	 */
	public static final URI DNA_MOLECULE = URI.create("http://www.biopax.org/release/biopax-level3.owl#Dna");

	/**
	 * A region on a RNA molecule. Usage: RNARegion is not a pool of independent molecules but a subregion 
	 * on these molecules. As such, every RNARegion has a defining RNA molecule. Examples: CDS, 3'; UTR, Hairpin
	 * (<a href="http://www.biopax.org/release/biopax-level3-documentation.pdf">RNA</a>).
	 * Aspects of the state of the RNA region, including cellular location and features, are defined here,
	 * using properties inherited from PhysicalEntity.
	 * Deprecated - please use RNA_REGION instead.
	 */
	@Deprecated
	public static final URI RNA = URI.create("http://www.biopax.org/release/biopax-level3.owl#RnaRegion");

	/**
	 * A region on a RNA molecule. Usage: RNARegion is not a pool of independent molecules but a subregion 
	 * on these molecules. As such, every RNARegion has a defining RNA molecule. Examples: CDS, 3'; UTR, Hairpin
	 * (<a href="http://www.biopax.org/release/biopax-level3-documentation.pdf">RNA</a>).
	 * Aspects of the state of the RNA region, including cellular location and features, are defined here,
	 * using properties inherited from PhysicalEntity.
	 */
	public static final URI RNA_REGION = URI.create("http://www.biopax.org/release/biopax-level3.owl#RnaRegion");
	
	/**
	 * A physical entity consisting of a sequence of ribonucleotide monophosphates; a ribonucleic acid.
	 * Usage: RNA should be used for pools of individual RNA molecules. For describing subregions on 
	 * those molecules use RNARegion.  Examples: messengerRNA, microRNA, ribosomalRNA. A specific example 
	 * is the let-7 microRNA.
	 * (<a href="http://www.biopax.org/release/biopax-level3-documentation.pdf">RNA</a>).
	 * Aspects of the state of the RNA molecule, including cellular location and features, are defined here,
	 * using properties inherited from PhysicalEntity.
	 */
	public static final URI RNA_MOLECULE = URI.create("http://www.biopax.org/release/biopax-level3.owl#Rna");

	/**
	 * A physical entity consisting of a sequence of amino acids; a protein monomer; a single polypeptide
	 * chain (<a href="http://www.biopax.org/release/biopax-level3-documentation.pdf">Protein</a>). Aspects 
	 * of the state of the protein, including cellular location and features, are defined here,
	 * using properties inherited from PhysicalEntity.
	 */
	public static final URI PROTEIN = URI.create("http://www.biopax.org/release/biopax-level3.owl#Protein");

	/**
	 * A small bioactive molecule (<a href="http://www.biopax.org/release/biopax-level3-documentation.pdf">SmallMolecule</a>). Small is not precisely defined, but includes all metabolites and most drugs
	 * and does not include large polymers, including complex carbohydrates. Aspects of the state of the small
	 * molecule, including cellular location and binding features, are defined here, using properties
	 * inherited from PhysicalEntity.
	 */
	public static final URI SMALL_MOLECULE = URI.create("http://www.biopax.org/release/biopax-level3.owl#SmallMolecule");

	/**
	 * A physical entity whose structure is comprised of other physical entities bound to each other covalently or non-covalently,
	 * at least one of which is a macromolecule (e.g. protein, DNA, or RNA) and the Stoichiometry of the components are known.
	 * Comment: Complexes must be stable enough to function as a biological unit; in general, the temporary association of an enzyme
	 * with its substrate(s) should not be considered or represented as a complex. A complex is the physical product of an interaction
	 * (complexAssembly) and is not itself considered an interaction. The boundaries on the size of complexes described by this class
	 * are not defined here, although possible, elements of the cell such a mitochondria would typically not be described using this
	 * class (later versions of this ontology may include a cellularComponent class to represent these). The strength of binding cannot
	 * be described currently, but may be included in future versions of the ontology, depending on community need. Examples: Ribosome,
	 * RNA polymerase II. Other examples of this class include complexes of multiple protein monomers and complexes of proteins and small
	 * molecules.
	 */
	public static final URI COMPLEX = URI.create("http://www.biopax.org/release/biopax-level3.owl#Complex");

	/* Roles */
	//public static final URI TRANSCRIPTION_FACTOR = URI.create("http://identifiers.org/go/GO:0003700");
	/**
	 * A small molecule which increases (activator) or decreases (inhibitor) the activity of an
	 * (allosteric) enzyme by binding to the enzyme at the regulatory site
	 * (which is different from the substrate-binding catalytic site) (<a href="http://identifiers.org/chebi/CHEBI:35224">Effector</a>).
	 */
	public static final URI EFFECTOR = URI.create("http://identifiers.org/chebi/CHEBI:35224");

	/**
	 * @param identity
	 * @param types
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in the following
	 * constructor or method: 
	 * <ul>
	 * <li>{@link TopLevel#TopLevel(URI)}, or</li>
	 * <li>{@link #setTypes(Set)}.</li>
	 * </ul>
	 */
	ComponentDefinition(URI identity, Set<URI> types) throws SBOLValidationException {
		super(identity);
		this.types = new HashSet<>();
		this.roles = new HashSet<>();
		this.sequences = new HashSet<>();
		this.components = new HashMap<>();
		this.sequenceAnnotations = new HashMap<>();
		this.sequenceConstraints = new HashMap<>();
		setTypes(types);
	}

	/**
	 * @param componentDefinition
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of the following
	 * constructors or methods:
	 * <ul>
	 * <li>{@link TopLevel#TopLevel(TopLevel)},</li>
	 * <li>{@link #addType(URI)},</li>
	 * <li>{@link Component#deepCopy()},</li>
	 * <li>{@link #addComponent(Component)},</li>
	 * <li>{@link SequenceConstraint#deepCopy()},</li>
	 * <li>{@link #addSequenceConstraint(SequenceConstraint)},</li>
	 * <li>{@link SequenceAnnotation#deepCopy()},</li>
	 * <li>{@link #addSequenceAnnotation(SequenceAnnotation)}, or</li>
	 * <li>{@link #setSequences(Set)}.</li>
	 * </ul>
	 */
	private ComponentDefinition(ComponentDefinition componentDefinition) throws SBOLValidationException {
		super(componentDefinition);
		this.types = new HashSet<>();
		this.roles = new HashSet<>();
		this.sequences = new HashSet<>();
		this.components = new HashMap<>();
		this.sequenceAnnotations = new HashMap<>();
		this.sequenceConstraints = new HashMap<>();
		for (URI type : componentDefinition.getTypes()) {
			this.addType(URI.create(type.toString()));
		}
		for (URI role : componentDefinition.getRoles()) {
			this.addRole(URI.create(role.toString()));
		}
		for (Component subComponent : componentDefinition.getComponents()) {
			this.addComponent(subComponent.deepCopy());
		}
		for (SequenceConstraint sequenceConstraint : componentDefinition.getSequenceConstraints()) {
			this.addSequenceConstraint(sequenceConstraint.deepCopy());
		}
		for (SequenceAnnotation sequenceAnnotation : componentDefinition.getSequenceAnnotations()) {
			this.addSequenceAnnotation(sequenceAnnotation.deepCopy());
		}
		this.setSequences(componentDefinition.getSequenceURIs());
	}
	
	void copy(ComponentDefinition componentDefinition) throws SBOLValidationException {
		((TopLevel)this).copy((TopLevel)componentDefinition);
		this.setSequences(componentDefinition.getSequenceURIs());
		for (URI role : componentDefinition.getRoles()) {
			this.addRole(URI.create(role.toString()));
		}
		for (Component component : componentDefinition.getComponents()) {
			String displayId = URIcompliance.findDisplayId(component);
			Component newComponent = this.createComponent(displayId, 
					component.getAccess(), component.getDefinitionURI());
			newComponent.copy(component);
		}
		for (Component component : componentDefinition.getComponents()) {
			if (!component.getMapsTos().isEmpty()) {
				Component copyComponent = this.getComponent(component.getDisplayId());
				for (MapsTo mapsTo : component.getMapsTos()) {
					String displayId = URIcompliance.findDisplayId(mapsTo);
					String localDisplayId = URIcompliance.findDisplayId(mapsTo.getLocal());
					MapsTo newMapsTo = copyComponent.createMapsTo(displayId, mapsTo.getRefinement(), localDisplayId, 
							mapsTo.getRemoteURI());
					newMapsTo.copy(mapsTo);
				}
			}	
		}
		for (SequenceConstraint sequenceConstraint : componentDefinition.getSequenceConstraints()) {
			String displayId = URIcompliance.findDisplayId(sequenceConstraint);
			String subjectDisplayId = URIcompliance.findDisplayId(sequenceConstraint.getSubject());
			String objectDisplayId = URIcompliance.findDisplayId(sequenceConstraint.getObject());
			SequenceConstraint newSequenceConstraint = this.createSequenceConstraint(displayId, 
					sequenceConstraint.getRestriction(), subjectDisplayId, objectDisplayId);
			newSequenceConstraint.copy(sequenceConstraint);
		}
		for (SequenceAnnotation sequenceAnnotation : componentDefinition.getSequenceAnnotations()) {
			String displayId = URIcompliance.findDisplayId(sequenceAnnotation);
			SequenceAnnotation newSequenceAnnotation = this.createSequenceAnnotation(
				displayId,"DUMMY__LOCATION");
			newSequenceAnnotation.copy(sequenceAnnotation);
		}
	}

	/**
	 * Adds the given type URI to this component definition's set of type URIs.
	 *
	 * @param typeURI the given type URI
	 * @return {@code true} if this set did not already contain the given type URI, {@code false} otherwise.
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 10503.
	 */
	public boolean addType(URI typeURI) throws SBOLValidationException {
		if (typeURI.equals(DNA_REGION)||typeURI.equals(RNA_REGION)||typeURI.equals(PROTEIN)||typeURI.equals(SMALL_MOLECULE)) {
			if (this.containsType(DNA_REGION)||this.containsType(RNA_REGION)||this.containsType(PROTEIN)||this.containsType(SMALL_MOLECULE)) {
				throw new SBOLValidationException("sbol-10503", this);
			}
		}
		return types.add(typeURI);
	}

	/**
	 * Removes the given type URI from the set of types.
	 *
	 * @param typeURI the specified type URI
	 * @return {@code true} if the matching type reference was removed successfully, {@code false} otherwise.
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 10502.
	 */
	public boolean removeType(URI typeURI) throws SBOLValidationException {
		if (types.size()==1 && types.contains(typeURI)) {
			throw new SBOLValidationException("sbol-10502", this);
		}
		return types.remove(typeURI);
	}

	/**
	 * Clears the existing set of types first, then adds the given 
	 * set of the types to this component definition.
	 *
	 * @param types the set of types to set to
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul>
	 * <li>if the following SBOL validation rule was violated: 10502.</li>
	 * <li>an SBOL validation rule violation occurred in {@link #addType(URI)}</li>
	 * </ul> 
	 */
	public void setTypes(Set<URI> types) throws SBOLValidationException {
		if (types==null || types.size()==0) {
			throw new SBOLValidationException("sbol-10502", this);
		}
		clearTypes();
		for (URI type : types) {
			addType(type);
		}
	}

	/**
	 * Returns the set of type URIs owned by this component definition.
	 *
	 * @return the set of type URIs owned by this component definition
	 */
	public Set<URI> getTypes() {
		Set<URI> result = new HashSet<>();
		result.addAll(types);
		return result;
	}

	/**
	 * Checks if the given type URI is included in this component definition's
	 * set of type URIs.
	 *
	 * @param typeURI the type URI to be checked
	 * @return {@code true} if this set contains the given type URI, {@code false} otherwise.
	 */
	public boolean containsType(URI typeURI) {
		return types.contains(typeURI);
	}

	/**
	 * Removes all entries of the list of <code>type</code> instances owned by this instance.
	 * The list will be empty after this call returns.
	 */
	private void clearTypes() {
		types.clear();
	}

	/**
	 * Adds the given role URI to this component definition's set of role URIs.
	 *
	 * @param roleURI the role URI to be added
	 * @return {@code true} if this set did not already contain the specified role, {@code false} otherwise.
	 */
	public boolean addRole(URI roleURI) {
		return roles.add(roleURI);
	}

	/**
	 * Removes the given role URI from the set of roles.
	 *
	 * @param roleURI the given role URI to be removed
	 * @return {@code true} if the matching role reference was removed successfully, {@code false} otherwise.
	 */
	public boolean removeRole(URI roleURI) {
		return roles.remove(roleURI);
	}

	/**
	 * Clears the existing set of roles first, and then adds the given
	 * set of the roles to this component definition.
	 *
	 * @param roles the set of roles to set to
	 */
	public void setRoles(Set<URI> roles) {
		clearRoles();
		if (roles==null) return;
		for (URI role : roles) {
			addRole(role);
		}
	}

	/**
	 * Returns the set of role URIs owned by this component definition. 
	 *
	 * @return the set of role URIs owned by this component definition.
	 */
	public Set<URI> getRoles() {
		Set<URI> result = new HashSet<>();
		result.addAll(roles);
		return result;
	}

	/**
	 * Checks if the given role URI is included in this component definition's set of role URIs.
	 *
	 * @param roleURI the role URI to be checked
	 * @return {@code true} if this set contains the given role URI, {@code false} otherwise.
	 */
	public boolean containsRole(URI roleURI) {
		return roles.contains(roleURI);
	}

	/**
	 * Removes all entries of this component definition's set of role URIs.
	 * The set will be empty after this call returns.	 
	 */
	public void clearRoles() {
		roles.clear();
	}

	/**
	 * Adds the URI of the given Sequence instance to this component definition's 
	 * set of sequence URIs. This method calls {@link #addSequence(URI)} with this Sequence URI.
	 *
	 * @param sequence the Sequence instance whose identity URI to be added
	 * @return {@code true} if this set did not already contain the identity URI of the given Sequence, {@code false} otherwise.
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 10513.
	 */
	public boolean addSequence(Sequence sequence) throws SBOLValidationException {
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (this.getSBOLDocument().getSequence(sequence.getIdentity())==null) {
				throw new SBOLValidationException("sbol-10513", sequence);
			}
		}
		return this.addSequence(sequence.getIdentity());
	}

	/**
	 * Adds the given sequence URI to this component definition's set of sequence URIs.
	 *
	 * @param sequenceURI the identity URI of the sequence to be added
	 * @return {@code true} if this set did not already contain the given sequence's URI, {@code false} otherwise.
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 10513. 
	 */
	public boolean addSequence(URI sequenceURI) throws SBOLValidationException {
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (this.getSBOLDocument().getSequence(sequenceURI)==null) {
				throw new SBOLValidationException("sbol-10513",this);
			}
		}
		return sequences.add(sequenceURI);
	}

	/**
	 * Constructs a compliant sequence URI with the given display ID and version, and then adds this URI
	 * to this component definition's set of sequence URIs.
	 * <p>
	 * This method creates a compliant sequence URI with the default
	 * URI prefix, which was set in the SBOLDocument instance hosting this component definition, the given 
	 * display ID and version. It then calls {@link #addSequence(URI)} with this Sequence URI.
	 *
	 * @param displayId the display ID of the sequence whose identity URI is to be added
	 * @param version version of the sequence whose identity URI is to be added
	 * @return {@code true} if this set did not already contain the given sequence's URI, {@code false} otherwise. 
	 * @throws SBOLValidationException see {@link #addSequence(URI)} 
	 */
	public boolean addSequence(String displayId,String version) throws SBOLValidationException {
		URI sequenceURI = URIcompliance.createCompliantURI(this.getSBOLDocument().getDefaultURIprefix(),
				TopLevel.SEQUENCE, displayId, version, this.getSBOLDocument().isTypesInURIs());
		return addSequence(sequenceURI);
	}

	/**
	 * Constructs a compliant sequence URI using the given sequence display ID, and then adds this URI to 
	 * this component definition's set of sequence URIs. This method calls {@link #addSequence(String, String)} with
	 * the given sequence display ID and an empty string as its version. 
	 *
	 * @param displayId the display ID of the sequence whose identity URI is to be added
	 * @return {@code true} if this set did not already contain the given sequence's URI, {@code false} otherwise.
	 * @throws SBOLValidationException see {@link #addSequence(String, String)}
	 */
	public boolean addSequence(String displayId) throws SBOLValidationException {
		return addSequence(displayId,"");
	}

	/**
	 * Returns the set of sequence URIs referenced by this component definition.
	 *
	 * @return the set of sequence URIs referenced by this component definition
	 */
	public Set<URI> getSequenceURIs() {
		Set<URI> result = new HashSet<>();
		result.addAll(sequences);
		return result;
	}

	/**
	 * Returns the set of sequences identities referenced by this component definition.
	 *
	 * @return the set of sequences identities referenced by this component definition
	 */
	public Set<URI> getSequenceIdentities() {
		if (this.getSBOLDocument()==null) return null;
		Set<URI> resolved = new HashSet<>();
		for(URI su : sequences) {
			Sequence seq = this.getSBOLDocument().getSequence(su);
			if(seq != null) {
				resolved.add(seq.getIdentity());
			}
		}
		return resolved;
	}
	
	/**
	 * Returns the set of sequences referenced by this component definition.
	 *
	 * @return the set of sequences referenced by this component definition
	 */
	public Set<Sequence> getSequences() {
		if (this.getSBOLDocument()==null) return null;
		Set<Sequence> resolved = new HashSet<>();
		for(URI su : sequences) {
			Sequence seq = this.getSBOLDocument().getSequence(su);
			if(seq != null) {
				resolved.add(seq);
			}
		}
		return resolved;
	}
	
	/**
	 * Returns the first sequence referenced by this component definition that matches the given sequence encoding.
	 * 
	 * @param encoding URI for a sequence encoding
	 * @return the first sequence that matches the given encoding
	 */
	public Sequence getSequenceByEncoding(URI encoding) {
		if (this.getSBOLDocument()==null) return null;
		for (Sequence sequence : this.getSequences()) {
			if (sequence.getEncoding().equals(encoding)) {
				return sequence;
			}
		}
		return null;
	}	
	
	/**
	 *  Return the elements of a nucleic acid sequence implied by the hierarchically included components.
	 *  <p>
	 *  This method first tries to obtain the length of a nucleic acid sequence from the set of sequences referenced by this component definition 
	 *  that has an {@link Sequence#IUPAC_DNA} encoding. It then iterates through this component defintion's 
	 *  sequence annotations, and update the length with the ending locations that have a larger value than the current length. It then populates
	 *  the elements with this length with unknown bases. This method iterates through this component defintion's sequence annotations to recursively
	 *  search for bases implied by the hierarchically included components, and fills the elements with these known bases.
	 *  
	 *  @return the elements of a nucleic sequence implied by the hierarchically included components
	 */
	public String getImpliedNucleicAcidSequence() {
		URI type = null;
		if (this.getTypes().contains(ComponentDefinition.DNA_REGION)) {
			type = ComponentDefinition.DNA_REGION;
		} else if (this.getTypes().contains(ComponentDefinition.RNA_REGION)) {
			type = ComponentDefinition.RNA_REGION;
		} else {
			return null;
		}
		String elements = "";
		int length = 0;
		if (this.getSequenceByEncoding(Sequence.IUPAC_DNA)!=null) {
			length = this.getSequenceByEncoding(Sequence.IUPAC_DNA).getElements().length();
		}
		for (SequenceAnnotation sequenceAnnotation : this.getSequenceAnnotations()) {
			for (Location location : sequenceAnnotation.getLocations()) {
				if (location instanceof Range) {
					Range range = (Range)location;
					if (range.getEnd()>length) {
						length = range.getEnd();
					}
				}
			}
		}
		for (int i = 0; i < length; i++) {
			elements += "N";
		}
		char[] elementsArray = elements.toCharArray();
		for (SequenceAnnotation sequenceAnnotation : this.getSequenceAnnotations()) {
			String subElements = null;
			if (!sequenceAnnotation.isSetComponent()) continue;
			if (sequenceAnnotation.getComponent().getDefinition()!=null) {
				ComponentDefinition compDef = sequenceAnnotation.getComponent().getDefinition();
				if (compDef.getSequenceByEncoding(Sequence.IUPAC_DNA)!=null) {
					subElements = compDef.getSequenceByEncoding(Sequence.IUPAC_DNA).getElements();
				} else {
					subElements = compDef.getImpliedNucleicAcidSequence();
				}
				String subElementsFinal = subElements;
				for (Location sourceLocation : sequenceAnnotation.getComponent().getSortedSourceLocations()) {
					subElementsFinal = "";
					if (sourceLocation instanceof Range) {
						Range range = (Range)sourceLocation;
						for (int i = range.getStart()-1; i < range.getEnd(); i++) {
							if (i < subElements.length()) {
								subElementsFinal += subElements.charAt(i);
							}
						}
						if (range.isSetOrientation() && range.getOrientation().equals(OrientationType.REVERSECOMPLEMENT)) {
							subElementsFinal = Sequence.reverseComplement(subElementsFinal, type);
						}
					}
				}
				int start = 0;
				for (Location location : sequenceAnnotation.getSortedLocations()) {
					if (location instanceof Range) {
						Range range = (Range)location;
						if (range.isSetOrientation() && range.getOrientation().equals(OrientationType.REVERSECOMPLEMENT)) {
							subElementsFinal = Sequence.reverseComplement(subElementsFinal, type);
						}
						int end = range.getEnd() - range.getStart() + 1 + start;
						for (int i = start /*0*/; i < end /*subElementsFinal.length()*/; i++) {
							if(range.getStart()+(i-start)-1>elementsArray.length) {
								return null;
							}
							if (i < subElementsFinal.length()) {
								elementsArray[(range.getStart()+(i-start))-1] = subElementsFinal.charAt(i);
							}
						}
						start = end;
					}
				}
			}
		}
		elements = String.valueOf(elementsArray);
		return elements;
	}

	/**
	 * Clears the existing set of sequences first, and then adds the given
	 * set of the sequences to this component definition.
	 *
	 * @param sequences the given set of the sequences to set to
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #addSequence(URI)}
	 */
	public void setSequences(Set<URI> sequences) throws SBOLValidationException {
		clearSequences();
		if (sequences==null) return;
		for (URI sequence : sequences) {
			addSequence(sequence);
		}
	}

	/**
	 * Removes the given sequence URI from the set of sequence URIs.
	 *
	 * @param sequenceURI the sequence URI 
	 * @return {@code true} if the matching sequence was removed successfully,
	 *         {@code false} otherwise.
	 */
	public boolean removeSequence(URI sequenceURI) {
		return sequences.remove(sequenceURI);
	}

	/**
	 * Removes all entries of this component definition's set of reference
	 * sequence URIs. The set will be empty after this call returns.
	 */
	public void clearSequences() {
		sequences.clear();
	}

	//	public boolean containsSequence(Sequence sequence) {
	//		return containsSequenceURI(sequence.getIdentity());
	//	}

	/**
	 * Checks if the given sequence URI is included in this component definition's
	 * set of sequence URIs.
	 *
	 * @param sequenceURI the sequence URI to be checked
	 * @return {@code true} if this set contains the given sequence URI, {@code false} otherwise.
	 */
	public boolean containsSequence(URI sequenceURI) {
		return sequences.contains(sequenceURI);
	}

	//	/**
	//	 * Test if any {@link SequenceAnnotation} instance exists.
	//	 * @return <code>true</code> if at least one such instance exists.
	//	 */
	//	public boolean isSetSequenceAnnotations() {
	//		if (sequenceAnnotations.isEmpty())
	//			return false;
	//		else
	//			return true;
	//	}

	/**
	 * Creates a child sequence annotation instance for this component definition with the given arguments, and then adds to this component definition's
	 * list of sequence annotations.
	 *
	 * @param identity the identity URI of the sequence annotation to be created
	 * @param locations the locations property of the sequence annotation to be created
	 * @return the created sequence annotation
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul>
	 * <li>an SBOL validation rule violation occurred in {@link SequenceAnnotation#SequenceAnnotation(URI, Set)}</li>
	 * <li>an SBOL validation rule violation occurred in {@link #addSequenceAnnotation(SequenceAnnotation)}</li>
	 * </ul>
	 */
	private SequenceAnnotation createSequenceAnnotation(URI identity, Set<Location> locations) throws SBOLValidationException {
		SequenceAnnotation sequenceAnnotation = new SequenceAnnotation(identity, locations);
		addSequenceAnnotation(sequenceAnnotation);
		return sequenceAnnotation;
	}

	/**
	 * Creates a child sequence annotation for this component definition with the given arguments,
	 * and then adds to this component definition's list of sequence annotations.
	 *
	 * @param displayId the display ID of the sequence annotation to be created
	 * @param location the location of the sequence annotation to be created
	 * @return the created sequence annotation
	 * @throws SBOLValidationException if any of the following condition is satisfied:
	 * <ul>
	 * <li>an SBOL validation rule violation occurred in {@link URIcompliance#createCompliantURI(String, String, String)};</li>
	 * <li>an SBOL validation rule violation occurred in {@link #createSequenceAnnotation(URI, Set)};</li>
	 * <li>an SBOL validation rule violation occurred in {@link SequenceAnnotation#setDisplayId(String)}; or</li>
	 * <li>an SBOL validation rule violation occurred in {@link SequenceAnnotation#setVersion(String)}.</li>
	 * </ul>
	 */
	private SequenceAnnotation createSequenceAnnotation(String displayId, Location location) throws SBOLValidationException {
		String URIprefix = this.getPersistentIdentity().toString();
		String version = this.getVersion();
		URI newSequenceAnnotationURI = createCompliantURI(URIprefix, displayId, version);
		Set<Location> locations = new HashSet<>();
		locations.add(location);
		SequenceAnnotation sa = createSequenceAnnotation(newSequenceAnnotationURI, locations);
		sa.setPersistentIdentity(createCompliantURI(URIprefix, displayId, ""));
		sa.setDisplayId(displayId);
		sa.setVersion(version);
		return sa;
	}

	/**
 	 * Creates a child sequence annotation for this component definition with the given arguments,
 	 * and then adds to this component definition's list of sequence annotations.
	 * <p>
	 * This method calls {@link #createSequenceAnnotation(String, String, OrientationType)} with
	 * the orientation type set to {@code null}.
	 *
	 * @param displayId the display ID of the sequence annotation to be created
	 * @param locationId the display ID of the location contained by sequence annotation to be created
	 * @return the created sequence annotation
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #createSequenceAnnotation(String, String, OrientationType)}
	 */
	public SequenceAnnotation createSequenceAnnotation(String displayId, String locationId) throws SBOLValidationException {
		return createSequenceAnnotation(displayId,locationId,(OrientationType)null);
	}

	/**
 	 * Creates a child sequence annotation for this component definition with the given arguments,
 	 * and then adds to this component definition's list of sequence annotations.
	 * <p>
	 * This method first creates a GenericLocation instance with a compliant URI. This URI starts with this
	 * component definition's persistent identity, followed by the given sequence annotation's display ID,
	 * which is then followed by the given generic location's display ID, and ends with this component definition version. 
	 *<p> 
	 * This method then creates a sequence annotation with a compliant URI, using this
	 * component definition's persistent identity, the given sequence annotation's display ID,
	 * and this component definition's version. It also adds the created generic location this sequence
	 * annotation. 
	 * 
	 * @param displayId the display ID of the sequence annotation to be created
	 * @param locationId the display ID of the generic location contained by the sequence annotation to be created
	 * @param orientation the orientation type of the sequence annotation to be created
	 * @return the created sequence annotation
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated: 
	 * 10201, 10202, 10204, 10206, 10522, 10902, 10905. 
	 */
	public SequenceAnnotation createSequenceAnnotation(String displayId,String locationId,OrientationType orientation) throws SBOLValidationException {
		String URIprefix = this.getPersistentIdentity().toString()+"/"+displayId;
		String version = this.getVersion();
		GenericLocation location = new GenericLocation(createCompliantURI(URIprefix,locationId,version));
		if (orientation!=null) location.setOrientation(orientation);
		location.setPersistentIdentity(URI.create(URIprefix+"/"+locationId));
		location.setDisplayId(locationId);
		location.setVersion(this.getVersion());
		return createSequenceAnnotation(displayId, location);
	}

	/**
	 * Creates a child sequence annotation for this component definition with the given arguments, 
	 * and then adds to its list of sequence annotations.
	 * <p>
	 * This method calls {@link #createSequenceAnnotation(String,String,int,OrientationType)} with
	 * a {@code null} OrientationType.
	 *
	 * @param displayId the display ID of the sequence annotation to be created
	 * @param locationId the display ID of the location contained by sequence annotation to be created
	 * @param at the integer data field for the Cut location for the sequence annotation to be created 
 	 * @return the created sequence annotation
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #createSequenceAnnotation(String, String, int, OrientationType)}
	 */
	public SequenceAnnotation createSequenceAnnotation(String displayId, String locationId, int at) throws SBOLValidationException {
		return createSequenceAnnotation(displayId,locationId,at,null);
	}

	/**
	 * Creates a child sequence annotation for this component definition with the given arguments, 
	 * and then adds to its list of sequence annotations.
	 * <p> 
	 * This method first creates a Cut instance with a compliant URI. This URI starts with this
	 * component definition's persistent identity, followed by the given sequence annotation's display ID,
	 * which is then followed by the given {@code locationId}, and ends with this component definition version. 
	 * <p>
	 * This method then creates a sequence annotation with the created Cut instance and a compliant URI. 
	 * This URI starts with this component definition's persistent identity, followed by the given sequence annotation's
	 * display ID, and ends with this component definition's version.
	 * 
	 * @param displayId the display ID of the sequence annotation to be created
	 * @param locationId the display ID of the Cut location contained by sequence annotation to be created
	 * @param at the integer data field for the Cut location for the sequence annotation to be created 
	 * @param orientation the orientation type for DNA molecules
 	 * @return the created sequence annotation
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated: 10201, 10202, 10204, 10206, 10522, 10902, 10905.
	 */
	public SequenceAnnotation createSequenceAnnotation(String displayId, String locationId, int at, OrientationType orientation) throws SBOLValidationException {
		String URIprefix = this.getPersistentIdentity().toString()+"/"+displayId;
		String version = this.getVersion();
		Cut location = new Cut(createCompliantURI(URIprefix,locationId,version),at);
		if (orientation!=null) location.setOrientation(orientation);
		location.setPersistentIdentity(URI.create(URIprefix+"/"+locationId));
		location.setDisplayId(locationId);
		location.setVersion(this.getVersion());
		return createSequenceAnnotation(displayId, location);
	}

	/**
	 * Creates a child sequence annotation for this component definition with the given arguments, and then adds to this ComponentDefinition's
	 * list of sequence annotations.
	 * <p>
	 * This method calls {@link #createSequenceAnnotation(String, String, int, int, OrientationType)} with
	 * a {@code null} OrientationType.
	 *
	 * @param displayId the display ID of the sequence annotation to be created 
	 * @param locationId the display ID of the location contained by sequence annotation to be created
	 * @param start the starting position of the Range
	 * @param end the ending position of the Range
	 * @return the created sequence annotation
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #createSequenceAnnotation(String, String, int, int, OrientationType)}. 
	 */
	public SequenceAnnotation createSequenceAnnotation(String displayId, String locationId, int start, int end) throws SBOLValidationException {
		return createSequenceAnnotation(displayId,locationId,start,end,null);
	}

	//	/**
	//	 * Creates a child sequence annotation for this ComponentDefinition
	//	 * instance with the given arguments, and then adds to this ComponentDefinition's
	//	 * list of sequence annotations.
	//	 * <p>
	//	 * If this component definition belongs to an SBOLDocument instance, then
	//	 * the SBOLDocument instance is checked for compliance first. Only a compliant SBOLDocument instance
	//	 * is allowed to be edited.
	//	 * <p>
	//	 * This method first creates a Range Location instance with a compliant URI. This URI is composed
	//	 * of this component definition's persistent identity, the given SequenceAnnotation
	//	 * instance display ID, the given {@code locationId}, and this
	//	 * component definition's version.
	//	 * <p>
	//	 * I then creates a sequence annotation with a compliant URI. This URI is composed of
	//	 * this component definition's persistent identity, the given SequenceAnnotation display ID,
	//	 * and this component definition's version.
	//	 *
	//	 * @param displayId The displayId identifier for this instance
	//	 * @param start starting position of the Range
	//	 * @param end ending position of the Range
	//	 * @param orientation indicate how region specified is oriented on the elements of a Sequence from their parent
	//	 * @param componentDefinitionId
	//	 * @return a sequence annotation
	//	 * @throws SBOLValidationException if the associated SBOLDocument is not compliant
	//	 * @throws SBOLValidationException if the created SequenceAnnotation URI is not compliant in this component definition's URI.
	//	 */
	//	public SequenceAnnotation createSequenceAnnotation(String displayId, int start, int end,OrientationType orientation,
	//			String componentDefinitionId) {
	//		if (this.getSBOLDocument()!=null) this.getSBOLDocument().checkReadOnly();
	//		SequenceAnnotation sequenceAnnotation = createSequenceAnnotation(displayId,"range",start,end,orientation);
	//		if (this.getComponent(componentDefinitionId)==null) {
	//			createComponent(componentDefinitionId,AccessType.PUBLIC,componentDefinitionId,"");
	//		}
	//		sequenceAnnotation.setComponent(componentDefinitionId);
	//		return sequenceAnnotation;
	//	}

	/**
	 * Creates a child sequence annotation for this ComponentDefinition
	 * instance with the given arguments, and then adds to this ComponentDefinition's
	 * list of sequence annotations.
	 * <p>
	 * This method first creates a Range instance with a compliant URI. This URI starts with this
	 * component definition's persistent identity, followed by the given sequence annotation's display ID,
	 * which is then followed by the given {@code locationId}, and ends with this component definition version.
	 * <p>
	 * This method then creates a sequence annotation with the created Range instance and a compliant URI. 
	 * This URI starts with this component definition's persistent identity, followed by the given sequence annotation's
	 * display ID, and ends with this component definition's version.
	 *
	 * @param displayId The displayId identifier for this instance
	 * @param locationId Specifies a region between two discrete positions
	 * @param start starting position of the Range
	 * @param end ending position of the Range
	 * @param orientation the orientation type for DNA molecules
	 * @return the created sequence annotation
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated: 10201, 10202, 10204, 10206, 10522, 10902, 10905.  
	 */
	public SequenceAnnotation createSequenceAnnotation(String displayId, String locationId, int start, int end,OrientationType orientation) throws SBOLValidationException {
		String URIprefix = this.getPersistentIdentity().toString()+"/"+displayId;
		String version = this.getVersion();
		Location location = new Range(createCompliantURI(URIprefix,locationId,version),start,end);
		if (orientation!=null) location.setOrientation(orientation);
		location.setPersistentIdentity(URI.create(URIprefix+"/"+locationId));
		location.setDisplayId(locationId);
		location.setVersion(this.getVersion());
		return createSequenceAnnotation(displayId, location);
	}

	/**
	 * Adds the specified instance to the list of sequenceAnnotations.
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated: 10522, 10905; or</li>
	 * <li>an SBOL validation rule violation occurred in {@link Identified#addChildSafely(Identified, java.util.Map, String, java.util.Map...)}.</li>
	 * </ul>
	 */
	void addSequenceAnnotation(SequenceAnnotation sequenceAnnotation) throws SBOLValidationException {
		sequenceAnnotation.setSBOLDocument(this.getSBOLDocument());
		sequenceAnnotation.setComponentDefinition(this);
		if (sequenceAnnotation.isSetComponent()) {
			if (sequenceAnnotation.getComponent()==null) {
				//throw new SBOLValidationException("Component '" + sequenceAnnotation.getComponentURI() + "' does not exist.");
				throw new SBOLValidationException("sbol-10905", sequenceAnnotation);
			}
			for (SequenceAnnotation sa : this.getSequenceAnnotations()) {
				if (sa.isSetComponent() && sa.getComponentURI().equals(sequenceAnnotation.getComponentURI())) {
					//throw new SBOLValidationException("Multiple sequence annotations cannot refer to the same component.");
					throw new SBOLValidationException("sbol-10522", sa);
				}
			}
		}
		for (Location location : sequenceAnnotation.getLocations()) {
			location.setSBOLDocument(this.getSBOLDocument());
			location.setComponentDefinition(this);
			if (location.isSetSequence() && !getSequenceURIs().contains(location.getSequenceURI())) {
				throw new SBOLValidationException("sbol-11003",this);
			}
		}
		addChildSafely(sequenceAnnotation, sequenceAnnotations, "sequenceAnnotation",
				components, sequenceConstraints);
	}

	/**
	 * Removes the given sequence annotation from the list of sequence annotations.
	 *
	 * @param sequenceAnnotation the given sequence annotation
	 * @return {@code true} if the matching sequence annotation was removed successfully, {@code false} otherwise.
	 */
	public boolean removeSequenceAnnotation(SequenceAnnotation sequenceAnnotation) {
		return removeChildSafely(sequenceAnnotation, sequenceAnnotations);
	}

	/**
	 * Returns the sequence annotation matching the given display ID.
	 *
	 * @param displayId the display ID for the sequence annotation to be retrieved
	 * @return the matching sequence annotation if present, or {@code null} otherwise.
	 */
	public SequenceAnnotation getSequenceAnnotation(String displayId) {
		try {
			return sequenceAnnotations.get(createCompliantURI(this.getPersistentIdentity().toString(),
					displayId,this.getVersion()));
		}
		catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the instance matching the given SequenceAnnotation URI from the
	 * list of sequence annotations.
	 *
	 * @param sequenceAnnotationURI the given SequenceAnnotation URI
	 * @return the matching sequence annotation if present, or
	 *         {@code null} otherwise.
	 */
	public SequenceAnnotation getSequenceAnnotation(URI sequenceAnnotationURI) {
		return sequenceAnnotations.get(sequenceAnnotationURI);
	}

	/**
	 * Returns the sequence annotation that references to the given component.
	 *
	 * @param component the component referenced by the sequence annotation to be retrieved
	 * @return the matching sequence annotation if present, or {@code null} otherwise.
	 */
	public SequenceAnnotation getSequenceAnnotation(Component component) {
		for (SequenceAnnotation sequenceAnnotation : this.getSequenceAnnotations()) {
			if (sequenceAnnotation.getComponent() != null &&
					sequenceAnnotation.getComponent().equals(component)) return sequenceAnnotation;
		}
		return null;
	}

	/**
	 * Returns the set of sequence annotations owned by this
	 * component definition.
	 *
	 * @return the set of sequence annotations owned by this
	 *         component definition.
	 */
	public Set<SequenceAnnotation> getSequenceAnnotations() {
		//		return (List<SequenceAnnotation>) structuralAnnotations.values();
		Set<SequenceAnnotation> sequenceAnnotations = new HashSet<>();
		sequenceAnnotations.addAll(this.sequenceAnnotations.values());
		return sequenceAnnotations;
	}

	/**
	 * @param successorMap
	 * @param component
	 * @param visited
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 10605.
	 */
	private void getSuccessorComponents(HashMap<Component,Set<Component>> successorMap,
			Component component, Set<Component> visited) throws SBOLValidationException {
		if (visited.contains(component)) {
			// TODO: cycle detected.  This needs to be handled better.
			throw new SBOLValidationException("Cycle in sequence constraints");
			//throw new SBOLValidationException("sbol-10605", component);
			//return;
		}
		visited.add(component);
		for (SequenceConstraint sc : this.getSequenceConstraints()) {
			if (sc.getSubject().equals(component)) {
				successorMap.get(component).add(sc.getObject());
				getSuccessorComponents(successorMap,sc.getObject(),visited);
				successorMap.get(component).addAll(successorMap.get(sc.getObject()));
			}
		}
		visited.remove(component);
	}

	private boolean isGenericSequenceAnnotation(SequenceAnnotation sequenceAnnotation) {
		boolean generic = true;
		for (Location location : sequenceAnnotation.getLocations()) {
			if (location instanceof Range || location instanceof Cut) {
				generic = false;
				break;
			}
		}
		return generic;
	}
	
	/**
	 * Returns a sorted list of components owned by this component definition. The order is determined by the
	 * order of appearance of components on a DNA strand.
	 *
	 * @return a sorted list of components owned by this component definition
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 10605.
	 */
	public List<Component> getSortedComponents() throws SBOLValidationException {
		List<Component> sortedComponents = new ArrayList<Component>();
		List<SequenceAnnotation> sortedSAs = new ArrayList<SequenceAnnotation>();
		sortedSAs.addAll(this.getSequenceAnnotations());
		Collections.sort(sortedSAs);
		HashMap<Component,Set<Component>> successorMap = new HashMap<Component,Set<Component>>();
		for (Component component : this.getComponents()) {
			successorMap.put(component, new HashSet<Component>());
		}
		for (int i = 0; i < sortedSAs.size(); i++) {
			SequenceAnnotation source = sortedSAs.get(i);
			if (isGenericSequenceAnnotation(source)) continue;
			if (source.isSetComponent()) {
				Component sourceComponent = source.getComponent();
				for (int j = i + 1; j < sortedSAs.size(); j++) {
					SequenceAnnotation target = sortedSAs.get(j);
					if (isGenericSequenceAnnotation(target)) continue;
					if (target.isSetComponent()) {
						Component targetComponent = target.getComponent();
						successorMap.get(sourceComponent).add(targetComponent);
					}
				}
			}
		}
		for (Component component : this.getComponents()) {
			getSuccessorComponents(successorMap,component,new HashSet<Component>());
		}
		while (true) {
			boolean change = false;
			for (Component component1 : this.getComponents()) {
				if (sortedComponents.contains(component1)) continue;
				boolean add = true;
				for (Component component2 : this.getComponents()) {
					if (component1 == component2) continue;
					if (sortedComponents.contains(component2)) continue;
					if (successorMap.get(component2).contains(component1)) {
						add = false;
						break;
					}
				}
				if (add) {
					sortedComponents.add(component1);
					change = true;
					break;
				}
			}
			if (!change) {
				break;
			}
		}
		return sortedComponents;
	}

	/**
	 * Returns a sorted list of sequence annotations owned by this
	 * component definition. The order is determined by each 
	 * sequence annotation's location. 
	 * <p>
	 * Priority is given to a sequence annotation whose location has a lower (starting) value. Here is an example.
	 * <ul>
	 * <li>SA1 has a range location (4, 10);</li>
	 * <li>SA2 has a range location (3, 7);</li>
	 * <li>SA3 has a cut location 2;</li>
	 * <li>SA4 has a cut location 4;</li>
	 * <li>SA5 has a cut location 6;</li>
	 * <li>SA6 has a cut location 10; and</li>
	 * <li>SA7 has a generic location.</li>
	 * </ul>
	 * The sorted list it returns is [SA3, SA2, SA4, SA1, SA5, SA6, SA7].
	 * 
	 *
	 * @return a sorted list of sequence annotations owned by this
	 *         component definition.
	 */
	public List<SequenceAnnotation> getSortedSequenceAnnotations() {
		List<SequenceAnnotation> sortedSAs = new ArrayList<SequenceAnnotation>();
		sortedSAs.addAll(this.getSequenceAnnotations());
		Collections.sort(sortedSAs);
		return sortedSAs;
	}

	class SADisplayIdComparator implements Comparator<Object> {

	    public int compare(Object obj1, Object obj2) {
	        SequenceAnnotation myObj1 = (SequenceAnnotation)obj1;
	        SequenceAnnotation myObj2 = (SequenceAnnotation)obj2;
	        if (myObj1.getDisplayId().startsWith("annotation") &&
	        		myObj2.getDisplayId().startsWith("annotation")) {
	        	int myObj1int = Integer.parseInt(myObj1.getDisplayId().replace("annotation",""));
	        	int myObj2int = Integer.parseInt(myObj2.getDisplayId().replace("annotation",""));
	        	return myObj1int - myObj2int;
	        }
	        return myObj1.getDisplayId().compareTo(myObj2.getDisplayId());
	    }
	}
	
	List<SequenceAnnotation> getSortedSequenceAnnotationsByDisplayId() {
		List<SequenceAnnotation> sortedSAs = new ArrayList<SequenceAnnotation>();
		sortedSAs.addAll(this.getSequenceAnnotations());
		Collections.sort(sortedSAs,new SADisplayIdComparator());
		return sortedSAs;
	}

	/**
	 * Removes all entries of this component definition's list of sequence annotations.
	 * The list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeSequenceAnnotation(SequenceAnnotation)} to iteratively remove
	 * each sequence annotation owned by this component definition.
	 *
	 */
	public void clearSequenceAnnotations() {
		Object[] valueSetArray = sequenceAnnotations.values().toArray();
		for (Object sequenceAnnotation : valueSetArray) {
			removeSequenceAnnotation((SequenceAnnotation)sequenceAnnotation);
		}
	}

	/**
	 * @param sequenceAnnotations the specified instance of  SequenceAnnotation to be added
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #addSequenceAnnotation(SequenceAnnotation)}
	 */
	void setSequenceAnnotations(Set<SequenceAnnotation> sequenceAnnotations) throws SBOLValidationException {
		clearSequenceAnnotations();
		for (SequenceAnnotation sequenceAnnotation : sequenceAnnotations) {
			addSequenceAnnotation(sequenceAnnotation);
		}
	}

	//	/**
	//	 * Test if the optional field variable <code>structuralInstantiations</code> is set.
	//	 * @return <code>true</code> if the field variable is not an empty list
	//	 */
	//	public boolean isSetSubComponents() {
	//		if (subComponents.isEmpty())
	//			return false;
	//		else
	//			return true;
	//	}


	/**
	 * Creates a child Component instance for this ComponentDefinition
	 * instance with the given arguments, and then adds to this ComponentDefinition's list of Component
	 * instances.
	 *
	 * @param identity the identifier for this instance
	 * @param access indicates whether the ComponentInstance can be referred to remotely
	 * @param componentDefinitionURI definition
	 * @return a Component instance
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul>
	 * <li>an SBOL validation rule violation occurred in {@link Component#Component(URI, AccessType, URI)}</li>
	 * <li>an SBOL validation rule violation occurred in {@link #addComponent(Component)}</li>
	 * </ul>
	 */
	private Component createComponent(URI identity, AccessType access, URI componentDefinitionURI) throws SBOLValidationException {
		Component component = new Component(identity, access, componentDefinitionURI);
		addComponent(component);
		return component;
	}

	/**
	 * Creates a child component for this component definition with the given arguments, 
	 * and then adds it to this component definition's list of components.
	 * <p>
	 * This method first creates a compliant URI for the component definition that is referenced by
	 * the child component to be created, i.e., the child component's definition property. 
	 * This URI starts with the default
	 * URI prefix, which was set in the SBOLDocument instance hosting this component definition, 
	 * followed by the given display ID and ends with {@code version}. 
	 * It then calls {@link #createComponent(String, AccessType, URI)}
	 * with this compliant component URI.
	 *
	 * @param displayId the display ID for the component to be created
	 * @param access the access property for the component to be created
	 * @param definitionId the display ID of the component definition referenced by the component to be created
	 * @param version the version for the component to be created
	 * @return the created component
	 * @throws SBOLValidationException if either of the following condition is satisfied: 
	 * <ul>
	 * <li>if either of the following SBOL validation rules was violated: 10204, 10206; or</li>
	 * <li>an SBOL validation exception occurred in {@link #createComponent(String, AccessType, URI)}</li>
	 * </ul>
	 */
	public Component createComponent(String displayId, AccessType access,
			String definitionId, String version) throws SBOLValidationException {
		URI definitionURI = URIcompliance.createCompliantURI(this.getSBOLDocument().getDefaultURIprefix(),
				TopLevel.COMPONENT_DEFINITION, definitionId, version, this.getSBOLDocument().isTypesInURIs());
		return createComponent(displayId,access,definitionURI);
	}

	/**
	 * Creates a child component for this component definition with the given arguments, 
	 * and then adds it to this component definition's list of components.
	 * <p>
	 * This method calls {@link #createComponent(String, AccessType, URI)}
	 * with the given arguments and an empty string for version.
	 *
	 * @param displayId the display ID for the component to be created
	 * @param access the access property for the component to be created
	 * @param definitionId the display ID of the component definition referenced by the component to be created
	 * @return the created component
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #createComponent(String, AccessType, String, String)}
	 */
	public Component createComponent(String displayId, AccessType access, String definitionId) throws SBOLValidationException {
		return createComponent(displayId,access,definitionId,"");
	}

	/**
	 * Creates a child component for this component definition with the given arguments, 
	 * and then adds to this component definition's list of components.
	 * <p>
	 * This method first creates a compliant URI for the child component to be created. 
	 * This URI starts with this component definition's persistent identity, 
	 * followed by the given display ID and ends with this component defintion's version. 
	 * 
	 * @param displayId the display ID for the component to be created
	 * @param access the access property for the component to be created
	 * @param definitionURI the URI of the component definition referenced by the component to be created
	 * @return the created component
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10201, 10202, 10204, 10206, 10602, 10604, 10605, 10607, 10803.
	 */
	public Component createComponent(String displayId, AccessType access, URI definitionURI) throws SBOLValidationException {
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (this.getSBOLDocument().getComponentDefinition(definitionURI)==null) {
				throw new SBOLValidationException("sbol-10604",this);
			}
		}
		String URIprefix = this.getPersistentIdentity().toString();
		String version = this.getVersion();
		Component c = createComponent(createCompliantURI(URIprefix, displayId, version),
				access, definitionURI);
		c.setPersistentIdentity(createCompliantURI(URIprefix, displayId, ""));
		c.setDisplayId(displayId);
		c.setVersion(version);
		return c;
	}

	/**
	 * Adds the given component to the list of components.
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated: 10604, 10605, 10803</li>
	 * <li>an SBOL validation rule violation occurred in {@link Identified#addChildSafely(Identified, java.util.Map, String, java.util.Map...)}</li>
	 * </ul>
	 */
	private void addComponent(Component component) throws SBOLValidationException {
		component.setSBOLDocument(this.getSBOLDocument());
		component.setComponentDefinition(this);
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (component.getDefinition()==null) {
				throw new SBOLValidationException("sbol-10604",component);
			}
		}
		if (component.getDefinition()!=null &&
				this.getIdentity().equals(component.getDefinition().getIdentity())) {
			throw new SBOLValidationException("sbol-10605", component);
		}
		Set<URI> visited = new HashSet<>();
		visited.add(this.getIdentity());
		try {
			SBOLValidate.checkComponentDefinitionCycle(this.getSBOLDocument(), component.getDefinition(), visited);
		} catch (SBOLValidationException e) {
			throw new SBOLValidationException("sbol-10605",component);
		}
		addChildSafely(component, components, "component",
				sequenceAnnotations, sequenceConstraints);
		for (MapsTo mapsTo : component.getMapsTos()) {
			if (this.getComponent(mapsTo.getLocalURI())==null) {
				throw new SBOLValidationException("sbol-10803", mapsTo);
			}
			mapsTo.setSBOLDocument(this.getSBOLDocument());
			mapsTo.setComponentDefinition(this);
			mapsTo.setComponentInstance(component);
		}
	}

	/**
	 * @param component
	 * @throws SBOLValidationException if any of the following conditions is satisfied:
	 * <ul>
	 * <li>if the following SBOL validation rules was violated: 10603, 10604;</li>
	 * <li>if an SBOL validation rule violation occurred in {@link SBOLValidate#checkComponentDefinitionCycle}; or </li>
	 * <li>if an SBOL validation rule violation occurred in {@link Identified#addChildSafely(Identified, java.util.Map, String, java.util.Map...)}</li>
	 * </ul>
	 */
	private void addComponentNoCheck(Component component) throws SBOLValidationException {
		component.setSBOLDocument(this.getSBOLDocument());
		component.setComponentDefinition(this);
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (component.getDefinition()==null) {
				throw new SBOLValidationException("sbol-10604",component);
			}
		}
		if (this.getIdentity().equals(component.getDefinitionURI())) {
			throw new SBOLValidationException("sbol-10603",component);
		}
		Set<URI> visited = new HashSet<>();
		visited.add(this.getIdentity());
		SBOLValidate.checkComponentDefinitionCycle(this.getSBOLDocument(), component.getDefinition(), visited);
		addChildSafely(component, components, "component",
				sequenceAnnotations, sequenceConstraints);
	}

	/**
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 10803.
	 */
	private void checkMapsTosLocalURIs() throws SBOLValidationException {
		for (Component component : this.getComponents()) {
			for (MapsTo mapsTo : component.getMapsTos()) {
				if (this.getComponent(mapsTo.getLocalURI())==null) {
					throw new SBOLValidationException("sbol-10803", mapsTo);
				}
				mapsTo.setSBOLDocument(this.getSBOLDocument());
				mapsTo.setComponentDefinition(this);
				mapsTo.setComponentInstance(component);
			}
		}
	}
	/**
	 * Removes the given component from the list of components.
	 * 
	 * @param component the given component
	 * @return {@code true} if the matching component was removed successfully,
	 *         {@code false} otherwise.
	 * @throws SBOLValidationException if any of the following SBOL validation rules were violated:
	 * 10803, 10808, 10905, 11402, 11404, 
	 */
	public boolean removeComponent(Component component) throws SBOLValidationException {
		for (SequenceAnnotation sa : sequenceAnnotations.values()) {
			if (sa.isSetComponent() && sa.getComponentURI().equals(component.getIdentity())) {
				throw new SBOLValidationException("sbol-10905",sa);
			}
		}
		for (SequenceConstraint sc : sequenceConstraints.values()) {
			if (sc.getSubjectURI().equals(component.getIdentity())) {
				throw new SBOLValidationException("sbol-11402", sc);
			}
			if (sc.getObjectURI().equals(component.getIdentity())) {
				throw new SBOLValidationException("sbol-11404", sc);
			}
		}
		for (Component c : components.values()) {
			for (MapsTo mt : c.getMapsTos()) {
				if (mt.getLocalURI().equals(component.getIdentity())) {
					throw new SBOLValidationException("sbol-10803", mt);
				}
			}
		}
		if (this.getSBOLDocument()!=null) {
			for (ComponentDefinition cd : this.getSBOLDocument().getComponentDefinitions()) {
				for (Component c : cd.getComponents()) {
					for (MapsTo mt : c.getMapsTos()) {
						if (mt.getRemoteURI().equals(component.getIdentity())) {
							throw new SBOLValidationException("sbol-10808", mt);
						}
					}
				}
			}
		}
		return removeChildSafely(component, components);
	}

	/**
	 * Returns the component matching the given component's display ID.
	 * <p>
	 * This method first creates a compliant URI for the component to be retrieved. It starts with
	 * this component definition's persistent identity, followed by the given component's display ID,
	 * and ends with this component definition's version.
	 * 
	 * @param displayId the display ID of the component to be retrieved
	 * @return the matching component if present, or {@code null} otherwise.
	 */
	public Component getComponent(String displayId) {
		try {
			return components.get(createCompliantURI(this.getPersistentIdentity().toString(),displayId,this.getVersion()));
		}
		catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the instance matching the given component's identity URI.
	 *
	 * @param componentURI the identity URI of the component to be retrieved
	 * @return the matching component if present, or {@code null} otherwise.
	 */
	public Component getComponent(URI componentURI) {
		return components.get(componentURI);
	}

	/**
	 * Returns the set of components owned by this component definition.
	 *
	 * @return the set of components owned by this
	 *         component definition.
	 */
	public Set<Component> getComponents() {
		Set<Component> components = new HashSet<>();
		components.addAll(this.components.values());
		return components;
	}

	/**
	 * Removes all entries of this component definition's list of components.
	 * The list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeComponent(Component component)} to iteratively remove
	 * each component.
	 *
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #removeComponent(Component)}.
	 */
	public void clearComponents() throws SBOLValidationException {
		Object[] valueSetArray = components.values().toArray();
		for (Object component : valueSetArray) {
			removeComponent((Component)component);
		}
	}

	/**
	 * @param components
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of the following methods:
	 * <ul>
	 * <li>{@link #clearComponents()}</li>
	 * <li>{@link #addComponentNoCheck(Component)}, or</li>
	 * <li>{@link #checkMapsTosLocalURIs()}.</li>
	 * </ul>
	 */
	void setComponents(Set<Component> components) throws SBOLValidationException {
		clearComponents();
		for (Component component : components) {
			addComponentNoCheck(component);
		}
		checkMapsTosLocalURIs();
	}

	//	/**
	//	 * Test if the optional field variable <code>sequenceConstraints</code> is set.
	//	 * @return <code>true</code> if the field variable is not an empty list
	//	 */
	//	public boolean isSetSequenceConstraints() {
	//		if (sequenceConstraints.isEmpty())
	//			return false;
	//		else
	//			return true;
	//	}

	/**
	 * Calls the StructuralConstraint constructor to create a new sequence constraint using the given arguments, 
	 * then adds to this component defintion's list of sequence constraints.
	 * 
	 * @param identity
	 * @param restriction
	 * @param subject
	 * @param object
	 * @return the created sequence constraint
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul>
	 * <li>{@link SequenceConstraint#SequenceConstraint(URI, RestrictionType, URI, URI)}</li>
	 * <li>{@link #addSequenceConstraint(SequenceConstraint)}</li>
	 * </ul>
	 */
	private SequenceConstraint createSequenceConstraint(URI identity, RestrictionType restriction, URI subject, URI object) throws SBOLValidationException {
		SequenceConstraint sequenceConstraint = new SequenceConstraint(identity, restriction, subject, object);
		addSequenceConstraint(sequenceConstraint);
		return sequenceConstraint;
	}

	/**
	 * Creates a child sequence constraint for this component definition with the given arguments, 
	 * and then adds to this component definition's list of sequence constraints.
	 * <p>
	 * Creation of a sequence constraint requires URIs of a subject component and an object component. 
	 * This method creates compliant URIs for them respectively. The subject component compliant URI is created with this 
	 * component defintion's persistent identity URI, followed by the given subject component's display ID, 
	 * followed by this component's version. The object component compliant URI is created similarly. 
	 * This method then calls {@link #createSequenceConstraint(String, RestrictionType, URI, URI)} to create
	 * the sequence constraint.
	 * <p>
	 * This method automatically creates a subject component if all of the following conditions are satisfied:
	 * <ul>
	 * <li>the associated SBOLDocument, i.e., the SBOLDocument instance hosting this component definition, is not {@code null};</li>
	 * <li>if default components should be automatically created when not present for the associated SBOLDocument instance,
	 * i.e., {@link SBOLDocument#isCreateDefaults} returns {@code true}; and</li>
	 * <li>if this component definition does not already have a component with the created compliant subject component URI.</li> 
	 * </ul>
	 * An object component is automatically created if the similar set of conditions hold.
	 *
	 * @param displayId the display ID of the sequence constraint to be created
	 * @param restriction the structural restriction of the subject and object components
	 * @param subjectId the display ID of the subject component
	 * @param objectId the display ID of the object component
	 * @return the created sequence constraint
	 * @throws SBOLValidationException if any of the following condition is satisfied:
	 * <ul>
	 * <li>if any of the following SBOL validation rules was violated: 
	 * 10201, 10202, 10204, 10206, 10602, 10604, 10605, 10607, 10803; or</li>
	 * <li>an SBOL validation rule violation occurred in {@link #createSequenceConstraint(String, RestrictionType, URI, URI)}.</li>
	 * </ul>
	 */
	public SequenceConstraint createSequenceConstraint(String displayId,
			RestrictionType restriction, String subjectId, String objectId) throws SBOLValidationException {
		URI subjectURI = URIcompliance.createCompliantURI(this.getPersistentIdentity().toString(),
				subjectId, this.getVersion());
		if (this.getSBOLDocument()!=null && this.getSBOLDocument().isCreateDefaults() &&
				this.getComponent(subjectURI)==null) {
			this.createComponent(subjectId,AccessType.PUBLIC,subjectId,"");
		}
		URI objectURI = URIcompliance.createCompliantURI(this.getPersistentIdentity().toString(),
				objectId, this.getVersion());
		if (this.getSBOLDocument()!=null && this.getSBOLDocument().isCreateDefaults() &&
				this.getComponent(objectURI)==null) {
			this.createComponent(objectId,AccessType.PUBLIC,objectId,"");
		}
		return createSequenceConstraint(displayId,restriction,subjectURI,objectURI);
	}


	/**
	 * Creates a child sequence constraint for this component definition
	 * with the given arguments, and then adds to this component definition's
	 * list of sequence constraints.
	 * 
	 * @param displayId the display ID of the sequence constraint to be created
	 * @param restriction the structural restriction of the subject and object components
	 * @param subjectURI the display ID of the subject component
	 * @param objectURI the display ID of the object component
	 * @return the created sequence constraint
	 * @throws SBOLValidationException if any of the following SBOL validation rule was violated:
	 * 10201, 10202, 10204, 10206, 11402, 11403, 11404, 11405, 11406, 11407, 11412.
	 */
	public SequenceConstraint createSequenceConstraint(String displayId,
			RestrictionType restriction, URI subjectURI, URI objectURI) throws SBOLValidationException {
		String URIprefix = this.getPersistentIdentity().toString();
		String version = this.getVersion();
		if (this.getSBOLDocument()!=null && this.getSBOLDocument().isCreateDefaults() &&
				this.getComponent(subjectURI)==null && this.getSBOLDocument().getComponentDefinition(subjectURI)!=null) {
			ComponentDefinition subject = this.getSBOLDocument().getComponentDefinition(subjectURI);
			Component subjectComp = this.getComponent(subject.getDisplayId());
			if (subjectComp==null) {
				subjectComp = this.createComponent(subject.getDisplayId(),AccessType.PUBLIC,subjectURI);
			}
			subjectURI = subjectComp.getIdentity();
		}
		if (this.getSBOLDocument()!=null && this.getSBOLDocument().isCreateDefaults() &&
				this.getComponent(objectURI)==null && this.getSBOLDocument().getComponentDefinition(objectURI)!=null) {
			ComponentDefinition object = this.getSBOLDocument().getComponentDefinition(objectURI);
			Component objectComp = this.getComponent(object.getDisplayId());
			if (objectComp==null) {
				objectComp = this.createComponent(object.getDisplayId(),AccessType.PUBLIC,objectURI);
			}
			objectURI = objectComp.getIdentity();	
		}
		SequenceConstraint sc = createSequenceConstraint(createCompliantURI(URIprefix, displayId, version),
				restriction, subjectURI, objectURI);
		sc.setPersistentIdentity(createCompliantURI(URIprefix, displayId, ""));
		sc.setDisplayId(displayId);
		sc.setVersion(version);
		return sc;
	}

	/**
	 * @param sequenceConstraint
	 * @throws SBOLValidationException if any of the following condition is satisfied:
	 * <ul>
	 * <li>if any of the following SBOL validation rule was violated: 11403, 11405, 11406; or</li>
	 * <li>an SBOL validation rule violation occurred in {@link Identified#addChildSafely(Identified, java.util.Map, String, java.util.Map...)}.</li>
	 * </ul>
	 */
	private void addSequenceConstraint(SequenceConstraint sequenceConstraint) throws SBOLValidationException {
		sequenceConstraint.setSBOLDocument(this.getSBOLDocument());
		sequenceConstraint.setComponentDefinition(this);
		if (sequenceConstraint.getSubject()==null) {
			throw new SBOLValidationException("sbol-11403", sequenceConstraint);
		}
		if (sequenceConstraint.getObject()==null) {
			throw new SBOLValidationException("sbol-11405", sequenceConstraint);
		}
		if (sequenceConstraint.getSubjectURI().equals(sequenceConstraint.getObjectURI())) {
			throw new SBOLValidationException("sbol-11406", sequenceConstraint);
		}
		SBOLValidate.checkSequenceConstraint(this, sequenceConstraint);
		addChildSafely(sequenceConstraint, sequenceConstraints, "sequenceConstraint",
				components, sequenceAnnotations);
	}

	/**
	 * Removes the given sequence constraint from the list of sequence constraints.
	 *
	 * @param sequenceConstraint the given sequence constraint
	 * @return {@code true} if the matching sequence constraint was removed successfully, {@code false} otherwise.
	 */
	public boolean removeSequenceConstraint(SequenceConstraint sequenceConstraint) {
		return removeChildSafely(sequenceConstraint,sequenceConstraints);
	}

	/**
	 * Returns the sequence constraint matching the given display ID. 
	 *
	 * @param displayId the display ID of the sequence constraint to be retrieved
	 * @return the matching sequence constraint if present, or {@code null} otherwise.
	 */
	public SequenceConstraint getSequenceConstraint(String displayId) {
		try {
			return sequenceConstraints.get(createCompliantURI(this.getPersistentIdentity().toString(),displayId,
					this.getVersion()));
		}
		catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the sequence constraint matching the given URI.
	 *
	 * @param sequenceConstraintURI the identity URI of the sequence constraint to be retrieved
	 * @return the matching sequence constraint if present, or
	 *         {@code null} otherwise.
	 */
	public SequenceConstraint getSequenceConstraint(URI sequenceConstraintURI) {
		return sequenceConstraints.get(sequenceConstraintURI);
	}

	/**
	 * Returns the set of sequence constraints owned by this
	 * component definition.
	 *
	 * @return the set of sequence constraints owned by this
	 *         component definition
	 */
	public Set<SequenceConstraint> getSequenceConstraints() {
		Set<SequenceConstraint> sequenceConstraints = new HashSet<>();
		sequenceConstraints.addAll(this.sequenceConstraints.values());
		return sequenceConstraints;
	}

	/**
	 * Removes all entries of this component definition's list of sequence constraints.
	 * The list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeSequenceConstraint(SequenceConstraint)} to iteratively remove
	 * each sequence constraint owned by this component definition.
	 */
	public void clearSequenceConstraints() {
		Object[] valueSetArray = sequenceConstraints.values().toArray();
		for (Object sequenceConstraint : valueSetArray) {
			removeSequenceConstraint((SequenceConstraint)sequenceConstraint);
		}
	}

	/**
	 * Clears the existing list of structuralConstraint instances, then appends all of the elements in the specified collection to the end of this list.
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #addSequenceConstraint(SequenceConstraint)}
	 */
	void setSequenceConstraints(Set<SequenceConstraint> sequenceConstraints) throws SBOLValidationException {
		clearSequenceConstraints();
		for (SequenceConstraint sequenceConstraint : sequenceConstraints) {
			addSequenceConstraint(sequenceConstraint);
		}
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.abstract_classes.TopLevel#updateCompliantURI(java.lang.String, java.lang.String, java.lang.String)
	 */
	/**
	 * @throws SBOLValidationException an SBOL validation rule violation occurred in either of the following methods:
	 * <ul>
	 * <li>{@link URIcompliance#isTopLevelURIformCompliant(URI)}, or</li>
	 * <li>{@link URIcompliance#isChildURIcompliant(Identified, Identified)}.</li>
	 * </ul>
	 */
	@Override
	void checkDescendantsURIcompliance() throws SBOLValidationException {
		//isTopLevelURIformCompliant(this.getIdentity());
		if (!this.getSequenceConstraints().isEmpty()) {
			for (SequenceConstraint sequenceConstraint : this.getSequenceConstraints()) {
				try {
					isChildURIcompliant(this, sequenceConstraint);
				}
				catch (SBOLValidationException e) {
					throw new SBOLValidationException(e.getRule(),sequenceConstraint);
				}
			}
		}
		if (!this.getComponents().isEmpty()) {
			for (Component component : this.getComponents()) {
				try {
					isChildURIcompliant(this, component);
				}
				catch (SBOLValidationException e) {
					throw new SBOLValidationException(e.getRule(),component);
				}
				if (!component.getMapsTos().isEmpty()) {
					// Check compliance of Component's children
					for (MapsTo mapsTo : component.getMapsTos()) {
						try {
							isChildURIcompliant(component, mapsTo);
						}
						catch (SBOLValidationException e) {
							throw new SBOLValidationException(e.getRule(),mapsTo);
						}
					}
				}
			}
		}
		if (!this.getSequenceAnnotations().isEmpty()) {
			for (SequenceAnnotation sequenceAnnotation : this.getSequenceAnnotations()) {
				try {
					isChildURIcompliant(this, sequenceAnnotation);
				}
				catch (SBOLValidationException e) {
					throw new SBOLValidationException(e.getRule(),sequenceAnnotation);
				}
				Set<Location> locations = sequenceAnnotation.getLocations();
				for (Location location : locations) {
					if (location instanceof Range) {
						try {
							isChildURIcompliant(sequenceAnnotation, location);
						}
						catch (SBOLValidationException e) {
							throw new SBOLValidationException(e.getRule(),location);
						}
					}
					if (location instanceof Cut) {
						try {
							isChildURIcompliant(sequenceAnnotation, location);
						}
						catch (SBOLValidationException e) {
							throw new SBOLValidationException(e.getRule(),location);
						}
					}
					if (location instanceof GenericLocation) {
						try {
							isChildURIcompliant(sequenceAnnotation, location);
						}
						catch (SBOLValidationException e) {
							throw new SBOLValidationException(e.getRule(),location);
						}
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.TopLevel#deepCopy()
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #ComponentDefinition(ComponentDefinition)}.
	 */
	@Override
	ComponentDefinition deepCopy() throws SBOLValidationException {
		return new ComponentDefinition(this);
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.abstract_classes.TopLevel#copy(java.lang.String, java.lang.String, java.lang.String)
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of the following constructors or methods:
	 * <ul>
	 * <li>{@link #deepCopy()},</li>
	 * <li>{@link URIcompliance#createCompliantURI(String, String, String)},</li>
	 * <li>{@link #setDisplayId(String)},</li>
	 * <li>{@link #setVersion(String)},</li>
	 * <li>{@link #setWasDerivedFrom(URI)},</li>
	 * <li>{@link #setIdentity(URI)}</li>
	 * <li>{@link Component#setDisplayId(String)}</li>
	 * <li>{@link Component#updateCompliantURI(String, String, String)},</li>
	 * <li>{@link #addComponent(Component)},</li>
 	 * <li>{@link SequenceConstraint#setDisplayId(String)}</li>
	 * <li>{@link SequenceConstraint#updateCompliantURI(String, String, String)},</li>
	 * <li>{@link #addSequenceConstraint(SequenceConstraint)},</li>
 	 * <li>{@link SequenceAnnotation#setDisplayId(String)}</li>
	 * <li>{@link SequenceAnnotation#updateCompliantURI(String, String, String)}, or</li>
	 * <li>{@link #addSequenceAnnotation(SequenceAnnotation)},</li>
	 * </ul>
	 */
	@Override
	ComponentDefinition copy(String URIprefix, String displayId, String version) throws SBOLValidationException {
		ComponentDefinition cloned = this.deepCopy();
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
		int count = 0;
		for (Component component : cloned.getComponents()) {
			if (!component.isSetDisplayId()) component.setDisplayId("component"+ ++count);
			component.updateCompliantURI(cloned.getPersistentIdentity().toString(),
					component.getDisplayId(),version);
			cloned.removeChildSafely(component, cloned.components);
			cloned.addComponent(component);
		}
		count = 0;
		for (SequenceConstraint sequenceConstraint : cloned.getSequenceConstraints()) {
			if (!sequenceConstraint.isSetDisplayId()) sequenceConstraint.setDisplayId("sequenceConstraint"+ ++count);
			sequenceConstraint.updateCompliantURI(cloned.getPersistentIdentity().toString(),
					sequenceConstraint.getDisplayId(),version);
			cloned.removeChildSafely(sequenceConstraint, cloned.sequenceConstraints);
			cloned.addSequenceConstraint(sequenceConstraint);
		}
		count = 0;
		for (SequenceAnnotation sequenceAnnotation : cloned.getSequenceAnnotations()) {
			if (!sequenceAnnotation.isSetDisplayId()) sequenceAnnotation.setDisplayId("sequenceAnnotation"+ ++count);
			sequenceAnnotation.updateCompliantURI(cloned.getPersistentIdentity().toString(),
					sequenceAnnotation.getDisplayId(),version);
			cloned.removeChildSafely(sequenceAnnotation, cloned.sequenceAnnotations);
			cloned.addSequenceAnnotation(sequenceAnnotation);
		}
		return cloned;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((roles == null) ? 0 : roles.hashCode());
		result = prime * result + ((sequences == null) ? 0 : sequences.hashCode());
		result = prime * result
				+ ((sequenceAnnotations == null) ? 0 : sequenceAnnotations.hashCode());
		result = prime * result
				+ ((sequenceConstraints == null) ? 0 : sequenceConstraints.hashCode());
		result = prime * result + ((components == null) ? 0 : components.hashCode());
		result = prime * result + ((types == null) ? 0 : types.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.abstract_classes.Documented#equals(java.lang.Instance)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ComponentDefinition other = (ComponentDefinition) obj;
		if (roles == null) {
			if (other.roles != null)
				return false;
		} else if (!roles.equals(other.roles))
			return false;
		if (sequences == null) {
			if (other.sequences != null)
				return false;
		} else if (!sequences.equals(other.sequences)) {
			if (getSequenceIdentities().size()!=getSequenceURIs().size() ||
					other.getSequenceIdentities().size()!=other.getSequenceURIs().size() ||
					!getSequenceIdentities().equals(other.getSequenceIdentities())) {
				return false;
			}
		}
		if (sequenceAnnotations == null) {
			if (other.sequenceAnnotations != null)
				return false;
		} else if (!sequenceAnnotations.equals(other.sequenceAnnotations))
			return false;
		if (sequenceConstraints == null) {
			if (other.sequenceConstraints != null)
				return false;
		} else if (!sequenceConstraints.equals(other.sequenceConstraints))
			return false;
		if (components == null) {
			if (other.components != null)
				return false;
		} else if (!components.equals(other.components))
			return false;
		if (types == null) {
			if (other.types != null)
				return false;
		} else if (!types.equals(other.types))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ComponentDefinition ["
				+ super.toString()
				+ ", types=" + types 
				+ (roles.size()>0?", roles=" + roles:"")  
				+ (this.getSequenceURIs().size()>0?", sequences=" + this.getSequenceURIs():"") 
				+ (this.getComponents().size()>0?", components=" + this.getComponents():"") 
				+ (this.getSequenceAnnotations().size()>0?", sequenceAnnotations=" + this.getSequenceAnnotations():"")
				+ (this.getSequenceConstraints().size()>0?", sequenceConstraints=" + this.getSequenceConstraints():"")
				+ "]";
	}

}
