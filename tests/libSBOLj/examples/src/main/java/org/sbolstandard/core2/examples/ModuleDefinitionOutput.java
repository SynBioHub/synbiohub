package org.sbolstandard.core2.examples;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.xml.namespace.QName;

import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.EDAMOntology;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.Model;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.OrientationType;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SystemsBiologyOntology;

import org.sbolstandard.core.datatree.NamespaceBinding;
import static org.sbolstandard.core.datatree.Datatree.NamespaceBinding;

/**
 * This example shows how to use  {@link org.sbolstandard.core2.ModuleDefinition} entities. 
 * In the example, a toggle switch ModuleDefinition is built from two sub modules: LacI inverter and TetR inverter modules. 
 * LacI and TetR protein ComponentDefinition entities from these submodules are mapped in the parent toggle switch module.
 *
 */
public class ModuleDefinitionOutput {
	
	private static final NamespaceBinding example=NamespaceBinding ("http://sbolstandard.org/example/", "example");
	private static final NamespaceBinding biopax=NamespaceBinding ("http://www.biopax.org/release/biopax-level3.owl#", "biopax");
	private static final NamespaceBinding so=NamespaceBinding ("http://identifiers.org/so/", "so");
	private static final NamespaceBinding sbo=NamespaceBinding ("http://identifiers.org/biomodels.sbo/", "sbo");
	private static final NamespaceBinding pr=NamespaceBinding ("http://www.partsregistry.org/", "pr");
	private static final NamespaceBinding vpr=NamespaceBinding ("http://www.virtualparts.org/part/", "vpr");
	private static final NamespaceBinding uniprot=NamespaceBinding ("http://identifiers.org/uniprot/", "uniprot");
	
			  
	private static class Terms
	{
		private static class biopaxTerms
		{
			public static QName DnaRegion=biopax.withLocalPart("DnaRegion");
			public static QName Protein=biopax.withLocalPart("Protein");
			public static QName SmallMolecule=biopax.withLocalPart("SmallMolecule");			
		}
		private static class soTerms
		{
			public static QName CDS=so.withLocalPart("SO:0000316");
			public static QName RBS=so.withLocalPart("SO:0000139");
			public static QName terminator=so.withLocalPart("SO:0000141");
			public static QName promoter=so.withLocalPart("SO:0000167");
			public static QName engineeredGene=so.withLocalPart("SO:0000280");			
		}
		private static class sboTerms
		{
			public static QName inhibitor=sbo.withLocalPart("SBO:0000020");	
			public static QName product=sbo.withLocalPart("SBO:0000011");				
		}
		
		private static class sequenceTypes
		{
			public static URI nucleotides=URI.create("http://www.chem.qmul.ac.uk/iubmb/misc/naseq.html");
			public static URI aminoacids=URI.create("http://www.chem.qmul.ac.uk/iupac/AminoAcid/");
			public static URI atoms=URI.create("http://www.opensmiles.org/opensmiles.html");			
		}
		
		private static class moduleRoles
		{
			public static URI inverter=URI.create("http://parts.igem.org/cgi/partsdb/pgroup.cgi?pgroup=inverter");
			
		}
		
		private static class interactionTypes
		{
			public static QName transcriptionalRepression=sbo.withLocalPart("SBO:0000169");
		}
		
		private static class participantRoles
		{
			public static QName promoter=sbo.withLocalPart("SBO:0000598");
			public static QName inhibitor=sbo.withLocalPart("SBO:0000020");
			
		}		
	}
	
	private static void setDefaultNameSpace(SBOLDocument document, String uri) throws SBOLValidationException
	{
		if (uri.endsWith("/"))
		{
			uri=uri.substring(0,uri.length()-1);
		}
		document.setDefaultURIprefix(uri);
	}
	
