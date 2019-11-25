package org.sbolstandard.core2.examples;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import javax.xml.namespace.QName;
import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.SequenceOntology;

import static org.sbolstandard.core.datatree.Datatree.*;

/**
 * This example shows how to embed application specific custom data into SBOL documents. 
 * A ComponentDefinition entity, representing a promoter, is annotated with information about the promoter's sigma factor and how it is regulated.
 *
 */
public class AnnotationOutput {

	public static void main( String[] args ) throws Exception
    {
		String prURI="http://partsregistry.org/";
		String prPrefix="pr";	
		
		SBOLDocument document = new SBOLDocument();				
		document.setDefaultURIprefix(prURI);	
		document.setTypesInURIs(true);		
		document.addNamespace(URI.create(prURI) , prPrefix);
				
		ComponentDefinition promoter = document.createComponentDefinition(
				"BBa_J23119",
				"",
				new HashSet<URI>(Arrays.asList(URI.create("http://www.biopax.org/release/biopax-level3.owl#DnaRegion"))));
		promoter.addRole(SequenceOntology.PROMOTER);
		promoter.setName("J23119");
		promoter.setDescription("Constitutive promoter");	
		
		promoter.createAnnotation(new QName(prURI, "group", prPrefix),
						"iGEM2006_Berkeley");	

		promoter.createAnnotation(new QName(prURI, "experience", prPrefix), 
				URI.create("http://parts.igem.org/cgi/partsdb/part_info.cgi?part_name=BBa_J23119"));

		Annotation sigmaFactor=new Annotation(QName(prURI, "sigmafactor", prPrefix), 
				"//rnap/prokaryote/ecoli/sigma70");
		Annotation regulation=new Annotation(QName(prURI, "regulation", prPrefix), 
				"//regulation/constitutive");
		promoter.createAnnotation(
				new QName(prURI, "information", prPrefix), 
				new QName(prURI, "Information", prPrefix), 
				"information", 
				new ArrayList<Annotation>(Arrays.asList(sigmaFactor,regulation)));
				
		SBOLWriter.write(document,(System.out));		
    }
}