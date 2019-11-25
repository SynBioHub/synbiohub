package org.sbolstandard.core2.Testing;

import static org.junit.Assert.*;

import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.SystemsBiologyOntology;

public class SystemsBiologyOntologyTest {
	private SystemsBiologyOntology descendents = null;

	@Before
	public void setUp() throws Exception {
		descendents = new SystemsBiologyOntology();
	}

	@Test
	public void test_getDescendantsMethods() {
		assertTrue(descendents.getDescendantsOf(SystemsBiologyOntology.PRODUCT).contains("SBO:0000603"));
		assertTrue(descendents.getDescendantsOf(SystemsBiologyOntology.PRODUCT).size() == 1);
		assertTrue(descendents.getDescendantsOf("SBO:0000003").contains("SBO:0000603"));
		assertTrue(descendents.getDescendantURIsOf(SystemsBiologyOntology.PRODUCT).contains(SystemsBiologyOntology.SIDE_PRODUCT));
		assertTrue(descendents.getDescendantURIsOf("SBO:0000003").contains(SystemsBiologyOntology.SIDE_PRODUCT));
		assertTrue(descendents.getDescendantURIsOf(SystemsBiologyOntology.PRODUCT).size() == 1);

	}
	
	@Test
	public void test_getDecendentNames() throws URISyntaxException
	{
		assertTrue(descendents.getDescendantNamesOf(SystemsBiologyOntology.CATALYST).size() != 0);
		assertTrue(descendents.getDescendantNamesOf(SystemsBiologyOntology.CATALYST).contains("enzymatic catalyst"));
		assertTrue(descendents.getDescendantNamesOf("SBO:0000013").size() != 0);
		assertTrue(descendents.getDescendantNamesOf("SBO:0000013").contains("enzymatic catalyst"));
	}

}
