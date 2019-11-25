package org.sbolstandard.core2.Testing;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.*;

/**
 * Tests for ComponentDefinition methods.
 * @author Meher Samineni
 *
 */
public class ComponentDefinitionTest {

	private SBOLDocument doc = null;
	private ComponentDefinition gRNA_b_gene = null;
	private String CRa_U6_seq = "GGTTTACCGAGCTCTTATTGGTTTTCAAACTTCATTGACTGTGCC";
	private HashSet <URI> types = null; 
	private Sequence generic_seq = null;
	@Before
	public void setUp() throws Exception {
				//actually add stuff to the doc. like cds, mds, seqs, modules, seqAnns, seqConstraints, etc....
				String prURI="http://partsregistry.org";
				doc = new SBOLDocument();
				doc.setDefaultURIprefix(prURI);
				doc.setTypesInURIs(false);
				doc.setComplete(true);
				types = new HashSet <URI >(Arrays.asList(ComponentDefinition.DNA_REGION));
				doc.createComponentDefinition("TetR_promoter", types); 
				generic_seq = doc.createSequence("generic_seq", "ttgacagctagctcagtcctaggtataatgctagc", Sequence.IUPAC_DNA);
				gRNA_b_gene = doc.createComponentDefinition("gRNA_b_gene", "", ComponentDefinition.DNA_REGION);
				gRNA_b_gene.addSequence(generic_seq);
		
	}
	
	@Test
	public void test_CD_addSequence() throws SBOLValidationException {
		
		doc.createComponentDefinition("CRa_U6","",ComponentDefinition.DNA_REGION);
		Sequence gen_seq = doc.createSequence("CRa_U6_seq", CRa_U6_seq, Sequence.IUPAC_DNA);
		doc.getComponentDefinition("CRa_U6", "").addSequence(gen_seq);
		assertTrue(doc.getComponentDefinition("CRa_U6", "").getSequences().size() == 1);
		int size = doc.getSequence("CRa_U6_seq", "").toString().length();
		assertTrue(size != 0);
		
	} 
	@Test
	 /* CD.createComponent(String, AccessType, String)
	 * Add a promoter component to a gene, get the promoter, remove the promoter
	 * toString the gene
	 */
	public void test_CD_methods() throws SBOLValidationException
	{
		/*test ComponentMethods*/
		
		//create 
		doc.createComponentDefinition("gRNA_gene_promoter_comp", ComponentDefinition.DNA_REGION);
		Component gRNA_promoter = gRNA_b_gene.createComponent("gRNA_gene_promoter", AccessType.PUBLIC, "gRNA_gene_promoter_comp");
		
		//check that the component exists
		assertNotNull(doc.getComponentDefinition("gRNA_b_gene", "").getComponent("gRNA_gene_promoter"));
		//check that the component retrieved matches the one made from createComponent)
		equals(doc.getComponentDefinition("gRNA_b_gene", "").getComponent("gRNA_gene_promoter") == gRNA_promoter);
		//remove promoter Component added
		doc.getComponentDefinition("gRNA_b_gene", "").removeComponent(gRNA_promoter);
		
		//check that component was removed from document
		assertNull(doc.getComponentDefinition("gRNA_b_gene", "").getComponent("gRNA_gene_promoter"));	
		
	} 
	/*
	 * Check toString method for CD class
	 */
	@Test
	public void test_toString() 
	{
		assertTrue(gRNA_b_gene.toString().length() != 0);
		assertNotNull(gRNA_b_gene.toString());
		assertTrue(!gRNA_b_gene.toString().contains("version="));
		assertTrue(!gRNA_b_gene.toString().contains("name="));

	}
	@Test
	public void test_createSeqAnnot_CD() throws SBOLValidationException
	{
		doc.createComponentDefinition("gRNA_gene_promoter_comp", ComponentDefinition.DNA_REGION);
		
		gRNA_b_gene.createComponent("gRNA_gene_promoter", AccessType.PUBLIC, "gRNA_gene_promoter_comp");
		
		//add SequenceAnnotation for CD with a displayID and LocationID
		SequenceAnnotation promoter_annot = gRNA_b_gene.createSequenceAnnotation("cutAt5", "cut1");
		assertNotNull(gRNA_b_gene.getSequenceAnnotation("cutAt5"));
		gRNA_b_gene.createSequenceAnnotation("cutAt10", "cut2");
		//remove SeqAnnotation for promoter
		assertTrue(gRNA_b_gene.removeSequenceAnnotation(promoter_annot));
		assertNull(gRNA_b_gene.getSequenceAnnotation("cutAt5"));
		
		//add SeqAnn for CD with displayId, locationID, int?
		promoter_annot = gRNA_b_gene.createSequenceAnnotation("cutAt5", "cut1", 5);
		assertNotNull(gRNA_b_gene.getSequenceAnnotation("cutAt5"));
		
		gRNA_b_gene.createSequenceAnnotation("cutAt6", "cut2", OrientationType.INLINE);		
		
	}
	
