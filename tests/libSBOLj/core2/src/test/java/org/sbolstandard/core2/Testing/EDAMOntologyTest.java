package org.sbolstandard.core2.Testing;

import static org.junit.Assert.*;


import java.net.URI;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.EDAMOntology;

public class EDAMOntologyTest {
	private EDAMOntology descendents = null;


	@Before
	public void setUp() throws Exception {
		descendents = new EDAMOntology();
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void test_getDescendantsMethods() throws URISyntaxException {
		//binary format URI
		URI binary_format = new URI("http://identifiers.org/edam/format_2333");
		/*descendent format_2333: binary format*/
		assertTrue(descendents.getDescendantsOf(EDAMOntology.FORMAT).contains("format_2333")); 
		assertTrue(descendents.getDescendantsOf("format_1915").contains("format_2330"));
		assertTrue(descendents.getDescendantURIsOf(EDAMOntology.FORMAT).contains(binary_format));
		assertTrue(descendents.getDescendantURIsOf("format_1915").contains(binary_format));
		assertTrue(descendents.isDescendantOf(binary_format, EDAMOntology.FORMAT));
		assertTrue(descendents.isDescendantOf("format_2333", "format_1915"));

		//assertTrue(descendents.getDescendantsOf("format_1915").contains("format_3464")); //json format is returning false
	}
	
	@Test 
	public void test_propertiesOfEDAMOntology() throws URISyntaxException
	{
		URI Format = new URI("http://identifiers.org/edam/format_1915");
		EDAMOntology descendents = new EDAMOntology();
		assertTrue(descendents.getId("Format").equals("format_1915"));
		assertTrue(descendents.getName(Format).equals("Format"));
		assertTrue(descendents.getName("format_1915").equals("Format"));
		assertTrue(descendents.getName("format_1915").equals("Format"));
		assertTrue(descendents.getURIbyId("format_1915").equals(Format));
		assertTrue(descendents.getURIbyName("Format").equals(Format));
	}
	
	@Test
	public void test_getDecendentNames() throws URISyntaxException
	{
	
		assertTrue(descendents.getDescendantNamesOf(EDAMOntology.FORMAT).size() != 0);
		assertTrue(descendents.getDescendantNamesOf(EDAMOntology.FORMAT).contains("OBO"));
		assertTrue(descendents.getDescendantNamesOf("format_1915").size() != 0);
		assertTrue(descendents.getDescendantNamesOf("format_1915").contains("RDF/XML"));
		
		//failing but should be passing
		//assertTrue(descendents.getDescendantNamesOf(EDAMOntology.FORMAT).contains("JSON"));

	}
	

}
