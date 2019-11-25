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
 * This class provides methods for accessing <a href=http://www.sequenceontology.org/> <i>Sequence Ontology</i></a> (SO) terms 
 * and querying about their relationships. 
 * 
 * @author Zhen Zhang
 * @author Tramy Nguyen
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */

public class SequenceOntology {

	// the map that contains all SO terms
	// will get initialized at the first access

	// EO: does the SO have a 1-1 key-value mapping?

	// private static Map<String, URI> so = null;

	private static final String URI_PREFIX = "http://identifiers.org/so/";

	/**
	 * Namespace of the Sequence Ontology (<a href="http://identifiers.org/so/">http://identifiers.org/so/</a>).
	 */
	public static final URI NAMESPACE = URI.create(URI_PREFIX);

	private static OBOOntology sequenceOntology = null;
	
	/**
	 * Construct an SO ontology and reads the OBO definition file, if it has not been read in before.
	 */
	public SequenceOntology() {
		if (sequenceOntology == null) {
			OBOParser oboParser = new OBOParser();
			InputStreamReader f = new InputStreamReader(getClass().
					getResourceAsStream("/ontologies/SequenceOntology/so-xp.obo"));
			try {
				oboParser.parse(f);
				sequenceOntology = oboParser.getOntology();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static URI convertSeqOntologyV1(String term)
	{
		String v1SO 	   = "http://purl.obolibrary.org/obo/SO_";
		String v2SO 	   = "http://identifiers.org/so/SO:";
		String convertedSO = term;
		if(term.startsWith(v1SO))
		{
			convertedSO = convertedSO.replace(v1SO, v2SO);
			return URI.create(convertedSO);
		}
		else if (term.startsWith("SO:")) {
			convertedSO = convertedSO.replace("SO:", v2SO);
		} else if (term.startsWith("so:")) {
			convertedSO = convertedSO.replace("so:", v2SO);
		}  
		return URI.create(convertedSO);
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
		List<String> IdList = new ArrayList<String>();	
		for (OBOStanza stanza : sequenceOntology.getStanzas()) {
			if (stanzaName.trim().equals(stanza.getName().trim())) {
				IdList.add(stanza.getId());
			}
		}
		if (IdList.isEmpty()) {
			try {
				throw new IllegalArgumentException("Illegal name " + stanzaName + ". It does not exist.");
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
		OBOStanza oboStanza = sequenceOntology.getStanza(id);
		if (oboStanza == null) {
			try {
				throw new IllegalArgumentException("ID " + id + " does not exist.");
			}
			catch (IllegalArgumentException e) {
				return null;
			}
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
		OBOStanza oboStanza = sequenceOntology.getStanza(stanzaId);
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
	 * Returns the URI that is composed of the Sequence Ontology (SO) namespace, i.e. "http://identifiers.org/so/", followed by the ID of an SO term, 
	 * of the stanza whose name matches the given name. If multiple matches are found, only the first matching
	 * one is returned. 
	 * 
	 * @param stanzaName the name of a term
	 * @return the URI of the given SO name
	 */
	public final URI getURIbyName(String stanzaName) {
		return getURIbyId(getId(stanzaName));
	}
	
	/** 
	 * Creates a URI by appending the given stanza ID to the end of the
	 * Sequence Ontology (SO) namespace，i.e. "http://identifiers.org/so/".
	 * 
	 * @param stanzaId the ID of a stanza
	 * @return the created URI
	 */
	public final URI getURIbyId(String stanzaId) {
		if (stanzaId==null) return null;
		OBOStanza oboStanza = sequenceOntology.getStanza(stanzaId.trim());
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
	 * Returns {@code true} if the stanza with Id1 is a descendant of the stanza with Id2. 
	 *  
	 * @param Id1 ID of the first stanza
	 * @param Id2 ID of the second stanza
	 * @return {@code true} if the stanza with Id1 is a descendant of the stanza with Id2, or {@code false} otherwise
	 */
	public boolean isDescendantOf(String Id1, String Id2) {
		OBOStanza stanza1 = sequenceOntology.getStanza(Id1);
		OBOStanza stanza2 = sequenceOntology.getStanza(Id2);
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
		return sequenceOntology.isDescendantOf(stanza1, stanza2);
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
	 * Returns the set of child IDs that are descendants of the given parent ID. 
	 * This set excludes the given parent ID.
	 * 
	 * @param parentId the ID of the parent stanza
	 * @return the set of child IDs that are descendants of the given parent ID
	 */
	public Set<String> getDescendantsOf(String parentId) {
		OBOStanza stanza1 = sequenceOntology.getStanza(parentId);
		if (stanza1 == null) {
			try {
				throw new IllegalArgumentException("Illegal ID: " + parentId + ". No match was found.");
			}
			catch (IllegalArgumentException e) {
				return null;
			}
		}
		return sequenceOntology.getDescendantsOf(stanza1);
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
	 * Creates a new URI from the Sequence Ontology (SO) namespace, i.e. "http://identifiers.org/so/", with the given ID. 
	 * For example, the method call <code>type("SO:0000001")</code> will return the URI <a>http://identifiers.org/so/SO:0000001</a>
	 * @param id the ID of an SO term
	 * @return the created URI
	 */
	private static final URI type(String id) {
		return URI.create(URI_PREFIX+id);
	}
		
	/**
	 * A regulatory_region composed of the TSS(s) and binding sites for TF_complexes of the basal transcription
	 * machinery (<a href="http://identifiers.org/so/SO:0000167">SO:0000167</a>).
	 */
	public static final URI PROMOTER = type("SO:0000167");

	/**
	 * A regulatory element of an operon to which activators or repressors bind, thereby effecting translation of genes
	 * in that operon (<a href="http://identifiers.org/so/SO:0000057">SO:0000057</a>).
	 */
	public static final URI OPERATOR = type("SO:0000057");

	/**
	 * A contiguous sequence which begins with, and includes, a start codon, and ends with, and includes, a stop codon
	 * (<a href="http://identifiers.org/so/SO:0000316">SO:0000316</a>).
	 */
	public static final URI CDS = type("SO:0000316");

	/**
	 * A region at the 5' end of a mature transcript (preceding the initiation codon) that is not translated into a
	 * protein (<a href="http://identifiers.org/so/SO:0000204">SO:0000204</a>).
	 */
	public static final URI FIVE_PRIME_UTR = type("SO:0000204");

	/**
	 * The sequence of DNA located either at the end of the transcript that causes RNA polymerase to terminate
	 * transcription (<a href="http://identifiers.org/so/SO:0000141">SO:0000141</a>).
	 */
	public static final URI TERMINATOR = type("SO:0000141");

	/**
	 * A transcriptional cis regulatory region that, when located between a CM and a gene's promoter, prevents the CRM
	 * from modulating that genes expression (<a href="http://identifiers.org/so/SO:0000627">SO:0000627</a>)
	 */
	public static final URI INSULATOR = type("SO:0000627");

	/**
	 * The origin of replication; starting site for duplication of a nucleic acid molecule to give two identical copies
	 * (<a href="http://identifiers.org/so/SO:0000296">SO:0000296</a>).
	 */
	public static final URI ORIGIN_OF_REPLICATION = type("SO:0000296");

	/**
	 * Non-covalent primer binding site for initiation of replication, transcription, or reverse transcription (<a
	 * href="http://identifiers.org/so/SO:0005850">SO:0005850</a>)
	 */
	public static final URI PRIMER_BINDING_SITE = type("SO:0005850");

	/**
	 * Region in mRNA where ribosome assembles (<a
	 * href="http://identifiers.org/so/SO:0000139">SO:0000139</a>)
	 */
	public static final URI RIBOSOME_ENTRY_SITE = type("SO:0000139");

	/**
	 * A region (or regions) that includes all of the sequence elements necessary to encode a functional transcript.
	 * A gene may include regulatory regions, transcribed regions and/or other functional sequence regions (<a
	 * href="http://identifiers.org/so/SO:0000704">SO:0000704</a>)
	 */
	public static final URI GENE = type("SO:0000704");

	/**
	 * Messenger RNA is the intermediate molecule between DNA and protein. It includes UTR and coding sequences. It does not contain introns (<a
	 * href="http://identifiers.org/so/SO:0000234">SO:0000234</a>)
	 */
	public static final URI MRNA = type("SO:0000234");

	/**
	 * Represents a region of a DNA molecule which is a nucleotide region (usually a palindrome) that is recognized by a
	 * restriction enzyme (<a href="http://identifiers.org/so/SO:0001687">SO:0001687</a>).
	 */
	public static final URI RESTRICTION_ENZYME_RECOGNITION_SITE = type("SO:0001687");

	/**
	 * A gene that is engineered (<a href="http://identifiers.org/so/SO:0000280">SO:0000280</a>).
	 */
	public static final URI ENGINEERED_GENE = type("SO:0000280");

	/**
	 * A region that is engineered (<a href="http://identifiers.org/so/SO:0000804">SO:0000804</a>).
	 */
	public static final URI ENGINEERED_REGION = type("SO:0000804");
	
	/**
	 * Any extent of continuous biological sequence. (<a href="http://identifiers.org/so/SO:0000110">SO:0000110</a>).
	 */
	public static final URI SEQUENCE_FEATURE = type("SO:0000110");
	
	/**
	 * A small RNA oligo, typically about 20 bases, that guides the cas nuclease to a target DNA sequence in the 
	 * CRISPR/cas mutagenesis method. (<a href="http://identifiers.org/so/SO:0001998">SO:0001998</a>).
	 */
	public static final URI SGRNA = type("SO:0001998");

	/**
	 * Attributes added to describe the different kinds of replicon. 
	 * (<a href="http://identifiers.org/so/SO:0000983">SO:0000983</a>).
	 */
	public static final URI STRAND_ATTRIBUTE = type("SO:0000983");
	
	/**
	 * A stand attribute that it indicates it is composed of a single strand.
	 * (<a href="http://identifiers.org/so/SO:0000984">SO:0000984</a>).
	 */
	public static final URI SINGLE = type("SO:0000984");
	
	/**
	 * A stand attribute that it indicates it is composed of a double strand.
	 * (<a href="http://identifiers.org/so/SO:0000985">SO:0000985</a>).
	 */
	public static final URI DOUBLE = type("SO:0000985");
	
	
	/**
	 * Attributes added to describe the different kinds of replicon. 
	 * (<a href="http://identifiers.org/so/SO:0000986">SO:0000986</a>).
	 */
	public static final URI TOPOLOGY_ATTRIBUTE = type("SO:0000986");
	
	/**
	 * A quality of a nucleotide polymer that has a 3'-terminal residue and a 5'-terminal residue.
	 * (<a href="http://identifiers.org/so/SO:0000987">SO:0000987</a>).
	 */
	public static final URI LINEAR = type("SO:0000987");
	
	/**
	 * A quality of a nucleotide polymer that has no terminal nucleotide residues.
	 * (<a href="http://identifiers.org/so/SO:0000988">SO:0000988</a>).
	 */
	public static final URI CIRCULAR = type("SO:0000988");
		
	/**
	 * An attribute describing a sequence consisting of nucleobases bound to a 
	 * repeating unit made of a 2-deoxy-D-ribose ring connected to a phosphate backbone.
	 * (<a href="http://identifiers.org/so/SO:0000352">SO:0000352</a>).
	 */
	public static final URI DNA = type("SO:0000352");

	/**
	 * An attribute describing a sequence consisting of nucleobases bound to a repeating 
	 * unit made of a D-ribose ring connected to a phosphate backbone.
	 * (<a href="http://identifiers.org/so/SO:0000356">SO:0000356</a>).
	 */
	public static final URI RNA = type("SO:0000356");
}
