package org.sbolstandard.core2.examples;

import static org.sbolstandard.core.datatree.Datatree.NamespaceBinding;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import org.sbolstandard.core2.Activity;
import org.sbolstandard.core2.Agent;
import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.GenericTopLevel;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.SequenceOntology;

import org.sbolstandard.core.datatree.NamespaceBinding;

/**
 * This example shows how to add provenance information using generic top level entities and custom annotations.
 * A codon optimization of a CDS component from another is documented using PROV-O's Activity, Agent, Usage and Association classes.
 */
public class Provenance_CodonOptimization {
	
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
						
		ComponentDefinition optimizedCds = getCds(document, "codon_optimized","Codon optimised CDS");
		ComponentDefinition sourceCds = getCds(document,"non_codon_optimized", "Non Codon optimised CDS");		
		optimizedCds.addWasDerivedFrom(sourceCds.getIdentity());
		
		//Create the agent definition for the codon optimization software		
		Agent agent=document.createAgent("codon_optimization_software");
		agent.setName("Codon Optimization Software");		
				
		//Create the generic top level entity for the codon optimization activity
		Activity activity=document.createActivity("codon_optimization_activity");
		activity.setName("Codon Optimization Activity");		
		
		//Create the qualifiedUsage annotation to describe the use of the non codon optimized CDS component
		activity.createUsage("usage", sourceCds.getIdentity()).addRole(URI.create("http://sbols.org/v2#source"));
		
		//Create the qualifiedAssociation annotation to describe the use of the software agent used in the activity
		activity.createAssociation("association", agent.getIdentity()).addRole(myAppNs.namespacedUri("codonoptimiser"));
								
		optimizedCds.addWasGeneratedBy(activity.getIdentity());
		SBOLWriter.write(document,System.out);	
    }
}
