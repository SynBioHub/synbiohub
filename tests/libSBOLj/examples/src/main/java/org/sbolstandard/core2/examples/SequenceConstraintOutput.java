package org.sbolstandard.core2.examples;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;

import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.RestrictionType;
import org.sbolstandard.core2.SequenceOntology;

/**
 * This example shows to add constraints between subcomponents of a ComponentDefinition entity using {@link org.sbolstandard.core2.SequenceConstraint} entities.
 * In the example, a promoter has two subcomponents: a core promoter region and and a binding site. 
 * Using a SequenceConstraint entity, it is specified that the core promoter region precedes the binding site.
 *
 */
public class SequenceConstraintOutput {
	public static void main( String[] args ) throws Exception
    {
		String prURI="http://partsregistry.org/";
		
		SBOLDocument document = new SBOLDocument();		
		document.setDefaultURIprefix(prURI);
		document.setTypesInURIs(true);

		ComponentDefinition promoter = document.createComponentDefinition(
				"BBa_K174004",
				"",
				new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA_REGION)));
		promoter.addRole(SequenceOntology.PROMOTER);		
		promoter.setName("pspac promoter");
		promoter.setDescription("LacI repressible promoter");	
		
		ComponentDefinition constPromoter = document.createComponentDefinition(
				"pspac",
				"",
				new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA_REGION)));
		constPromoter.addRole(SequenceOntology.PROMOTER);		
		constPromoter.setName("constitutive promoter");
		constPromoter.setDescription("pspac core promoter region");	
		
		ComponentDefinition operator = document.createComponentDefinition(
				"LacI_operator",
				"",
				new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA_REGION)));				
		operator.addRole(SequenceOntology.OPERATOR);		
		operator.setName("LacI operator");
		operator.setDescription("LacI binding site");	
		
		Component promoterComponent=promoter.createComponent ("promoter",AccessType.PUBLIC, constPromoter.getIdentity());
		Component operatorComponent=promoter.createComponent ("operator",AccessType.PUBLIC, operator.getIdentity());
		
		promoter.createSequenceConstraint(
				 "r1", 
				RestrictionType.PRECEDES, promoterComponent.getIdentity(),operatorComponent.getIdentity() );
				
		SBOLWriter.write(document,(System.out));		
    }
}
