package org.sbolstandard.core2;

import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Methods to test GenBank/SBOL conversion.
 * @author Ernst Oberortner
 * @author Chris Myers
 */
public class GenbankTest {

	private static final String NEWLINE = System.lineSeparator();
	
	/**
	 * @param directory - test file directory
	 * @return list of files in test directory
	 */
	public static List<String> fileList(String directory) {
        List<String> fileNames = new ArrayList<>();
        
        Path dirPath = Paths.get(directory);
        if(!Files.isDirectory(dirPath)) {
        	return fileNames;
        }
        
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dirPath)) {
            for (Path path : directoryStream) {
            	if(!Files.isDirectory(path)) {
            		fileNames.add(path.toString());
            	}
            }
        } catch (IOException ex) {}
        
        return fileNames;
    }
	
	/**
	 * Test parsing multiline labal that contains SO terms.
	 */
	@Test
	public void testParseMultilineLabelContainsSOTerm() {
		String genbank = 
				"LOCUS       simple                    56 bp    DNA     circular      29-MAR-2016" + NEWLINE +
				"FEATURES             Location/Qualifiers                        " + NEWLINE +
				"     CDS             1..56                                      " + NEWLINE +
				"                     /note=\"this is a very interesting and yet not studied " + NEWLINE +
				"                     gene since it does not start with a typical start codon " + NEWLINE +
				"                     and its length is not a multiple of 3\"" + NEWLINE +
				"ORIGIN"+ NEWLINE +
				"         1 CGTGGAAACC GTTCGAGAGC AAAAATCATA GTGGAATAAC ATTTAGTCTT GATAGT" + NEWLINE +
				"//";		
		
		try (
				BufferedInputStream bis =
					new BufferedInputStream(new ByteArrayInputStream(genbank.getBytes()));
			) {
			
			SBOLReader.setURIPrefix("http://synbio.jgi.doe.gov/");
			
			SBOLDocument doc = SBOLReader.read(bis);
			
			// two CDs
			assertTrue(null != doc.getComponentDefinitions());
			//assertTrue(doc.getComponentDefinitions().size() == 2);
					// -- one CD for the CDS ("child")
					// -- one CD for the entire sequence ("parent")
			
			// two Sequences
			assertTrue(null != doc.getSequences());
			//assertTrue(doc.getSequences().size() == 2);
		} catch(Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	/**
	 * Test parsing of multi-line labels.
	 */
	@Test
	public void testParseMultilineLabels() {
		String genbank = 
				"LOCUS       simple                    56 bp    DNA     circular      PRI 29-MAR-2016" + NEWLINE +
				//"LOCUS       simple                    60 bp    DNA     circular      29-MAR-2016" + NEWLINE +
				"FEATURES             Location/Qualifiers                        " + NEWLINE +
				"     CDS             1..60                                      " + NEWLINE +
				"                     /gene=\"dnaN\"                             " + NEWLINE +
				"                     /locus_tag=\"MMCAP2_0002\"" + NEWLINE +
				"                     /EC_number=\"2.7.7.7\"" + NEWLINE +
				"                     /note=\"identified by match to protein family HMM PF00712;" + NEWLINE +
				"                     match to protein family HMM PF02767; match to protein " + NEWLINE +
				"                     family HMM PF02768; match to protein family HMM TIGR00663\"" + NEWLINE +
				"                     /codon_start=1" + NEWLINE +
				"                     /transl_table=4" + NEWLINE +
				"                     /product=\"DNA polymerase III, beta subunit\"" + NEWLINE +
				"                     /protein_id=\"ACU79029.1\"" + NEWLINE +
				"                     /db_xref=\"GI:256384459\"" + NEWLINE +
				"                     /translation=\"MNFSINRMVLLDNLSKAAKVIDPKNVNPSLAGIYLNVLSDQVNI" + NEWLINE +
				"                     IATSGILSFKSILNNQNSDLEVKQEGKVLLKPKFVLEMLRRLDDEFVVFSMVEDNELI" + NEWLINE +
				"                     IKTDNSDFSIGVLNSEDYPLIGFREKGIEFNLNPKEVKKTIYQVFVSMNENNKKLILT" + NEWLINE +
				"                     GLNLKLNNNKAIFSTTDSFRISQKILEIQSDNNEDIDITIPFKTALELPKLLDNAENL" + NEWLINE +
				"                     KIIIVEGYITFIIDNVIFQSNLIDGRFPNVQIAFPTKFETIITVKQKSILKVLSRFDL" + NEWLINE +
				"                     VADDGLPAIVNIKVNEDKIEFKSFISEVGKYEEDFDDFVIEGNKSLSISFNTRFLIDA" + NEWLINE +
				"                     IKTLDEDRIELKLINSTKPIVINNVYDEHLKQVILPTFLSN\"" + NEWLINE +
				"ORIGIN"+ NEWLINE +
				"         1 CGTGGAAACC GTTCGAGAGC AAAAATCATA GTGGAATAAC ATTTAGTCTT GATAGTTCCA" + NEWLINE +
				"//";		
		
		try (
				BufferedInputStream bis =
					new BufferedInputStream(new ByteArrayInputStream(genbank.getBytes()));
			) {
			
			SBOLReader.setURIPrefix("http://synbio.jgi.doe.gov/");
			
			SBOLDocument doc = SBOLReader.read(bis);
			
			// two CDs
			assertTrue(null != doc.getComponentDefinitions());
			assertTrue(doc.getComponentDefinitions().size() == 1);
					// -- one CD for the CDS ("child")
					// -- one CD for the entire sequence ("parent")
			
			// two Sequences
			assertTrue(null != doc.getSequences());
			assertTrue(doc.getSequences().size() == 1);
			
			for(ComponentDefinition cd : doc.getComponentDefinitions()) {
				assertTrue(null != cd.getSequenceAnnotations());
				
				assertTrue(null != cd.getComponents());

				if(!cd.getComponents().isEmpty()) {
					// "parent"
					// has exactly 1 sequence annotation
					assertTrue(cd.getSequenceAnnotations().size() == 1);
					
					
					SequenceAnnotation sa = new ArrayList<SequenceAnnotation>(cd.getSequenceAnnotations()).get(0);
					
					assertTrue(null != sa.getAnnotations());
					assertTrue(sa.getAnnotations().size() == 10);

					for(Annotation anno : sa.getAnnotations()) {
						
						String expectedValue = "";
						if("".equals(anno.getQName().getLocalPart())) {
						} else if("note".equals(anno.getQName().getLocalPart())) {
						}
						
						switch(anno.getQName().getLocalPart()) {
						case "gene":
							expectedValue = "\"dnaN\"";
							break;
						case "locus_tag":
							expectedValue = "\"MMCAP2_0002\"";
							break;
						case "EC_number":
							expectedValue = "\"2.7.7.7\"";
							break;
						case "note":
							expectedValue = "\"identified by match to protein family HMM PF00712; match to protein family HMM PF02767; match to protein family HMM PF02768; match to protein family HMM TIGR00663\"";
							break;
						case "codon_start":
							expectedValue = "1";
							break;
						case "transl_table":
							expectedValue = "4";
							break;
						case "product":
							expectedValue = "\"DNA polymerase III, beta subunit\"";
							break;
						case "protein_id":
							expectedValue = "\"ACU79029.1\"";
							break;
						case "db_xref":
							expectedValue = "\"GI:256384459\"";
							break;
						case "translation":
							expectedValue = "\"MNFSINRMVLLDNLSKAAKVIDPKNVNPSLAGIYLNVLSDQVNIIATSGILSFKSILNNQNSDLEVKQEGKVLLKPKFVLEMLRRLDDEFVVFSMVEDNELIIKTDNSDFSIGVLNSEDYPLIGFREKGIEFNLNPKEVKKTIYQVFVSMNENNKKLILTGLNLKLNNNKAIFSTTDSFRISQKILEIQSDNNEDIDITIPFKTALELPKLLDNAENLKIIIVEGYITFIIDNVIFQSNLIDGRFPNVQIAFPTKFETIITVKQKSILKVLSRFDLVADDGLPAIVNIKVNEDKIEFKSFISEVGKYEEDFDDFVIEGNKSLSISFNTRFLIDAIKTLDEDRIELKLINSTKPIVINNVYDEHLKQVILPTFLSN\"";
							break;
						}
						
						assertTrue(expectedValue.equals(anno.getStringValue()));
					}

				} else {
					// "child"
					// does not have sequence annotations
					//assertTrue(cd.getSequenceAnnotations().isEmpty());
				}
			}
		} catch(Exception e) {
			assertTrue(false);
		}
	}
	
/////////////////////////////////////////
// while the Travis-CI issues persist, 
// we're keeping the testGenomeGenbanks() test commented out
/////////////////////////////////////////
//	@Test
//	public void testGenomeGenbanks() {
//		try {
//			// for every file in the directory
//			for(String filename : fileList("./src/test/resources/test/data/GenBank/genomes")) {
//				Path file = Paths.get(filename);
//				
//				// ignore sub-directories for the time being
//				if(Files.isDirectory(file)) { continue; }
//				
//				// parse the file
//				GenBank.read(new BufferedInputStream(Files.newInputStream(file)));
//				
//			}
//		} catch(Exception e) {
//			assertTrue(false);	// no exception allowed
//		}
//	}
	
}
