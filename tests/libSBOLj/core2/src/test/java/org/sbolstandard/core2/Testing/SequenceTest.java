package org.sbolstandard.core2.Testing;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;

public class SequenceTest {
	private SBOLDocument doc = null;
	private Sequence generic_seq = null;
	private String CRa_U6_seq = "GGTTTACCGAGCTCTTATTGGTTTTCAAACTTCATTGACTGTGCC";

	@Before
	public void setUp() throws Exception 
	{
		String prURI="http://partsregistry.org";
		doc = new SBOLDocument();
		doc.setDefaultURIprefix(prURI);
		doc.setTypesInURIs(false);
		doc.setComplete(true);
		generic_seq = doc.createSequence("generic_seq", "ttgacagctagctcagtcctaggtataatgctagc", Sequence.IUPAC_DNA);

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test_deep_copy() throws SBOLValidationException
	{
		Sequence gen_seq = doc.createSequence("CRa_U6_seq", CRa_U6_seq, Sequence.IUPAC_DNA);
		Sequence copy_seq = (Sequence) doc.createCopy(gen_seq, "copy_seq");
		//assertTrue(copy_seq.equals(gen_seq));
		assertTrue(copy_seq.getElements().equals(CRa_U6_seq));
		
	}
	@Test
	public void test_toString()
	{	
		assertTrue(doc.getSequence("generic_seq", "").toString().length() != 0);
		assertNotNull(generic_seq.toString());
		assertTrue(generic_seq.toString().contains("displayId="+generic_seq.getDisplayId()));
		assertTrue(!generic_seq.toString().contains("name="+generic_seq.getDisplayId()));
	}
	

}
