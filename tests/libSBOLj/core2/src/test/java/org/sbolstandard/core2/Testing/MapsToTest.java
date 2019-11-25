package org.sbolstandard.core2.Testing;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.MapsTo;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

public class MapsToTest {
	private SBOLDocument doc = null;
	private ComponentDefinition gRNA_b_gene = null;
	private ComponentDefinition gene_CD = null;
	private Component gene = null;
	private Component target_gene = null;
	private Component protein = null;

	@Before
	public void setUp() throws Exception {
		String prURI="http://partsregistry.org";
		doc = new SBOLDocument();
		doc.setDefaultURIprefix(prURI);
		doc.setTypesInURIs(false);
		doc.setComplete(true);
		/*create CD's for main CD and sub-components*/
		gRNA_b_gene = doc.createComponentDefinition("gRNA_b_gene", "", ComponentDefinition.DNA_REGION);
		doc.createComponentDefinition("promoter_CD", "", ComponentDefinition.DNA_REGION);
		gene_CD = doc.createComponentDefinition("gene_CD", "", ComponentDefinition.DNA_REGION);
		doc.createComponentDefinition("terminator_CD", "", ComponentDefinition.DNA_REGION);
		
		gRNA_b_gene.createComponent("promoter", AccessType.PUBLIC, "promoter_CD");
		gene = gRNA_b_gene.createComponent("gene", AccessType.PUBLIC, "gene_CD");
		gRNA_b_gene.createComponent("terminator", AccessType.PUBLIC, "terminator_CD");
		
		doc.createComponentDefinition("target_gene_CD", "", ComponentDefinition.DNA_REGION);
		target_gene = gRNA_b_gene.createComponent("target_gene", AccessType.PUBLIC, "target_gene_CD");
		
		doc.createComponentDefinition("target_protein", ComponentDefinition.DNA_REGION);
		protein = gene_CD.createComponent("protein", AccessType.PUBLIC, "target_protein");
	}

	@After
	public void tearDown() throws Exception {
		
	}


	@Test
	public void test_toString() throws SBOLValidationException
	{
		MapsTo geneMapsTo = gene.createMapsTo("local_gene", RefinementType.USELOCAL, target_gene.getDisplayId(), protein.getDisplayId());
		assertTrue(geneMapsTo.toString().length() != 0);
		assertNotNull(geneMapsTo.toString());
		assertTrue(geneMapsTo.toString().contains("identity="));
		assertTrue(geneMapsTo.toString().contains("displayId="));
		assertFalse(geneMapsTo.toString().contains("name="));
		assertFalse(geneMapsTo.toString().contains("description="));
		assertTrue(geneMapsTo.toString().contains("refinement="));
		assertTrue(geneMapsTo.toString().contains("local="));
		assertTrue(geneMapsTo.toString().contains("remote="));

	}
	

}