	@Test
	public void test_privateConst_SQ() throws SBOLValidationException
	{
		doc.createComponentDefinition("gRNA_gene_promoter_comp", ComponentDefinition.DNA_REGION);
		gRNA_b_gene.createComponent("gRNA_gene_promoter", AccessType.PUBLIC, "gRNA_gene_promoter_comp");
		
		//add SequenceAnnotation for CD with a displayID and LocationID
		gRNA_b_gene.createSequenceAnnotation("cutAt5", "cut1");
		gRNA_b_gene.createSequenceAnnotation("cutAt6", "cut2", 5);
		gRNA_b_gene.createSequenceAnnotation("cutAt7", "cut3", OrientationType.INLINE);		

		assertNotNull(gRNA_b_gene.getSequenceAnnotation("cutAt5"));
		ComponentDefinition test = (ComponentDefinition) doc.createCopy(gRNA_b_gene, "gRNA_b_gene_copy", "");
		assertTrue(test.getSequenceAnnotations().size() == 3);
	}
	
	@Test
	public void test_seqConstraint_CD() throws SBOLValidationException
	{
		ComponentDefinition gRNA_gene_promoter_comp = doc.createComponentDefinition("gRNA_gene_promoter_comp", ComponentDefinition.DNA_REGION);
		ComponentDefinition gRNA_gene_terminator_comp = doc.createComponentDefinition("gRNA_gene_terminator_comp", ComponentDefinition.DNA_REGION);

		gRNA_b_gene.createComponent("gRNA_gene_promoter", AccessType.PUBLIC, "gRNA_gene_promoter_comp");
		gRNA_b_gene.createComponent("gRNA_terminator", AccessType.PUBLIC, "gRNA_gene_terminator_comp");
		SequenceConstraint promoter_first = gRNA_b_gene.createSequenceConstraint("promoter_first", RestrictionType.PRECEDES, "gRNA_gene_promoter", "gRNA_terminator");
		//check that added SeqConstraint exists with getSeqConst(displayID)
		assertNotNull(gRNA_b_gene.getSequenceConstraint("promoter_first"));

		assertTrue(promoter_first.getObjectDefinition().equals(gRNA_gene_terminator_comp));
		assertTrue(promoter_first.getSubjectDefinition().equals(gRNA_gene_promoter_comp));
		assertTrue(promoter_first.equals(gRNA_b_gene.getSequenceConstraint("promoter_first")));
		//check added SeqConstraint exists with getSeqConst(URI)
		URI seq_const = promoter_first.getObjectURI();
		assertFalse(promoter_first.equals(gRNA_b_gene.getSequenceConstraint(seq_const)));	
		
		//remove added SeqConstraint
		gRNA_b_gene.removeSequenceConstraint(promoter_first);
		//check that seqConst was removed
		assertNull(gRNA_b_gene.getSequenceConstraint("promoter_first"));
	}
	

