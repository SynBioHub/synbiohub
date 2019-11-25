package org.sbolstandard.core2;

import static org.junit.Assert.*;

import java.net.URI;

import org.junit.Test;


/**
 * Tests for the ontology parser
 * @author Zhen Zhang
 *
 */
public class OntologyTest {
	
	private static final String SO_URI_PREFIX = "http://identifiers.org/so/";
	private static final String SBO_URI_PREFIX = "http://identifiers.org/biomodels.sbo/";

	SequenceOntology sequenceOntology = new SequenceOntology();
	String SO_child1_id = "SO:1001260";
	String SO_parent1_id = "SO:0000243"; 
	String SO_parent2_id = "SO:1001268";
	String SO_parent3_id = "SO:0000001";
	String SO_child1_name = "internal_Shine_Dalgarno_sequence";
	String SO_parent1_name = "internal_ribosome_entry_site";
	URI SO_child1_uri = URI.create(SO_URI_PREFIX + SO_child1_id);
	URI SO_parent1_uri = URI.create(SO_URI_PREFIX + SO_parent1_id);

	SystemsBiologyOntology systemsBiologyOntology = new SystemsBiologyOntology();
	String SBO_child1_id = "SBO:0000348";
	String SBO_parent1_id = "SBO:0000346";
	String SBO_parent2_id ="SBO:0000009";
	String SBO_parent3_id = "SBO:0000002";	
	String SBO_child1_name = "exponential time constant";
	String SBO_parent1_name = "temporal measure";
	URI SBO_child1_uri = URI.create(SBO_URI_PREFIX + SBO_child1_id);
	URI SBO_parent1_uri = URI.create(SBO_URI_PREFIX + SBO_parent1_id);
	
	@Test
	public void test_SO_getId1() {
		assertTrue(sequenceOntology.getId(SO_parent1_name).equals(SO_parent1_id));
	}
	
	@Test
	public void test_SO_getId2() {
		assertTrue(sequenceOntology.getId(SO_parent1_name).equals(SO_parent1_id));
	}
	
	@Test
	public void test_SO_getIdURI1() {
		assertTrue(sequenceOntology.getId(SO_child1_uri).equals(SO_child1_id));
	}

	@Test
	public void test_SO_getIdURI2() {
		assertTrue(sequenceOntology.getId(SO_parent1_uri).equals(SO_parent1_id));
	}
	
	@Test
	public void test_SO_getName1() {
		assertTrue(sequenceOntology.getName(SO_child1_id).equals(SO_child1_name));
	}

	@Test
	public void test_SO_getName2() {
		assertTrue(sequenceOntology.getName(SO_parent1_id).equals(SO_parent1_name));
	}

	@Test
	public void test_SO_getNameURI1() {
		assertTrue(sequenceOntology.getName(SO_child1_uri).equals(SO_child1_name));
	}

	@Test
	public void test_SO_getNameURI2() {
		assertTrue(sequenceOntology.getName(SO_parent1_uri).equals(SO_parent1_name));
	}
	
	@Test
	public void test_SO_getURIbyId1() {
		assertTrue(sequenceOntology.getURIbyId(SO_child1_id).equals(SO_child1_uri));
	}

	@Test
	public void test_SO_getURIbyId2() {
		assertTrue(sequenceOntology.getURIbyId(SO_parent1_id).equals(SO_parent1_uri));
	}

	@Test
	public void test_SO_getURIbyName1() {
		assertTrue(sequenceOntology.getURIbyName(SO_child1_name).equals(SO_child1_uri));
	}

	@Test
	public void test_SO_getURIbyName2() {
		assertTrue(sequenceOntology.getURIbyName(SO_parent1_name).equals(SO_parent1_uri));
	}

	@Test
	public void test_SO_isDescendantOf1() {
		// Term SO:1001260 has two is_a relations.
		assertTrue(sequenceOntology.isDescendantOf(SO_child1_id, SO_parent1_id));
	}
	
//	@Test
//	public void test_SO_isDescendantOf2() {
//		// Term SO:1001260 has two is_a relations.
//		assertTrue(sequenceOntology.isDescendantOf(SO_child1_id, SO_parent2_id));
//	}
	
