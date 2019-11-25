package org.sbolstandard.core2;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
//import java.util.HashMap;
//import java.util.Map;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oboparser.obo.OBOOntology;
import org.oboparser.obo.OBOParser;
import org.oboparser.obo.OBOStanza;

/**
 * This class provides methods for accessing <a href="http://www.ebi.ac.uk/sbo/main/"><i>Systems Biology Ontology</i></a> (SBO) terms 
 * and querying about their relationships.
 * 
 * 
 * 
 * 
 * @author Zhen Zhang
 * @author Tramy Nguyen
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */

public class SystemsBiologyOntology {

	// the map that contains all SBO terms
	// will get initialized at the first access

	//private static Map<String, URI> sbo = null;

	private static final String URI_PREFIX = "http://identifiers.org/biomodels.sbo/";

	/**
	 * Namespace of the Systems Biology Ontology (<a href="http://identifiers.org/biomodels.sbo/">http://identifiers.org/biomodels.sbo/</a>).
	 */
	public static final URI NAMESPACE = URI.create(URI_PREFIX);

	/*
	public static String getTerm(URI uri) {

		// if the SO terms have not been loaded,
		// then load them now
		if(null == sbo) {
			loadSBO();
		}

		if(sbo.containsValue(uri) && null != uri) {
			// here, we need to iterate over the SBO terms
			for(String term : sbo.keySet()) {
				// if the URI of the current SBO term matches the provided URI,
				// then we return the term
				if(sbo.get(term).toString().equalsIgnoreCase(uri.toString())) {
					return term;
				}
			}
		}

		return null;
	}

	public static URI getURI(String term) {
		// if the SO terms have not been loaded,
		// then load them now
		if(null == sbo) {
			loadSBO();
		}

		if(null != term) {
			return sbo.get(term.toUpperCase());
		}

		return null;
	}

	private static void loadSBO() {

		// this needs to be enhanced, of course
		sbo = new HashMap<>();

		// interaction types
		sbo.put("PROMOTER", URI.create(URI_PREFIX + "SBO:0000167"));

		// participation roles

	}
	 */

	private static OBOOntology systemsBiologyOntology = null;
	
