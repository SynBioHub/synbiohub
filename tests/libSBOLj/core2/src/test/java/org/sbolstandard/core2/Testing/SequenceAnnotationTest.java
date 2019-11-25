package org.sbolstandard.core2.Testing;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Cut;
import org.sbolstandard.core2.GenericLocation;
import org.sbolstandard.core2.Location;
import org.sbolstandard.core2.OrientationType;
import org.sbolstandard.core2.Range;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceOntology;

public class SequenceAnnotationTest {
	private SBOLDocument doc = null;
	private ComponentDefinition gRNA_b_gene = null;
	private ComponentDefinition promoter_CD = null;
	private SequenceAnnotation promoter_SA = null;
	private SequenceAnnotation gene_SA = null;
	private SequenceAnnotation terminator_SA = null;

	@Before
	public void setUp() throws Exception 
	{
		String prURI="http://partsregistry.org";
		doc = new SBOLDocument();
		doc.setDefaultURIprefix(prURI);
		doc.setTypesInURIs(false);
		doc.setComplete(true);
		/*create CD's for main CD and sub-components*/
		gRNA_b_gene = doc.createComponentDefinition("gRNA_b_gene", "", ComponentDefinition.DNA_REGION);
		promoter_CD = doc.createComponentDefinition("promoter_CD", "", ComponentDefinition.DNA_REGION);
		doc.createComponentDefinition("gene_CD", "", ComponentDefinition.DNA_REGION);
		doc.createComponentDefinition("terminator_CD", "", ComponentDefinition.DNA_REGION);
		gRNA_b_gene.createComponent("promoter", AccessType.PUBLIC, "promoter_CD");
		gRNA_b_gene.createComponent("gene", AccessType.PUBLIC, "promoter_CD");
		gRNA_b_gene.createComponent("terminator", AccessType.PUBLIC, "promoter_CD");
		
		/*create SequenceAnnotations*/
		promoter_SA = gRNA_b_gene.createSequenceAnnotation("promoter_SA", "cutAt1");
		promoter_SA.setComponent("promoter");
		gene_SA = gRNA_b_gene.createSequenceAnnotation("gene_SA", "cutAt50");
		terminator_SA = gRNA_b_gene.createSequenceAnnotation("terminator_SA", "cutAt100");

	}

	@After
	public void tearDown() throws Exception {
		
	}


	@Test
	public void test_locationMethods() throws SBOLValidationException{
		Cut promoter_cut = promoter_SA.addCut("promoter_cut", 1);
		assertTrue(gRNA_b_gene.getSequenceAnnotation("promoter_SA").getLocation("promoter_cut").equals(promoter_cut));
		promoter_cut.unsetOrientation();
		assertNull(promoter_cut.getOrientation());
		Location test = promoter_cut; 
		assertNotNull(test.toString());
		Cut terminator_cut = terminator_SA.addCut("terminator_cut", 100);
		assertTrue(gRNA_b_gene.getSequenceAnnotation("terminator_SA").getLocation("terminator_cut").equals(terminator_cut));
		Cut gene_cut = gene_SA.addCut("gene_cut", 50, OrientationType.INLINE);
		assertTrue(gRNA_b_gene.getSequenceAnnotation("gene_SA").getLocation("gene_cut").equals(gene_cut));
		
		gene_SA.removeLocation(gene_cut);
		assertNull(gene_SA.getLocation("gene_cut"));
		
		Range gene_range = gene_SA.addRange("gene_range",50,99);
		assertTrue(gRNA_b_gene.getSequenceAnnotation("gene_SA").getLocation("gene_range").equals(gene_range));
		gene_range.unsetOrientation();
		assertNull(gene_range.getOrientation());
		promoter_SA.removeLocation(promoter_cut);
		assertNull(promoter_SA.getLocation(promoter_cut.getIdentity()));

		GenericLocation promoter_glocation = promoter_SA.addGenericLocation("promoter_glocation");
		assertTrue(gRNA_b_gene.getSequenceAnnotation("promoter_SA").getLocation("promoter_glocation").equals(promoter_glocation));
		
		terminator_SA.removeLocation(terminator_cut);
		assertNull(terminator_SA.getLocation("terminator_cut"));

		GenericLocation terminator_glocation = terminator_SA.addGenericLocation("terminator_glocation", OrientationType.INLINE);
		assertTrue(gRNA_b_gene.getSequenceAnnotation("terminator_SA").getLocation("terminator_glocation").equals(terminator_glocation));
	}
	
	@Test
	public void test_roleMethod() throws SBOLValidationException
	{
		assertEquals(promoter_SA.getRoles().size(), 0);
		promoter_SA.unsetComponent();
		assertTrue(promoter_SA.addRole(SequenceOntology.PROMOTER));
		promoter_SA.addRole(SequenceOntology.PROMOTER);
		assertFalse(promoter_SA.getRoles().size() == 2);
		assertEquals(promoter_SA.getRoles().size(), 1);
		assertTrue(promoter_SA.containsRole(SequenceOntology.PROMOTER));
		promoter_SA.removeRole(SequenceOntology.PROMOTER);
		assertFalse(promoter_SA.containsRole(SequenceOntology.PROMOTER));
		promoter_SA.clearRoles();
		assertEquals(promoter_SA.getRoles().size(), 0);
	}
	@Test
	public void test_getCD()
	{
		assertTrue(promoter_SA.getComponentDefinition().equals(promoter_CD));
		
	}
	
	@Test
	public void test_unsetComponent(){
		promoter_SA.unsetComponent();
		assertNull(promoter_SA.getComponent());
	}
	
	@Test
	public void test_toString()
	{
		assertTrue(promoter_SA.toString().length() != 0);
		assertNotNull(promoter_SA.toString());
		assertTrue(!promoter_SA.toString().contains("version="));
		assertTrue(!promoter_SA.toString().contains("name="));
		
	}
	
	/*
	 * 
	 * move to Location Test class
	 */
	@Test
	public void test_LocToString() throws SBOLValidationException
	{
		Location test = promoter_SA.addGenericLocation("promoter_cut");
		assertTrue(test.toString().length() != 0);
		assertNotNull(test.toString());
		assertTrue(test.toString().contains("identity="));
		assertTrue(test.toString().contains("displayId="));
		assertTrue(!test.toString().contains("description="));
		assertTrue(!test.toString().contains("orientation="));
		assertTrue(!test.toString().contains("name="));

	}

}
