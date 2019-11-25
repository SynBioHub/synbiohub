/**
 * 
 */
package org.sbolstandard.core2;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Checks a set of SBOL files in which each should fail a particular validation rule.
 * @author Meher Samineni
 * @author Chris Myers
 *
 */
@RunWith(Parameterized.class)
public class ValidationTest {
	
	private File file;
	private static HashSet<Integer> testedRules = new HashSet<Integer>(); 
	private static HashSet<Integer> failedTests = new HashSet<Integer>(); 
	
	/**
	 * @param file - file to test
	 */
	public ValidationTest(File file) {
		this.file = file;
	}
	
	/**
	 * @return a set of files to test
	 */
	@Parameterized.Parameters
	public static java.util.Collection<File> files() {
		File file_base = null ;
		java.util.Collection<File> col = new HashSet<File>();
		try {
			file_base = new File(ValidationTest.class.getResource("/SBOLTestSuite/InvalidFiles/").toURI());
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
	 * Run each xml/XML file under the "Validation" sub-directory to test SBOL validation exceptions.
	 * 
	 * @throws IOException see {@link IOException}
	 * @throws SBOLConversionException see {@link SBOLConversionException}
	 * @throws SBOLValidationException see {@link SBOLValidationException}
	 */
	@Test
	public void testValidation() throws IOException, SBOLConversionException, SBOLValidationException {
		SBOLReader.setKeepGoing(true);
		SBOLDocument doc = SBOLReader.read(file);
		SBOLValidate.validateSBOL(doc, true, true, true);
		if (SBOLReader.getNumErrors() > 0) {
			for(String error : SBOLReader.getErrors())
			{
				if(!error.split(":")[0].equals((file.getName()).replace(".xml", "")))
				{
					System.out.println(file.getName().replace(".xml", ""));
					System.out.println(error);
					fail();

				}
				else {
					String ruleId = error.split(":")[0].replace(".xml", "").replace("sbol-", "").trim();
					testedRules.add(Integer.parseInt(ruleId));
				}
			}

		} else if (SBOLValidate.getNumErrors() > 0) {
			for(String error : SBOLValidate.getErrors())
			{
				if(!error.split(":")[0].equals(file.getName().replace(".xml", "")))
				{
					String ruleId = file.getName().replace(".xml", "").replace("sbol-", "").trim();
					failedTests.add(Integer.parseInt(ruleId));
					System.out.println(file.getName().replace(".xml", ""));
					System.out.println(error);
					fail();
				}
				else {
					String ruleId = error.split(":")[0].replace(".xml", "").replace("sbol-", "").trim();
					testedRules.add(Integer.parseInt(ruleId));
				}

			}
		} else {
			String ruleId = file.getName().replace(".xml", "").replace("sbol-", "").trim();
			failedTests.add(Integer.parseInt(ruleId));
			System.out.println(file.getName().replace(".xml", ""));
			fail();
		}
	}

	/**
	 * Print out remaining rules that have not had a test yet.
	 */
	@SuppressWarnings("unchecked")
	@AfterClass
	public static void printRemainingRules() {
		HashSet<Integer> red = new HashSet<Integer>(Arrays.asList(
				10101, 10105, 10106,
				10201, 10203, 10204, 10206, 10208, 10212, 10213, 10220, 10221,
				10303, 10306,
				10401, 10402, 10403, 
				10501, 10502, 10503, 10507, 10512, 10519, 10521, 10522, 10524, 10526,
				10602, 10603, 10606, 10607, 
				10701,
				10801, 10802, 10803, 10804, 10805, 10810, 
				10901, 10902, 10904, 10905,
				11002, 
				11101, 11102, 11103, 11104, 
				11201, 11202, 
				11301,
				11401, 11402, 11403, 11404, 11405, 11406, 11407, 11413,
				11501, 11502, 11504, 11508, 
				11601, 11602, 11604, 11605, 11606, 11607, 11609,
				11701, 11702, 11704, 11706, 
				11801, 11802, 
				11901, 11902, 11906, 
				12001, 12002, 12003, 12004, 
				12101, 12102,
				// TODO: I don't think these can be tested in this way
//				12201, 12203, 12204, 12205, 12206,
				12301, 12302, 12303,
				12401, 12402, 12403, 12404, 12405, 12406,
				12501, 12502, 12503,
				12601, 12602, 12603, 12605,
				12701,
				12801,
				12901, 12902, 12903, 12904, 12906, 12907, 12914,
				13001, 13002, 13003, 13004, 13007, 13009, 13013,
				13101, 13102,
				13201, 13202, 13204, 13207, 13208
				));
		HashSet<Integer> blue = new HashSet<Integer>(Arrays.asList(
				10202, 10222, 10223,
				10304, 10307,
				10405, 
				10513, 10516,
				10604, 10605, 
				10807, 10808, 10809, 10811, 
				11409, 11410, 11411, 11414,
				11608, 
				11703, 11705,
				12103,
				12407,
				12604, 12606,
				12905, 12908,
				13005, 13008, 13010, 13011, 13012, 13014, 13015, 13016, 13017,
				13103
				));
		HashSet<Integer> green = new HashSet<Integer>(Arrays.asList(
				10215, 10216, 10217, 10218, 10219, 10224, 10225, 10226, 10227,
				10302,
				10407,
				10511, 10518, 10520, 10523, 10525, 10527,
				10903, 
				11412, 
				11507, 11511, 
				11905, 11907,
				12007,
				12408, 12409, 12410, 12411,
				12504, 12505, 12506, 12507,
				12909, 12910, 12911, 12912, 12913,
				13006, 13018, 13019, 13020, 13021, 13022,
				13206
				));
		HashSet<Integer> yellow = new HashSet<Integer>(Arrays.asList(
				10214,
				10305,
				10404, 10406, 
				10504, 10505, 10506, 10508, 10509, 10510, 10514, 10515, 
				11408,
				11503, 11505, 11506, 11509, 11510, 
				11603,
				11903, 11904, 
				12005, 12006,
				13203, 13205
				));
		HashSet<Integer> removed = new HashSet<Integer>(Arrays.asList(
				10102, 10103, 10104, 
				10205,
				10301,
				10517,
				10601,
				10806, 
				12202
				));
		if (failedTests.size() > 0) {
			System.out.println();
			System.out.println("Warning: the following tests failed.");
			System.out.println(sortIntegerHashSet(failedTests));
		}
		HashSet<Integer> removedTested = (HashSet<Integer>) removed.clone();
		if (removedTested.retainAll(failedTests) == true && removedTested.size() == 0) {
		}
		else {
			System.out.println();
			System.out.println("Warning: tests below were created but are marked removed:");
			System.out.println(sortIntegerHashSet(removedTested));
		}
		if (removed.retainAll(testedRules) == true && removed.size() == 0) {
		}
		else {
			System.out.println();
			System.out.println("Warning: tests below were created but are marked removed:");
			System.out.println(sortIntegerHashSet(removed));
		}
		HashSet<Integer> yellowTested = (HashSet<Integer>) yellow.clone();
		if (yellowTested.retainAll(failedTests) == true && yellowTested.size() == 0) {
		}
		else {
			System.out.println();
			System.out.println("Warning: tests below were created but are marked yellow triangle.");
			System.out.println(sortIntegerHashSet(yellowTested));
		}
		if (yellow.retainAll(testedRules) == true && yellow.size() == 0) {
		}
		else {
			System.out.println();
			System.out.println("Warning: tests below were created but are marked yellow triangle.");
			System.out.println(sortIntegerHashSet(yellow));
		}
		//System.out.println("tested rules: ");
		//System.out.println(sortIntegerHashSet(testedRules));

		HashSet<Integer> redNotTested = (HashSet<Integer>) red.clone();
		red.retainAll(testedRules); 
		//System.out.println(sortIntegerHashSet(green));
		redNotTested.removeAll(red);
		if (!redNotTested.isEmpty()) {
			System.out.println();
			System.out.println("Red checked rules not tested: ");
			System.out.println(sortIntegerHashSet(redNotTested));
		}

		HashSet<Integer> blueNotTested = (HashSet<Integer>) blue.clone();
		blue.retainAll(testedRules); 
		//System.out.println(sortIntegerHashSet(green));
		blueNotTested.removeAll(blue);
		if (!blueNotTested.isEmpty()) {
			System.out.println();
			System.out.println("Blue partially checked rules not tested: ");
			System.out.println(sortIntegerHashSet(blueNotTested));
		}

		HashSet<Integer> greenNotTested = (HashSet<Integer>) green.clone();
		green.retainAll(testedRules); 
		//System.out.println(sortIntegerHashSet(green));
		greenNotTested.removeAll(green);
		if (!greenNotTested.isEmpty()) {
			System.out.println();
			System.out.println("Green best practice rules not tested: ");
			System.out.println(sortIntegerHashSet(greenNotTested));
		}
	} 

	/**
	 * @param set - a set of validation rules
	 * @return a sorted set of validation rules
	 */
	public static ArrayList<Integer> sortIntegerHashSet(HashSet<Integer> set) {
		ArrayList<Integer> sorted= new ArrayList<Integer>(set);
		Collections.sort(sorted);
		return sorted;
	}
}
