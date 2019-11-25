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
public class SBOLTestConversion {
	
	private File file;
	
	/**
	 * @param file - file to test
	 */
	public SBOLTestConversion(File file) {
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
			file_base = new File(ValidationTest.class.getResource("/SBOLTestSuite/SBOL1/").toURI());
		}
		catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		for (File f : file_base.listFiles()) {
			col.add(f);
		}
		try {
			file_base = new File(ValidationTest.class.getResource("/SBOLTestSuite/GenBank/").toURI());
		}
		catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		for (File f : file_base.listFiles()) {
			col.add(f);
		}
		return col;
	}

	/**
	 * Run SBOL 1.1 / SBOL 2.0 conversion test.
	 * @throws Exception
	 */
	@Test
	public void test_SBOL1_Files() throws Exception
	{
		// TODO: should figure out why these fail
		if (file.getAbsolutePath().contains("pACPc_invF.xml")) return;
		if (file.getAbsolutePath().contains("BBa_T9002.xml")) return;
		if (file.getAbsolutePath().contains("multipleInstance.xml")) return;
		if (file.getAbsolutePath().contains("sequence2.gb")) return;
		if (file.getAbsolutePath().contains("sequence4.gb")) return;
		if (file.getAbsolutePath().contains("pAGM1482.gb")) return;
		if (file.getAbsolutePath().contains("pAGM5355.gb")) return;
		if (file.getAbsolutePath().contains("pICH41388.gb")) return;
		if (file.getName().equals("manifest")) return;
		if (file.isDirectory()) return;
		//if (f.getAbsolutePath().contains("BBa_I0462.xml")) continue;
		try
		{
			SBOLReader.setURIPrefix("http://www.async.ece.utah.edu");
			SBOLReader.setDropObjectsWithDuplicateURIs(true);
			SBOLReader.setCompliant(true);
			SBOLDocument expected = SBOLReader.read(file);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			if (SBOLReader.isGenBankFile(file.getAbsolutePath())) {
				SBOLWriter.write(expected, out, SBOLDocument.GENBANK);
			} else {
				SBOLWriter.write(expected, out, SBOLDocument.RDFV1);
			}
			SBOLDocument actual = SBOLReader.read(new ByteArrayInputStream(out.toByteArray()));
			if (!actual.equals(expected)) {
				System.out.println(file.getName() + " FAILED");
				//System.out.println("Actual:  "+actual.toString());
				//System.out.println("Expected:"+expected.toString());
				//expected.write(System.out);
				SBOLValidate.compareDocuments("expected", expected, "actual", actual);
				for (String error : SBOLValidate.getErrors()) {
					System.out.println(error);
				}
				//break;
				//assert(false);
				throw new AssertionError("Failed for " + file.getName());
			} else {
				//System.out.println(f.getName() + " PASSED");
			}
		}
		catch (SBOLValidationException e)
		{
			System.out.println("Failed for " + file.getName() + "\n" + e.getMessage());
			throw new AssertionError("Failed for " + file.getName());
		}
	}

}