	@Test
	public void test_SO_isDescendantOf3() {
		// Term SO:1001260 has two is_a relations.
		assertTrue(sequenceOntology.isDescendantOf(SO_child1_id, SO_parent3_id));
	}
	
//	@Test
//	public void test_SO_isDescendantOf4() {
//		assertFalse(sequenceOntology.isDescendantOf(SO_parent1_id, SO_parent2_id));
//	}

//	@Test
//	public void test_SO_isDescendantOf5() {
//		assertFalse(sequenceOntology.isDescendantOf(SO_parent2_id, SO_child1_id));
//	}

	@Test
	public void test_SBO_getId1() {
		assertTrue(systemsBiologyOntology.getId(SBO_parent1_name).equals(SBO_parent1_id));
	}
	
	@Test
	public void test_SBO_getId2() {
		assertTrue(systemsBiologyOntology.getId(SBO_parent1_name).equals(SBO_parent1_id));
	}
	
	@Test
	public void test_SBO_getIdURI1() {
		assertTrue(systemsBiologyOntology.getId(SBO_child1_uri).equals(SBO_child1_id));
	}

	@Test
	public void test_SBO_getIdURI2() {
		assertTrue(systemsBiologyOntology.getId(SBO_parent1_uri).equals(SBO_parent1_id));
	}
	
	@Test
	public void test_SBO_getName1() {
		assertTrue(systemsBiologyOntology.getName(SBO_child1_id).equals(SBO_child1_name));
	}

	@Test
	public void test_SBO_getName2() {
		assertTrue(systemsBiologyOntology.getName(SBO_parent1_id).equals(SBO_parent1_name));
	}

	@Test
	public void test_SBO_getNameURI1() {
		assertTrue(systemsBiologyOntology.getName(SBO_child1_uri).equals(SBO_child1_name));
	}

	@Test
	public void test_SBO_getNameURI2() {
		assertTrue(systemsBiologyOntology.getName(SBO_parent1_uri).equals(SBO_parent1_name));
	}
	
	@Test
	public void test_SBO_getURIbyId1() {
		assertTrue(systemsBiologyOntology.getURIbyId(SBO_child1_id).equals(SBO_child1_uri));
	}

	@Test
	public void test_SBO_getURIbyId2() {
		assertTrue(systemsBiologyOntology.getURIbyId(SBO_parent1_id).equals(SBO_parent1_uri));
	}

	@Test
	public void test_SBO_getURIbyName1() {
		assertTrue(systemsBiologyOntology.getURIbyName(SBO_child1_name).equals(SBO_child1_uri));
	}

	@Test
	public void test_SBO_getURIbyName2() {
		assertTrue(systemsBiologyOntology.getURIbyName(SBO_parent1_name).equals(SBO_parent1_uri));
	}

	@Test
	public void test_SBO_isDescendantOf1() {
		// Term SBO:1001260 has two is_a relations.
		assertTrue(systemsBiologyOntology.isDescendantOf(SBO_child1_id, SBO_parent1_id));
	}
	
	@Test
	public void test_SBO_isDescendantOf2() {
		// Term SBO:1001260 has two is_a relations.
		assertTrue(systemsBiologyOntology.isDescendantOf(SBO_child1_id, SBO_parent2_id));
	}
	
	/**
	 * 
	 */
	@Test
	public void test_SBO_isDescendantOf3() {
		// Term SBO:1001260 has two is_a relations.
		assertTrue(systemsBiologyOntology.isDescendantOf(SBO_child1_id, SBO_parent3_id));
	}
	
	@Test
	public void test_SBO_isDescendantOf4() {
		assertFalse(systemsBiologyOntology.isDescendantOf(SBO_parent1_id, SBO_parent2_id));
	}

	@Test
	public void test_SBO_isDescendantOf5() {
		assertFalse(systemsBiologyOntology.isDescendantOf(SBO_parent2_id, SBO_child1_id));
	}
	
}
