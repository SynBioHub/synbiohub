package org.sbolstandard.core2.examples;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.sbolstandard.core2.Activity;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.Usage;

/**
* Here, we exemplify how to specify a build operations using SBOL's PROV-O support.
* 
* The build operations are:
* -- cut     ... linearize a (circular) vector using a restriction enzyme.
* -- amplify ... amplification of a linear or circular construct using 5' and 3' primers
* -- join    ... assembly of two construct using a ligase  
* 
* 
* NOTE! All operations are protocol-agnostic. That is, we do not specify HOW
*       these operations should be executed in the lab.
*       
* @author Ernst Oberortner
* 
*/
public class Provenance_SpecifyBuildOperations {
	
	// TODO:
	// add "Build" ontology term to (at least) operations
	
	public static final SequenceOntology so;
	static {
		 so = new SequenceOntology();
	}

	public static final URI DNA = ComponentDefinition.DNA_REGION;
	
	public static final Set<URI> LINEAR_SINGLE_STRANDED_DNA = 
			new HashSet<>(Arrays.asList(
					so.getURIbyId("SO:0000987"),	// linear
					so.getURIbyId("SO:0000984"),	// single-stranded
					ComponentDefinition.DNA_REGION));		// DNA
			
	public static final Set<URI> LINEAR_DOUBLE_STRANDED_DNA = 
			new HashSet<>(Arrays.asList(
					so.getURIbyId("SO:0000987"),	// linear
					so.getURIbyId("SO:0000985"),	// double-stranded
					ComponentDefinition.DNA_REGION));		// DNA

	public static final URI VECTOR_PLASMID = so.getURIbyId("SO:0000755");

	public static final URI PCR_PRODUCT = so.getURIbyId("SO:0000006");
	
	// the result of an amplification/PCR is a linear double-stranded DNA construct
	// we add also that an amplified construct is the product of a PCR reaction
	public static Set<URI> AMPLIFIED_CONSTRUCT = new HashSet<> (Arrays.asList(PCR_PRODUCT));
	static {
		AMPLIFIED_CONSTRUCT.addAll(LINEAR_DOUBLE_STRANDED_DNA);
	}

	public static final URI FORWARD_PRIMER = so.getURIbyId("SO:0000121");
	public static final URI REVERSE_PRIMER = so.getURIbyId("SO:0000132");
	
	// GeneOntology
	// "Type II site-specific deoxyribonuclease activity"
	public static final URI RESTRICTION_ENZYME = URI.create("http://purl.obolibrary.org/obo/GO_0009036");
	
	public static final URI UPSTREAM = so.getURIbyId("SO:0001631");
	public static final URI DOWNSTREAM = so.getURIbyId("SO:0001632");

	public static final String BUILD_PREFIX = "http://sbolstandard.org/build/";
	
