package org.sbolstandard.core2.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;

public class ModuleDefModuleTree {

	private Random rand = new Random();
	private int maxDepth;
	private int numOfModules;
	private String expectedError;
	
	/**
	 * Address (depth and location on a layer) of the module designated to create a circular reference to 
	 */
	private ArrayList<Integer> addrCirfRef;
	
	public ModuleDefModuleTree(int maxDepth, int numOfModules) {
		this.maxDepth = maxDepth;
		this.numOfModules = numOfModules;
		this.expectedError = null;
		this.addrCirfRef = new ArrayList<Integer>();
	}

	public void setRandomSeed(long seed) {
		rand.setSeed(seed);
	}

	public String generateModel() throws SBOLValidationException, IOException, SBOLConversionException {
		SBOLDocument doc = new SBOLDocument();
		String version = "1.0";

		doc.setDefaultURIprefix("http://sbols.org/CRISPR_Example/");
		doc.setComplete(true);
		doc.setCreateDefaults(true);

		ModuleDefinition md1 = doc.createModuleDefinition("md_top", version);
		ArrayList<String> mdNames = new ArrayList<String>();
		mdNames.add("md_top");
		int depth = maxDepth;
		ArrayList<Integer> address = new ArrayList<Integer>();
		int selectedDepth = randInt(1, maxDepth);
		
		for (int i=0; i<selectedDepth; i++) {
			addrCirfRef.add(randInt(1, numOfModules));
		}
		System.out.println("depth = " + depth);
		System.out.println("mdNames = " + mdNames);
		System.out.println("address = " + address);
		System.out.println("addrSelfRef = " + addrCirfRef);
		createModuleMDGraph(doc, depth, mdNames, address, md1);
		SBOLWriter.write(doc, "TestCircularModules.rdf");
		System.out.println("Finished writing.");
		//SBOLReader.read("/Users/zhangz/libSBOLproject/libSBOLj/examples/TestCircularModules.rdf");
		//SBOLReader.read("/Users/zhangz/libSBOLproject/libSBOLj/core2/src/test/resources/test/data/Validation/sbol-11705.rdf");
		SBOLValidate.validateSBOL(doc, true, true, true);
		if (SBOLValidate.getNumErrors() > 0) {
			for (String error : SBOLValidate.getErrors()) {
				System.out.println("TEST: error = " + error);
			}			
		}
		return expectedError;
	}
	
	public String getExpectedError() {
		return expectedError;
	}

	public void setExpectedError(String expectedError) {
		this.expectedError = expectedError;
	}

	public void createModuleMDGraph(SBOLDocument doc, int depth, ArrayList<String> mdNames, ArrayList<Integer> address, 
			ModuleDefinition parentMd) throws SBOLValidationException {
		//System.out.println("depth = " + depth);		
		if (depth > 0) {
			for (int i=1; i<=numOfModules; i++) {				
				address.add(i);
				System.out.println("address = " + intArrayListToString(address));
				System.out.println("addrSelfRef = " + intArrayListToString(addrCirfRef));
				String mdId ="md_" + intArrayListToString(address);			
				String mId = "m_" + intArrayListToString(address);
				System.out.println("mdId = " + mdId);
				//System.out.println("mId = " + mId);
				if (intArrayListToString(address).equals(intArrayListToString(addrCirfRef))) {
					int circularRefIndex = randInt(0, mdNames.size()-1);
					// int circleRefIndex = mdNames.size()-1; // Force 10704 to occur. 
					System.out.println("circleRefIndex = " + circularRefIndex);
					System.out.println("address = " + intArrayListToString(address));
					System.out.println("addrCirRef = " + intArrayListToString(addrCirfRef));
					System.out.println("mdNames = " + mdNames);
					if (circularRefIndex == mdNames.size() -1 ) {
						System.out.println("Expect 11704");
						expectedError = "11704";
					}
					else {
						System.out.println("Expect 11705");
						expectedError = "11705";
					}
					System.out.println("Last module displayId (mId) = " + mId);
					System.out.println("parentMd displayId = " + parentMd.getDisplayId());
					System.out.println("mdName = " + mdNames.get(circularRefIndex));
					// create the circular reference from module with mId to module definition selected by mdNames.get(circularRefIndex).
					parentMd.createModule(mId, mdNames.get(circularRefIndex)); 
					
				}
				else {
					ModuleDefinition childMd = doc.createModuleDefinition(mdId);
					parentMd.createModule(mId, mdId);				
					mdNames.add(mdId);
					//System.out.println(mdId);
					//System.out.println("Interm. Module displayId = " + m.getDisplayId());
					System.out.println("mdNames = " + mdNames);
					createModuleMDGraph(doc, depth-1, mdNames, address, childMd);
					mdNames.remove(mdId);
				}
				
				address.remove(address.size()-1);				
			}
		}
	}

	private static String intArrayListToString(ArrayList<Integer> address) {
		String str = ""; 
		for (Integer j: address) {
			str = str + j + "_";
		}
		
		return str;		
	}
	
	/**
	 * Returns a pseudo-random number between min and max, inclusive.
	 * The difference between min and max can be at most
	 * <code>Integer.MAX_VALUE - 1</code>.
	 *
	 * @param min Minimum value
	 * @param max Maximum value.  Must be greater than min.
	 * @return Integer between min and max, inclusive.
	 * @see java.util.Random#nextInt(int)
	 */
	public int randInt(int min, int max) {

		// NOTE: This will (intentionally) not run as written so that folks
		// copy-pasting have to think about how to initialize their
		// Random instance.  Initialization of the Random instance is outside
		// the main scope of the question, but some decent options are to have
		// a field that is initialized once and then re-used as needed or to
		// use ThreadLocalRandom (if using at least Java 1.7).
		//Random rand = new Random();

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}
}
