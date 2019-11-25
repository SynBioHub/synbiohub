package org.sbolstandard.core2.Testing;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceOntology;

public class AnnotationTest {

	private SBOLDocument doc = null;
	private ComponentDefinition gRNA_b_gene = null;
	private String prURI="http://partsregistry.org/";

	@Before
	public void setUp() throws Exception {
		doc = new SBOLDocument();
		doc.setDefaultURIprefix(prURI);
		doc.setTypesInURIs(false);
		doc.setComplete(true);
		
		gRNA_b_gene = doc.createComponentDefinition("gRNA_b_gene", ComponentDefinition.DNA_REGION);
		gRNA_b_gene.addRole(SequenceOntology.ENGINEERED_GENE);
		gRNA_b_gene.setName("gRNA_b_gene");
		gRNA_b_gene.setDescription("protein");	
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test_pubConstructors() throws SBOLValidationException, URISyntaxException
	{
		Annotation CD_annot = gRNA_b_gene.createAnnotation(new QName(prURI, "protein", "pr"),
				true);	
		assertTrue(gRNA_b_gene.getAnnotation(CD_annot.getQName()).getBooleanValue());
		assertTrue(gRNA_b_gene.getAnnotations().size() == 1);
		assertTrue(CD_annot.isBooleanValue());
		CD_annot.setBooleanValue(false);
		assertFalse(gRNA_b_gene.getAnnotation(CD_annot.getQName()).getBooleanValue());
		gRNA_b_gene.removeAnnotation(CD_annot);
		assertTrue(gRNA_b_gene.getAnnotations().size() == 0);
		
		//test constructor with double
		CD_annot = gRNA_b_gene.createAnnotation(new QName(prURI, "protein", "pr"),
				1.0);	
		assertTrue(gRNA_b_gene.getAnnotation(CD_annot.getQName()).getDoubleValue() == 1.0);
		assertTrue(gRNA_b_gene.getAnnotations().size() == 1);
		assertTrue(CD_annot.isDoubleValue());
		CD_annot.setDoubleValue(5.0);
		assertTrue(CD_annot.getDoubleValue() == 5.0);
		gRNA_b_gene.removeAnnotation(CD_annot);
		assertTrue(gRNA_b_gene.getAnnotations().size() == 0);

		//test constructor with int
		CD_annot =  gRNA_b_gene.createAnnotation(new QName(prURI, "protein", "pr"),
				2);	
		assertTrue(gRNA_b_gene.getAnnotation(CD_annot.getQName()).getIntegerValue() == 2);
		assertTrue(gRNA_b_gene.getAnnotations().size() == 1);
		assertTrue(CD_annot.isIntegerValue());
		CD_annot.setIntegerValue(7);
		assertTrue(CD_annot.getIntegerValue() == 7);
		gRNA_b_gene.removeAnnotation(CD_annot);
		assertTrue(gRNA_b_gene.getAnnotations().size() == 0);
		
		//test constructor with string
		CD_annot = gRNA_b_gene.createAnnotation(new QName(prURI, "protein", "pr"),
				"protein");	
		assertTrue(gRNA_b_gene.getAnnotation(CD_annot.getQName()).getStringValue().equals("protein"));
		assertTrue(gRNA_b_gene.getAnnotations().size() == 1);
		assertTrue(CD_annot.isStringValue());
		CD_annot.setStringValue("notAProtein");
		assertTrue(CD_annot.getStringValue().equals("notAProtein"));
		gRNA_b_gene.removeAnnotation(CD_annot);
		assertTrue(gRNA_b_gene.getAnnotations().size() == 0);
		
		//test constructor with URI
		CD_annot = gRNA_b_gene.createAnnotation(new QName(prURI, "protein", "pr"), new URI("http://www.sbolstandard.org/protein"));
		assertTrue(gRNA_b_gene.getAnnotation(CD_annot.getQName()).getURIValue().equals(new URI("http://www.sbolstandard.org/protein")));
		assertTrue(gRNA_b_gene.getAnnotations().size() == 1);
		assertTrue(CD_annot.isURIValue());
		CD_annot.setURIValue(new URI("http://www.sbolstandard.org/gene"));
		assertTrue(CD_annot.getURIValue().equals(new URI("http://www.sbolstandard.org/gene")));
		gRNA_b_gene.removeAnnotation(CD_annot);
		assertTrue(gRNA_b_gene.getAnnotations().size() == 0);

	}
	
	@Test
	public void test_toString() throws SBOLValidationException
	{
		Annotation CD_annot = gRNA_b_gene.createAnnotation(new QName(prURI, "protein", "pr"),
				true);	
		//System.out.println(CD_annot.toString());
		assertTrue(CD_annot.toString().contains("value=" + true));
	}
	

}
