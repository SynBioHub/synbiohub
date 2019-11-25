package org.sbolstandard.core2;

import static org.sbolstandard.core.datatree.Datatree.NamespaceBinding;

import javax.xml.namespace.QName;

import org.sbolstandard.core.datatree.NamespaceBinding;
/**
 * Provides qualified names for SBOL2.0 objects.
 * 
 * @author Tramy Nguyen
 * @author Matthew Pocock
 * @author Goksel Misirli
 * @author Chris Myers
 * @version 2.1
 */
class Sbol2Terms
{

	/**
	 * The namespace binding for SBOL2.0
	 */
	public static final NamespaceBinding rdf = NamespaceBinding("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
	public static final NamespaceBinding sbol2 = NamespaceBinding("http://sbols.org/v2#", "sbol");
	public static final NamespaceBinding dc = NamespaceBinding("http://purl.org/dc/terms/", "dcterms");
	public static final NamespaceBinding prov = NamespaceBinding("http://www.w3.org/ns/prov#", "prov");
	public static final NamespaceBinding om = NamespaceBinding("http://www.ontology-of-units-of-measure.org/resource/om-2/", "om");

	static final class Description {
		static final QName Description = rdf.withLocalPart("Description");
		static final QName type = rdf.withLocalPart("type");
	}
	
	/**
	 * A group of qualified terms for Activity related SBOL objects
	 *
	 */
	static final class Activity {
		static final QName Activity = prov.withLocalPart("Activity");
		static final QName type 	  	   = sbol2.withLocalPart("type");
		static final QName startedAtTime   = prov.withLocalPart("startedAtTime");
		static final QName endedAtTime	   = prov.withLocalPart("endedAtTime");
		static final QName wasInformedBy   = prov.withLocalPart("wasInformedBy");
		static final QName qualifiedAssociation = prov.withLocalPart("qualifiedAssociation");
		static final QName qualifiedUsage  = prov.withLocalPart("qualifiedUsage");
	}
	
	/**
	 * A group of qualified terms for Agent related SBOL objects
	 *
	 */
	static final class Agent {
		static final QName Agent = prov.withLocalPart("Agent");
	}

	/**
	 * A group of qualified terms for Annotation related SBOL objects
	 *
	 */
	static final class Annotation {
		static final QName Annotation = sbol2.withLocalPart("Annotation");
		static final QName relation   = sbol2.withLocalPart("relation");
		static final QName value	     = sbol2.withLocalPart("value");
	}
	
	/**
	 * A group of qualified terms for Association related SBOL objects
	 *
	 */
	static final class Association {
		static final QName Association = prov.withLocalPart("Association");
		static final QName role = prov.withLocalPart("hadRole");
		static final QName plan = prov.withLocalPart("hadPlan");
		static final QName agent = prov.withLocalPart("agent");
	}

	/**
	 * A group of qualified terms for Component related SBOL objects
	 *
	 */
	static final class Component {
		static final QName Component = sbol2.withLocalPart("Component");
		static final QName roles 	 = sbol2.withLocalPart("role");
		static final QName roleIntegration = sbol2.withLocalPart("roleIntegration");
		static final QName sourceLocation = sbol2.withLocalPart("sourceLocation");
	}

	/**
	 * A group of qualified terms for Collection related SBOL objects
	 *
	 */
	static final class Collection {
		static final QName Collection = sbol2.withLocalPart("Collection");
		static final QName hasMembers = sbol2.withLocalPart("member");
		//		  static final QName access 	   = sbol2.withLocalPart("access");
	}

	/**
	 * A group of qualified terms for Experiment related SBOL objects
	 *
	 */
	static final class Experiment {
		static final QName Experiment = sbol2.withLocalPart("Experiment");
		static final QName hasExperimentalData = sbol2.withLocalPart("experimentalData");
		//		  static final QName access 	   = sbol2.withLocalPart("access");
	}

	/**
	 * A group of qualified terms for ExperimentalData related SBOL objects
	 *
	 */
	static final class ExperimentalData {
		static final QName ExperimentalData = sbol2.withLocalPart("ExperimentalData");
	}

	/**
	 * A group of qualified terms for ComponentDefinition related SBOL objects
	 *
	 */
	static final class ComponentDefinition {
		static final QName ComponentDefinition    = sbol2.withLocalPart("ComponentDefinition");
		static final QName type 	  	 		  = sbol2.withLocalPart("type");
		static final QName roles 	  	 		  = sbol2.withLocalPart("role");
		static final QName hasSequence			  = sbol2.withLocalPart("sequence");
		static final QName hasSequenceAnnotations = sbol2.withLocalPart("sequenceAnnotation");
		static final QName hasSequenceConstraints = sbol2.withLocalPart("sequenceConstraint");
		static final QName hasComponent = sbol2.withLocalPart("component"); 
		static final QName hasSubComponent = sbol2.withLocalPart("subComponent"); 
	}

	/**
	 * A group of qualified terms for ComponentInstance related SBOL objects
	 *
	 */
	static final class ComponentInstance {
		static final QName ComponentInstance 	   = sbol2.withLocalPart("componentInstance");
		static final QName access 				   = sbol2.withLocalPart("access");
		static final QName hasMapsTo 		   	   = sbol2.withLocalPart("mapsTo");
		static final QName hasComponentDefinition  = sbol2.withLocalPart("definition");
	}

	/**
	 * A group of qualified terms for Combinatorial Derivation related SBOL objects
	 *
	 */
	static final class CombinatorialDerivation {
		static final QName CombinatorialDerivation = sbol2.withLocalPart("CombinatorialDerivation");
		static final QName template                = sbol2.withLocalPart("template");
		static final QName strategy                = sbol2.withLocalPart("strategy");
		static final QName hasVariableComponent  = sbol2.withLocalPart("variableComponent");
	}
	
	/**
	 * A group of qualified terms for Variable Component related SBOL objects
	 *
	 */
	static final class VariableComponent {
		static final QName VariableComponent  = sbol2.withLocalPart("VariableComponent");
		static final QName hasOperator           = sbol2.withLocalPart("operator");
		static final QName hasVariable           = sbol2.withLocalPart("variable");
		static final QName hasVariants           = sbol2.withLocalPart("variant");
		static final QName hasVariantCollections = sbol2.withLocalPart("variantCollection");
		static final QName hasVariantDerivations = sbol2.withLocalPart("variantDerivation");
	}
	
	/**
	 * A group of qualified terms for Implementation related SBOL objects
	 *
	 */
	static final class Implementation {
		static final QName Implementation		 = sbol2.withLocalPart("Implementation");
		static final QName built				 = sbol2.withLocalPart("built");
	}
	
	/**
	 * A group of qualified terms for Attachment related SBOL objects
	 *
	 */
	static final class Attachment {
		static final QName Attachment		 	 = sbol2.withLocalPart("Attachment");
		static final QName source				 = sbol2.withLocalPart("source");
		static final QName format				 = sbol2.withLocalPart("format");
		static final QName size					 = sbol2.withLocalPart("size");
		static final QName hash					 = sbol2.withLocalPart("hash");
	}
	
	/**
	 * A group of qualified terms for Cut related SBOL objects
	 *
	 */
	static final class Cut {
		static final QName Cut 		  = sbol2.withLocalPart("Cut");
		static final QName at 		  = sbol2.withLocalPart("at");
		static final QName orientation = sbol2.withLocalPart("orientation");
	}

	/**
	 * A group of qualified terms for FunctionalComponent related SBOL objects
	 *
	 */
	static final class FunctionalComponent {
		static final QName FunctionalComponent = sbol2.withLocalPart("FunctionalComponent");
		static final QName direction   	   	  = sbol2.withLocalPart("direction");
	}

	static final class GenericLocation {
		static final QName GenericLocation = sbol2.withLocalPart("GenericLocation");
		static final QName orientation = sbol2.withLocalPart("orientation");
		// TODO: this is only here for backwards compatibility with a bug
		static final QName Orientation = sbol2.withLocalPart("Orientation");
	}

	/**
	 * A group of qualified terms for TopLevel related SBOL objects
	 *
	 */
	static final class GenericTopLevel {
		static final QName GenericTopLevel = sbol2.withLocalPart("GenericTopLevel");
		static final QName rdfType 		  = sbol2.withLocalPart("rdfType");
	}

	/**
	 * A group of qualified terms for Identified related SBOL objects
	 *
	 */
	static final class Identified {
		static final QName Identified 	   	 = sbol2.withLocalPart("Identified");
		//static final QName identity   	   	 = sbol2.withLocalPart("identity");
		static final QName persistentIdentity = sbol2.withLocalPart("persistentIdentity");
		static final QName version   	 = sbol2.withLocalPart("version");
		static final QName timeStamp   	     = sbol2.withLocalPart("timeStamp");
		static final QName hasAnnotations 	 = sbol2.withLocalPart("annotation");
		static final QName wasDerivedFrom	 = prov.withLocalPart("wasDerivedFrom");
		static final QName wasGeneratedBy	 = prov.withLocalPart("wasGeneratedBy");
		static final QName displayId   = sbol2.withLocalPart("displayId");
		static final QName title 	  = dc.withLocalPart("title");
		static final QName description = dc.withLocalPart("description");
	}
	
	/**
	 * A group of qualified terms for TopLevel related SBOL objects
	 *
	 */
	static final class TopLevel {
		static final QName TopLevel 	   	 = sbol2.withLocalPart("TopLevel");
		static final QName hasAttachment   	 = sbol2.withLocalPart("attachment");
	}

	/**
	 * A group of qualified terms for Interaction related SBOL objects
	 *
	 */
	static final class Interaction {
		static final QName Interaction 	    = sbol2.withLocalPart("Interaction");
		static final QName type 			    = sbol2.withLocalPart("type");
		static final QName hasParticipations = sbol2.withLocalPart("participation");
	}

	/**
	 * A group of qualified terms for Location related SBOL objects
	 *
	 */
	static final class Location {
		static final QName Location = sbol2.withLocalPart("location");
		static final QName sequence = sbol2.withLocalPart("sequence");
	}

	/**
	 * A group of qualified terms for Measured related SBOL objects
	 *
	 */
	static final class Measured {
		static final QName Measured = sbol2.withLocalPart("Measured");
		static final QName hasMeasure = sbol2.withLocalPart("measure");
	}

	/**
	 * A group of qualified terms for Measured related SBOL objects
	 *
	 */
	static final class Measure {
		static final QName Measure = om.withLocalPart("Measure");
		static final QName hasNumericalValue = om.withLocalPart("hasNumericalValue");
		static final QName hasUnit = om.withLocalPart("hasUnit");
		static final QName type = sbol2.withLocalPart("type");
	}

	/**
	 * A group of qualified terms for MapsTo related SBOL objects
	 *
	 */
	static final class MapsTo {
		static final QName MapsTo 	 = sbol2.withLocalPart("MapsTo");
		static final QName refinement = sbol2.withLocalPart("refinement");
		static final QName hasRemote  = sbol2.withLocalPart("remote");
		static final QName hasLocal   = sbol2.withLocalPart("local");
	}

	/**
	 * A group of qualified terms for Model related SBOL objects
	 *
	 */
	static final class Model {
		static final QName Model	    = sbol2.withLocalPart("Model");
		static final QName source	= sbol2.withLocalPart("source");
		static final QName language  = sbol2.withLocalPart("language");
		static final QName framework = sbol2.withLocalPart("framework");
		static final QName roles	    = sbol2.withLocalPart("role");

	}

	/**
	 * A group of qualified terms for ModuleDefinition related SBOL objects
	 *
	 */
	static final class ModuleDefinition {
		static final QName ModuleDefinition 	  = sbol2.withLocalPart("ModuleDefinition");
		static final QName roles			   	  = sbol2.withLocalPart("role");
		static final QName hasModule	       	  = sbol2.withLocalPart("module");
		static final QName hasSubModule	       	  = sbol2.withLocalPart("subModule");
		static final QName hasInteractions  	  = sbol2.withLocalPart("interaction");
		static final QName hasModels 	   		  = sbol2.withLocalPart("model");
		static final QName hasfunctionalComponent = sbol2.withLocalPart("functionalComponent");

	}

	/**
	 * A group of qualified terms for Module related SBOL objects
	 *
	 */
	static final class Module {
		static final QName Module 				= sbol2.withLocalPart("Module");
		static final QName hasMapsTo			    = sbol2.withLocalPart("mapsTo");
		static final QName hasMapping			    = sbol2.withLocalPart("mapping");
		static final QName hasDefinition		    = sbol2.withLocalPart("definition");
		//static final QName hasInstantiatedModule = sbol2.withLocalPart("instantiatedModule");
	}

	/**
	 * A group of qualified terms for multirange related SBOL objects
	 *
	 */
	static final class MultiRange {
		static final QName MultiRange = sbol2.withLocalPart("MultiRange");
		static final QName hasRanges  = sbol2.withLocalPart("range");
	}


	/**
	 * A group of qualified terms for Participation related SBOL objects
	 *
	 */
	static final class Participation {
		static final QName Participation  = sbol2.withLocalPart("Participation");
		static final QName role 		   	 = sbol2.withLocalPart("role");
		static final QName hasParticipant = sbol2.withLocalPart("participant");

	}
	
	/**
	 * A group of qualified terms for Plan related SBOL objects
	 *
	 */
	static final class Plan {
		static final QName Plan = prov.withLocalPart("Plan");
	}

	/**
	 * A group of qualified terms for Range related SBOL objects
	 *
	 */
	static final class Range {
		static final QName Range 	  = sbol2.withLocalPart("Range");
		static final QName start 	  = sbol2.withLocalPart("start");
		static final QName end   	  = sbol2.withLocalPart("end");
		static final QName orientation = sbol2.withLocalPart("orientation");
	}

	//
	/**
	 * A group of qualified terms for Sequence related SBOL objects
	 *
	 */
	static final class Sequence {
		static final QName Sequence = sbol2.withLocalPart("Sequence");
		static final QName elements = sbol2.withLocalPart("elements");
		static final QName encoding = sbol2.withLocalPart("encoding");

	}
	//
	/**
	 * A group of qualified terms for SequenceAnnotation related SBOL objects
	 *
	 */
	static final class SequenceAnnotation {
		static final QName SequenceAnnotation = sbol2.withLocalPart("SequenceAnnotation");
		static final QName hasComponent 		 = sbol2.withLocalPart("component");
		static final QName hasLocation 		 = sbol2.withLocalPart("location");
		static final QName roles 	  	 		  = sbol2.withLocalPart("role");
//		static final QName roleIntegration = sbol2.withLocalPart("roleIntegration");
	}

	/**
	 * A group of qualified terms for SequenceConstraint related SBOL objects
	 *
	 */
	static final class SequenceConstraint {
		static final QName SequenceConstraint = sbol2.withLocalPart("SequenceConstraint");
		static final QName restriction 	     = sbol2.withLocalPart("restriction");
		static final QName hasSubject 		 = sbol2.withLocalPart("subject");
		static final QName hasObject 		 = sbol2.withLocalPart("object");
	}
	
	/**
	 * A group of qualified terms for Plan related SBOL objects
	 *
	 */
	static final class Usage {
		static final QName Usage = prov.withLocalPart("Usage");
		static final QName entity = prov.withLocalPart("entity");
		static final QName role = prov.withLocalPart("hadRole");
	}

//	static final class SequenceURI {
//		static final URI encoding 	  = URI.create(sbol2.getNamespaceURI() + "encoding");
//		static final URI DnaSequenceV1 = URI.create("http://dx.doi.org/10.1021/bi00822a023");
//	}

//	static final class DnaComponentV1URI {
//		static final URI roles = URI.create("http://purl.obolibrary.org/obo/SO_0000804");
//		static final URI type  = URI.create("http://www.biopax.org/release/biopax-level3.owl#DnaRegion");
//		//static final URI restriction  = URI.create(sbol2.getNamespaceURI() + "precedes");
//	}

	// Moved to ComponentInstance
	//	static final class Access {
	//		static final URI PUBLIC  = URI.create(sbol2.getNamespaceURI() + "public");
	//		static final URI PRIVATE = URI.create(sbol2.getNamespaceURI() + "private");
	//	}

	// Moved to FunctionalComponent
	//	static final class Direction {
	//		static final URI input  = URI.create(sbol2.getNamespaceURI() + "input");
	//		static final URI output = URI.create(sbol2.getNamespaceURI() + "output");
	//		static final URI inout  = URI.create(sbol2.getNamespaceURI() + "inout");
	//		static final URI none   = URI.create(sbol2.getNamespaceURI() + "none");
	//	}

}