	public static void main( String[] args ) throws Exception
    {
		SBOLDocument document = new SBOLDocument();		
		
		setDefaultNameSpace(document, pr.getNamespaceURI());		
		ComponentDefinition gfp= createComponenDefinition(document, pr.withLocalPart("BBa_E0040"),"gfp", Terms.biopaxTerms.DnaRegion, Terms.soTerms.CDS, "gfp coding sequence");
		ComponentDefinition tetR= createComponenDefinition(document, pr.withLocalPart("BBa_C0040"),"tetR", Terms.biopaxTerms.DnaRegion, Terms.soTerms.CDS, "tetR coding sequence");
		ComponentDefinition lacI= createComponenDefinition(document, pr.withLocalPart("BBa_C0012"),"lacI", Terms.biopaxTerms.DnaRegion, Terms.soTerms.CDS, "lacI coding sequence");
		ComponentDefinition placI= createComponenDefinition(document, pr.withLocalPart("BBa_R0010"), "pLacI", Terms.biopaxTerms.DnaRegion, Terms.soTerms.promoter, "pLacI promoter");
		ComponentDefinition ptetR= createComponenDefinition(document, pr.withLocalPart("BBa_R0040"),"pTetR", Terms.biopaxTerms.DnaRegion, Terms.soTerms.promoter, "pTet promoter");
		ComponentDefinition rbslacI= createComponenDefinition(document, pr.withLocalPart("BBa_J61101"), "BBa_J61101 RBS",Terms.biopaxTerms.DnaRegion, Terms.soTerms.RBS, "RBS1");
		ComponentDefinition rbstetR= createComponenDefinition(document, pr.withLocalPart("BBa_J61120"), "BBa_J61101 RBS",Terms.biopaxTerms.DnaRegion, Terms.soTerms.RBS, "RBS2");
		ComponentDefinition rbsgfp= createComponenDefinition(document, pr.withLocalPart("BBa_J61130"), "BBa_J61101 RBS",Terms.biopaxTerms.DnaRegion, Terms.soTerms.RBS, "RBS2");
		
		setDefaultNameSpace(document, uniprot.getNamespaceURI());		
		ComponentDefinition GFP= createComponenDefinition(document, uniprot.withLocalPart("P42212"), "GFP",Terms.biopaxTerms.Protein, Terms.sboTerms.product, "GFP protein");		
		ComponentDefinition TetR= createComponenDefinition(document, uniprot.withLocalPart("Q6QR72"), "TetR",Terms.biopaxTerms.Protein, Terms.sboTerms.inhibitor, "TetR protein");
		ComponentDefinition LacI= createComponenDefinition(document, uniprot.withLocalPart("P03023"),"LacI", Terms.biopaxTerms.Protein, Terms.sboTerms.inhibitor, "LacI protein");
				
		setDefaultNameSpace(document, pr.getNamespaceURI());
		ComponentDefinition lacITerminator= createComponenDefinition(document, pr.withLocalPart("ECK120029600"),"ECK120029600", Terms.biopaxTerms.DnaRegion, Terms.soTerms.terminator, "Terminator1");
		ComponentDefinition tetRTerminator= createComponenDefinition(document, pr.withLocalPart("ECK120033736"), "ECK120033736",Terms.biopaxTerms.DnaRegion, Terms.soTerms.terminator, "Terminator2");
		
		setDefaultNameSpace(document, vpr.getNamespaceURI());
		ComponentDefinition tetRInverter= createComponenDefinition(document, vpr.withLocalPart("pIKELeftCassette_1"), "TetR Inverter", Terms.biopaxTerms.DnaRegion, Terms.soTerms.engineeredGene, "TetR Inverter");
		ComponentDefinition lacIInverter= createComponenDefinition(document, vpr.withLocalPart("pIKERightCassette_1"), "LacI Inverter", Terms.biopaxTerms.DnaRegion, Terms.soTerms.engineeredGene, "LacI Inverter");
		ComponentDefinition toggleSwitch= createComponenDefinition(document, vpr.withLocalPart("pIKE_Toggle_1"), "LacI/TetR Toggle Switch", Terms.biopaxTerms.DnaRegion, Terms.soTerms.engineeredGene, "LacI/TetR Toggle Switch");
		
		//tetR inverter sequences
		addPRSequence(document, ptetR,"tccctatcagtgatagagattgacatccctatcagtgatagagatactgagcac");		
		addPRSequence(document, rbslacI,"aaagacaggacc");		
		addPRSequence(document, lacI,"atggtgaatgtgaaaccagtaacgttatacgatgtcgcagagtatgccggtgtctcttatcagaccgtttcccgcgtggtgaaccaggccagccacgtttctgcgaaaacgcgggaaaaagtggaagcggcgatggcggagctgaattacattcccaaccgcgtggcacaacaactggcgggcaaacagtcgttgctgattggcgttgccacctccagtctggccctgcacgcgccgtcgcaaattgtcgcggcgattaaatctcgcgccgatcaactgggtgccagcgtggtggtgtcgatggtagaacgaagcggcgtcgaagcctgtaaagcggcggtgcacaatcttctcgcgcaacgcgtcagtgggctgatcattaactatccgctggatgaccaggatgccattgctgtggaagctgcctgcactaatgttccggcgttatttcttgatgtctctgaccagacacccatcaacagtattattttctcccatgaagacggtacgcgactgggcgtggagcatctggtcgcattgggtcaccagcaaatcgcgctgttagcgggcccattaagttctgtctcggcgcgtctgcgtctggctggctggcataaatatctcactcgcaatcaaattcagccgatagcggaacgggaaggcgactggagtgccatgtccggttttcaacaaaccatgcaaatgctgaatgagggcatcgttcccactgcgatgctggttgccaacgatcagatggcgctgggcgcaatgcgcgccattaccgagtccgggctgcgcgttggtgcggatatctcggtagtgggatacgacgataccgaagacagctcatgttatatcccgccgttaaccaccatcaaacaggattttcgcctgctggggcaaaccagcgtggaccgcttgctgcaactctctcagggccaggcggtgaagggcaatcagctgttgcccgtctcactggtgaaaagaaaaaccaccctggcgcccaatacgcaaaccgcctctccccgcgcgttggccgattcattaatgcagctggcacgacaggtttcccgactggaaagcgggcaggctgcaaacgacgaaaactacgctttagtagcttaataa");
		addPRSequence(document, lacITerminator,"ttcagccaaaaaacttaagaccgccggtcttgtccactaccttgcagtaatgcggtggacaggatcggcggttttcttttctcttctcaa");		
		
		//lacI inverter sequences
		addPRSequence(document, placI,"tccctatcagtgatagagattgacatccctatcagtgatagagatactgagcac");		
		addPRSequence(document, rbstetR,"aaagacaggacc");	
		addPRSequence(document, tetR,"atgtccagattagataaaagtaaagtgattaacagcgcattagagctgcttaatgaggtcggaatcgaaggtttaacaacccgtaaactcgcccagaagctaggtgtagagcagcctacattgtattggcatgtaaaaaataagcgggctttgctcgacgccttagccattgagatgttagataggcaccatactcacttttgccctttagaaggggaaagctggcaagattttttacgtaataacgctaaaagttttagatgtgctttactaagtcatcgcgatggagcaaaagtacatttaggtacacggcctacagaaaaacagtatgaaactctcgaaaatcaattagcctttttatgccaacaaggtttttcactagagaatgcattatatgcactcagcgctgtggggcattttactttaggttgcgtattggaagatcaagagcatcaagtcgctaaagaagaaagggaaacacctactactgatagtatgccgccattattacgacaagctatcgaattatttgatcaccaaggtgcagagccagccttcttattcggccttgaattgatcatatgcggattagaaaaacaacttaaatgtgaaagtgggtccgctgcaaacgacgaaaactacgctttagtagcttaataa");
		addPRSequence(document, rbsgfp,"aaagaaacgaca");
		addPRSequence(document, gfp,"atgcgtaaaggagaagaacttttcactggagttgtcccaattcttgttgaattagatggtgatgttaatgggcacaaattttctgtcagtggagagggtgaaggtgatgcaacatacggaaaacttacccttaaatttatttgcactactggaaaactacctgttccatggccaacacttgtcactactttcggttatggtgttcaatgctttgcgagatacccagatcatatgaaacagcatgactttttcaagagtgccatgcccgaaggttatgtacaggaaagaactatatttttcaaagatgacgggaactacaagacacgtgctgaagtcaagtttgaaggtgatacccttgttaatagaatcgagttaaaaggtattgattttaaagaagatggaaacattcttggacacaaattggaatacaactataactcacacaatgtatacatcatggcagacaaacaaaagaatggaatcaaagttaacttcaaaattagacacaacattgaagatggaagcgttcaactagcagaccattatcaacaaaatactccaattggcgatggccctgtccttttaccagacaaccattacctgtccacacaatctgccctttcgaaagatcccaacgaaaagagagaccacatggtccttcttgagtttgtaacagctgctgggattacacatggcatggatgaactatacaaataataa");				
		addPRSequence(document, tetRTerminator,"ttcagccaaaaaacttaagaccgccggtcttgtccactaccttgcagtaatgcggtggacaggatcggcggttttcttttctcttctcaa");		
		
		addSubComponents(document, tetRInverter, ptetR,rbslacI,lacI,lacITerminator);
		addSubComponents(document, lacIInverter, placI,rbstetR,tetR,rbsgfp,gfp,tetRTerminator);
		addSubComponents(document, toggleSwitch, tetRInverter,lacIInverter);
		
		setDefaultNameSpace(document, example.getNamespaceURI());
		ModuleDefinition laciInverterModuleDef=document.createModuleDefinition("laci_inverter");
		laciInverterModuleDef.addRole(Terms.moduleRoles.inverter);
		
		
		ModuleDefinition tetRInverterModuleDef=document.createModuleDefinition("tetr_inverter");
		tetRInverterModuleDef.addRole(Terms.moduleRoles.inverter);
		
		createInverter(document,laciInverterModuleDef,placI,LacI);
		
		createInverter(document,tetRInverterModuleDef,ptetR,TetR);
		
		ModuleDefinition toggleSwitchModuleDef=document.createModuleDefinition("toggle_switch"); 
		toggleSwitchModuleDef.addRole(toURI(example.withLocalPart("module_role/toggle_switch")));	
				
		FunctionalComponent  toggleSwitchModuleDef_TetR=toggleSwitchModuleDef.createFunctionalComponent(
				"TetR", AccessType.PUBLIC, TetR.getIdentity(), DirectionType.INOUT);
		
		FunctionalComponent  toggleSwitchModuleDef_LacI=toggleSwitchModuleDef.createFunctionalComponent(
				"LacI" , 
				AccessType.PUBLIC, 
				LacI.getIdentity(),
				DirectionType.INOUT);
		
		
		Module lacInverterSubModule=toggleSwitchModuleDef.createModule(
				"laci_inverter", 
				laciInverterModuleDef.getIdentity());				
		
		lacInverterSubModule.createMapsTo(
				"LacI_mapping", 
				RefinementType.USEREMOTE,
				toggleSwitchModuleDef_LacI.getIdentity(),
				laciInverterModuleDef.getFunctionalComponent("TF").getIdentity()
				);
		
		
		Module tetRInverterSubModule=toggleSwitchModuleDef.createModule(
				"tetr_inverter", 				
				tetRInverterModuleDef.getIdentity());
		
		tetRInverterSubModule.createMapsTo(
				"TetR_mapping", 
				RefinementType.USEREMOTE, 
				toggleSwitchModuleDef_TetR.getIdentity(),
				tetRInverterModuleDef.getFunctionalComponent("TF").getIdentity() 
				);
		
		Model model=document.createModel(
				"toogleswitch",
				URI.create("http://virtualparts.org/part/pIKE_Toggle_1"), 
				EDAMOntology.SBML, 
				SystemsBiologyOntology.CONTINUOUS_FRAMEWORK);
								
		toggleSwitchModuleDef.addModel(model.getIdentity());
								
		SBOLWriter.write(document,(System.out));		
    }
	
