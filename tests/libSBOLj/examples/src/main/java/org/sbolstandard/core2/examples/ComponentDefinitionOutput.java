package org.sbolstandard.core2.examples;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;

import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.OrientationType;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceOntology;

/**
 * This example shows creating complex {@link org.sbolstandard.core2.ComponentDefinition} entities. In the example, the BBa_F2620 PoPS Receiver device is 
 * represented with its sub components and their corresponding nucleotide sequences.
 *
 */
public class ComponentDefinitionOutput {
	
	public static SBOLDocument createComponentDefinitionOutput() throws SBOLValidationException {
				
		String prURI="http://partsregistry.org/";		
		String prPrefix="pr";	
		
		SBOLDocument document = new SBOLDocument();				
		document.setTypesInURIs(true);
		document.addNamespace(URI.create(prURI), prPrefix);
		document.setDefaultURIprefix(prURI);	
	
		Sequence seqpTetR=document.createSequence(
				"BBa_R0040",
				"",
				"tccctatcagtgatagagattgacatccctatcagtgatagagatactgagcac", 
				URI.create("http://www.chem.qmul.ac.uk/iubmb/misc/naseq.html")
				);
		
		Sequence seqRbs=document.createSequence(
				"BBa_B0034",
				"",
				 "aaagaggagaaa", 
				URI.create("http://www.chem.qmul.ac.uk/iubmb/misc/naseq.html")
				);

		Sequence seqCds=document.createSequence(
				"BBa_C0062",
				"",
				 "atgcttatctgatatgactaaaatggtacattgtgaatattatttactcgcgatcatttatcctcattctatggttaaatctgatatttcaatcctagataattaccctaaaaaatggaggcaatattatgatgacgctaatttaataaaatatgatcctatagtagattattctaactccaatcattcaccaattaattggaatatatttgaaaacaatgctgtaaataaaaaatctccaaatgtaattaaagaagcgaaaacatcaggtcttatcactgggtttagtttccctattcatacggctaacaatggcttcggaatgcttagttttgcacattcagaaaaagacaactatatagatagtttatttttacatgcgtgtatgaacataccattaattgttccttctctagttgataattatcgaaaaataaatatagcaaataataaatcaaacaacgatttaaccaaaagagaaaaagaatgtttagcgtgggcatgcgaaggaaaaagctcttgggatatttcaaaaatattaggttgcagtgagcgtactgtcactttccatttaaccaatgcgcaaatgaaactcaatacaacaaaccgctgccaaagtatttctaaagcaattttaacaggagcaattgattgcccatactttaaaaattaataacactgatagtgctagtgtagatcac", 
				URI.create("http://www.chem.qmul.ac.uk/iubmb/misc/naseq.html")
				);
		
		Sequence seqTer=document.createSequence(
				"BBa_B0015",
				"",
				 "ccaggcatcaaataaaacgaaaggctcagtcgaaagactgggcctttcgttttatctgttgtttgtcggtgaacgctctctactagagtcacactggctcaccttcgggtgggcctttctgcgtttata", 
				URI.create("http://www.chem.qmul.ac.uk/iubmb/misc/naseq.html")
				);
		
		Sequence seqPluxR=document.createSequence(
				"BBa_R0062",
				"",
				"acctgtaggatcgtacaggtttacgcaagaaaatggtttgttatagtcgaataaa", 
				URI.create("http://www.chem.qmul.ac.uk/iubmb/misc/naseq.html")
				);
		
		ComponentDefinition pTetR = document.createComponentDefinition(
				"BBa_R0040",
				"",
				new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA_REGION)));
		
		pTetR.addRole(SequenceOntology.PROMOTER);
		pTetR.setName("pTetR");
		pTetR.setDescription("TetR repressible promoter");	
		pTetR.addSequence(seqpTetR.getIdentity());
		
		ComponentDefinition rbs = document.createComponentDefinition(
				"BBa_B0034",
				"",
				new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA_REGION)));				
		rbs.addRole(SequenceOntology.RIBOSOME_ENTRY_SITE);
		rbs.setName("BBa_B0034");
		rbs.setDescription("RBS based on Elowitz repressilator");	
		rbs.addSequence(seqRbs.getIdentity());
		
		ComponentDefinition cds = document.createComponentDefinition(
				"BBa_C0062",
				"",
				new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA_REGION)));
		cds.addRole(SequenceOntology.CDS);
		cds.setName("luxR");
		cds.setDescription("luxR coding sequence");	
		cds.addSequence(seqCds.getIdentity());
		
		ComponentDefinition ter = document.createComponentDefinition(
				"BBa_B0015",
				"",
				new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA_REGION)));		
		ter.addRole(URI.create("http://identifiers.org/so/SO:0000141"));
		ter.setName("BBa_B0015");
		ter.setDescription("Double terminator");	
		ter.addSequence(seqTer.getIdentity());
		
		ComponentDefinition pluxR = document.createComponentDefinition(
				"BBa_R0062",
				"",
				new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA_REGION)));
		pluxR.addRole(SequenceOntology.PROMOTER);//
		pluxR.setName("pLuxR");
		pluxR.setDescription("LuxR inducible promoter");	
		pluxR.addSequence(seqPluxR.getIdentity());
										
		ComponentDefinition device = document.createComponentDefinition(
				"BBa_F2620",
				"",
				new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA_REGION)));
				device.addRole(URI.create("http://identifiers.org/so/SO:00001411"));//biological region							
		device.setName("BBa_F2620");
		device.setDescription("3OC6HSL -> PoPS Receiver");	
		
		Component comPtetR=device.createComponent("pTetR", AccessType.PUBLIC, pTetR.getIdentity());
		Component comRbs=device.createComponent("rbs", AccessType.PUBLIC,rbs.getIdentity());
		Component comCds=device.createComponent("luxR", AccessType.PUBLIC, cds.getIdentity());
		Component comTer=device.createComponent("ter", AccessType.PUBLIC, ter.getIdentity());
		Component comPluxR=device.createComponent( "pLuxR", AccessType.PUBLIC, pluxR.getIdentity());		
		
		int start=1;
		int end=seqPluxR.getElements().length();
						
		SequenceAnnotation anno=device.createSequenceAnnotation("anno1", "location1",start, end, OrientationType.INLINE);
		anno.setComponent(comPtetR.getIdentity());
		
		start=end+1;
		end=seqRbs.getElements().length() + end + 1;
		SequenceAnnotation anno2= device.createSequenceAnnotation("anno2","location2", start,end,OrientationType.INLINE);
		anno2.setComponent(comRbs.getIdentity());
			
		start=end+1;
		end=seqCds.getElements().length() + end + 1;
		SequenceAnnotation anno3= device.createSequenceAnnotation("anno3","location3", start,end,OrientationType.INLINE);
		anno3.setComponent(comCds.getIdentity());
		
		start=end+1;
		end=seqTer.getElements().length() + end + 1;		
		SequenceAnnotation anno4= device.createSequenceAnnotation("anno4","location4", start,end,OrientationType.INLINE);
		anno4.setComponent(comTer.getIdentity());

		start=end+1;
		end=seqPluxR.getElements().length() + end + 1;
		SequenceAnnotation anno5= device.createSequenceAnnotation("anno5", "location5",start,end,OrientationType.INLINE);
		anno5.setComponent(comPluxR.getIdentity());
		
		return document;
	}
	
	public static void main( String[] args ) throws Exception
    {
		SBOLDocument document = createComponentDefinitionOutput();
		SBOLWriter.write(document,(System.out));		
    }
}	