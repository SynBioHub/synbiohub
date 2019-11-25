package org.sbolstandard.core2.Testing;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Cut;
import org.sbolstandard.core2.OrientationType;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SequenceAnnotation;

public class CutTest {

	private SBOLDocument doc = null;
	private ComponentDefinition gRNA_b_gene = null;
	private SequenceAnnotation promoter_annot = null;
	private SequenceAnnotation terminator_annot = null;
	private Cut prom_cut = null;
	private Cut term_cut = null;
	
	@Before
	public void setUp() throws Exception {
		String prURI="http://partsregistry.org";
		doc = new SBOLDocument();
		doc.setDefaultURIprefix(prURI);
		doc.setTypesInURIs(false);
		doc.setComplete(true);
		
		gRNA_b_gene = doc.createComponentDefinition("gRNA_b_gene", ComponentDefinition.DNA_REGION);
		promoter_annot = gRNA_b_gene.createSequenceAnnotation("promoter_annot", "cut1");
		prom_cut = promoter_annot.addCut("cutAt5", 50, OrientationType.INLINE);
		terminator_annot = gRNA_b_gene.createSequenceAnnotation("cutAt10", "cut2");
		term_cut = terminator_annot.addCut("cutAt10", 100, OrientationType.INLINE);

	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void test_compareTo()
	{
		assertFalse(prom_cut.compareTo(term_cut) > 0);
		assertTrue(prom_cut.compareTo(term_cut) < 0);
		assertFalse(term_cut.compareTo(prom_cut) < 0);
		assertFalse(term_cut.compareTo(prom_cut) == 0);

	} 
	
	@Test
	public void test_toString()
	{
		assertTrue(prom_cut.toString().length() != 0);
		assertNotNull(prom_cut.toString());
		assertTrue(prom_cut.toString().contains("identity="));
		assertTrue(prom_cut.toString().contains("displayId="));
		assertTrue(!prom_cut.toString().contains("description="));
		assertTrue(!prom_cut.toString().contains("name="));
		assertTrue(prom_cut.toString().contains("orientation="));
	}
	
}
