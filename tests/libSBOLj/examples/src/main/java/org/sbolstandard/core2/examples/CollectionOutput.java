package org.sbolstandard.core2.examples;

import java.net.URI;

import org.sbolstandard.core2.Collection;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;

/**
 * This examples shows how to create {@link org.sbolstandard.core2.Collection} entities. 
 * The Collection entity in the example is used to represent a library of promoters.
 *
 */
public class CollectionOutput {

	/**
	 * @param args
	 * @throws SBOLValidationException see SBOL validation rule violation at {@link Collection#addMember(URI)}
	 * @throws SBOLConversionException
	 */
	public static void main( String[] args ) throws SBOLValidationException, SBOLConversionException
    {
		SBOLDocument document = new SBOLDocument();				
		document.setDefaultURIprefix("http://parts.igem.org/Promoters/Catalog");		
		document.setTypesInURIs(false);		
		
		Collection col=document.createCollection("Anderson","");		
		col.setName("Anderson promoters");
		col.setDescription("The Anderson promoter collection");				
		col.addMember(URI.create("http://partsregistry.org/Part:BBa_J23119"));
		col.addMember(URI.create("http://partsregistry.org/Part:BBa_J23118"));	
				
		SBOLWriter.write(document,(System.out));
		
    }
}