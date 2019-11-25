package org.sbolstandard.core2.examples;

import java.net.URI;
import org.sbolstandard.core2.Model;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.SystemsBiologyOntology;

/**
 * This example shows how to create {@link org.sbolstandard.core2.Model} entities. In the example, the Model entity refers to an ODE model in the SBML format.
 *
 */
public class ModelOutput {
	public static void main( String[] args ) throws Exception
    {
		SBOLDocument document = new SBOLDocument();				
		document.setTypesInURIs(false);		
		document.setDefaultURIprefix("http://www.sbolstandard.org/examples/");
		
		Model model=document.createModel(
				"pIKE_Toggle_1",
				"",
				URI.create("http://virtualparts.org/part/pIKE_Toggle_1"),
				URI.create("http://identifiers.org/edam/format_2585"), 
				SystemsBiologyOntology.CONTINUOUS_FRAMEWORK);
		model.setName("pIKE_Toggle_1 toggle switch");
	
		SBOLWriter.write(document,(System.out));		
    }
}