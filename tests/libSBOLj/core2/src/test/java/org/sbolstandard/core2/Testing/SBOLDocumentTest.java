package org.sbolstandard.core2.Testing;

import static org.junit.Assert.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.xml.namespace.QName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Model;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SystemsBiologyOntology;
import org.sbolstandard.core2.TopLevel;

public class SBOLDocumentTest {
	private SBOLDocument doc = null;
	private TopLevel gRNA_b_gene = null;
	private String prURI="http://partsregistry.org";

	@Before
	public void setUp() throws Exception {
		doc = new SBOLDocument();
		doc.setDefaultURIprefix(prURI);
		doc.setTypesInURIs(false);
		doc.setComplete(true);
		
		 doc.createComponentDefinition("http://partsregistry.org", "gRNA_promoter", "", ComponentDefinition.DNA_REGION);
		 doc.createComponentDefinition(prURI, "CRa_promoter", "", ComponentDefinition.DNA_REGION);
		 doc.createComponentDefinition(prURI, "TetR_promoter","", ComponentDefinition.DNA_REGION); 
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test_docModelMethods() throws SBOLValidationException {
		Model model=doc.createModel(
				"pIKE_Toggle_1",
				"1.0",
				URI.create("http://virtualparts.org/part/pIKE_Toggle_1"),
				URI.create("http://identifiers.org/edam/format_2585"), 
				SystemsBiologyOntology.CONTINUOUS_FRAMEWORK); 
		assertTrue(doc.getModels().size() == 1);
		assertTrue(doc.getModel("pIKE_Toggle_1", "").equals(model));
		doc.clearModels();
		assertTrue(doc.getModels().size() == 0);		
	}
	
	@Test
	public void test_createTLmethods() throws SBOLValidationException
	{
		ComponentDefinition gRNA_b_gene = doc.createComponentDefinition("http://partsregistry.org", "gRNA_b_gene", "", ComponentDefinition.DNA_REGION);
		assertNotNull(gRNA_b_gene);
		assertTrue(doc.getComponentDefinition("gRNA_b_gene", "").equals(gRNA_b_gene));
	}
	
	@Test
	public void test_namespaceMethods() throws URISyntaxException, SBOLValidationException
	{
		List<QName> doc_namespaces = doc.getNamespaces();
		doc_namespaces.get(0);
		assertTrue(doc.getNamespaces().size() == 5);
		QName created_ns = new QName("http://www.w3.org/1999/02/prov#","prov");
		doc.addNamespace(created_ns);
		assertTrue(doc.getNamespaces().size() == 6);
		// TODO: something wrong here
		doc.removeNamespace(new URI(created_ns.getNamespaceURI()));
		assertTrue(doc.getNamespace(new URI(created_ns.getNamespaceURI())) == null);
		doc.addNamespace(created_ns);
		assertTrue(doc.getNamespaces().size() == 6);

		//what if namespace is empty
		doc.clearNamespaces();

	}
	
	@Test
	public void test_renameTopLevel() throws SBOLValidationException
	{		 
		gRNA_b_gene = doc.createComponentDefinition("http://partsregistry.org", "gRNA_b_gene", "", ComponentDefinition.DNA_REGION);
		doc.rename(gRNA_b_gene, "gRNA_b_gene2");
		assertNotNull(doc.getComponentDefinition("gRNA_b_gene2", ""));
		assertNull(doc.getComponentDefinition("gRNA_b_gene", ""));
		doc.rename(gRNA_b_gene, "gRNA_b_gene3", "");
		assertNotNull(doc.getComponentDefinition("gRNA_b_gene3", ""));
		doc.rename(gRNA_b_gene, prURI, "gRNA_b_gene4", "");
		assertNotNull(doc.getComponentDefinition("gRNA_b_gene4", ""));
	}
	
	@Test
	public void test_readFile() throws SBOLValidationException, URISyntaxException, IOException, SBOLConversionException
	{
		/*SBOLDocument test_doc = new SBOLDocument();
		test_doc.setDefaultURIprefix(prURI);
		test_doc.setTypesInURIs(false);
		test_doc.setComplete(true);
		File file_base = new File(SBOLDocumentTestSuite.class.getResource("/test/data/toggle.xml/").toURI());
		test_doc.read(file_base);
		assertNotNull(test_doc);
		test_doc = new SBOLDocument();
		test_doc.setDefaultURIprefix(prURI);
		test_doc.setTypesInURIs(false);
		test_doc.setComplete(true);
		test_doc.read(file_base.toString());
		assertNotNull(test_doc);
		test_doc = new SBOLDocument();
		test_doc.setDefaultURIprefix(prURI);
		test_doc.setTypesInURIs(false);
		test_doc.setComplete(true);
		InputStream file = new FileInputStream(file_base.toString());
		test_doc.read(file);
		assertNotNull(test_doc); */
	}	
	
	@Test
	public void test_createCopy() throws SBOLValidationException, IOException, SBOLConversionException
	{
		SBOLDocument repression_doc = new SBOLDocument();
		repression_doc.setDefaultURIprefix(prURI);
		repression_doc.setTypesInURIs(false);
		//repression_doc.setComplete(true);
		
		SBOLDocument copied_doc = new SBOLDocument();
		copied_doc.setDefaultURIprefix(prURI);
		copied_doc.setTypesInURIs(false);
		//copied_doc.setComplete(true);
		
		///core2/src/test/java/org/sbolstandard/core2/Testing/RepressionModel.xml"
		InputStream docAsStream = SequenceConstraintTest.class.getResourceAsStream("/SBOLTestSuite/SBOL2/RepressionModel.xml");
		repression_doc.read(docAsStream);
		//repression_doc.read("C:/Users/meher/Documents/workspace/libSBOLj/core2/src/test/resources/SBOL2/RepressionModel.xml");
		copied_doc.createCopy(repression_doc);
		assertTrue(repression_doc.equals(copied_doc));
		
		
		
	}

	

}
