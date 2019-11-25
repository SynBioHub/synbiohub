package org.sbolstandard.core2;

import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;

import org.junit.Test;

/**
 * Methods to test FASTA/SBOL conversion.
 * @author Ernst Oberortner
 * @author Chris Myers
 */
public class FASTATest {

	/**
	 * Test read FASTA conversion
	 */
	@Test
	public void testReadInputStreamStringStringStringURI() {
		String URIprefix = "http://sbols.org/";
		String version = "1.0";


		// one FASTA entry
		String fasta = 
				">test" + SBOLTestUtils.NEWLINE + 
				"acgt";
		try (
				ByteArrayInputStream bais = 
					new ByteArrayInputStream(fasta.getBytes());
			) {
		
			SBOLReader.setVersion(version);
			SBOLReader.setURIPrefix(URIprefix);
			SBOLDocument doc = SBOLReader.read(bais);
//			doc.setDefaultURIprefix(URIprefix);
			
			assertTrue(doc.getSequences().size() == 1);
			assertTrue(URIprefix.equals(doc.getDefaultURIprefix()));

			assertTrue(null != doc.getSequence("test", version));
		} catch(Exception e) {
			assertTrue(false);	// no exception allowed
		}

		// two FASTA entries
		fasta = ">test1" + SBOLTestUtils.NEWLINE + 
				"acgt" + SBOLTestUtils.NEWLINE +
				">test2" + SBOLTestUtils.NEWLINE +
				"cgta";
		try (
				ByteArrayInputStream bais = 
					new ByteArrayInputStream(fasta.getBytes());
			) {
		
			SBOLReader.setURIPrefix(URIprefix);
			SBOLReader.setVersion(version);
			SBOLDocument doc = SBOLReader.read(bais);

			assertTrue(doc.getSequences().size() == 2);

			// how can I retrieve a sequence nicely from the Document?
			assertTrue(null != doc.getSequence("test1", version));
			assertTrue(null != doc.getSequence("test2", version));
		} catch(Exception e) {
			assertTrue(false);	// no exception allowed
		}
	
		// one multi-line FASTA entry
		fasta = ">test1" + SBOLTestUtils.NEWLINE + 
				"acgt" + SBOLTestUtils.NEWLINE +
				"acgt" + SBOLTestUtils.NEWLINE;
		try (
				ByteArrayInputStream bais = 
					new ByteArrayInputStream(fasta.getBytes());
			) {
		
			SBOLReader.setURIPrefix(URIprefix);
			SBOLDocument doc = SBOLReader.read(bais);

			assertTrue(doc.getSequences().size() == 1);

			// how can I retrieve a sequence nicely from the Document?
			assertTrue(null != doc.getSequence("test1", version));
			Sequence seq = doc.getSequence("test1", version);
			assertTrue("acgtacgt".equals(seq.getElements()));
		} catch(Exception e) {
			assertTrue(false);	// no exception allowed
		}
	}
	
//		@Test
//		public void testWriteSequenceString() {
//			fail("Not yet implemented"); 
//		}
	//
//		@Test
//		public void testWriteSBOLDocumentString() {
//			fail("Not yet implemented"); 
//		}
	//
//		@Test
//		public void testWriteSequenceFile() {
//			fail("Not yet implemented"); 
//		}
	//
//		@Test
//		public void testWriteSBOLDocumentFile() {
//			fail("Not yet implemented"); 
//		}
	//
//		@Test
//		public void testWriteSequenceOutputStream() {
//			fail("Not yet implemented"); 
//		}
	//
//		@Test
//		public void testWriteSBOLDocumentOutputStream() {
//			fail("Not yet implemented"); 
//		}

//		@Test
//		public void testReadFileStringStringStringURI() {
//			fail("Not yet implemented");
//		}
	//
//		@Test
//		public void testReadStringStringStringStringURI() {
//			fail("Not yet implemented");
//		}

}