	private static void createInverter(SBOLDocument document, ModuleDefinition moduleDef, ComponentDefinition promoter, ComponentDefinition TF) throws Exception
	{
		FunctionalComponent  laciInverterModuleDef_promoter=moduleDef.createFunctionalComponent(
				"promoter", 
				AccessType.PUBLIC, 
				promoter.getIdentity(),
				DirectionType.INOUT);
		
		FunctionalComponent  laciInverterModuleDef_TF=moduleDef.createFunctionalComponent(
				"TF", 
				AccessType.PUBLIC, 
				TF.getIdentity(),
				DirectionType.INOUT);
				
		Interaction interaction=moduleDef.createInteraction(
				"LacI_pLacI", 
				new HashSet<URI>(Arrays.asList(toURI(Terms.interactionTypes.transcriptionalRepression))));
		
		Participation participation=interaction.createParticipation(
				promoter.getDisplayId(), 
				laciInverterModuleDef_promoter.getIdentity(),	
				toURI(Terms.participantRoles.promoter));
		
		Participation participation2=interaction.createParticipation(
				TF.getDisplayId(), 
				laciInverterModuleDef_TF.getIdentity(),
				toURI(Terms.participantRoles.inhibitor));
	}		
	
	private static ComponentDefinition createComponenDefinition(SBOLDocument document,QName identifier,String name, QName type, QName role,String description) throws SBOLValidationException
	{
		ComponentDefinition componentDef = document.createComponentDefinition(
				identifier.getLocalPart(), 				
				new HashSet<URI>(Arrays.asList(toURI(type))));
		componentDef.addRole(toURI(role));
		componentDef.setName(name);
		componentDef.setDescription(description);
		return componentDef;
	}
	
