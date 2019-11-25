package org.sbolstandard.core2.examples;

import static org.sbolstandard.core.datatree.Datatree.NamespaceBinding;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;

import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.SystemsBiologyOntology;

import org.sbolstandard.core.datatree.NamespaceBinding;

/**
 * This example shows to create simple {@link org.sbolstandard.core2.ModuleDefinition} entities. 
 * The module includes an interaction entity to represent the translation of a protein.
 *
 */
public class SimpleModuleDefinition {
	private static final NamespaceBinding example=NamespaceBinding ("http://sbolstandard.org/example/", "example");	

	public static void main( String[] args ) throws Exception
    {
		SBOLDocument document = new SBOLDocument();		
		
		setDefaultNameSpace(document, example.getNamespaceURI());		
		document.setTypesInURIs(true);
		
		ModuleDefinition module=document.createModuleDefinition("GFP_expression");
		FunctionalComponent  cds=module.createFunctionalComponent(
				"Constitutive_GFP", 
				AccessType.PUBLIC, 
				URI.create("http://sbolstandard.org/example/GFP_generator"),
				DirectionType.IN);
				
		FunctionalComponent  protein =module.createFunctionalComponent(
				"GFP_protein", 
				AccessType.PUBLIC, 
				URI.create("http://sbolstandard.org/example/GFP"),
				DirectionType.OUT);
		
		Interaction interaction=module.createInteraction("express_GFP", new HashSet<URI>(Arrays.asList(URI.create("Transcription"))));
		interaction.createParticipation("CDS", cds.getIdentity(),SystemsBiologyOntology.PROMOTER);
		interaction.createParticipation("Protein", protein.getIdentity(),SystemsBiologyOntology.PRODUCT);
		
			
		SBOLWriter.write(document,(System.out));	
    }
	
	private static void setDefaultNameSpace(SBOLDocument document, String uri) throws SBOLValidationException
	{
		if (uri.endsWith("/"))
		{
			uri=uri.substring(0,uri.length()-1);
		}
		document.setDefaultURIprefix(uri);
	}	
}