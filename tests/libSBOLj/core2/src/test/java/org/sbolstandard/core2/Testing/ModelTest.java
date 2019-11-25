package org.sbolstandard.core2.Testing;

import static org.junit.Assert.*;

import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.Model;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SystemsBiologyOntology;

public class ModelTest {
	
	private SBOLDocument document = null;
	private Model test_model = null;

	@Before
	public void setUp() throws Exception {
		document = new SBOLDocument();				
		document.setTypesInURIs(false);		
		document.setDefaultURIprefix("http://www.sbolstandard.org/examples");
		
		test_model=document.createModel(
				"pIKE_Toggle_1",
				"",
				URI.create("http://virtualparts.org/part/pIKE_Toggle_1"),
				URI.create("http://identifiers.org/edam/format_2585"), 
				SystemsBiologyOntology.CONTINUOUS_FRAMEWORK);
		test_model.setName("pIKE_Toggle_1 toggle switch");
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void test_toString()
	{
		String model = test_model.toString();
		assertTrue(model.length() != 0);
		assertNotNull(model);
		assertTrue(model.contains("name=" + test_model.getName()));
		assertTrue(model.contains("identity=" + test_model.getIdentity()));
		assertTrue(model.contains("displayId=" + test_model.getDisplayId()));
		assertTrue(!model.contains("description=" + test_model.getDescription())); 
		assertTrue(model.contains("source=" + test_model.getSource())); 
		assertTrue(model.contains("language=" + test_model.getLanguage())); 
		assertTrue(model.contains("framework=" + test_model.getFramework())); 


		

	}
	

}
