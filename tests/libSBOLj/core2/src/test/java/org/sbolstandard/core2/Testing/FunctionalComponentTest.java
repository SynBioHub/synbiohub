package org.sbolstandard.core2.Testing;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.MapsTo;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

public class FunctionalComponentTest {
	private SBOLDocument doc = null;
	private SBOLDocument copy_doc = null;

	private ComponentDefinition gRNA_b_gene = null;
	private ModuleDefinition CRISPR_Template = null;
	private ComponentDefinition target_gene = null;
	private FunctionalComponent gRNA_b_gene_fc = null;
	private FunctionalComponent target_gene_fc = null;
	private ComponentDefinition target_protein = null;
	private Component protein = null;
	
	private MapsTo tar_protein = null;
	@Before
	public void setUp() throws Exception {
		String prURI="http://partsregistry.org";
		doc = new SBOLDocument();
		doc.setDefaultURIprefix(prURI);
		doc.setTypesInURIs(false);
		doc.setComplete(true);
		
		copy_doc = new SBOLDocument();
		copy_doc.setDefaultURIprefix(prURI);
		copy_doc.setTypesInURIs(false);
		copy_doc.setComplete(true);
		
		gRNA_b_gene = doc.createComponentDefinition("gRNA_b_gene", ComponentDefinition.DNA_REGION);
		target_gene = doc.createComponentDefinition("target_gene", ComponentDefinition.DNA_REGION);
		CRISPR_Template = doc.createModuleDefinition("CRISPR_Template");
		gRNA_b_gene_fc = CRISPR_Template.createFunctionalComponent("gRNA_b_gene_fc", AccessType.PUBLIC, "gRNA_b_gene", DirectionType.OUT);
		target_gene_fc = CRISPR_Template.createFunctionalComponent("target_gene_fc", AccessType.PUBLIC, "target_gene", DirectionType.NONE);

		target_protein = doc.createComponentDefinition("target_protein", ComponentDefinition.DNA_REGION);
		protein = gRNA_b_gene.createComponent("protein", AccessType.PUBLIC, "target_protein");
		tar_protein = gRNA_b_gene_fc.createMapsTo("target_protein_mapsTo", RefinementType.USELOCAL, "target_gene_fc", "protein");

	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void test_MapsToMethods() throws SBOLValidationException
	{
	assertTrue(gRNA_b_gene_fc.getMapsTos().size() == 1);
	gRNA_b_gene_fc.clearMapsTos();
	assertTrue(gRNA_b_gene_fc.getMapsTos().size() == 0);
	tar_protein = gRNA_b_gene_fc.createMapsTo("target_protein_mapsTo", RefinementType.USELOCAL, target_gene_fc.getIdentity(), protein.getIdentity());
	assertTrue(gRNA_b_gene_fc.getMapsTos().size() == 1);
	assertTrue(gRNA_b_gene_fc.getMapsTo("target_protein_mapsTo").equals(tar_protein));
	assertTrue(gRNA_b_gene_fc.getMapsTo(tar_protein.getIdentity()).equals(tar_protein));
	assertTrue(gRNA_b_gene_fc.removeMapsTo(tar_protein));
	assertTrue(gRNA_b_gene_fc.getMapsTos().size() == 0);
	tar_protein = gRNA_b_gene_fc.createMapsTo("target_protein_mapsTo", RefinementType.USELOCAL, target_gene_fc.getIdentity(), protein.getIdentity());

	assertTrue(tar_protein.getLocalDefinition().equals(target_gene));
	assertTrue(tar_protein.getRemoteDefinition().equals(target_protein));
	}
	
	/*@Test
	public void test_deepCopy() throws SBOLValidationException
	{
		copy_doc.createCopy(doc);
		assertTrue(copy_doc.equals(doc));
	} */
	
	
}
