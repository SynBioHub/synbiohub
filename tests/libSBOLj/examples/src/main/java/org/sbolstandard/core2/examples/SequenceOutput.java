package org.sbolstandard.core2.examples;
import java.net.URI;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.Sequence;

/**
 * This example shows how to create {@link org.sbolstandard.core2.Sequence} entities.
 *
 */
public class SequenceOutput {

	public static void main( String[] args ) throws Exception
    {
		String prURI="http://partsregistry.org/";
		
		SBOLDocument document = new SBOLDocument();		
		document.setDefaultURIprefix(prURI);
		document.setTypesInURIs(true);
		
		Sequence seq=document.createSequence(
				"BBa_J23119",
				"",
				"ttgacagctagctcagtcctaggtataatgctagc", 
				URI.create("http://www.chem.qmul.ac.uk/iubmb/misc/naseq.html")
				);
		seq.addWasDerivedFrom(URI.create("http://parts.igem.org/Part:BBa_J23119:Design"));
		
		SBOLWriter.write(document,(System.out));		
    }
	
}