	/**
	 * Construct an SBO ontology object and read the OBO definition file, if it has not been constructed.
	 */
	public SystemsBiologyOntology() {
		if (systemsBiologyOntology == null) {
			OBOParser oboParser = new OBOParser();
			InputStreamReader f = new InputStreamReader(getClass().
					getResourceAsStream("/ontologies/SystemsBiologyOntology/sbo_full.obo"));
			try {
				oboParser.parse(f);
				systemsBiologyOntology = oboParser.getOntology();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Returns the extracted ID of the given term's URI. 
	 * 
	 * @param termURI the identity URI of a term 
	 * @return the extracted ID of the given term's URI
	 */
	public final String getId(URI termURI) {
		String termURIstr = termURI.toString().trim();
		if (!termURIstr.startsWith(URI_PREFIX)) {
			try {
				throw new IllegalArgumentException("Illegal " + termURI.toString() + ". It does not begin with the URI prefix " + URI_PREFIX);
			}
			catch (IllegalArgumentException e) {
				return null;
			}
		}
		int beginIndex = termURIstr.lastIndexOf("/") + 1;
		return termURIstr.substring(beginIndex, termURIstr.length());
	}
	
	/**
	 * Returns the ID of the stanza whose name matches the given stanza name. 
	 * If multiple matches are found, only the first matching one is returned.
	 *  
	 * @param stanzaName the name of a stanza
	 * @return the matching stanza ID, or {@code null} if no match is found.
	 */
	public final String getId(String stanzaName) {
		//return sequenceOntology.getStanza(stanzaName).getName();
		List<String> IdList = new ArrayList<String>();	
		for (OBOStanza stanza : systemsBiologyOntology.getStanzas()) {
			if (stanzaName.trim().equals(stanza.getName().trim())) {
				IdList.add(stanza.getId());
			}
		}
		if (IdList.isEmpty()) {
			try {
				throw new IllegalArgumentException("Illegal name " + stanzaName + ". It does not exit.");
			}
			catch (IllegalArgumentException e) {
				return null;
			}
		}
		return IdList.get(0);
	}
	
	/**
	 * Returns the name field of the stanza that matches the ID for the given term URI.
	 * 
	 * @param termURI the identity URI of a term
	 * @return the name field of the stanza whose ID is referred to by the given term URI.
	 */
	public final String getName(URI termURI) {
		String oboURIstr = termURI.toString().trim();
		if (!oboURIstr.startsWith(URI_PREFIX)) {
			try {
				throw new IllegalArgumentException("Illegal " + termURI.toString() + ". It does not contain URI prefix " + URI_PREFIX);
			}
			catch (IllegalArgumentException e) {
				return null;
			}
		}
		int beginIndex = oboURIstr.lastIndexOf("/") + 1;
		String id = oboURIstr.substring(beginIndex, oboURIstr.length());
		OBOStanza oboStanza = systemsBiologyOntology.getStanza(id);
		if (oboStanza == null) {
			try {
				throw new IllegalArgumentException("ID " + id + " does not exist.");
			}
			catch (IllegalArgumentException e) {
				return null;			}
		}
		return oboStanza.getName();
	}
	
	/**
	 * Returns the name field of the stanza that matches the ID referred by the given stanza URI.
	 * 
	 * @param stanzaId the ID of a stanza
	 * @return the name field of the stanza that matches the ID referred by the given stanza URI,
	 * or {@code null} if this no match was found
	 */
	public final String getName(String stanzaId) {
		OBOStanza oboStanza = systemsBiologyOntology.getStanza(stanzaId);
		if (oboStanza == null) {
			try {
				throw new IllegalArgumentException("Illegal ID " + stanzaId + " does not exist.");
			}
			catch (IllegalArgumentException e) {
				return null;
			}
		}
		return oboStanza.getName();
	}
	
	/**
	 * Returns the URI that is composed of the Systems Biology Ontology (SBO) namespace, "http://identifiers.org/biomodels.sbo/", 
	 * followed by the ID of an SBO term, of the stanza whose name matches the given name. If multiple matches are found, only the first matching
	 * one is returned. 
	 * 
	 * @param stanzaName the name of a term
	 * @return the URI of the given SBO name
	 */
	public final URI getURIbyName(String stanzaName) {
		return getURIbyId(getId(stanzaName));
	}
	
	/** 
	 * Creates a URI by appending the given stanza ID to the end of the
	 * Systems Biology Ontology (SBO) namespace，i.e. "http://identifiers.org/biomodels.sbo/".
	 * 
	 * @param stanzaId the ID of a stanza
	 * @return the created URI
	 */
	public final URI getURIbyId(String stanzaId) {
		if (stanzaId==null) return null;
		OBOStanza oboStanza = systemsBiologyOntology.getStanza(stanzaId.trim());
		if (oboStanza == null) {
			try {
				throw new IllegalArgumentException("ID " + stanzaId + " does not exist.");
			}
			catch (IllegalArgumentException e) {
				return null;
			}
		}
		return URI.create(URI_PREFIX+stanzaId);
	}
	
	
	/**
	 * Returns {@code true} if the stanza with childURI is a descendant of the stanza with parentURI. This method first
	 * extracts IDs for the child and parent, and then passes them to {@link #isDescendantOf(String, String)}.
	 *   
	 * @param childURI the URI of the child stanza
	 * @param parentURI the URI of the parent stanza
	 * @return {@code true} if the stanza with childURI is a descendant of the stanza with parentURI, or {@code false} otherwise
	 */
	
	public final boolean isDescendantOf(URI childURI, URI parentURI) {
		String childId = getId(childURI);
		String parentId = getId(parentURI);
		if (childId==null || parentId==null) return false;
		return isDescendantOf(childId,parentId);
	}
	
	/**
	 * Returns {@code true} if the stanza with Id1 is a descendant of the stanza with Id2.
	 *   
	 * @param Id1 ID of the first stanza
	 * @param Id2 ID of the second stanza
	 * @return {@code true} if the stanza with Id1 is a descendant of the stanza with Id2, or {@code false} otherwise
	 */
	public final boolean isDescendantOf(String Id1, String Id2) {
		OBOStanza stanza1 = systemsBiologyOntology.getStanza(Id1);
		OBOStanza stanza2 = systemsBiologyOntology.getStanza(Id2);
		if (stanza1 == null) {
			try {
				throw new IllegalArgumentException("Illegal ID: " + Id1 + ". No match was found.");
			}
			catch (IllegalArgumentException e) {
				return false;
			}
		}
		if (stanza2 == null) {
			try {
				throw new IllegalArgumentException("Illegal ID: " + Id2 + ". No match was found.");
			}
			catch (IllegalArgumentException e) {
				return false;
			}
		}
		return systemsBiologyOntology.isDescendantOf(stanza1, stanza2);
	}
	
	/**
 	 * Returns the set of child IDs that are descendants of the given parent ID.
 	 * This set excludes the given parent ID.
	 * 
	 * @param parentId the ID of the parent stanza
	 * @return the set of child IDs that are descendants of the given parent ID
	 */
	public Set<String> getDescendantsOf(String parentId) {
		OBOStanza stanza1 = systemsBiologyOntology.getStanza(parentId);
		if (stanza1 == null) {
			try {
				throw new IllegalArgumentException("Illegal ID: " + parentId + ". No match was found.");
			}
			catch (IllegalArgumentException e) {
				return null;
			}
		}
		return systemsBiologyOntology.getDescendantsOf(stanza1);
	}
	
	/**
 	 * Returns the set of child IDs that are descendants of the given parent URI. 
 	 * This set excludes the given parent ID.
	 * 
	 * @param parentURI the URI of the parent stanza
	 * @return the set of child IDs that are descendants of the given parent URI 
	 */
	public final Set<String> getDescendantsOf(URI parentURI) {
		String parentId = getId(parentURI);
		if (parentId==null) return new HashSet<String>();
		return getDescendantsOf(parentId);
	}
	
	/**
	 * Returns the set of child URIs that are descendants of the given parent ID.
	 * This set excludes the given parent URI.
	 *  
	 * @param parentId the ID of the parent stanza
	 * @return the set of child URIs that are descendants of the given parent ID. 
	 */

	public final Set<URI> getDescendantURIsOf(String parentId) {
		Set<String> descendents = getDescendantsOf(parentId);
		if (descendents==null) return null;
		Set<URI> descendentURIs = new HashSet<URI>();
		for (String child : descendents) {
			descendentURIs.add(getURIbyId(child));
		}
		return descendentURIs;
	}
	
	/**
	 * Returns the set of child URIs that are descendants of the given parent URI.
	 * This set excludes the given parent URI. 
	 * 
	 * @param parentURI the URI of the parent stanza
	 * @return the set of child URIs that are descendants of the given parent URI
	 */

	public final Set<URI> getDescendantURIsOf(URI parentURI) {
		Set<String> descendents = getDescendantsOf(parentURI);
		if (descendents==null) return null;
		Set<URI> descendentURIs = new HashSet<URI>();
		for (String child : descendents) {
			descendentURIs.add(getURIbyId(child));
		}
		return descendentURIs;
	}

	/**
	 * Returns the set of child names that are descendants of a given parent ID.
	 * This set excludes the given parent name.
	 *  
	 * @param parentId the ID of the parent stanza
	 * @return the set of child names that are descendants of a given parent ID. 
	 */

	public final Set<String> getDescendantNamesOf(String parentId) {
		Set<String> descendents = getDescendantsOf(parentId);
		if (descendents==null) return null;
		Set<String> descendentNames = new HashSet<String>();
		for (String child : descendents) {
			descendentNames.add(getName(child));
		}
		return descendentNames;
	}
	
	/**
	 * Returns the set of child names that are descendants of a given parent URI.
	 * This set excludes the given parent name. 
	 * 
	 * @param parentURI the URI of the parent stanza
	 * @return the set of child names that are descendants of a given parent URI
	 */

	public final Set<String> getDescendantNamesOf(URI parentURI) {
		Set<String> descendents = getDescendantsOf(parentURI);
		if (descendents==null) return null;
		Set<String> descendentNames = new HashSet<String>();
		for (String child : descendents) {
			descendentNames.add(getName(child));
		}
		return descendentNames;
	}

	/**
	 * Creates a new URI from the Systems Biology Ontology (SBO) namespace with the given local name. For example, the method call
	 * <code>term("SBO_0000001")</code> will return the URI <a>http://purl.obolibrary.org/obo/SBO_0000001</a>
	 * @param id the ID of a SBO term
	 * @return the created URI
	 */
	private static final URI type(String id) {
		return URI.create(URI_PREFIX+id);
	}

	// Modeling frameworks
	/**
	 * Set of assumptions that underlay a mathematical description
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000004">SBO:0000004</a>).
	 */
	public static final URI MODELING_FRAMEWORK 		 	 = type("SBO:0000004");
	
	/**
	 * Modelling approach where the quantities of participants are considered continuous,
	 * and represented by real values (<a href="http://identifiers.org/biomodels.sbo/SBO:0000062">SBO:0000062</a>).
	 * The associated simulation methods make use of differential equations.
	 */
	public static final URI CONTINUOUS_FRAMEWORK 		 	 = type("SBO:0000062");

	/**
	 * Modelling approach where the quantities of participants are considered continuous, and represented by
	 * real values (<a href="http://identifiers.org/biomodels.sbo/SBO:0000293">SBO:0000293</a>).
	 * The associated simulation methods make use of differential equations. The models do not
	 * take into account the distribution of the entities and describe only the temporal fluxes.
	 */
	public static final URI NON_SPATIAL_CONTINUOUS_FRAMEWORK = type("SBO:0000293");

	/**
	 * Modelling approach where the quantities of participants are considered continuous, and represented
	 * by real values (<a href="http://identifiers.org/biomodels.sbo/SBO:0000292">SBO:0000292</a>).  The associated simulation methods make use of differential equations. The models
	 * take into account the distribution of the entities and describe the spatial fluxes.
	 */
	public static final URI SPATIAL_CONTINUOUS_FRAMEWORK 	 = type("SBO:0000292");

	/**
	 * Modelling approach where the quantities of participants are considered discrete, and represented by
	 * integer values (<a href="http://identifiers.org/biomodels.sbo/SBO:0000063">SBO:0000063</a>). The associated simulation methods can be deterministic or stochastic.
	 */
	public static final URI DISCRETE_FRAMEWORK 				 = type("SBO:0000063");

	/**
	 * Modelling approach where the quantities of participants are considered discrete, and represented by
	 * integer values (<a href="http://identifiers.org/biomodels.sbo/SBO:0000295">SBO:0000295</a>).
	 * The associated simulation methods can be deterministic or stochastic.The models do
	 * not take into account the distribution of the entities and describe only the temporal fluxes.
	 */
	public static final URI NON_SPATIAL_DISCRETE_FRAMEWORK 	 = type("SBO:0000295");

	/**
	 * Modelling approach where the quantities of participants are considered discrete, and represented
	 * by integer values (<a href="http://identifiers.org/biomodels.sbo/SBO:0000294">SBO:0000294</a>).
	 * The associated simulation methods can be deterministic or stochastic. The models
	 * take into account the distribution of the entities and describe the spatial fluxes.
	 */
	public static final URI SPATIAL_DISCRETE_FRAMEWORK 		 = type("SBO:0000294");

	/**
	 * Modelling approach, typically used for metabolic models, where the flow of metabolites (flux)
	 * through a network can be calculated (<a href="http://identifiers.org/biomodels.sbo/SBO:0000624">SBO:0000624</a>). This approach will generally produce a set of solutions
	 * (solution space), which may be reduced using objective functions and constraints on individual fluxes.
	 */
	public static final URI FLUX_BALANCE_FRAMEWORK 			 = type("SBO:0000624");

	/**
	 * Modelling approach, pioneered by Rene Thomas and Stuart Kaufman, where the evolution of a system
	 * is described by the transitions between discrete activity states of "genes" that control each other (<a href="http://identifiers.org/biomodels.sbo/SBO:0000234">SBO:0000234</a>).
	 */
	public static final URI LOGICAL_FRAMEWORK 				 = type("SBO:0000234");

	/**
	 * Equationally defined algebraic framework usually interpreted as a two-valued logic using the basic
	 * Boolean operations (conjunction, disjunction and negation), together with the constants '0' and '1'
	 * denoting false and true values, respectively (<a href="http://identifiers.org/biomodels.sbo/SBO:0000547">SBO:0000547</a>).
	 */
	public static final URI BOOLEAN_LOGICAL_FRAMEWORK 		 = type("SBO:0000547");

	// Interaction types
	/**
	 * Representation of an entity that manifests, unfolds or develops through time, such as a discrete event, 
	 * or a mutual or reciprocal action or influence that happens between participating physical entities, 
	 * and/or other occurring entities (<a href="http://identifiers.org/biomodels.sbo/SBO:0000231">SBO:0000231</a>). 
	 */
	public static final URI OCCURRING_ENTITY_REPRESENTATION		   = type("SBO:0000231");
	
	/**
	 * The potential action that a biological entity has on other entities (<a href="http://identifiers.org/biomodels.sbo/SBO:0000412">SBO:0000412</a>). Example are
	 * enzymatic activity, binding activity etc.
	 */
	public static final URI BIOLOGICAL_ACTIVITY 				   = type("SBO:0000412");

	/**
	 * A sequential series of actions, motions, or occurrences, such as chemical reactions,
	 * that affect one or more entities in a phenomenologically characteristic manner (<a href="http://identifiers.org/biomodels.sbo/SBO:0000375">SBO:0000375</a>).
	 */
	public static final URI PROCESS 							   = type("SBO:0000375");

	/**
	 * An event involving one or more physical entities that modifies the structure, location
	 * or free energy of at least one of the participants (<a href="http://identifiers.org/biomodels.sbo/SBO:0000167">SBO:0000167</a>).
	 */
	public static final URI BIOCHEMICAL_OR_TRANSPORT_REACTION 	   = type("SBO:0000167");

	/**
	 * An event involving one or more chemical entities that modifies the electrochemical
	 * structure of at least one of the participants.
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000176">SBO:0000176</a>)
	 */
	public static final URI BIOCHEMICAL_REACTION 				   = type("SBO:0000176");

	/**
	 * Chemical reaction where a proton is given by a compound, the acid, to another one, the
	 * base (Brønsted-Lowry definition) (<a href="http://identifiers.org/biomodels.sbo/SBO:0000208">SBO:0000208</a>). An alternative, more general, definition is a reaction
	 * where a compound, the base, gives a pair of electrons to another, the acid
	 * (Lewis definition).
	 */
	public static final URI ACID_BASE_REACTION 					   = type("SBO:0000208");

	/**
	 * Removal of a proton (hydrogen ion H+) from a chemical entity (<a href="http://identifiers.org/biomodels.sbo/SBO:0000213">SBO:0000213</a>).
	 */
	public static final URI DEPROTONATION 						   = type("SBO:0000213");

	/**
	 * Addition of a proton (H+) to a chemical entity (<a href="http://identifiers.org/biomodels.sbo/SBO:0000212">SBO:0000212</a>).
	 */
	public static final URI PROTONATION 						   = type("SBO:0000212");

	/**
	 * Biochemical reaction that does not result in the modification of covalent bonds of
	 * reactants, but rather modifies the conformation of some reactants, that is the relative
	 * position of their atoms in space (<a href="http://identifiers.org/biomodels.sbo/SBO:0000181">SBO:0000181</a>).
	 */
	public static final URI CONFORMATIONAL_TRANSITION 			   = type("SBO:0000181");

	/**
	 * Biochemical reaction that results in the modification of some covalent bonds (<a href="http://identifiers.org/biomodels.sbo/SBO:0000182">SBO:0000182</a>).
	 */
	public static final URI CONVERSION 							   = type("SBO:0000182");

	/**
	 * Covalent reaction that results in the addition of a chemical group on a molecule
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000210">SBO:0000210</a>).
	 */
	public static final URI ADDITION_OF_A_CHEMICAL_GROUP 		   = type("SBO:0000210");

	/**
	 * Addition of an acetyl group (-COCH3) to a chemical entity
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000215">SBO:0000215</a>).
	 */
	public static final URI ACETYLATION 						   = type("SBO:0000215");

	/**
	 * Addition of a saccharide group to a chemical entity
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000217">SBO:0000217</a>).
	 */
	public static final URI GLYCOSYLATION 						   = type("SBO:0000217");

	/**
	 * Addition of an hydroxyl group (-OH) to a chemical entity
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000233">SBO:0000233</a>).
	 */
	public static final URI HYDROXYLATION 						   = type("SBO:0000233");

	/**
	 * Addition of a methyl group (-CH3) to a chemical entity
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000214">SBO:0000214</a>).
	 */
	public static final URI METHYLATION 						   = type("SBO:0000214");

	/**
	 * Addition of a myristoyl (CH3-[CH2]12-CO-) to a chemical entity
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000219">SBO:0000219</a>).
	 */
	public static final URI MYRISTOYLATION 						   = type("SBO:0000219");

	/**
	 * Addition of a palmitoyl group (CH3-[CH2]14-CO-) to a chemical entity
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000218">SBO:0000218</a>).
	 */
	public static final URI PALMITOYLATION 						   = type("SBO:0000218");

	/**
	 * Addition of a phosphate group (-H2PO4) to a chemical entity
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000216">SBO:0000216</a>).
	 */
	public static final URI PHOSPHORYLATION 					   = type("SBO:0000216");

	/**
	 * Addition of a prenyl group (generic sense) to a chemical entity
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000221">SBO:0000221</a>).
	 */
	public static final URI PRENYLATION 						   = type("SBO:0000221");

	/**
	 * Addition of a farnesyl group (CH2-CH=C(CH3)-CH2-CH2-CH=C(CH3)-CH2-CH2-CH=C(CH3)2)
	 * to a chemical entity
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000222">SBO:0000222</a>).
	 */
	public static final URI FARNESYLATION 						   = type("SBO:0000222");

	/**
	 * Addition of a geranylgeranyl group
	 * (CH2-CH=C(CH3)-CH2-CH2-CH=C(CH3)-CH2-CH2-CH=C(CH3)-CH2-CH2-CH=C(CH3)2)
	 * to a chemical entity
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000223">SBO:0000223</a>).
	 */
	public static final URI GERANYLGERANYLATION 				   = type("SBO:0000223");

	/**
	 * Addition of a sulfate group (SO4--) to a chemical entity
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000220">SBO:0000220</a>).
	 */
	public static final URI SULFATION 							   = type("SBO:0000220");

	/**
	 * Covalent linkage to the protein ubiquitin
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000224">SBO:0000224</a>).
	 */
	public static final URI UBIQUITINATION 						   = type("SBO:0000224");

	/**
	 * Rupture of a covalent bond resulting in the conversion of one physical entity into
	 * several physical entities
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000178">SBO:0000178</a>).
	 */
	public static final URI CLEAVAGE 							   = type("SBO:0000178");

	/**
	 * Covalent reaction that results in the removal of a chemical group from a molecule
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000211">SBO:0000211</a>).
	 */
	public static final URI REMOVAL_OF_A_CHEMICAL_GROUP 		   = type("SBO:0000211");

	/**
	 * Removal of an amine group from a molecule, often under the addition of water
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000401">SBO:0000401</a>).
	 */
	public static final URI DEAMINATION 						   = type("SBO:0000401");

	/**
	 * Removal of a carbonyl group (-C-O-) from a molecule, usually as carbon monoxide
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000400">SBO:0000400</a>).
	 */
	public static final URI DECARBONYLATION 				  	   = type("SBO:0000400");

	/**
	 * A process in which a carboxyl group (COOH) is removed from a molecule as carbon dioxide
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000399">SBO:0000399</a>).
	 */
	public static final URI DECARBOXYLATION 					   = type("SBO:0000399");

	/**
	 * Removal of a phosphate group (-H2PO4) from a chemical entity
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000330">SBO:0000330</a>).
	 */
	public static final URI DEPHOSPHORYLATION 					   = type("SBO:0000330");

	/**
	 * Covalent reaction that results in the transfer of a chemical group from one molecule
	 * to another
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000402">SBO:0000402</a>).
	 */
	public static final URI TRANSFER_OF_A_CHEMICAL_GROUP 		   = type("SBO:0000402");

	/**
	 * The transfer of an amino group between two molecules (<a href="http://identifiers.org/biomodels.sbo/SBO:0000403">SBO:0000403</a>). Commonly in biology this is
	 * restricted to reactions between an amino acid and an alpha-keto carbonic acid, whereby
	 * the reacting amino acid is converted into an alpha-keto acid, and the alpha-keto acid
	 * reactant into an amino acid.
	 */
	public static final URI TRANSAMINATION 						   = type("SBO:0000403");

	/**
	 * Complete disappearance of a physical entity
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000179">SBO:0000179</a>).
	 */
	public static final URI DEGRADATION 						   = type("SBO:0000179");

	/**
	 * Transformation of a non-covalent complex that results in the formation of several
	 * independent biochemical entities
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000180">SBO:0000180</a>).
	 */
	public static final URI DISSOCIATION 						   = type("SBO:0000180");

	/**
	 * Decomposition of a compound by reaction with water, where the hydroxyl and H groups are
	 * incorporated into different products
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000376">SBO:0000376</a>).
	 */
	public static final URI HYDROLYSIS 							   = type("SBO:0000376");

	/**
	 * Ionization is the physical process of converting an atom or molecule into an ion by
	 * changing the difference between the number of protons and electrons
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000209">SBO:0000209</a>).
	 */
	public static final URI IONISATION 							   = type("SBO:0000209");

	/**
	 * A reaction in which the principal reactant and principal product are isomers of each other
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000377">SBO:0000377</a>).
	 */
	public static final URI ISOMERISATION 						   = type("SBO:0000377");

	/**
	 * Interaction between several biochemical entities that results in the formation of a
	 * non-covalent complex
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000177">SBO:0000177</a>).
	 */
	public static final URI NON_COVALENT_BINDING 				   = type("SBO:0000177");

	/**
	 * Chemical process in which atoms have their oxidation number (oxidation state) changed
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000200">SBO:0000200</a>).
	 */
	public static final URI REDOX_REACTION 						   = type("SBO:0000200");

	/**
	 * Chemical process during which a molecular entity loses electrons
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000201">SBO:0000201</a>).
	 */
	public static final URI OXIDATION 							   = type("SBO:0000201");

	/**
	 * Chemical process in which a molecular entity gain electrons
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000202">SBO:0000202</a>).
	 */
	public static final URI REDUCTION 							   = type("SBO:0000202");

	/**
	 * Movement of a physical entity without modification of the structure of the entity
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000185">SBO:0000185</a>).
	 */
	public static final URI TRANSPORT_REACTION 					   = type("SBO:0000185");

	/**
	 * A transport reaction which results in the removal of the transported entity from the cell
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000588">SBO:0000588</a>).
	 */
	public static final URI TRANSCELLULAR_MEMBRANE_EFFLUX_REACTION = type("SBO:0000588");

	/**
	 * A transport reaction which results in the entry of the transported entity, into the cell
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000587">SBO:0000587</a>).
	 */
	public static final URI TRANSCELLULAR_MEMBRANE_INFLUX_REACTION = type("SBO:0000587");

	/**
	 * Biochemical networks can be affected by external influences (<a href="http://identifiers.org/biomodels.sbo/SBO:0000357">SBO:0000357</a>). Those influences can be
	 * well-defined physical perturbations, such as a light pulse, or a change in temperature
	 * but also more complex of not well defined phenomena, for instance a biological process,
	 * an experimental setup, or a mutation.
	 */
	public static final URI BIOLOGICAL_EFFECT_OF_A_PERTURBATION    = type("SBO:0000357");

	/**
	 * Process that involves the participation of chemical or biological entities and is
	 * composed of several elementary steps or reactions
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000205">SBO:0000205</a>).
	 */
	public static final URI COMPOSITE_BIOCHEMICAL_PROCESS 		   = type("SBO:0000205");

	/**
	 * Process in which a DNA duplex is transformed into two identical DNA duplexes
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000204">SBO:0000204</a>).
	 */
	public static final URI DNA_REPLICATION 					   = type("SBO:0000204");

	/**
	 * A composite biochemical process through which a gene sequence is fully converted into
	 * mature gene products (<a href="http://identifiers.org/biomodels.sbo/SBO:0000589">SBO:0000589</a>). These gene products may include RNA species as well as proteins,
	 * and the process encompasses all intermediate steps required to generate the active form
	 * of the gene product.
	 */
	public static final URI GENETIC_PRODUCTION 					   = type("SBO:0000589");

	/**
	 * Process through which a DNA sequence is copied to produce a complementary RNA
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000183">SBO:0000183</a>).
	 */
	public static final URI TRANSCRIPTION 						   = type("SBO:0000183");

	/**
	 * Process in which a polypeptide chain is produced from a messenger RNA
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000184">SBO:0000184</a>).
	 */
	public static final URI TRANSLATION 						   = type("SBO:0000184");

	/**
	 * An aggregation of interactions and entities into a single process
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000395">SBO:0000395</a>).
	 */
	public static final URI ENCAPSULATING_PROCESS 				   = type("SBO:0000395");

	/**
	 * Mutual or reciprocal action or influence between molecular entities
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000342">SBO:0000342</a>).
	 */
	public static final URI MOLECULAR_OR_GENETIC_INTERACTION 	   = type("SBO:0000342");

	/**
	 * A phenomenon whereby an observed phenotype, qualitative or quantative, is not explainable
	 * by the simple additive effects of the individual gene pertubations alone (<a href="http://identifiers.org/biomodels.sbo/SBO:0000343">SBO:0000343</a>). Genetic
	 * interaction between perturbed genes is usually expected to generate a 'defective'
	 * phenotype. The level of defectiveness is often used to sub-classify this phenomenon.
	 */
	public static final URI GENETIC_INTERACTION 				   = type("SBO:0000343");

	/**
	 * Genetic enhancement is said to have occurred when the phenotypic effect of an initial
	 * mutation in a gene is made increasingly severe by a subsequent mutation
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000501">SBO:0000501</a>).
	 */
	public static final URI GENETIC_ENHANCEMENT 				   = type("SBO:0000501");

	/**
	 * Genetic suppression is said to have occurred when the phenotypic effect of an initial
	 * mutation in a gene is less severe, or entirely negated, by a subsequent mutation
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000500">SBO:0000500</a>).
	 */
	public static final URI GENETIC_SUPPRESSION 				   = type("SBO:0000500");

	/**
	 * Synthetic lethality is said to have occurred where gene mutations, each of which map to
	 * a separate locus, fail to complement in an offspring to correct a phenotype, as would be
	 * expected
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000502">SBO:0000502</a>).
	 */
	public static final URI SYNTHETIC_LETHALITY 				   = type("SBO:0000502");

	/**
	 * Relationship between molecular entities, based on contacts, direct or indirect
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000344">SBO:0000344</a>).
	 */
	public static final URI MOLECULAR_INTERACTION 				   = type("SBO:0000344");

	/**
	 * The process by which two or more proteins interact non-covalently to form a protein
	 * complex
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000526">SBO:0000526</a>).
	 */
	public static final URI PROTEIN_COMPLEX_FORMATION 			   = type("SBO:0000526");

	/**
	 * One or more processes that are not represented in certain representations or
	 * interpretations of a model
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000397">SBO:0000397</a>).
	 */
	public static final URI OMITTED_PROCESS 					   = type("SBO:0000397");

	/**
	 * A biochemical network can generate phenotypes or affects biological processes (<a href="http://identifiers.org/biomodels.sbo/SBO:0000358">SBO:0000358</a>). Such
	 * processes can take place at different levels and are independent of the biochemical
	 * network itself.
	 */
	public static final URI PHENOTYPE 							   = type("SBO:0000358");

	/**
	 * Assignment of a state or a value to a state variable, characteristic or property, of a
	 * biological entity
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000464">SBO:0000464</a>).
	 */
	public static final URI STATE_VARIABLE_ASSIGNMENT 			   = type("SBO:0000464");

	/**
	 * A process that can modify the state of petri net 'places'
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000591">SBO:0000591</a>).
	 */
	public static final URI PETRI_NET_TRANSITION 				   = type("SBO:0000591");

	/**
	 * An equivocal or conjectural process, whose existence is assumed but not proven
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000396">SBO:0000396</a>).
	 */
	public static final URI UNCERTAIN_PROCESS 					   = type("SBO:0000396");

	/**
	 * Connectedness between entities and/or interactions representing their relatedness or
	 * influence
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000374">SBO:0000374</a>).
	 */
	public static final URI RELATIONSHIP 						   = type("SBO:0000374");

	/**
	 * Modification of the execution of an event or a process
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000168">SBO:0000168</a>).
	 */
	public static final URI CONTROL 							   = type("SBO:0000168");

	/**
	 * Regulation of the influence of a reaction participant by binding an effector to a
	 * binding site of the participant different of the site of the participant conveying the
	 * influence
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000239">SBO:0000239</a>).
	 */
	public static final URI ALLOSTERIC_CONTROL 				       = type("SBO:0000239");

	/**
	 * Decrease in amount of a material or conceptual entity
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000394">SBO:0000394</a>).
	 */
	public static final URI CONSUMPTION 						   = type("SBO:0000394");

	/**
	 * Negative modulation of the execution of a process
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000169">SBO:0000169</a>).
	 */
	public static final URI INHIBITION 							   = type("SBO:0000169");

	/**
	 * Control that precludes the execution of a process
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000407">SBO:0000407</a>).
	 */
	public static final URI ABSOLUTE_INHIBITION 				   = type("SBO:0000407");

	/**
	 * Generation of a material or conceptual entity
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000393">SBO:0000393</a>).
	 */
	public static final URI PRODUCTION 							   = type("SBO:0000393");

	/**
	 * Positive modulation of the execution of a process
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000170">SBO:0000170</a>).
	 */
	public static final URI STIMULATION 						   = type("SBO:0000170");

	/**
	 * Control that always triggers the controlled process
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000411">SBO:0000411</a>).
	 */
	public static final URI ABSOLUTE_STIMULATION 				   = type("SBO:0000411");

	/**
	 * Modification of the velocity of a reaction by lowering the energy of the transition state
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000172">SBO:0000172</a>).
	 */
	public static final URI CATALYSIS 							   = type("SBO:0000172");

	/**
	 * Control that is necessary to the execution of a process
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000171">SBO:0000171</a>).
	 */
	public static final URI NECESSARY_STIMULATION 				   = type("SBO:0000171");

	/**
	 * Term to signify those material or conceptual entities that are identical in some respect
	 * within a frame of reference
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000392">SBO:0000392</a>).
	 */
	public static final URI EQUIVALENCE 					       = type("SBO:0000392");

	/**
	 * Combining the influence of several entities or events in a unique influence
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000237">SBO:0000237</a>).
	 */
	public static final URI LOGICAL_COMBINATION 				   = type("SBO:0000237");

	/**
	 * All the preceding events or participating entities are necessary to perform the control
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000173">SBO:0000173</a>).
	 */
	public static final URI AND 								   = type("SBO:0000173");

	/**
	 * The preceding event or participating entity cannot participate to the control
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000238">SBO:0000238</a>).
	 */
	public static final URI NOT 								   = type("SBO:0000238");

	/**
	 * Any of the preceding events or participating entities are necessary to perform the control
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000174">SBO:0000174</a>).
	 */
	public static final URI OR 									   = type("SBO:0000174");

	/**
	 * Only one of the preceding events or participating entities can perform the control at one time
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000175">SBO:0000175</a>).
	 */
	public static final URI XOR 								   = type("SBO:0000175");

	/**
	 * Relationship between entities (material or conceptual) and logical operators, or between
	 * logical operators themselves
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000398">SBO:0000398</a>).
	 */
	public static final URI LOGICAL_RELATIONSHIP 				   = type("SBO:0000398");

	/**
	 * The connectedness between entities as related by their position
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000413">SBO:0000413</a>).
	 */
	public static final URI POSITIONAL_RELATIONSHIP 			   = type("SBO:0000413");

	/**
	 * Positional relationship between entities on the same strand (e.g. in DNA), or on the same side
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000414">SBO:0000414</a>).
	 */
	public static final URI CIS 								   = type("SBO:0000414");

	/**
	 * An entity that is a subset of another entity or object
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000469">SBO:0000469</a>).
	 */
	public static final URI CONTAINMENT 						   = type("SBO:0000469");

	/**
	 * Positional relationship between entities on different sides, or strands
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000415">SBO:0000415</a>).
	 */
	public static final URI TRANS 								   = type("SBO:0000415");

	// Participant roles
	/**
	 * The function of a physical or conceptual entity, that is its role, in the execution of an event or process
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000003">SBO:0000003</a>). 
	 */
	public static final URI PARTICIPANT_ROLE 		 = type("SBO:0000003");

	/**
	 * Logical or physical subset of the event space that contains pools, that is sets of participants
	 * considered identical when it comes to the event they are involved into (<a href="http://identifiers.org/biomodels.sbo/SBO:0000289">SBO:0000289</a>). A compartment can have any
	 * number of dimensions, including 0, and be of any size including null.
	 */
	public static final URI FUNCTIONAL_COMPARTMENT 		 = type("SBO:0000289");

	/**
	 * Substance that changes the velocity of a process without itself being consumed or
	 * transformed by the reaction
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000019">SBO:0000019</a>).
	 */
	public static final URI MODIFIER 					 = type("SBO:0000019");

	/**
	 * A modifier that can exhibit either inhibitory or stimulatory effects on a process
	 * depending on the context in which it occurs (<a href="http://identifiers.org/biomodels.sbo/SBO:0000595">SBO:0000595</a>). For example, the observed effect may be
	 * dependent upon the concentration of the modifier.
	 */
	public static final URI DUAL_ACTIVITY_MODIFIER 		 = type("SBO:0000595");

	/**
	 * Substance that decreases the probability of a chemical reaction without itself being
	 * consumed or transformed by the reaction
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000020">SBO:0000020</a>).
	 */
	public static final URI INHIBITOR 					 = type("SBO:0000020");

	/**
	 * Substance that decreases the probability of a chemical reaction, without itself being
	 * consumed or transformed by the reaction, by stericaly hindering the interaction between
	 * reactants
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000206">SBO:0000206</a>).
	 */
	public static final URI COMPETITIVE_INHIBITOR 	 	 = type("SBO:0000206");

	/**
	 * Substance that decreases the probability of a chemical reaction, without itself being
	 * consumed or transformed by the reaction, and without sterically hindering the
	 * interaction between reactants
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000207">SBO:0000207</a>).
	 */
	public static final URI NON_COMPETITIVE_INHIBITOR 	 = type("SBO:0000207");

	/**
	 * A silencer is a modifier which acts in a manner that completely prevents an event or
	 * process from occurring (<a href="http://identifiers.org/biomodels.sbo/SBO:0000597">SBO:0000597</a>). For example, a silencer in gene expression is usually a
	 * transcription factor that binds a DNA sequence in such a way as to completely prevent
	 * the binding of RNA polymerase, and thus fully suppresses transcription.
	 */
	public static final URI SILENCER 					 = type("SBO:0000597");

	/**
	 * A modifier whose activity is not known or has not been specified
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000596">SBO:0000596</a>).
	 */
	public static final URI MODIFIER_OF_UNKNOWN_ACTIVITY = type("SBO:0000596");

	/**
	 * Substance that accelerates the velocity of a chemical reaction without itself being
	 * consumed or transformed
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000459">SBO:0000459</a>).
	 */
	public static final URI STIMULATOR 				     = type("SBO:0000459");

	/**
	 * Substance that accelerates the velocity of a chemical reaction without itself being
	 * consumed or transformed (<a href="http://identifiers.org/biomodels.sbo/SBO:0000013">SBO:0000013</a>). This effect is achieved by lowering the free energy of the
	 * transition state.
	 */
	public static final URI CATALYST 					 = type("SBO:0000013");

	/**
	 * A substance that accelerates the velocity of a chemical reaction without itself being
	 * consumed or transformed, by lowering the free energy of the transition state (<a href="http://identifiers.org/biomodels.sbo/SBO:0000460">SBO:0000460</a>). The
	 * substance acting as a catalyst is an enzyme.
	 */
	public static final URI ENZYMATIC_CATALYST 			 = type("SBO:0000460");

	/**
	 * A substance that is absolutely required for occurrence and stimulation of a reaction
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000461">SBO:0000461</a>).
	 */
	public static final URI ESSENTIAL_ACTIVATOR 		 = type("SBO:0000461");

	/**
	 * An essential activator that affects the apparent value of the Michaelis constant(s)
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000535">SBO:0000535</a>).
	 */
	public static final URI BINDING_ACTIVATOR 			 = type("SBO:0000535");

	/**
	 * An essential activator that affects the apparent value of the catalytic constant
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000534">SBO:0000534</a>).
	 */
	public static final URI CATALYTIC_ACTIVATOR 		 = type("SBO:0000534");

	/**
	 * An essential activator that affects the apparent value of the specificity constant (<a href="http://identifiers.org/biomodels.sbo/SBO:0000533">SBO:0000533</a>).
	 * Mechanistically, the activator would need to be bound before reactant and product
	 * binding can take place.
	 */
	public static final URI SPECIFIC_ACTIVATOR 			 = type("SBO:0000533");

	/**
	 * An activator which is not necessary for an enzymatic reaction, but whose presence will
	 * further increase enzymatic activity
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000462">SBO:0000462</a>).
	 */
	public static final URI NON_ESSENTIAL_ACTIVATOR 	 = type("SBO:0000462");

	/**
	 * Substance that increases the probability of a chemical reaction without itself being
	 * consumed or transformed by the reaction (<a href="http://identifiers.org/biomodels.sbo/SBO:0000021">SBO:0000021</a>). This effect is achieved by increasing the
	 * difference of free energy between the reactant(s) and the product(s).
	 */
	public static final URI POTENTIATOR 				 = type("SBO:0000021");

	/**
	 * A participant whose presence does not alter the velocity of a process or event
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000594">SBO:0000594</a>).
	 */
	public static final URI NEUTRAL_PARTICIPANT 		 = type("SBO:0000594");

	/**
	 * Substance that is produced in a reaction (<a href="http://identifiers.org/biomodels.sbo/SBO:0000011">SBO:0000011</a>). In a chemical equation the Products are the
	 * elements or compounds on the right hand side of the reaction equation. A product can be
	 * produced and consumed by the same reaction, its global quantity remaining unchanged.
	 */
	public static final URI PRODUCT 					 = type("SBO:0000011");

	/**
	 * A substance that is produced in a chemical reaction but is not itself the primary
	 * product or focus of that reaction (<a href="http://identifiers.org/biomodels.sbo/SBO:0000603">SBO:0000603</a>). Examples include, but are not limited to, currency
	 * compounds such as ATP, NADPH and protons.
	 */
	public static final URI SIDE_PRODUCT 				 = type("SBO:0000603");

	/**
	 * A region of DNA to which various transcription factors and RNA polymerase must bind
	 * in order to initiate transcription for a gene
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000598">SBO:0000598</a>).
	 */
	public static final URI PROMOTER 				 	 = type("SBO:0000598");

	/**
	 * Substance consumed by a chemical reaction (<a href="http://identifiers.org/biomodels.sbo/SBO:0000010">SBO:0000010</a>). Reactants react with each other to form the
	 * products of a chemical reaction. In a chemical equation the Reactants are the elements
	 * or compounds on the left hand side of the reaction equation. A reactant can be consumed
	 * and produced by the same reaction, its global quantity remaining unchanged.
	 */
	public static final URI REACTANT 					 = type("SBO:0000010");

	/**
	 * Entity participating in a physical or functional interaction
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000336">SBO:0000336</a>).
	 */
	public static final URI INTERACTOR 					 = type("SBO:0000336");

	/**
	 * Molecule which is acted upon by an enzyme (<a href="http://identifiers.org/biomodels.sbo/SBO:0000015">SBO:0000015</a>). 
	 * The substrate binds with the enzyme's active site, and the enzyme catalyzes a chemical reaction involving the substrate.
	 */
	public static final URI SUBSTRATE 					 = type("SBO:0000015");

	/**
	 * A substance that is consumed in a chemical reaction but is not itself the primary
	 * substrate or focus of that reaction (<a href="http://identifiers.org/biomodels.sbo/SBO:0000604">SBO:0000604</a>). 
	 * Examples include, but are not limited to, currency compounds such as ATP, NADPH and protons.
	 */
	public static final URI SIDE_SUBSTRATE 				 = type("SBO:0000604");
	
	/**
	 * Conceptual or material entity that is the object of an inhibition process, and is 
	 * acted upon by an inhibitor (<a href="http://identifiers.org/biomodels.sbo/SBO:0000642">SBO:0000642</a>). 
	 */
	public static final URI INHIBITED 				 = type("SBO:0000642");
	
	/**
	 * Conceptual or material entity that is the object of a stimulation process, and is 
	 * acted upon by a stimulator (<a href="http://identifiers.org/biomodels.sbo/SBO:0000643">SBO:0000643</a>). 
	 */
	public static final URI STIMULATED 				 = type("SBO:0000643");
	
	/**
	 * Conceptual or material entity that is the object of a modification process, and is 
	 * acted upon by a modifier (<a href="http://identifiers.org/biomodels.sbo/SBO:0000644">SBO:0000644</a>). 
	 */
	public static final URI MODIFIED 				 = type("SBO:0000644");
	
	/**
	 * An entity that acts as the starting material for genetic production 
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000645">SBO:0000645</a>). 
	 */
	public static final URI TEMPLATE 				 = type("SBO:0000645");
	/**
	 * A value, numerical or symbolic, that defines certain characteristics of systems 
	 * or system functions, or is necessary in their derivation.
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000545">SBO:0000545</a>). 
	 */
	public static final URI SYSTEMS_DESCRIPTION_PARAMETER = type("SBO:0000545");
	/**
	 * A measure of the rate of growth of an organism, usually in culture. This can be expressed as increase in 
	 * cell number or, more usually as an increase in dry weight of cells (grams), measured over a unit time period. 
	 * Usually expressed as hour -1.
	 * (<a href="http://identifiers.org/biomodels.sbo/SBO:0000545">SBO:0000610</a>). 
	 */
	public static final URI GROWTH_RATE = type("SBO:0000610");
}
