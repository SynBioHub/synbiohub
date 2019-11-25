package org.sbolstandard.core2.Testing;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.StrategyType;
import org.sbolstandard.core2.CombinatorialDerivation;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.RestrictionType;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.SystemsBiologyOntology;

public class CombinatorialDesignTest {
	static SBOLDocument doc = new SBOLDocument();
	
	@Before
	public void setupDocument() throws SBOLValidationException {
		doc.setDefaultURIprefix("http://sbols.org/CRISPR_Example/");
		doc.setComplete(true);
		doc.setCreateDefaults(true);
		
		String version = "1.0.0";
		
		// Create ComponentDefinition for cas9_generic protein
		doc.createComponentDefinition("cas9_generic", version, ComponentDefinition.PROTEIN);
		
		// Create ComponentDefinition for gRNA_generic RNA
		doc.createComponentDefinition("gRNA_generic",version, ComponentDefinition.RNA_REGION).addRole(SequenceOntology.SGRNA);

		// Create ComponentDefinition for cas9_gRNA_complex
		doc.createComponentDefinition("cas9_gRNA_complex",version, ComponentDefinition.COMPLEX);

		// Create ComponentDefinition for target gene
		doc.createComponentDefinition("target_gene",version, ComponentDefinition.DNA_REGION).addRole(SequenceOntology.PROMOTER);

		// Create ComponentDefinition for target protein
		doc.createComponentDefinition("target",version, ComponentDefinition.PROTEIN);

		// Create ModuleDefinition for CRISPR_Repression_Template		
		ModuleDefinition  CRISPR_Template = doc.createModuleDefinition("CRISPR_Template", version);

		// Complex Formation Interaction for Cas9m_BFP and gRNA 
		Interaction Cas9Complex_Formation = CRISPR_Template.createInteraction("cas9_complex_formation", SystemsBiologyOntology.NON_COVALENT_BINDING);
		Cas9Complex_Formation.createParticipation("cas9_generic", "cas9_generic",SystemsBiologyOntology.REACTANT);
		Cas9Complex_Formation.createParticipation("gRNA_generic", "gRNA_generic",SystemsBiologyOntology.REACTANT);
		Cas9Complex_Formation.createParticipation("cas9_gRNA_complex", "cas9_gRNA_complex",SystemsBiologyOntology.PRODUCT);

		// Production of target from target gene
		Interaction EYFP_production = CRISPR_Template.createInteraction("target_production", SystemsBiologyOntology.GENETIC_PRODUCTION);
		EYFP_production.createParticipation("target_gene", "target_gene",SystemsBiologyOntology.PROMOTER);
		EYFP_production.createParticipation("target", "target",SystemsBiologyOntology.PRODUCT);
	
		// Inhibition of target by cas9m_BFP_gRNA 
		Interaction target_generic_gene_inhibition = CRISPR_Template.createInteraction("target_gene_inhibition", SystemsBiologyOntology.INHIBITION);
		target_generic_gene_inhibition.createParticipation("cas9_gRNA_complex", "cas9_gRNA_complex",SystemsBiologyOntology.INHIBITOR);
		target_generic_gene_inhibition.createParticipation("target_gene", "target_gene",SystemsBiologyOntology.PROMOTER);
		
		// Create Sequence for CRa_U6 promoter
		String CRa_U6_seq_elements = "GGTTTACCGAGCTCTTATTGGTTTTCAAACTTCATTGACTGTGCC" +
                "AAGGTCGGGCAGGAAGAGGGCCTATTTCCCATGATTCCTTCATAT" +
                "TTGCATATACGATACAAGGCTGTTAGAGAGATAATTAGAATTAAT" +
                "TTGACTGTAAACACAAAGATATTAGTACAAAATACGTGACGTAGA" +
                "AAGTAATAATTTCTTGGGTAGTTTGCAGTTTTAAAATTATGTTTT" +
                "AAAATGGACTATCATATGCTTACCGTAACTTGAAATATAGAACCG" +
                "ATCCTCCCATTGGTATATATTATAGAACCGATCCTCCCATTGGCT" +
                "TGTGGAAAGGACGAAACACCGTACCTCATCAGGAACATGTGTTTA" +
                "AGAGCTATGCTGGAAACAGCAGAAATAGCAAGTTTAAATAAGGCT" +
                "AGTCCGTTATCAACTTGAAAAAGTGGCACCGAGTCGGTGCTTTTT" +
                "TTGGTGCGTTTTTATGCTTGTAGTATTGTATAATGTTTTT";
		doc.createSequence("CRa_U6_seq", version, CRa_U6_seq_elements, Sequence.IUPAC_DNA); 
		
		// Create Sequence for gRNA_b coding sequence
		String gRNA_b_elements = "AAGGTCGGGCAGGAAGAGGGCCTATTTCCCATGATTCCTTCATAT" +
                "TTGCATATACGATACAAGGCTGTTAGAGAGATAATTAGAATTAAT" +
                "TTGACTGTAAACACAAAGATATTAGTACAAAATACGTGACGTAGA" +
                "AAGTAATAATTTCTTGGGTAGTTTGCAGTTTTAAAATTATGTTTT" +
                "AAAATGGACTATCATATGCTTACCGTAACTTGAAAGTATTTCGAT" +
                "TTCTTGGCTTTATATATCTTGTGGAAAGGACGAAACACCGTACCT" +
                "CATCAGGAACATGTGTTTAAGAGCTATGCTGGAAACAGCAGAAAT" +
                "AGCAAGTTTAAATAAGGCTAGTCCGTTATCAACTTGAAAAAGTGG" +
                "CACCGAGTCGGTGCTTTTTTT";
		doc.createSequence("gRNA_b_seq", version, gRNA_b_elements, Sequence.IUPAC_DNA);
		
		// Create Sequence for mKate
		String mKate_seq_elements = "TCTAAGGGCGAAGAGCTGATTAAGGAGAACATGCACATGAAGCTG" +
                "TACATGGAGGGCACCGTGAACAACCACCACTTCAAGTGCACATCC" +
                "GAGGGCGAAGGCAAGCCCTACGAGGGCACCCAGACCATGAGAATC" +
                "AAGGTGGTCGAGGGCGGCCCTCTCCCCTTCGCCTTCGACATCCTG" +
                "GCTACCAGCTTCATGTACGGCAGCAAAACCTTCATCAACCACACC" +
                "CAGGGCATCCCCGACTTCTTTAAGCAGTCCTTCCCTGAGGTAAGT" +
                "GGTCCTACCTCATCAGGAACATGTGTTTTAGAGCTAGAAATAGCA" +
                "AGTTAAAATAAGGCTAGTCCGTTATCAACTTGAAAAAGTGGCACC" +
                "GAGTCGGTGCTACTAACTCTCGAGTCTTCTTTTTTTTTTTCACAG" +
                "GGCTTCACATGGGAGAGAGTCACCACATACGAAGACGGGGGCGTG" +
                "CTGACCGCTACCCAGGACACCAGCCTCCAGGACGGCTGCCTCATC" +
                "TACAACGTCAAGATCAGAGGGGTGAACTTCCCATCCAACGGCCCT" +
                "GTGATGCAGAAGAAAACACTCGGCTGGGAGGCCTCCACCGAGATG" +
                "CTGTACCCCGCTGACGGCGGCCTGGAAGGCAGAAGCGACATGGCC" +
                "CTGAAGCTCGTGGGCGGGGGCCACCTGATCTGCAACTTGAAGACC" +
                "ACATACAGATCCAAGAAACCCGCTAAGAACCTCAAGATGCCCGGC" +
                "GTCTACTATGTGGACAGAAGACTGGAAAGAATCAAGGAGGCCGAC" +
                "AAAGAGACCTACGTCGAGCAGCACGAGGTGGCTGTGGCCAGATAC" +
                "TGCG";
		doc.createSequence("mKate_seq", version, mKate_seq_elements, Sequence.IUPAC_DNA);

		// Create Sequence for CRP_b promoter
		String CRP_b_seq_elements =  "GCTCCGAATTTCTCGACAGATCTCATGTGATTACGCCAAGCTACG" +
                "GGCGGAGTACTGTCCTCCGAGCGGAGTACTGTCCTCCGAGCGGAG" +
                "TACTGTCCTCCGAGCGGAGTACTGTCCTCCGAGCGGAGTTCTGTC" +
                "CTCCGAGCGGAGACTCTAGATACCTCATCAGGAACATGTTGGAAT" +
                "TCTAGGCGTGTACGGTGGGAGGCCTATATAAGCAGAGCTCGTTTA" +
                "GTGAACCGTCAGATCGCCTCGAGTACCTCATCAGGAACATGTTGG" +
                "ATCCAATTCGACC";
		doc.createSequence("CRP_b_seq", version, CRP_b_seq_elements, Sequence.IUPAC_DNA);
		
		// Create ComponentDefinition for a Constitutive Promoter
		doc.createComponentDefinition("pConst", version, ComponentDefinition.DNA_REGION).addRole(SequenceOntology.PROMOTER);
		
		// Create ComponentDefinition for cas9m_BFP coding sequence
		doc.createComponentDefinition("cas9m_BFP_cds", version, ComponentDefinition.DNA_REGION).addRole(SequenceOntology.CDS);
		
		// Create ComponentDefinition for cas9m_BFP gene
		ComponentDefinition cas9m_BFP_gene = doc.createComponentDefinition("cas9m_BFP_gene", version, ComponentDefinition.DNA_REGION);
		cas9m_BFP_gene.addRole(SequenceOntology.PROMOTER);
		cas9m_BFP_gene.createSequenceConstraint("cas9m_BFP_gene_constraint", RestrictionType.PRECEDES, "pConst", "cas9m_BFP_cds");
		
		// Create ComponentDefintion for cas9m_BFP protein
		doc.createComponentDefinition("cas9m_BFP", version, ComponentDefinition.PROTEIN);
		
		// Create ComponentDefintion for CRa_U6 promoter
		ComponentDefinition CRa_U6 = doc.createComponentDefinition("CRa_U6", version, ComponentDefinition.DNA_REGION);
		CRa_U6.addRole(SequenceOntology.PROMOTER);
		CRa_U6.addSequence("CRa_U6_seq");
		
		// Create ComponentDefintion for gRNA_b coding sequence
		ComponentDefinition gRNA_b_nc = doc.createComponentDefinition("gRNA_b_nc", version, ComponentDefinition.DNA_REGION);
		gRNA_b_nc.addRole(SequenceOntology.CDS);
		gRNA_b_nc.addSequence("gRNA_b_seq");
		
		// Create ComponentDefinition for gRNA_b terminator
		doc.createComponentDefinition("gRNA_b_terminator", version, ComponentDefinition.DNA_REGION).addRole(SequenceOntology.TERMINATOR); 
		
		// Create ComponentDefinition for gRNA_b gene
		ComponentDefinition gRNA_b_gene = doc.createComponentDefinition("gRNA_b_gene", version, ComponentDefinition.DNA_REGION);
		gRNA_b_gene.addRole(SequenceOntology.PROMOTER);
		gRNA_b_gene.createSequenceConstraint("gRNA_b_gene_constraint1", RestrictionType.PRECEDES, "CRa_U6", "gRNA_b_nc");
		gRNA_b_gene.createSequenceConstraint("gRNA_b_gene_constraint2", RestrictionType.PRECEDES, "gRNA_b_nc","gRNA_b_terminator");

		// Create ComponentDefinition for gRNA_b RNA
		doc.createComponentDefinition("gRNA_b", version, ComponentDefinition.RNA_REGION).addRole(SequenceOntology.SGRNA);
		SequenceOntology so = new SequenceOntology();
		URI sgrna = so.getURIbyName("sgRNA");
		  
		// Create ComponentDefinition for cas9m_BFP gRNA_b complex 
		doc.createComponentDefinition("cas9m_BFP_gRNA_b", version, ComponentDefinition.COMPLEX);
		
		// Create ComponentDefinition for mKate coding sequence
		ComponentDefinition mKate_cds = doc.createComponentDefinition("mKate_cds", version, ComponentDefinition.DNA_REGION);
		mKate_cds.addRole(SequenceOntology.CDS);
		mKate_cds.addSequence("mKate_seq");
		
		// Create ComponentDefinition for mKate gene
		ComponentDefinition mKate_gene = doc.createComponentDefinition("mKate_gene", version, ComponentDefinition.DNA_REGION);
		mKate_gene.addRole(SequenceOntology.PROMOTER);
		mKate_gene.createSequenceConstraint("mKate_gene_constraint", RestrictionType.PRECEDES, "pConst", "mKate_cds");
		
		// Create ComponentDefinition for mKate protein
		doc.createComponentDefinition("mKate", version, ComponentDefinition.PROTEIN);

		// Create ComponentDefinition for Gal4VP16 coding sequence
		ComponentDefinition Gal4VP16_cds = doc.createComponentDefinition("Gal4VP16_cds", version, ComponentDefinition.DNA_REGION);
		Gal4VP16_cds.addRole(SequenceOntology.CDS);
		
		// Create ComponentDefintion for Gal4VP16 gene
		ComponentDefinition Gal4VP16_gene = doc.createComponentDefinition("Gal4VP16_gene", version, ComponentDefinition.DNA_REGION);
		Gal4VP16_gene.addRole(SequenceOntology.PROMOTER);
		Gal4VP16_gene.createSequenceConstraint("GAL4VP16_gene_constraint", RestrictionType.PRECEDES, "pConst", "Gal4VP16_cds");
		
		// Create ComponentDefintion for Gal4VP16 protein
		doc.createComponentDefinition("Gal4VP16", version, ComponentDefinition.PROTEIN);
		
		// Create ComponentDefinition for CRP_b promoter
		ComponentDefinition CRP_b = doc.createComponentDefinition("CRP_b", version, ComponentDefinition.DNA_REGION);
		CRP_b.addRole(SequenceOntology.PROMOTER);
		CRP_b.addSequence("CRP_b_seq");
		
		// Create ComponentDefintiion for EYFP coding sequence
		ComponentDefinition EYFP_cds = doc.createComponentDefinition("EYFP_cds", version, ComponentDefinition.DNA_REGION);
		EYFP_cds.addRole(SequenceOntology.CDS);
		
		// Create ComponentDefinition for EYFP gene
		ComponentDefinition EYFP_gene = doc.createComponentDefinition("EYFP_gene", version, ComponentDefinition.DNA_REGION);
		EYFP_gene.addRole(SequenceOntology.PROMOTER);
		EYFP_gene.createSequenceConstraint("EYFP_gene_constraint", RestrictionType.PRECEDES, "CRP_b", "EYFP_cds");

		// Create ComponentDefintiion for EYFP protein
		doc.createComponentDefinition("EYFP", version, ComponentDefinition.PROTEIN);

		// Create ModuleDefintion for CRISPR Repression
		ModuleDefinition CRPb_circuit = doc.createModuleDefinition("CRPb_characterization_circuit", version);
		
		// Create the FunctionalComponents for the ModuleDefinition CRISPR_Repression
		CRPb_circuit.createFunctionalComponent("cas9m_BFP", AccessType.PRIVATE, "cas9m_BFP", version, DirectionType.NONE);
		CRPb_circuit.createFunctionalComponent("cas9m_BFP_gene", AccessType.PRIVATE, "cas9m_BFP_gene", version, DirectionType.NONE);
		
		CRPb_circuit.createFunctionalComponent("gRNA_b", AccessType.PRIVATE, "gRNA_b", version, DirectionType.NONE);
		CRPb_circuit.createFunctionalComponent("gRNA_b_gene", AccessType.PRIVATE, "gRNA_b_gene", version, DirectionType.NONE);

		CRPb_circuit.createFunctionalComponent("mKate", AccessType.PRIVATE, "mKate", version, DirectionType.NONE);
		CRPb_circuit.createFunctionalComponent("mKate_gene", AccessType.PRIVATE, "mKate_gene", version, DirectionType.NONE);

		CRPb_circuit.createFunctionalComponent("Gal4VP16", AccessType.PRIVATE, "Gal4VP16", version, DirectionType.NONE);
		CRPb_circuit.createFunctionalComponent("Gal4VP16_gene", AccessType.PRIVATE, "Gal4VP16_gene", version, DirectionType.NONE);

		CRPb_circuit.createFunctionalComponent("EYFP", AccessType.PRIVATE, "EYFP", version, DirectionType.NONE);
		CRPb_circuit.createFunctionalComponent("EYFP_gene", AccessType.PRIVATE, "EYFP_gene", version, DirectionType.NONE);

		CRPb_circuit.createFunctionalComponent("cas9m_BFP_gRNA_b", AccessType.PRIVATE,"cas9m_BFP_gRNA_b", version, DirectionType.NONE);

		/* Production of mKate from the mKate gene */
		Interaction mKate_production = CRPb_circuit.createInteraction("mKate_production", SystemsBiologyOntology.GENETIC_PRODUCTION);
		mKate_production.createParticipation("mKate", "mKate",SystemsBiologyOntology.PRODUCT);
		mKate_production.createParticipation("mKate_gene", "mKate_gene",SystemsBiologyOntology.PROMOTER);

		// Production of GAL4VP16 from the GAL4VP16 gene
		Interaction GAL4VP16_production = CRPb_circuit.createInteraction("Gal4VP16_production", SystemsBiologyOntology.GENETIC_PRODUCTION);
		GAL4VP16_production.createParticipation("Gal4VP16_gene", "Gal4VP16_gene",SystemsBiologyOntology.PROMOTER);
		GAL4VP16_production.createParticipation("Gal4VP16", "Gal4VP16",SystemsBiologyOntology.PRODUCT);

		// Production of cas9m_BFP from the cas9m_BFP gene
		Interaction cas9m_BFP_production = CRPb_circuit.createInteraction("cas9m_BFP_production", SystemsBiologyOntology.GENETIC_PRODUCTION);
		cas9m_BFP_production.createParticipation("cas9m_BFP_gene", "cas9m_BFP_gene",SystemsBiologyOntology.PROMOTER);
		cas9m_BFP_production.createParticipation("cas9m_BFP", "cas9m_BFP",SystemsBiologyOntology.PRODUCT);

		// Production of gRNA_b from the gRNA_b gene
		Interaction gRNA_b_production = CRPb_circuit.createInteraction("gRNA_b_production", SystemsBiologyOntology.GENETIC_PRODUCTION);
		gRNA_b_production.createParticipation("gRNA_b_gene", "gRNA_b_gene",SystemsBiologyOntology.PROMOTER);
		gRNA_b_production.createParticipation("gRNA_b", "gRNA_b",SystemsBiologyOntology.PRODUCT);
		
		// Activation of EYFP production by GAL4VP16
		Interaction EYFP_Activation = CRPb_circuit.createInteraction("EYFP_Activation", SystemsBiologyOntology.STIMULATION);
		EYFP_Activation.createParticipation("Gal4VP16", "Gal4VP16",SystemsBiologyOntology.STIMULATOR);
		EYFP_Activation.createParticipation("EYFP_gene", "EYFP_gene",SystemsBiologyOntology.PROMOTER);
		
		// Degradation of mKate
		Interaction mKate_deg = CRPb_circuit.createInteraction("mKate_deg", SystemsBiologyOntology.DEGRADATION);
		mKate_deg.createParticipation("mKate", "mKate",SystemsBiologyOntology.REACTANT);
		
		// Degradation of GAL4VP16
		Interaction GAL4VP16_deg = CRPb_circuit.createInteraction("Gal4VP16_deg", SystemsBiologyOntology.DEGRADATION);
		GAL4VP16_deg.createParticipation("Gal4VP16", "Gal4VP16",SystemsBiologyOntology.REACTANT);
		
		// Degradation of cas9m_BFP
		Interaction cas9m_BFP_deg = CRPb_circuit.createInteraction("cas9m_BFP_deg", SystemsBiologyOntology.DEGRADATION);
		cas9m_BFP_deg.createParticipation("cas9m_BFP", "cas9m_BFP",SystemsBiologyOntology.REACTANT);
		
		// Degradation of gRNA_b
		Interaction gRNA_b_deg = CRPb_circuit.createInteraction("gRNA_b_deg", SystemsBiologyOntology.DEGRADATION);
		gRNA_b_deg.createParticipation("gRNA_b", "gRNA_b",SystemsBiologyOntology.REACTANT);
		
		// Degradation of EYFP
		Interaction EYFP_deg = CRPb_circuit.createInteraction("EYFP_deg", SystemsBiologyOntology.DEGRADATION);
		EYFP_deg.createParticipation("EYFP", "EYFP",SystemsBiologyOntology.REACTANT);
		
		// Degradation of cas9m_BFP_gRNA_b
		Interaction cas9m_BFP_gRNA_b_deg = CRPb_circuit.createInteraction("cas9m_BFP_gRNA_b_deg", SystemsBiologyOntology.DEGRADATION);
		cas9m_BFP_gRNA_b_deg.createParticipation("cas9m_BFP_gRNA_b", "cas9m_BFP_gRNA_b",SystemsBiologyOntology.REACTANT);
		
		// Create Template Module
		Module Template_Module = CRPb_circuit.createModule("CRISPR_Template", "CRISPR_Template", version);
		
		// Add MapsTos to Template Module 
		Template_Module.createMapsTo("cas9m_BFP_map", RefinementType.USELOCAL, "cas9m_BFP", "cas9_generic");
		Template_Module.createMapsTo("gRNA_b_map", RefinementType.USELOCAL, "gRNA_b", "gRNA_generic");
		Template_Module.createMapsTo("cas9m_BFP_gRNA_map", RefinementType.USELOCAL, "cas9m_BFP_gRNA_b", "cas9_gRNA_complex");
		Template_Module.createMapsTo("EYFP_map", RefinementType.USELOCAL, "EYFP", "target");
		Template_Module.createMapsTo("EYFP_gene_map", RefinementType.USELOCAL, "EYFP_gene", "target_gene");
		
		// Create CombinatorialDerivations
		CombinatorialDerivation Combinatorial_Derivation = doc.createCombinatorialDerivation("combDev", version, 
				"Gal4VP16", version);
		Combinatorial_Derivation.setStrategy(StrategyType.SAMPLE);
		 
		
		SBOLValidate.validateSBOL(doc, true, true, true);
		
		if (SBOLValidate.getNumErrors() > 0) {
			for (String error : SBOLValidate.getErrors()) {
				System.out.println(error);
			}
			fail();
		}
	}
	
	/*@Test
	public void ReadWriteTest() throws SBOLValidationException, IOException, SBOLConversionException {
		doc.write("CombinatorialDesign.rdf");

		SBOLDocument in = SBOLReader.read("CombinatorialDesign.rdf");
		
		SBOLValidate.compareDocuments("read-in file", in, "built file", doc);
		
		System.out.println(SBOLValidate.getErrors());
		
		assertTrue(SBOLValidate.getErrors().isEmpty());
	}*/
	
}