	private static Sequence addPRSequence(SBOLDocument document, ComponentDefinition componentDef, String elements) throws SBOLValidationException
	{
		return addSequence(document, componentDef, componentDef.getDisplayId(), Terms.sequenceTypes.nucleotides, elements);		
	}
	
	private static void addSubComponents(SBOLDocument document, ComponentDefinition componentDef, ComponentDefinition ... subComponents)	throws Exception
	{
		addSubComponents(document, componentDef, Arrays.asList(subComponents));
	}
	private static void addSubComponents(SBOLDocument document, ComponentDefinition componentDef, List<ComponentDefinition> subComponents)	throws Exception
	{
		int i=1;
		int start=0;
		int end=0;
		for (ComponentDefinition subComponent:subComponents)
		{
			Component component=componentDef.createComponent(
					subComponent.getDisplayId(), 
					AccessType.PUBLIC, 
					subComponent.getIdentity());
			
			start=end+1;
			end=start + getSequenceLength(document, subComponent);		
			SequenceAnnotation annotation=componentDef.createSequenceAnnotation("anno" + i,"location" + i,  start, end, OrientationType.INLINE);					
			annotation.setComponent(component.getIdentity());
			i++;
		}
	}
	
	private static int getSequenceLength (SBOLDocument document, ComponentDefinition componentDef) throws Exception
	{		if (componentDef.getSequences()!=null && componentDef.getSequences().size()>0)
		{
			Sequence sequence=componentDef.getSequences().iterator().next();
			return sequence.getElements().length();
		}		
		else
		{
			int total=0;
			for (SequenceAnnotation annotation:componentDef.getSequenceAnnotations())
			{
				if (annotation.getComponent()!=null)
				{
					Component component=annotation.getComponent();
										
					ComponentDefinition subComponentDef=component.getDefinition();
					total= total + getSequenceLength(document, subComponentDef);
				}
				else
				{
					throw new Exception ("Can't get sequence length for an incomplete design");
				}
			}
			return total;
		}
	}
	
	private static Sequence addSequence(SBOLDocument document, ComponentDefinition componentDef, String displayId, URI sequenceType, String elements) throws SBOLValidationException
	{
		Sequence sequence=document.createSequence(displayId,elements,sequenceType);				
		componentDef.addSequence(sequence.getIdentity());
		return sequence;
	}
	
	private static URI toURI(QName name)
	{
		return URI.create(name.getNamespaceURI() + name.getLocalPart());
	}	
}