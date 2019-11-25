package org.sbolstandard.core2;

import static org.sbolstandard.core.datatree.Datatree.NamespaceBinding;

import javax.xml.namespace.QName;

import org.sbolstandard.core.datatree.NamespaceBinding;
/**
 * Provides qualified names for sbol1.0 objects.
 * 
 * @author Tramy Nguyen
 * @author Matthew Pocock
 * @author Goksel Misirli
 * @author Chris Myers
 * @version 2.1
 */
class Sbol1Terms
{

	/**
	 * The namespacebinding for SBOL1.0
	 */
	static final NamespaceBinding sbol1 = NamespaceBinding("http://sbols.org/v1#", "");
	static final NamespaceBinding rdf = NamespaceBinding("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");

	/**
	 * A group of qualified terms for Collection related SBOL objects
	 *
	 */
	static final class Collection {
		static final QName Collection  = sbol1.withLocalPart("Collection");
		static final QName uri   	  = sbol1.withLocalPart("uri");
		static final QName displayId   = sbol1.withLocalPart("displayId");
		static final QName name	      = sbol1.withLocalPart("name");
		static final QName description = sbol1.withLocalPart("description");
		static final QName component  = sbol1.withLocalPart("component");
	}

	/**
	 * A group of qualified terms for DNAComponent related SBOL objects
	 *
	 */
	static final class DNAComponent {
		static final QName DNAComponent = sbol1.withLocalPart("DnaComponent");
		static final QName uri   	   = sbol1.withLocalPart("uri");
		static final QName displayId    = sbol1.withLocalPart("displayId");
		static final QName name	   	   = sbol1.withLocalPart("name");
		static final QName description  = sbol1.withLocalPart("description");
		static final QName type   	   = rdf.withLocalPart("type");
		static final QName annotations  = sbol1.withLocalPart("annotation");
		static final QName dnaSequence  = sbol1.withLocalPart("dnaSequence");

	}

	/**
	 * A group of qualified terms for DNASequence related SBOL objects
	 *
	 */
	static final class DNASequence {
		static final QName DNASequence = sbol1.withLocalPart("DnaSequence");
		static final QName uri   	  = sbol1.withLocalPart("uri");
		static final QName nucleotides = sbol1.withLocalPart("nucleotides");
	}

	/**
	 * A group of qualified terms for SequenceAnnotations related SBOL objects
	 *
	 */
	static final class SequenceAnnotations {
		static final QName SequenceAnnotation  = sbol1.withLocalPart("SequenceAnnotation");
		static final QName uri   	   		  = sbol1.withLocalPart("uri");
		static final QName bioStart	   		  = sbol1.withLocalPart("bioStart");
		static final QName bioEnd	   		  = sbol1.withLocalPart("bioEnd");
		static final QName strand	   		  = sbol1.withLocalPart("strand");
		static final QName subComponent 		  = sbol1.withLocalPart("subComponent");
		static final QName precedes	   		  = sbol1.withLocalPart("precedes");
	}


}