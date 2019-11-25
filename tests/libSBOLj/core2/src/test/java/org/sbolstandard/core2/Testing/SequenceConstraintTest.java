package org.sbolstandard.core2.Testing;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SequenceConstraint;

public class SequenceConstraintTest {
	private String prURI="http://partsregistry.org";
	private SBOLDocument repression_doc = null;
	
	@Before
	public void setUp() throws Exception {
		repression_doc = new SBOLDocument();
		repression_doc.setDefaultURIprefix(prURI);
		repression_doc.setTypesInURIs(false);
		repression_doc.setComplete(true);
		InputStream docAsStream = SequenceConstraintTest.class.getResourceAsStream("/SBOLTestSuite/SBOL2/RepressionModel.xml");
		repression_doc.read(docAsStream);
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void test_toString() throws URISyntaxException
	{
		assertNotNull(repression_doc);
		URI gRNA_gene = new URI("http://sbols.org/CRISPR_Example/gRNA_b_gene/1.0");
		ComponentDefinition gRNA_b_gene = repression_doc.getComponentDefinition(gRNA_gene);
		assertNotNull(gRNA_b_gene);
		assertTrue(gRNA_b_gene.getSequenceConstraints().size() == 2);
		SequenceConstraint gRNA_b_gene_constraint1 = gRNA_b_gene.getSequenceConstraint("gRNA_b_gene_constraint1");
		assertTrue(gRNA_b_gene_constraint1.toString().length() != 0);
		assertNotNull(gRNA_b_gene_constraint1.toString());
		assertTrue(gRNA_b_gene_constraint1.toString().contains("identity="));
		assertTrue(gRNA_b_gene_constraint1.toString().contains("displayId="));
		assertFalse(gRNA_b_gene_constraint1.toString().contains("name="));
		assertFalse(gRNA_b_gene_constraint1.toString().contains("description="));
		assertTrue(gRNA_b_gene_constraint1.toString().contains("restriction="));
		assertTrue(gRNA_b_gene_constraint1.toString().contains("subject="));
		assertTrue(gRNA_b_gene_constraint1.toString().contains("object="));
		
	}
	
}
