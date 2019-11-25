package org.sbolstandard.core2.examples;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import javax.xml.namespace.QName;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.GenericTopLevel;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.SequenceOntology;

/**
 * This example shows embedding custom application specific data into SBOL documents as top-level entities. Such data are represented using {@link org.sbolstandard.core2.GenericTopLevel} entities 
 * and can be referenced from other SBOL entities via custom annotations.
 *
 */
public class GenericTopLevelOutput {

	public static void main( String[] args ) throws Exception
    {
		String myAppURI="http://www.myapp.org/";
		String myAppPrefix="myapp";
		String prURI="http://www.partsregistry.org/";
				
		SBOLDocument document = new SBOLDocument();		
		document.addNamespace(URI.create(myAppURI+ "/") , myAppPrefix);		
		document.setDefaultURIprefix(prURI);
		document.setTypesInURIs(true);
		
		GenericTopLevel topLevel=document.createGenericTopLevel(
				"datasheet1",
				"",
				new QName("http://www.myapp.org", "Datasheet", myAppPrefix)
				);
		topLevel.setName("Datasheet 1");		
		topLevel.createAnnotation(new QName(myAppURI, "characterizationData", myAppPrefix), 
				URI.create(myAppURI + "/measurement/1"));				
		topLevel.createAnnotation(new QName(myAppURI, "transcriptionRate", myAppPrefix), "1");			
		
		ComponentDefinition promoter = document.createComponentDefinition(
				"BBa_J23119",
				"",
				new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA_REGION)));		
		promoter.addRole(SequenceOntology.PROMOTER);
		promoter.setName("J23119");
		promoter.setDescription("Constitutive promoter");					
		promoter.createAnnotation(new QName(myAppURI, "datasheet", myAppPrefix), topLevel.getIdentity());
		promoter.addWasDerivedFrom(URI.create("http://www.partsregistry.org/Part:BBa_J23119"));		
		
		SBOLWriter.write(document,(System.out));		
    }
}