	@Test
	public void test_sortedComponents_CD() throws SBOLValidationException
	{
		/*not actually testing sorting components*/
		/*sorting by: create several seqAnnotation, cuts, constraints, then check the sorted
		 * based on the annotations and constraints*/
		/*test: a precedes b and b precedes c*/
		/*interesting test: have a seqAnnotation and Constraints conflict*/
		
		doc.createComponentDefinition("gRNA_gene_promoter_comp", ComponentDefinition.DNA_REGION);
		doc.createComponentDefinition("gRNA_gene_terminator_comp", ComponentDefinition.DNA_REGION);
		doc.createComponentDefinition("gRNA_gene_gene_comp", ComponentDefinition.DNA_REGION);

		gRNA_b_gene.createComponent("gRNA_gene_promoter", AccessType.PUBLIC, "gRNA_gene_promoter_comp");
		gRNA_b_gene.createComponent("gRNA_gene", AccessType.PUBLIC, "gRNA_gene_gene_comp");
		gRNA_b_gene.createComponent("gRNA_gene_terminator", AccessType.PUBLIC, "gRNA_gene_terminator_comp");
		assertTrue(gRNA_b_gene.getSortedComponents().size() == 3);
		
		/*same type of annotations */
		//add SequenceAnnotation for CD with a displayID and LocationID
		gRNA_b_gene.createSequenceAnnotation("cutAt1", "cut1");
		gRNA_b_gene.createSequenceAnnotation("cutAt5", "cut2", 5);
		gRNA_b_gene.createSequenceAnnotation("cutAt10", "cut3", OrientationType.INLINE);	
		
		List<Component> sorted_Cuts = gRNA_b_gene.getSortedComponents();
		//System.out.println(sorted_Cuts.size());
		
		/*
		doc.createComponentDefinition("gRNA_gene_promoter_comp", ComponentDefinition.DNA);
		Component promoter = gRNA_b_gene.createComponent("gRNA_gene_promoter", AccessType.PUBLIC, "gRNA_gene_promoter_comp");		
		Component terminator = null;
		assertTrue(gRNA_b_gene.getSortedComponents().contains(promoter));
		assertFalse(gRNA_b_gene.getSortedComponents().contains(terminator));
		doc.createComponentDefinition("gRNA_gene_terminator_comp", ComponentDefinition.DNA);
		terminator = gRNA_b_gene.createComponent("gRNA_terminator", AccessType.PUBLIC, "gRNA_gene_terminator_comp");
		assertTrue(gRNA_b_gene.getSortedComponents().contains(terminator));
*/
	} 

	
	
//	
//	@Test
//	public void addType_CD() throws URISyntaxException, SBOLValidationException
//	{
//
//		TetR_promoter = new ComponentDefinition(new URI("http://partsregistry.org"), types);
//
//		try{TetR_promoter.addType(ComponentDefinition.DNA);fail();} 
//		catch(SBOLValidationException e){}
//		
//		types = new HashSet <URI >(Arrays.asList(ComponentDefinition.RNA));
//		TetR_promoter = new ComponentDefinition(new URI("http://partsregistry.org"), types);
//
//		try{TetR_promoter.addType(ComponentDefinition.RNA);fail();}
//		catch(SBOLValidationException e){}
//		
//		types = new HashSet <URI >(Arrays.asList(ComponentDefinition.PROTEIN));
//		TetR_promoter = new ComponentDefinition(new URI("http://partsregistry.org"), types);
//
//		try{TetR_promoter.addType(ComponentDefinition.PROTEIN);fail();}
//		catch(SBOLValidationException e){}
//		
//		types = new HashSet <URI >(Arrays.asList(ComponentDefinition.SMALL_MOLECULE));
//		TetR_promoter = new ComponentDefinition(new URI("http://partsregistry.org"), types);
//
//		try{TetR_promoter.addType(ComponentDefinition.SMALL_MOLECULE); fail();}
//		catch(SBOLValidationException e){}
//		
//		types = new HashSet <URI >(Arrays.asList(ComponentDefinition.SMALL_MOLECULE));
//		TetR_promoter = new ComponentDefinition(new URI("http://partsregistry.org"), types);
//
//		try{assertTrue(TetR_promoter.addType(ComponentDefinition.DNA));}
//		catch(SBOLValidationException e){}
//		
//		//else a SBOLDocument can't be null --> this is checked further up the hierarchy
//	}
//	
//		@Test
//		public void removeType_CD() throws URISyntaxException, SBOLValidationException
//		{
//			TetR_promoter = new ComponentDefinition(new URI("http://partsregistry.org"), types);
//			try{TetR_promoter.removeType(ComponentDefinition.DNA);fail();}
//			catch(SBOLValidationException e){}
//			TetR_promoter.addType( URI.create("http://identifiers.org/chebi/CHEBI:4705"));
//			assertTrue(TetR_promoter.removeType(ComponentDefinition.DNA));
//		}
//		
//		@Test
//		public void removeRole_CD() throws URISyntaxException, SBOLValidationException
//		{
//			TetR_promoter = new ComponentDefinition(new URI("http://partsregistry.org"), types);
//			URI promoter_role = new URI("http://identifiers.org/so/SO:0000167");
//			assertTrue(TetR_promoter.addRole(promoter_role));
//			assertTrue(TetR_promoter.removeRole(promoter_role));
//			
//		}
//		
//		@Test
//		public void containsRole_CD() throws URISyntaxException, SBOLValidationException
//		{
//			URI promoter_role = new URI("http://identifiers.org/so/SO:0000167");
//			assertTrue(TetR_promoter.addRole(promoter_role));
//			assertTrue(TetR_promoter.containsRole(promoter_role));
//		}
//		
//		@Test 
//		public void addSeq_CD() throws URISyntaxException, SBOLValidationException 
//		{			
//			doc.addComponentDefinition(TetR_promoter);
//			try{assertTrue(TetR_promoter.addSequence(s));}
//			catch(SBOLValidationException e){}
//			
//			try{TetR_promoter.addSequence(new Sequence(null, null, null));fail();	}
//			catch(SBOLValidationException e){}
//			ComponentDefinition TetR_promoter2 = new ComponentDefinition(new URI("http://partsregistry.org"), types);
//			try{assertTrue(TetR_promoter2.addSequence(s));}
//			catch(SBOLValidationException e){}
//			
//		} 
//		
//		@Test
//		public void removeSeq_CD() throws URISyntaxException, SBOLValidationException
//		{		
//			TetR_promoter.addSequence(s);
//			//case 1: document1 is null
//			assertTrue(TetR_promoter.containsSequence(Sequence.IUPAC_DNA));
//			assertTrue(TetR_promoter.removeSequence(Sequence.IUPAC_DNA));	
//			TetR_promoter.clearSequences();
//			//assertTrue(TetR_promoter.containsSequence(Sequence.IUPAC_PROTEIN));
//			//assertFalse(TetR_promoter.containsSequence(Sequence.IUPAC_RNA));
//			//assertTrue(TetR_promoter.removeSequence(Sequence.IUPAC_PROTEIN));	
//			//assertTrue(TetR_promoter.getSequences().size() == 0);
//		}
//		
//		
//		
//
	@After
	public void tearDown() throws Exception 
	{
	}


}
