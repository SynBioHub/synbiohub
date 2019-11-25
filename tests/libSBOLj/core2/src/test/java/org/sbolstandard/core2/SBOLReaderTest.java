package org.sbolstandard.core2;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

/**
 * This test compares a generated SBOLDocument to a "golden" file read from disk. 
 * @author Tramy Nguyen
 * @author Chris Myers
 *
 */
public class SBOLReaderTest extends SBOLAbstractTests
{
	@Override
	public void runTest(final String fileName, final SBOLDocument expected, boolean compliant) throws SBOLValidationException, IOException, SBOLConversionException 
	{
		InputStream resourceAsStream = SBOLReaderTest.class.getResourceAsStream(fileName);
		if (resourceAsStream == null)
			resourceAsStream = SBOLReaderTest.class.getResourceAsStream("/" + fileName);

		assert resourceAsStream != null : "Failed to find test resource '" + fileName + "'";
		SBOLReader.setCompliant(compliant);
		
		SBOLDocument actual;
		if (compliant) {
			SBOLReader.setURIPrefix("http://www.async.ece.utah.edu");
			SBOLReader.setVersion("");
		}
		else SBOLReader.unsetURIPrefix();

//		if(fileType.equals("rdf"))
//			actual = SBOLReader.read(resourceAsStream);
//		else if (fileType.equals("json"))
//			actual = SBOLReader.read(resourceAsStream,SBOLDocument.JSON);
//		else if (fileType.equals("turtle"))
//			actual = SBOLReader.read(resourceAsStream,SBOLDocument.TURTLE);
//		else
		actual = SBOLReader.read(resourceAsStream);
		if (!actual.equals(expected)) {
			System.out.println("Differences found for " + fileName);
			SBOLValidate.compareDocuments("actual", actual, "expected", expected);
			for (String error : SBOLValidate.getErrors()) {
				System.out.println(error);
			}
			
		}
		assertTrue(actual.equals(expected));
	}

}
