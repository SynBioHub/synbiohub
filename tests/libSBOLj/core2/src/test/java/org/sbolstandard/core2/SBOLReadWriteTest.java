package org.sbolstandard.core2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URISyntaxException;
import java.util.HashSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Test conversion betweeen SBOL 1.1 and SBOL 2.0.
 * Reads and converts SBOL 1.1 to an SBOL 2.0 document.
 * Writes and converts the SBOL 2.0 to an SBOL 1.1 document.
 * Reads and converts this SBOL 1.1 document to a new SBOL 2.0 document.
 * Compares the first and second converted SBOL 2.0 documents.
 * @author Chris Myers
 *
 */
@RunWith(Parameterized.class)
public class SBOLReadWriteTest {
	
	private File file;
	
	/**
	 * @param file - file to test
	 */
	public SBOLReadWriteTest(File file) {
		this.file = file;
	}
	
	/**
	 * @return list of files to test
	 */
	@Parameterized.Parameters
	public static java.util.Collection<File> files() {
		File file_base = null ;
		java.util.Collection<File> col = new HashSet<File>();
		try {
			file_base = new File(ValidationTest.class.getResource("/SBOLTestSuite/SBOL2/").toURI());
		}
		catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		for (File f : file_base.listFiles()) {
			if (f.getName().equals("manifest")) continue;
			col.add(f);
		}
		try {
			file_base = new File(ValidationTest.class.getResource("/SBOLTestSuite/SBOL2_bp/").toURI());
		}
		catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		for (File f : file_base.listFiles()) {
			if (f.getName().equals("manifest")) continue;
			col.add(f);
		}
		try {
			file_base = new File(ValidationTest.class.getResource("/SBOLTestSuite/SBOL2_ic/").toURI());
		}
		catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		for (File f : file_base.listFiles()) {
			if (f.getName().equals("manifest")) continue;
			col.add(f);
		}
		try {
			file_base = new File(ValidationTest.class.getResource("/SBOLTestSuite/SBOL2_nc/").toURI());
		}
		catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		for (File f : file_base.listFiles()) {
			if (f.getName().equals("manifest")) continue;
			col.add(f);
		}
		return col;
	}

	/**
	 * Run SBOL 1.1 / SBOL 2.0 conversion test.
	 * @throws Exception
	 */
	@Test
	public void test_SBOL2_Files() throws Exception
	{
		try
		{
			SBOLReader.setURIPrefix("http://www.async.ece.utah.edu");
			SBOLDocument expected = SBOLReader.read(file);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			SBOLWriter.write(expected, out);
			SBOLDocument actual = SBOLReader.read(new ByteArrayInputStream(out.toByteArray()));
			if (!actual.equals(expected)) {
				System.out.println(file.getName() + " FAILED");
				System.out.println("Actual:  "+actual.toString());
				System.out.println("Expected:"+expected.toString());
				SBOLValidate.compareDocuments("expected", expected, "actual", actual);
				//break;
				//assert(false);
				throw new AssertionError("Failed for " + file.getName());
			} else {
				//System.out.println(file.getName() + " PASSED");
			}
		}
		catch (SBOLValidationException e)
		{
			System.out.println("Failed for " + file.getAbsolutePath() + "\n" + e.getMessage());
			throw new AssertionError("Failed for " + file.getName());
		}
	}

}
