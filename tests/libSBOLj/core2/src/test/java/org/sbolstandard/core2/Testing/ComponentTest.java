package org.sbolstandard.core2.Testing;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.MapsTo;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.RoleIntegrationType;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceOntology;

public class ComponentTest {
	private SBOLDocument doc = null;
	private ComponentDefinition gRNA_b_gene = null;
	private ComponentDefinition gene_CD = null;
	private Component promoter = null;
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
		
		/*create Components   */
		promoter = gRNA_b_gene.createComponent("promoter", AccessType.PUBLIC, "promoter_CD");
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
	public void test_roleMethods() throws SBOLValidationException
	{	
		/*add roles    */
		promoter.setRoleIntegration(RoleIntegrationType.OVERRIDEROLES);
		assertTrue(promoter.addRole(SequenceOntology.PROMOTER));
		assertTrue(promoter.getRoles().contains(SequenceOntology.PROMOTER));
		assertTrue(promoter.containsRole(SequenceOntology.PROMOTER));
		/*remove role*/
		promoter.removeRole(SequenceOntology.PROMOTER);
		assertFalse(promoter.containsRole(SequenceOntology.PROMOTER));
		/*clear Roles  */
		assertTrue(promoter.addRole(SequenceOntology.PROMOTER));
		promoter.clearRoles();
		assertFalse(promoter.containsRole(SequenceOntology.PROMOTER));
		
		Set<URI> promoter_roles = new HashSet<URI>();
		promoter_roles.add(SequenceOntology.PROMOTER);
		promoter.setRoles(promoter_roles);
		assertTrue(promoter.containsRole(SequenceOntology.PROMOTER));
	}
	
	@Test
	public void test_ComponentMapsTo() throws SBOLValidationException
	{
		MapsTo geneMapsTo = gene.createMapsTo("local_gene", RefinementType.USELOCAL, target_gene.getDisplayId(), protein.getDisplayId());
		assertTrue(gene.getMapsTos().size() == 1);
		assertNotNull(gene.getMapsTo("local_gene"));
		assertEquals(gene.getMapsTo("local_gene"), geneMapsTo);
		gene.removeMapsTo(geneMapsTo);
		assertTrue(gene.getMapsTos().size() == 0);
		geneMapsTo = gene.createMapsTo("local_gene", RefinementType.USELOCAL, target_gene.getIdentity(), protein.getIdentity());
		assertNotNull(gene.getMapsTo(geneMapsTo.getIdentity()));
		assertTrue(gene.getMapsTos().size() == 1);
		gene.clearMapsTos();
		assertTrue(gene.getMapsTos().size() == 0);
	} 
	
	
	@Test
	public void test_toString()
	{
		assertTrue(gene.toString().length() != 0);
		assertNotNull(gene.toString());
		assertTrue(gene.toString().contains("identity="));
		assertTrue(gene.toString().contains("displayId="));
		assertFalse(gene.toString().contains("name="));
		assertFalse(gene.toString().contains("description="));

	}
	
}
