package org.sbolstandard.core2;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Assume;

/**
 * This test compares a generated SBOLDocument to the SBOLDocument obtained by 
 * writing and reading the original document.
 * @author Tramy Nguyen
 * @author Chris Myers
 *
 */
public class SBOLWriterTest extends SBOLAbstractTests {

	@Override
	public void runTest(final String fileName, final SBOLDocument expected, boolean compliant) throws SBOLValidationException, SBOLConversionException, IOException 
	{
		assumeNotNull(expected);
		SBOLValidate.validateSBOL(expected, false, compliant, false);
		if (SBOLValidate.getNumErrors()>0) {
			for (String error : SBOLValidate.getErrors()) {
				System.err.println(error);
			}
			assertTrue(false);
		}
		SBOLDocument actual = SBOLTestUtils.writeAndRead(expected,compliant);
		if (!actual.equals(expected)) {
			System.out.println("Expected:"+expected.toString());
			System.out.println("Actual  :"+actual.toString());
		}
		assertTrue(actual.equals(expected));
	}

	private static <A> void assumeNotNull(A a) {
		Assume.assumeNotNull(a);
	}

}