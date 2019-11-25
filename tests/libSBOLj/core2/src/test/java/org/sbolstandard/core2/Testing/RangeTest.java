package org.sbolstandard.core2.Testing;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Range;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SequenceAnnotation;

public class RangeTest {
	private SBOLDocument doc = null;
	private ComponentDefinition gRNA_b_gene = null;
	private SequenceAnnotation gene_SA = null;
	private Range gene_range = null;

	@Before
	public void setUp() throws Exception {
		String prURI="http://partsregistry.org";
		doc = new SBOLDocument();
		doc.setDefaultURIprefix(prURI);
		doc.setTypesInURIs(false);
		doc.setComplete(true);
		/*create CD's for main CD and sub-components*/
		gRNA_b_gene = doc.createComponentDefinition("gRNA_b_gene", "", ComponentDefinition.DNA_REGION);
		gene_SA = gRNA_b_gene.createSequenceAnnotation("gene_SA", "cutAt50");
		
		
		/*create SequenceAnnotations*/
		gene_range = gene_SA.addRange("gene_range",50,99);
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void test_toString()
	{
		assertTrue(gene_range.toString().length() != 0);
		assertNotNull(gene_range.toString());
		assertTrue(gene_range.toString().contains("identity="));
		assertTrue(gene_range.toString().contains("displayId="));
		assertFalse(gene_range.toString().contains("name="));
		assertFalse(gene_range.toString().contains("description="));
		assertTrue(gene_range.toString().contains("start="));
		assertTrue(gene_range.toString().contains("end="));
		
	}
	
}
