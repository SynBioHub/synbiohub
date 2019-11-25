package org.sbolstandard.core2.examples;

import static org.sbolstandard.core.datatree.Datatree.NamespaceBinding;
import java.net.URI;
import java.sql.NClob;
import java.util.ArrayList;
import java.util.Arrays;

import org.sbolstandard.core2.Activity;
import org.sbolstandard.core2.Agent;
import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.GenericTopLevel;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.SequenceOntology;

import org.sbolstandard.core.datatree.NamespaceBinding;

/**
 * This example shows how to add provenance information using generic top level entities and custom annotations to derive strains.
 * In the example, provenance is incorporated to document the derivation of the B.subtilis 168 strain form the Ncib 3610 strain using X-ray mutagenesis.
 */
public class Provenance_StrainDerivation {
	
	private static ComponentDefinition getCds(SBOLDocument document, String id, String name) throws SBOLValidationException
	{
		ComponentDefinition cds = document.createComponentDefinition(id, ComponentDefinition.DNA_REGION);
		cds.addRole(SequenceOntology.CDS);
		cds.setName(name);
		return cds;
	}
	
	public static void main( String[] args ) throws Exception
    {		
		NamespaceBinding myAppNs = NamespaceBinding("http://myapp.com/", "myapp");
		
		SBOLDocument document = new SBOLDocument();
		document.addNamespace(URI.create(myAppNs.getNamespaceURI()),myAppNs.getPrefix());		
		document.setDefaultURIprefix(myAppNs.getNamespaceURI());						
		
		ComponentDefinition b168 = getCds(document, "bsubtilis168","Bacillus subtilis 168");
		ComponentDefinition b3610 = getCds(document,"bsubtilisncimb3610", "Bacillus subtilis NCIMB 3610");		
		b168.addWasDerivedFrom(b3610.getIdentity());
				
		//Create the agent definition to represent X-ray		
		Agent agent=document.createAgent("x_ray");
		agent.setName("X-ray");		
				
		//Create the generic top level entity for the X-ray mutagenesis activity
		Activity activity=document.createActivity("xraymutagenesis");
		activity.setName("X-ray mutagenesis");		
		
		//Create the qualifiedUsage annotation to describe the use of the parent strain 
		activity.createUsage("usage", b3610.getIdentity()).addRole(URI.create("http://sbols.org/v2#source"));
		
		//Create the qualifiedAssociation annotation to describe the use of the agent used in the activity
		activity.createAssociation("association",agent.getIdentity()).addRole( myAppNs.namespacedUri("mutagen"));
		
		b168.addWasGeneratedBy(activity.getIdentity());
		
		SBOLWriter.write(document,System.out);	

    }
}
