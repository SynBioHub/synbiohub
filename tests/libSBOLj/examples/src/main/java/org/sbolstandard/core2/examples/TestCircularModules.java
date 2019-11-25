package org.sbolstandard.core2.examples;

import org.sbolstandard.core2.SBOLValidationException;

public class TestCircularModules {
	/**
	 * number of modules created for each module definition. All module definitions ahve the same number of modules.
	 */
	private static int numOfModules = 5; 
	
	/**
	 * maximum depth for the module definition and module tree. 
	 */
	private static int maxDepth = 4;
	
	
	private static int numOfRuns = 1;//100;
	
	// Check if an SBOLValidationException is thrown in every run, and check if it throws the right exception.
	/**
	 * Tests circular reference between ModuleDefinition and Module (validation rules 11704 and 11705). 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ModuleDefModuleTree tree = new ModuleDefModuleTree(maxDepth, numOfModules);
		tree.setRandomSeed(12);
		for (int i = 0; i < numOfRuns ; i++) {
			boolean exceptionCaught = false;
			//String expectedError = null;
			try {
				 tree.generateModel();				 
			} catch (SBOLValidationException e) {
				exceptionCaught = true;				
				if (!e.getMessage().contains(tree.getExpectedError())) {
					throw new Exception("Unexpected exception!");
				}
			}  
			if (!exceptionCaught) {
				throw new Exception("Exception NOT caught!");
			}
		}
	}
}
