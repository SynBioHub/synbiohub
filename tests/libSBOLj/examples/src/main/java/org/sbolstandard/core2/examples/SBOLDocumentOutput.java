package org.sbolstandard.core2.examples;

import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLWriter;

/**
 * This example shows how to create an {@link org.sbolstandard.core2.SBOLDocument}.
 *
 */
public class SBOLDocumentOutput {

	public static void main( String[] args ) throws Exception
    {
		SBOLDocument document = new SBOLDocument();
		SBOLWriter.write(document,(System.out));		
    }
}
