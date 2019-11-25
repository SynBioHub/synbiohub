package org.sbolstandard.core2.Testing;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.EDAMOntology;
import org.sbolstandard.core2.SequenceOntology;

public class SequenceOntologyTest {
	private SequenceOntology SQ	= null;

	@Before
	public void setUp() throws Exception {
		SQ = new SequenceOntology();
	}

	@Test
	public void test_descendentsOfMethods() throws URISyntaxException
	{
		URI constitutive_promoter = new URI("http://identifiers.org/so/SO:0002050");
		assertTrue(SQ.getDescendantsOf(SequenceOntology.PROMOTER).contains("SO:0002050"));
		assertTrue(SQ.getDescendantsOf("SO:0000167").contains("SO:0002050"));
		assertTrue(SQ.getDescendantURIsOf(SequenceOntology.PROMOTER).contains(constitutive_promoter));
		assertTrue(SQ.getDescendantURIsOf("SO:0000167").contains(constitutive_promoter));		
	}
	
	@Test
	public void test_getDecendentNames() throws URISyntaxException
	{
		assertTrue(SQ.getDescendantNamesOf(SequenceOntology.PROMOTER).size() != 0);
		assertTrue(SQ.getDescendantNamesOf(SequenceOntology.PROMOTER).contains("constitutive_promoter"));
		assertTrue(SQ.getDescendantNamesOf("SO:0000167").size() != 0);
		assertTrue(SQ.getDescendantNamesOf("SO:0000167").contains("constitutive_promoter"));
	}
	
	
	@After
	public void tearDown() throws Exception {
	}


	
}