	/**
	 * MAIN
	 * 
	 * @param args
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) 
			throws Exception {

		// exemplify the cut operation
		specifyCutOperation();
		
		// exemplify the amplify operation 
		specifyAmplifyOperation();
		
		// exemplify the join operation
		specifyJoinOperation();
	}

	/**
	 * specifies to perform a cut operation in order to linearize a vector using a restriction enzyme
	 * 
	 * @throws Exception
	 */
	public static void specifyCutOperation() 
			throws Exception {

		// instantiate a document
		SBOLDocument document = new SBOLDocument();				
		document.setDefaultURIprefix(BUILD_PREFIX);
		
		ComponentDefinition vector = document.createComponentDefinition(
				"vector", VECTOR_PLASMID);
		vector.setName("vector");
		
		ComponentDefinition enzyme = document.createComponentDefinition(
				"restriction_enzyme", RESTRICTION_ENZYME);
		enzyme.setName("restriction_enzyme");

		//Create the generic top level entity for the cut operation
		Activity activity = document.createActivity("cut_" + vector.getName() + "_with_" + enzyme.getName());
		activity.setName("cut(" + vector.getName() + ", " + enzyme.getName() + ")");

		//Create the qualifiedUsage annotation to describe the inputs of the cut operation
		activity.createUsage("vector", vector.getIdentity()).addRole(VECTOR_PLASMID);
		activity.createUsage("enzyme", enzyme.getIdentity()).addRole(RESTRICTION_ENZYME);

		// the result of the cut operation
		ComponentDefinition linearized_vector = document.createComponentDefinition(
				"linearized_vector", LINEAR_DOUBLE_STRANDED_DNA);
		linearized_vector.setName("linearized_vector");
		linearized_vector.addWasGeneratedBy(activity.getIdentity());
		
		// serialize the document to a file
		SBOLWriter.write(document, System.out);
   }
	
	
	/**
	 * specifies the amplify operation, which amplifies a linear DNA construct using 
	 * 5' and 3' primers
	 * 
	 * @throws Exception
	 */
	public static void specifyAmplifyOperation() 
			throws Exception {
		
		// instantiate a document
		SBOLDocument document = new SBOLDocument();				
		document.setDefaultURIprefix(BUILD_PREFIX);
		
		// the linear DNA construct
		ComponentDefinition dnaConstruct = document.createComponentDefinition(
				"dna_construct", LINEAR_SINGLE_STRANDED_DNA);
		dnaConstruct.setName("dna_construct");
		
		// the 5' primer for amplification
		ComponentDefinition fivePrimer = document.createComponentDefinition(
				"five_primer", FORWARD_PRIMER);
		fivePrimer.setName("five_primer");
		
		// the 3' primer for amplification
		ComponentDefinition threePrimer = document.createComponentDefinition(
				"three_primer", REVERSE_PRIMER);
		threePrimer.setName("three_primer");
		
		
		//Create the generic top level entity for the amplify operation
		Activity amplifyOperation = document.createActivity(
				"amplify_" + dnaConstruct.getName() + 
				"_with_" + fivePrimer.getName() + 
				"_and_" + threePrimer.getName());
		
		amplifyOperation.setName("amplify(" + dnaConstruct.getName() + ", " + 
				fivePrimer.getName() + ", " + 
				threePrimer.getName() +")");

		// create the qualifiedUsage annotation to describe the inputs of the amplification operation
		// -- the amplicon
		Usage usageDNAConstruct = amplifyOperation.createUsage("dna_construct", dnaConstruct.getIdentity());
		usageDNAConstruct.addRole(URI.create("http://sbols.org/v2#source"));
		usageDNAConstruct.addRole(PCR_PRODUCT);
		// -- the forward primer
		Usage usageFwdPrimer = amplifyOperation.createUsage("forward_primer", fivePrimer.getIdentity());
		usageFwdPrimer.addRole(FORWARD_PRIMER);
		usageFwdPrimer.addRole(FORWARD_PRIMER);
		// -- the reverse primer
		Usage usageRevPrimer = amplifyOperation.createUsage("reverse_primer", threePrimer.getIdentity());
		usageRevPrimer.addRole(REVERSE_PRIMER);
		usageRevPrimer.addRole(REVERSE_PRIMER);

		// the result of the amplification operation
		ComponentDefinition amplified_construct = document.createComponentDefinition(
				"my_amplified_dna", AMPLIFIED_CONSTRUCT);
		amplified_construct.setName("my_amplified_dna");
		amplified_construct.addWasGeneratedBy(amplifyOperation.getIdentity());
		
		// serialize the document to a file
		SBOLWriter.write(document, System.out);
	}


	/**
	 * specifies a join operation, which joins two linear DNA constructs
	 * 
	 * NOTE! at this point, we do not specify any further information 
	 * about how to execute the join operation!
	 * 
	 * @throws Exception
	 */
	public static void specifyJoinOperation() 
			throws Exception {
		
		// instantiate a document
		SBOLDocument document = new SBOLDocument();				
		document.setDefaultURIprefix(BUILD_PREFIX);

		// the first linear DNA construct
		ComponentDefinition cdPart1 = document.createComponentDefinition(
				"dna_part_1", LINEAR_DOUBLE_STRANDED_DNA);
		cdPart1.setName("dna_part_1");

		// the second linear DNA construct
		ComponentDefinition cdPart2 = document.createComponentDefinition(
				"dna_part_2", LINEAR_DOUBLE_STRANDED_DNA);
		cdPart2.setName("dna_part_2");

		//Create the generic top level entity for the join operation
		Activity joinOperation = document.createActivity(
				"join_" + cdPart1.getName() + 
				"_with_" + cdPart2.getName());
		
		joinOperation.setName("join(" + cdPart1.getName() + ", " + cdPart2.getName() + ")");
		
		// specify the "inputs" to the join operation
		joinOperation.createUsage("dna_part_1", cdPart1.getIdentity()).addRole(UPSTREAM);
		joinOperation.createUsage("dna_part_2", cdPart2.getIdentity()).addRole(DOWNSTREAM);
		
		// specify the "output" of the join operation
		ComponentDefinition cdJoinedPart = document.createComponentDefinition(
				"joined_dna_part", LINEAR_DOUBLE_STRANDED_DNA);
		cdJoinedPart.setName("joined_dna_part");
		
		cdJoinedPart.addWasGeneratedBy(joinOperation.getIdentity());
		
		// serialize the document to a file
		SBOLWriter.write(document, System.out);
	}
}