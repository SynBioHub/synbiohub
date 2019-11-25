package org.sbolstandard.core2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.synbiohub.frontend.SynBioHubException;
import org.synbiohub.frontend.SynBioHubFrontend;

class readTester {
	static String filenameXml 	= "writeTesterString_v1.3.xml";
	static String filenameJson   = "writeTesterString_v1.3.json";
	static String filenameTurtle = "writeTesterString_v1.3.ttl";

	static String filenameV1_1 	= "SBOL1/partial_pIKE_left_cassette.xml";
	static String filenameV1_2 	= "SBOL1/partial_pIKE_right_casette.xml";
	static String filenameV1_3 	= "SBOL1/partial_pIKE_right_cassette.xml";
	static String filenameV1_4 	= "SBOL1/partial_pTAK_left_cassette.xml";
	static String filenameV1_5 	= "SBOL1/partial_pTAK_right_cassette.xml";
	static String filenameV1_6 	= "SBOL1/pIKE_pTAK_cassettes 2.xml";
	static String filenameV1_7 	= "SBOL1/pIKE_pTAK_cassettes.xml";
	static String filenameV1_8 	= "SBOL1/pIKE_pTAK_left_right_cassettes.xml";
	static String filenameV1_9 	= "SBOL1/pIKE_pTAK_toggle_switches.xml";
	static String filenameV1_10 	= "SBOL1/miRNA_sbol.xml";
	static String filenameV1_11 	= "SBOL1/labhost_All.xml";
	static String filenameV1_12 	= "SBOL1/BBa_I0462.xml";
	static String filenameV1_13 	= "SBOL1/pACPc_invF.xml";
	static String filenameV1_14 	= "SBOL1/precedesTest.xml";
	static String filenameV1_15 	= "ComponentDefinitionOutput.xml";
	static String filenameV1_16 	= "SimpleComponentDefinitionExample.xml";
	static String filenameV1_17 	= "namespace.xml";
	static String filenameV1_18 	= "SBOL1/SBOL1and2Test.xml";
	static String filenameV1_19 	= "toggle.xml";


	static String path = "/Users/myers/git/libSBOLj/core2/src/test/resources/";

	public static void main(String[] args) throws SBOLValidationException, IOException, SBOLConversionException, SynBioHubException {
		SBOLReader.setURIPrefix("http://dummy.org");
		SBOLDocument doc = SBOLReader.read("/Users/myers/Downloads/BBa_F2620-4.xml");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		doc.write(out,SBOLDocument.GFF3format);
		doc = SBOLReader.read(new ByteArrayInputStream(out.toByteArray()));
		doc.write(System.out,SBOLDocument.GFF3format);

//		File dir = new File("/Users/myers/Downloads/GFF_v3_34_strains/");
//		File[] directoryListing = dir.listFiles();
//		if (directoryListing != null) {
//			for (File child : directoryListing) {
//				System.out.println("Reading: " + child.getAbsolutePath());
//				SBOLDocument doc = SBOLReader.read(child);
//				System.out.println("Writing: " + child.getAbsolutePath().replace(".gff", ".xml"));
//				doc.write(System.out,SBOLDocument.GFF3format);
//				break;
//				//doc.write(child.getAbsolutePath().replace(".gff", ".xml"));
//		    }
//		} 

//		long submitStartTime = System.currentTimeMillis();
//		SynBioHubFrontend sbh = new SynBioHubFrontend("http://localhost:7777", "https://synbiohub.org");
//		sbh.login("myers@ece.utah.edu", "test");
//		String result = sbh.sparqlAdminQuery("SELECT ?s ?p ?o FROM <https://synbiohub.org/user/test> WHERE { ?s ?p ?o. }");
//		System.out.println(result);

//		SBOLDocument doc = SBOLValidate.validate(System.out,
//				System.err, 
//				"/Users/myers/Downloads/testannotation.xml", 
//				"http://dummy.org/", "", false, false, false, false, "1", false, "", "", 
//				"/Users/myers/Downloads/testannotation.xml", 
//				"",	false, false, false, false, null, false, true, false);
//		
//		doc = doc.changeURIPrefixVersion("https://synbiohub.org", null, "1");
//		
//		long submitEndTime = System.currentTimeMillis();
//		double submitDuration = (submitEndTime - submitStartTime) * 1.0 / 1000;
//
//		System.out.println("Change URI prefex Time (in sec): " + submitDuration);
//		doc.write(System.out);
	}
}
