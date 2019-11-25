package org.sbolstandard.core2;

import static org.sbolstandard.core.datatree.Datatree.NamedProperty;
import static org.sbolstandard.core.datatree.Datatree.NamespaceBinding;
import static org.sbolstandard.core.datatree.Datatree.QName;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//import javax.sound.midi.Sequence;
import javax.xml.namespace.QName;

import org.junit.Test;

/**
 * Abstract tests used by SBOLReaderTest, SBOLWriterTest, and SBOLGenerateFile.
 * @author Tramy Nguyen
 * @author Meher Samineni
 * @author Eugene Choe
 * @author Chris Myers
 *
 */
public abstract class SBOLAbstractTests {

	String VERSION_1_0 = "1.0";
	String URIprefix = "http://www.async.ece.utah.edu";

	/**
	 * Test model remove method.
	 * @throws SBOLValidationException 
	 * @throws IOException 
	 * @throws SBOLConversionException 
	 */
	@Test
	public void test_Model_remove() throws SBOLValidationException, SBOLConversionException, IOException 
	{
		String prURI="http://partsregistry.org/";
		String prPrefix="pr";
		SBOLDocument document = new SBOLDocument();
		document.setDefaultURIprefix(prURI);
		document.setTypesInURIs(true);
		document.addNamespace(URI.create(prURI), prPrefix);

		String M1_ID = "ID";
		String M1_Version = "1.0";
		String M1_Source = "www.example.com";
		URI M1_URISource = URI.create(M1_Source);

		Model M1 = document.createModel(M1_ID, M1_Version, M1_URISource, EDAMOntology.SBML,
				SystemsBiologyOntology.CONTINUOUS_FRAMEWORK);
		document.removeModel(M1);
		runTest("/SBOLTestSuite/SBOL2/test_Model_remove.xml", document, true);
	}

	/**
	 * Test sequence remove method.
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_Sequence_remove() throws SBOLValidationException, SBOLConversionException, IOException 
	{
		String prURI="http://partsregistry.org/";
		String prPrefix="pr";
		SBOLDocument document = new SBOLDocument();
		document.setDefaultURIprefix(prURI);
		document.setTypesInURIs(true);
		document.addNamespace(URI.create(prURI), prPrefix);

		String SeqID = "ID";
		String SeqVersion = "1.0";
		String SeqElements = "Element";
		URI SeqEncoding = URI.create("www.example.com");
		Sequence Seq = document.createSequence(SeqID, SeqVersion, SeqElements, SeqEncoding);
		document.removeSequence(Seq);
		runTest("/SBOLTestSuite/SBOL2/test_Sequence_remove.xml", document, true);
	}


	/**
	 * Test source_location method.
	 * @throws SBOLValidationException
	 * @throws IOException
	 * @throws SBOLConversionException
	 */
	@Test
	public void test_source_location() throws SBOLValidationException, SBOLConversionException, IOException
	{
		String prURI="http://partsregistry.org/";
		String prPrefix="pr";
		SBOLDocument document = new SBOLDocument();
		document.setDefaultURIprefix(prURI);
		document.setTypesInURIs(true);
		document.addNamespace(URI.create(prURI), prPrefix);

		ComponentDefinition cd_comp = document.createComponentDefinition(
				"cd_comp",
				"",
				ComponentDefinition.DNA_REGION);

		cd_comp.addRole(SequenceOntology.PROMOTER);
		cd_comp.setName("cd_comp");
		cd_comp.setDescription("Constitutive promoter");

		ComponentDefinition cd_base1 = document.createComponentDefinition(
				"cd_base_1",
				         "",
				         ComponentDefinition.DNA_REGION);
		cd_base1.addRole(SequenceOntology.PROMOTER);

		ComponentDefinition cd_base2 = document.createComponentDefinition(
				"cd_base_2",
				"",
				ComponentDefinition.DNA_REGION);
		cd_base2.addRole(SequenceOntology.PROMOTER);


		// SBOLTestUtils.addPRSequence(document, promoter,"aaagacaggacc");
		Sequence seq_base1 = document.createSequence(
				"seq_base1",
				"",
				"ttgacagctagctcagtcctaggtataatgctagc",
				Sequence.IUPAC_DNA
		);

		Sequence seq_base2 = document.createSequence(
				"seq_base2",
				"",
				"ttgacagctagctcagtcctaggtataatgctagttagcgc",
				Sequence.IUPAC_DNA
		);

		Sequence seq_comp = document.createSequence(
				"seq_comp",
				"",
				"acagctagctcacagctagctc",
				Sequence.IUPAC_DNA
		);

		cd_base1.addSequence(seq_base1.getIdentity());
		cd_base2.addSequence(seq_base2.getIdentity());

		Component base1_insert_c = cd_comp.createComponent("base1", AccessType.PUBLIC, cd_base1.getIdentity());
		Component base2_insert_c = cd_comp.createComponent("base2", AccessType.PUBLIC, cd_base2.getIdentity());

		base1_insert_c.addSourceRange("seq_base1", 4, 14, OrientationType.INLINE);
		base2_insert_c.addSourceRange("seq_base2", 4, 14, OrientationType.INLINE);

		cd_comp.addSequence(seq_comp);

		SequenceAnnotation sa_base1 = cd_comp.createSequenceAnnotation("base_s1", "base_c1", 1, 11);
		sa_base1.setComponent(base1_insert_c.getIdentity());
		SequenceAnnotation sa_base2 = cd_comp.createSequenceAnnotation("base_s2", "base_c2", 12, 22);
		sa_base2.setComponent(base2_insert_c.getIdentity());

		Location seq_loc1 = sa_base1.getLocation("base_c1");
		seq_loc1.setSequence(seq_comp.getIdentity());

		Location seq_loc2 = sa_base2.getLocation("base_c2");
		seq_loc2.setSequence(seq_comp.getIdentity());

		runTest("/SBOLTestSuite/SBOL2/test_source_location.xml", document, true);
	}


	/**
	 * Test Collection remove method
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_Collection_remove() throws SBOLValidationException, SBOLConversionException, IOException 
	{
		String prURI="http://partsregistry.org/";
		String prPrefix="pr";
		SBOLDocument document = new SBOLDocument();
		document.setDefaultURIprefix(prURI);
		document.setTypesInURIs(true);
		document.addNamespace(URI.create(prURI), prPrefix);

		String Col1_ID = "ID";
		String Col1_Version = "1.0";
		Collection Col1 = document.createCollection(Col1_ID, Col1_Version);
		document.removeCollection(Col1);
		runTest("/SBOLTestSuite/SBOL2/test_Collection_remove.xml", document, true);
	}

	/**
	 * Test module definition remove method.
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_ModuleDefinition_remove() throws SBOLValidationException, SBOLConversionException, IOException 
	{
		String prURI="http://partsregistry.org/";
		String prPrefix="pr";
		SBOLDocument document = new SBOLDocument();
		document.setDefaultURIprefix(prURI);
		document.setTypesInURIs(true);
		document.addNamespace(URI.create(prURI), prPrefix);

		String MD_ID = "ID";
		String MD_Version = "1.0";
		ModuleDefinition MD = document.createModuleDefinition(MD_ID, MD_Version);
		document.removeModuleDefinition(MD);
		runTest("/SBOLTestSuite/SBOL2/test_ModuleDefinition_remove.xml", document, true);
	}

	/**
	 * Test GenericTopLevel remove method
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_ActivityType() throws SBOLValidationException, SBOLConversionException, IOException
	{
		String actURI="http://www.myactivity.org/";
		String actPrefix="activity";
		String prURI="http://www.partsregistry.org/";

		SBOLDocument document = new SBOLDocument();
		document.setDefaultURIprefix(prURI);
		document.setTypesInURIs(true);
		Activity act = document.createActivity("activityId");

		//Activity act = new Activity(URI.create(actURI));
		act.addType(URI.create("http://purl.obolibrary.org/obo/OBI_0000415"));
		act.addType(URI.create("http://purl.obolibrary.org/obo/CHEBI_23924"));
		
		runTest("/SBOLTestSuite/SBOL2/ActivityTypeOutput.xml", document, true);
	}

	/**
	 * Test ComponentDefinition remove method
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_ComponentDefinition_remove() throws SBOLValidationException, SBOLConversionException, IOException 
	{
		String prURI="http://partsregistry.org/";
		String prPrefix="pr";
		SBOLDocument document = new SBOLDocument();
		document.setDefaultURIprefix(prURI);
		document.setTypesInURIs(true);
		document.addNamespace(URI.create(prURI), prPrefix);

		String CD_ID = "ID";
		String CD_Version = "1.0";
		Set<URI> CD_Types = new HashSet<URI>();
		CD_Types.add(URI.create("www.example.com"));
		ComponentDefinition CD = document.createComponentDefinition(CD_ID, CD_Version, CD_Types);
		document.removeComponentDefinition(CD);
		runTest("/SBOLTestSuite/SBOL2/test_ComponentDefinition_remove.xml", document, true);
	}

	/**
	 * Test GenericTopLevel remove method
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_GenericTopLevel_remove() throws SBOLValidationException, SBOLConversionException, IOException
	{
		String prURI="http://partsregistry.org/";
		String prPrefix="pr";
		SBOLDocument document = new SBOLDocument();
		document.setDefaultURIprefix(prURI);
		document.setTypesInURIs(true);
		document.addNamespace(URI.create(prURI), prPrefix);

		String GTL_ID = "ID";
		String GTL_Version = "1.0";
		String GTL_Qname = "name";
		GenericTopLevel GTL = document.createGenericTopLevel(GTL_ID, GTL_Version, new QName(prURI, "group", prPrefix));
		document.removeGenericTopLevel(GTL);
		runTest("/SBOLTestSuite/SBOL2/test_GenericTopLevel_remove.xml", document, true);
	}


	/**
	 * Test annotation output.
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_AnnotationOutput() throws SBOLValidationException, SBOLConversionException, IOException
	{
		String prURI="http://partsregistry.org/";
		String prPrefix="pr";
		SBOLDocument document = new SBOLDocument();

		document.setDefaultURIprefix(prURI);
		document.setTypesInURIs(true);

		document.addNamespace(URI.create(prURI), prPrefix);



		ComponentDefinition promoter = document.createComponentDefinition(
				"BBa_J23119",
				"",
				new HashSet<URI>(Arrays.asList(URI.create("http://www.biopax.org/release/biopax-level3.owl#DnaRegion"))));

		promoter.addRole(SequenceOntology.PROMOTER);
		promoter.setName("J23119");
		promoter.setDescription("Constitutive promoter");

		promoter.createAnnotation(new QName(prURI, "group", prPrefix),
				"iGEM2006_Berkeley");

		promoter.createAnnotation(new QName(prURI, "experience", prPrefix),
				URI.create("http://parts.igem.org/cgi/partsdb/part_info.cgi?part_name=BBa_J23119"));

		Annotation sigmaFactor=new Annotation(QName(prURI, "sigmafactor", prPrefix),
				"//rnap/prokaryote/ecoli/sigma70");
		Annotation regulation=new Annotation(QName(prURI, "regulation", prPrefix),
				"//regulation/constitutive");
		promoter.createAnnotation(
				new QName(prURI, "information", prPrefix),
				new QName(prURI, "Information", prPrefix),
				"information",
				new ArrayList<Annotation>(Arrays.asList(sigmaFactor,regulation)));

		//		SBOLWriter.write(document,(System.out));
		runTest("/SBOLTestSuite/SBOL2/AnnotationOutput.xml", document, true);
	}

	/**
	 * Test collection output.
	 * @throws SBOLValidationException see SBOL validation rule violation at {@link Collection#addMember(URI)}
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_CollectionOutput() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		//Collection col=document.createCollection(URI.create("http://parts.igem.org/Promoters/Catalog/Anderson"));

		document.setDefaultURIprefix("http://parts.igem.org/Promoters/Catalog");
		document.setTypesInURIs(false);
		Collection col=document.createCollection("Anderson","");

		col.setName("Anderson promoters");
		col.setDescription("The Anderson promoter collection");
		col.addMember(URI.create("http://partsregistry.org/Part:BBa_J23119"));
		col.addMember(URI.create("http://partsregistry.org/Part:BBa_J23118"));

		//		SBOLWriter.write(document,(System.out));
		runTest("/SBOLTestSuite/SBOL2_ic/CollectionOutput.xml", document, true);
	}

	/**
	 * Test ComponentDefinition Output
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_ComponentDefinitionOutput() throws SBOLValidationException, SBOLConversionException, IOException
	{
		String prURI="http://partsregistry.org/";

		String prPrefix="pr";
		SBOLDocument document = new SBOLDocument();
		document.setTypesInURIs(true);
		document.addNamespace(URI.create(prURI), prPrefix);
		document.setDefaultURIprefix(prURI);
		/*Sequence seqdevice=document.createSequence(
					"BBa_F2620",
				     "",
					 "",
					URI.create("http://www.chem.qmul.ac.uk/iubmb/misc/naseq.html")
					);
		 */

		Sequence seqpTetR=document.createSequence(
				"BBa_R0040",
				"",
				"tccctatcagtgatagagattgacatccctatcagtgatagagatactgagcac",
				Sequence.IUPAC_DNA
				);

		Sequence seqRbs=document.createSequence(
				"BBa_B0034",
				"",
				"aaagaggagaaa",
				Sequence.IUPAC_DNA
				);

		Sequence seqCds=document.createSequence(
				"BBa_C0062",
				"",
				"atgcttatctgatatgactaaaatggtacattgtgaatattatttactcgcgatcatttatcctcattctatggttaaatctgatatttcaatcctagataattaccctaaaaaatggaggcaatattatgatgacgctaatttaataaaatatgatcctatagtagattattctaactccaatcattcaccaattaattggaatatatttgaaaacaatgctgtaaataaaaaatctccaaatgtaattaaagaagcgaaaacatcaggtcttatcactgggtttagtttccctattcatacggctaacaatggcttcggaatgcttagttttgcacattcagaaaaagacaactatatagatagtttatttttacatgcgtgtatgaacataccattaattgttccttctctagttgataattatcgaaaaataaatatagcaaataataaatcaaacaacgatttaaccaaaagagaaaaagaatgtttagcgtgggcatgcgaaggaaaaagctcttgggatatttcaaaaatattaggttgcagtgagcgtactgtcactttccatttaaccaatgcgcaaatgaaactcaatacaacaaaccgctgccaaagtatttctaaagcaattttaacaggagcaattgattgcccatactttaaaaattaataacactgatagtgctagtgtagatcac",
				Sequence.IUPAC_DNA
				);

		Sequence seqTer=document.createSequence(
				"BBa_B0015",
				"",
				"ccaggcatcaaataaaacgaaaggctcagtcgaaagactgggcctttcgttttatctgttgtttgtcggtgaacgctctctactagagtcacactggctcaccttcgggtgggcctttctgcgtttata",
				Sequence.IUPAC_DNA
				);

		Sequence seqPluxR=document.createSequence(
				"BBa_R0062",
				"",
				"acctgtaggatcgtacaggtttacgcaagaaaatggtttgttatagtcgaataaa",
				Sequence.IUPAC_DNA
				);

		ComponentDefinition pTetR = document.createComponentDefinition(
				"BBa_R0040",
				"",
				new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA_REGION)));

		pTetR.addRole(SequenceOntology.PROMOTER);
		pTetR.setName("BBa_R0040");
		pTetR.setDescription("TetR repressible promoter");
		pTetR.addSequence(seqpTetR.getIdentity());

		ComponentDefinition rbs = document.createComponentDefinition(
				"BBa_B0034",
				"",
				new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA_REGION)));


		rbs.addRole(SequenceOntology.RIBOSOME_ENTRY_SITE);
		rbs.setName("BBa_B0034");
		rbs.setDescription("RBS based on Elowitz repressilator");
		rbs.addSequence(seqRbs.getIdentity());

		ComponentDefinition cds = document.createComponentDefinition(
				"BBa_C0062",
				"",
				new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA_REGION)));
		cds.addRole(SequenceOntology.CDS);
		cds.setName("BBa_C0062");
		cds.setDescription("luxR coding sequence");
		cds.addSequence(seqCds.getIdentity());

		ComponentDefinition ter = document.createComponentDefinition(
				"BBa_B0015",
				"",
				new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA_REGION)));

		ter.addRole(URI.create("http://identifiers.org/so/SO:0000141"));
		ter.setName("BBa_B0015");
		ter.setDescription("Double terminator");
		ter.addSequence(seqTer.getIdentity());

		ComponentDefinition pluxR = document.createComponentDefinition(
				"BBa_R0062",
				"",
				new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA_REGION)));
		pluxR.addRole(SequenceOntology.PROMOTER);//
		pluxR.setName("BBa_R0062");
		pluxR.setDescription("LuxR inducible promoter");
		pluxR.addSequence(seqPluxR.getIdentity());


		ComponentDefinition device = document.createComponentDefinition(
				"BBa_F2620",
				"",
				new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA_REGION)));
		device.addRole(URI.create("http://identifiers.org/so/SO:0001411"));//biological region
		device.setName("BBa_F2620");
		device.setDescription("3OC6HSL -> PoPS Receiver");
		//device.addSequence(seqdevice.getIdentity());

		Component comPtetR=device.createComponent("pTetR", AccessType.PUBLIC, pTetR.getIdentity());
		Component comRbs=device.createComponent("rbs", AccessType.PUBLIC,cds.getIdentity());
		Component comCds=device.createComponent("luxR", AccessType.PUBLIC, rbs.getIdentity());
		Component comTer=device.createComponent("ter", AccessType.PUBLIC, ter.getIdentity());
		Component comPluxR=device.createComponent( "pLuxR", AccessType.PUBLIC, pluxR.getIdentity());


		int start=1;
		int end=seqPluxR.getElements().length();

		SequenceAnnotation anno=device.createSequenceAnnotation("anno1", "range", start, end, OrientationType.INLINE);
		anno.setComponent(comPtetR.getIdentity());

		start=end+1;
		end=seqRbs.getElements().length() + end + 1;
		SequenceAnnotation anno2= device.createSequenceAnnotation("anno2", "range", start,end,OrientationType.INLINE);
		anno2.setComponent(comRbs.getIdentity());

		start=end+1;
		end=seqCds.getElements().length() + end + 1;
		SequenceAnnotation anno3= device.createSequenceAnnotation("anno3", "range", start,end,OrientationType.INLINE);
		anno3.setComponent(comCds.getIdentity());

		start=end+1;
		end=seqTer.getElements().length() + end + 1;
		SequenceAnnotation anno4= device.createSequenceAnnotation("anno4", "range", start,end,OrientationType.INLINE);
		anno4.setComponent(comTer.getIdentity());

		start=end+1;
		end=seqPluxR.getElements().length() + end + 1;
		SequenceAnnotation anno5= device.createSequenceAnnotation("anno5", "range", start,end,OrientationType.INLINE);
		anno5.setComponent(comPluxR.getIdentity());

		//			SBOLWriter.write(document,(System.out));
		runTest("/SBOLTestSuite/SBOL2/ComponentDefinitionOutput.xml", document, true);
	}


	/**
	 * Test Cut location
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_CutExample() throws SBOLValidationException, SBOLConversionException, IOException
	{
		String prURI="http://partsregistry.org/";
		SBOLDocument document = new SBOLDocument();
		document.setDefaultURIprefix(prURI);
		document.setTypesInURIs(true);
		ComponentDefinition promoter = document.createComponentDefinition(
				"BBa_J23119",
				"",
				new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA_REGION)));
		promoter.addRole(SequenceOntology.PROMOTER);
		promoter.addRole(URI.create("http://identifiers.org/so/SO:0000613"));

		promoter.setName("J23119 promoter");
		promoter.setDescription("Constitutive promoter");
		promoter.addWasDerivedFrom(URI.create("http://partsregistry.org/Part:BBa_J23119"));

		document.setDefaultURIprefix(prURI);
		Sequence seq=document.createSequence(
				"BBa_J23119",
				"",
				"ttgacagctagctcagtcctaggtataatgctagc",
				URI.create("http://www.chem.qmul.ac.uk/iubmb/misc/naseq.html")
				);
		seq.addWasDerivedFrom(URI.create("http://parts.igem.org/Part:BBa_J23119:Design"));
		promoter.addSequence(seq.getIdentity());

		//promoter.createSequenceAnnotation("cut", 10);
		promoter.createSequenceAnnotation("cutat10", "cut", 10, OrientationType.INLINE);
		promoter.createSequenceAnnotation("cutat12", "cut", 12, OrientationType.INLINE);

		//			SBOLWriter.write(document,(System.out));
		runTest("/SBOLTestSuite/SBOL2_bp/CutExample.xml", document, true);
	}

	/**
	 * Test GenericTopLevel output
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_GenericTopLevelOutput() throws SBOLValidationException, SBOLConversionException, IOException
	{
		String myAppURI="http://www.myapp.org/";
		String myAppPrefix="myapp";
		String prURI="http://www.partsregistry.org/";

		SBOLDocument document = new SBOLDocument();
		document.addNamespace(URI.create(myAppURI) , myAppPrefix);

		document.setDefaultURIprefix(prURI);
		document.setTypesInURIs(true);

		GenericTopLevel topLevel=document.createGenericTopLevel(
				"datasheet1",
				"",
				new QName("http://www.myapp.org/", "Datasheet", myAppPrefix)
				);
		topLevel.setName("Datasheet 1");

		topLevel.createAnnotation(new QName(myAppURI, "characterizationData", myAppPrefix),
				URI.create(myAppURI + "/measurement/1"));


		topLevel.createAnnotation(new QName(myAppURI, "transcriptionRate", myAppPrefix), "1");


		ComponentDefinition promoter = document.createComponentDefinition(
				"BBa_J23119",
				"",
				new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA_REGION)));

		promoter.addRole(SequenceOntology.PROMOTER);
		promoter.setName("J23119");
		promoter.setDescription("Constitutive promoter");

		promoter.createAnnotation(new QName(myAppURI, "datasheet", myAppPrefix), topLevel.getIdentity());
		promoter.addWasDerivedFrom(URI.create("http://www.partsregistry.org/Part:BBa_J23119"));

		runTest("/SBOLTestSuite/SBOL2/GenericTopLevelOutput.xml", document, true);
	}

	/**
	 * Test locatio to sequence output.
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_LocationToSeqeunce() throws SBOLValidationException, SBOLConversionException, IOException
	{
		String prURI="http://partsregistry.org/";
		String prPrefix="pr";
		SBOLDocument document = new SBOLDocument();

		document.setDefaultURIprefix(prURI);
		document.setTypesInURIs(true);

		document.addNamespace(URI.create(prURI), prPrefix);



		ComponentDefinition promoter = document.createComponentDefinition(
				"BBa_J23119",
				"",
				new HashSet<URI>(Arrays.asList(URI.create("http://www.biopax.org/release/biopax-level3.owl#DnaRegion"))));

		promoter.addRole(SequenceOntology.PROMOTER);
		promoter.setName("J23119");
		promoter.setDescription("Constitutive promoter");

		promoter.createAnnotation(new QName(prURI, "group", prPrefix),
				"iGEM2006_Berkeley");

		promoter.createAnnotation(new QName(prURI, "experience", prPrefix),
				URI.create("http://parts.igem.org/cgi/partsdb/part_info.cgi?part_name=BBa_J23119"));

		Annotation sigmaFactor=new Annotation(QName(prURI, "sigmafactor", prPrefix),
				"//rnap/prokaryote/ecoli/sigma70");
		Annotation regulation=new Annotation(QName(prURI, "regulation", prPrefix),
				"//regulation/constitutive");
		promoter.createAnnotation(
				new QName(prURI, "information", prPrefix),
				new QName(prURI, "Information", prPrefix),
				"information",
				new ArrayList<Annotation>(Arrays.asList(sigmaFactor,regulation)));

		SequenceAnnotation sa = promoter.createSequenceAnnotation("someSequenceAnnotation", "range", 1, 10);

		sa.addGenericLocation("generic_location", OrientationType.INLINE);
		Location loc = sa.getLocation("range");

		// SBOLTestUtils.addPRSequence(document, promoter,"aaagacaggacc");
		Sequence seq1 = document.createSequence(
				"BBa_J23120",
				"",
				"ttgacagctagctcagtcctaggtataatgctagc",
				URI.create("http://www.chem.qmul.ac.uk/iubmb/misc/naseq.html")
		);

		Sequence seq2 = document.createSequence(
				"BBa_J23121",
				"",
				"ttgacagctagctcagtcctaggtataatgctagttagcgc",
				URI.create("http://www.chem.qmul.ac.uk/iubmb/misc/naseq.html")
		);

		seq1.addWasDerivedFrom(URI.create("http://parts.igem.org/Part:BBa_J23120:Design"));
		seq2.addWasDerivedFrom(URI.create("http://parts.igem.org/Part:BBa_J23121:Design"));
		promoter.addSequence(seq1.getIdentity());
		promoter.addSequence(seq2.getIdentity());

		loc.setSequence(seq1.getIdentity());

		runTest("/SBOLTestSuite/SBOL2_bp/LocationToSequenceOutput.xml", document, true);
	}

	/**
	 * Test model output
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_ModelOutput() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();

		document.setTypesInURIs(false);

		document.setDefaultURIprefix("http://www.sbolstandard.org/examples");

		Model model=document.createModel(
				"pIKE_Toggle_1",
				"",
				URI.create("http://virtualparts.org/part/pIKE_Toggle_1"),
				URI.create("http://identifiers.org/edam/format_2585"),
				SystemsBiologyOntology.CONTINUOUS_FRAMEWORK);
		model.setName("pIKE_Toggle_1 toggle switch");


		//		SBOLWriter.write(document,(System.out));
		runTest("/SBOLTestSuite/SBOL2/ModelOutput.xml", document, true);
	}


	/**
	 * Test ModuleDefinition output
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_ModuleDefinitionOutput() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();

		setDefaultNameSpace(document, SBOLTestUtils.pr.getNamespaceURI());
		ComponentDefinition gfp     = SBOLTestUtils.createComponenDefinition(document, SBOLTestUtils.pr.withLocalPart("BBa_E0040"),"gfp", ComponentDefinition.DNA_REGION, SequenceOntology.CDS, "gfp coding sequence");
		ComponentDefinition tetR    = SBOLTestUtils.createComponenDefinition(document, SBOLTestUtils.pr.withLocalPart("BBa_C0040"),"tetR", ComponentDefinition.DNA_REGION, SequenceOntology.CDS, "tetR coding sequence");
		ComponentDefinition lacI    = SBOLTestUtils.createComponenDefinition(document, SBOLTestUtils.pr.withLocalPart("BBa_C0012"),"lacI", ComponentDefinition.DNA_REGION, SequenceOntology.CDS, "lacI coding sequence");
		ComponentDefinition placI   = SBOLTestUtils.createComponenDefinition(document, SBOLTestUtils.pr.withLocalPart("BBa_R0010"), "pLacI", ComponentDefinition.DNA_REGION, SequenceOntology.PROMOTER, "pLacI promoter");
		ComponentDefinition ptetR   = SBOLTestUtils.createComponenDefinition(document, SBOLTestUtils.pr.withLocalPart("BBa_R0040"),"pTetR", ComponentDefinition.DNA_REGION, SequenceOntology.PROMOTER, "pTet promoter");
		ComponentDefinition rbslacI = SBOLTestUtils.createComponenDefinition(document, SBOLTestUtils.pr.withLocalPart("BBa_J61101"), "BBa_J61101 RBS",ComponentDefinition.DNA_REGION, SequenceOntology.RIBOSOME_ENTRY_SITE, "RBS1");
		ComponentDefinition rbstetR = SBOLTestUtils.createComponenDefinition(document, SBOLTestUtils.pr.withLocalPart("BBa_J61120"), "BBa_J61101 RBS",ComponentDefinition.DNA_REGION, SequenceOntology.RIBOSOME_ENTRY_SITE, "RBS2");
		ComponentDefinition rbsgfp  = SBOLTestUtils.createComponenDefinition(document, SBOLTestUtils.pr.withLocalPart("BBa_J61130"), "BBa_J61101 RBS",ComponentDefinition.DNA_REGION, SequenceOntology.RIBOSOME_ENTRY_SITE, "RBS2");

		setDefaultNameSpace(document, SBOLTestUtils.uniprot.getNamespaceURI());
		//ComponentDefinition GFP  =
		SBOLTestUtils.createComponenDefinition(document, SBOLTestUtils.uniprot.withLocalPart("P42212"), "GFP",ComponentDefinition.PROTEIN, SystemsBiologyOntology.PRODUCT, "GFP protein");
		ComponentDefinition TetR = SBOLTestUtils.createComponenDefinition(document, SBOLTestUtils.uniprot.withLocalPart("Q6QR72"), "TetR",ComponentDefinition.PROTEIN, SystemsBiologyOntology.INHIBITOR, "TetR protein");
		ComponentDefinition LacI = SBOLTestUtils.createComponenDefinition(document, SBOLTestUtils.uniprot.withLocalPart("P03023"),"LacI", ComponentDefinition.PROTEIN, SystemsBiologyOntology.INHIBITOR, "LacI protein");

		setDefaultNameSpace(document, SBOLTestUtils.pr.getNamespaceURI());
		ComponentDefinition lacITerminator = SBOLTestUtils.createComponenDefinition(document, SBOLTestUtils.pr.withLocalPart("ECK120029600"),"ECK120029600", ComponentDefinition.DNA_REGION, SequenceOntology.TERMINATOR, "Terminator1");
		ComponentDefinition tetRTerminator = SBOLTestUtils.createComponenDefinition(document, SBOLTestUtils.pr.withLocalPart("ECK120033736"), "ECK120033736",ComponentDefinition.DNA_REGION, SequenceOntology.TERMINATOR, "Terminator2");

		setDefaultNameSpace(document, SBOLTestUtils.vpr.getNamespaceURI());
		ComponentDefinition tetRInverter = SBOLTestUtils.createComponenDefinition(document, SBOLTestUtils.vpr.withLocalPart("pIKELeftCassette_1"), "TetR Inverter", ComponentDefinition.DNA_REGION, SequenceOntology.ENGINEERED_GENE, "TetR Inverter");
		ComponentDefinition lacIInverter = SBOLTestUtils.createComponenDefinition(document, SBOLTestUtils.vpr.withLocalPart("pIKERightCassette_1"), "LacI Inverter", ComponentDefinition.DNA_REGION, SequenceOntology.ENGINEERED_GENE, "LacI Inverter");
		ComponentDefinition toggleSwitch = SBOLTestUtils.createComponenDefinition(document, SBOLTestUtils.vpr.withLocalPart("pIKE_Toggle_1"), "LacI/TetR Toggle Swicth", ComponentDefinition.DNA_REGION, SequenceOntology.ENGINEERED_GENE, "LacI/TetR Toggle Swicth");

		//tetR inverter sequences
		SBOLTestUtils.addPRSequence(document, ptetR,"tccctatcagtgatagagattgacatccctatcagtgatagagatactgagcac");
		SBOLTestUtils.addPRSequence(document, rbslacI,"aaagacaggacc");
		SBOLTestUtils.addPRSequence(document, lacI,"atggtgaatgtgaaaccagtaacgttatacgatgtcgcagagtatgccggtgtctcttatcagaccgtttcccgcgtggtgaaccaggccagccacgtttctgcgaaaacgcgggaaaaagtggaagcggcgatggcggagctgaattacattcccaaccgcgtggcacaacaactggcgggcaaacagtcgttgctgattggcgttgccacctccagtctggccctgcacgcgccgtcgcaaattgtcgcggcgattaaatctcgcgccgatcaactgggtgccagcgtggtggtgtcgatggtagaacgaagcggcgtcgaagcctgtaaagcggcggtgcacaatcttctcgcgcaacgcgtcagtgggctgatcattaactatccgctggatgaccaggatgccattgctgtggaagctgcctgcactaatgttccggcgttatttcttgatgtctctgaccagacacccatcaacagtattattttctcccatgaagacggtacgcgactgggcgtggagcatctggtcgcattgggtcaccagcaaatcgcgctgttagcgggcccattaagttctgtctcggcgcgtctgcgtctggctggctggcataaatatctcactcgcaatcaaattcagccgatagcggaacgggaaggcgactggagtgccatgtccggttttcaacaaaccatgcaaatgctgaatgagggcatcgttcccactgcgatgctggttgccaacgatcagatggcgctgggcgcaatgcgcgccattaccgagtccgggctgcgcgttggtgcggatatctcggtagtgggatacgacgataccgaagacagctcatgttatatcccgccgttaaccaccatcaaacaggattttcgcctgctggggcaaaccagcgtggaccgcttgctgcaactctctcagggccaggcggtgaagggcaatcagctgttgcccgtctcactggtgaaaagaaaaaccaccctggcgcccaatacgcaaaccgcctctccccgcgcgttggccgattcattaatgcagctggcacgacaggtttcccgactggaaagcgggcaggctgcaaacgacgaaaactacgctttagtagcttaataa");
		SBOLTestUtils.addPRSequence(document, lacITerminator,"ttcagccaaaaaacttaagaccgccggtcttgtccactaccttgcagtaatgcggtggacaggatcggcggttttcttttctcttctcaa");

		//lacI inverter sequences
		SBOLTestUtils.addPRSequence(document, placI,"tccctatcagtgatagagattgacatccctatcagtgatagagatactgagcac");
		SBOLTestUtils.addPRSequence(document, rbstetR,"aaagacaggacc");
		SBOLTestUtils.addPRSequence(document, tetR,"atgtccagattagataaaagtaaagtgattaacagcgcattagagctgcttaatgaggtcggaatcgaaggtttaacaacccgtaaactcgcccagaagctaggtgtagagcagcctacattgtattggcatgtaaaaaataagcgggctttgctcgacgccttagccattgagatgttagataggcaccatactcacttttgccctttagaaggggaaagctggcaagattttttacgtaataacgctaaaagttttagatgtgctttactaagtcatcgcgatggagcaaaagtacatttaggtacacggcctacagaaaaacagtatgaaactctcgaaaatcaattagcctttttatgccaacaaggtttttcactagagaatgcattatatgcactcagcgctgtggggcattttactttaggttgcgtattggaagatcaagagcatcaagtcgctaaagaagaaagggaaacacctactactgatagtatgccgccattattacgacaagctatcgaattatttgatcaccaaggtgcagagccagccttcttattcggccttgaattgatcatatgcggattagaaaaacaacttaaatgtgaaagtgggtccgctgcaaacgacgaaaactacgctttagtagcttaataa");
		SBOLTestUtils.addPRSequence(document, rbsgfp,"aaagaaacgaca");
		SBOLTestUtils.addPRSequence(document, gfp,"atgcgtaaaggagaagaacttttcactggagttgtcccaattcttgttgaattagatggtgatgttaatgggcacaaattttctgtcagtggagagggtgaaggtgatgcaacatacggaaaacttacccttaaatttatttgcactactggaaaactacctgttccatggccaacacttgtcactactttcggttatggtgttcaatgctttgcgagatacccagatcatatgaaacagcatgactttttcaagagtgccatgcccgaaggttatgtacaggaaagaactatatttttcaaagatgacgggaactacaagacacgtgctgaagtcaagtttgaaggtgatacccttgttaatagaatcgagttaaaaggtattgattttaaagaagatggaaacattcttggacacaaattggaatacaactataactcacacaatgtatacatcatggcagacaaacaaaagaatggaatcaaagttaacttcaaaattagacacaacattgaagatggaagcgttcaactagcagaccattatcaacaaaatactccaattggcgatggccctgtccttttaccagacaaccattacctgtccacacaatctgccctttcgaaagatcccaacgaaaagagagaccacatggtccttcttgagtttgtaacagctgctgggattacacatggcatggatgaactatacaaataataa");
		SBOLTestUtils.addPRSequence(document, tetRTerminator,"ttcagccaaaaaacttaagaccgccggtcttgtccactaccttgcagtaatgcggtggacaggatcggcggttttcttttctcttctcaa");

		SBOLTestUtils.addSubComponents(document, tetRInverter, ptetR,rbslacI,lacI,lacITerminator);
		SBOLTestUtils.addSubComponents(document, lacIInverter, placI,rbstetR,tetR,rbsgfp,gfp,tetRTerminator);
		SBOLTestUtils.addSubComponents(document, toggleSwitch, tetRInverter,lacIInverter);

		/*ModuleDefinition laciInverterModuleDef=document.createModuleDefinition(toURI(example.withLocalPart("laci_inverter")),
				new HashSet<URI>(Arrays.asList(Terms.moduleRoles.inverter)));
		 */
		setDefaultNameSpace(document, SBOLTestUtils.example.getNamespaceURI());
		ModuleDefinition laciInverterModuleDef=document.createModuleDefinition("laci_inverter");
		laciInverterModuleDef.addRole(SBOLTestUtils.Terms.moduleRoles.inverter);


		ModuleDefinition tetRInverterModuleDef=document.createModuleDefinition("tetr_inverter");
		tetRInverterModuleDef.addRole(SBOLTestUtils.Terms.moduleRoles.inverter);

		SBOLTestUtils.createInverter(document,laciInverterModuleDef,placI,LacI);

		SBOLTestUtils.createInverter(document,tetRInverterModuleDef,ptetR,TetR);

		ModuleDefinition toggleSwitchModuleDef=document.createModuleDefinition("toggle_switch");
		toggleSwitchModuleDef.addRole(SBOLTestUtils.toURI(SBOLTestUtils.example.withLocalPart("module_role/toggle_switch")));

		FunctionalComponent  toggleSwitchModuleDef_TetR=toggleSwitchModuleDef.createFunctionalComponent(
				"TetR", AccessType.PUBLIC, TetR.getIdentity(), DirectionType.INOUT);

		FunctionalComponent  toggleSwitchModuleDef_LacI=toggleSwitchModuleDef.createFunctionalComponent(
				"LacI" ,
				AccessType.PUBLIC,
				LacI.getIdentity(),
				DirectionType.INOUT);


		Module lacInverterSubModule=toggleSwitchModuleDef.createModule(
				"laci_inverter",
				laciInverterModuleDef.getIdentity());

		lacInverterSubModule.createMapsTo(
				"LacI_mapping",
				RefinementType.USEREMOTE,
				toggleSwitchModuleDef_LacI.getIdentity(),
				laciInverterModuleDef.getFunctionalComponent("TF").getIdentity());

		Module tetRInverterSubModule=toggleSwitchModuleDef.createModule(
				"tetr_inverter",
				tetRInverterModuleDef.getIdentity());

		tetRInverterSubModule.createMapsTo(
				"TetR_mapping",
				RefinementType.USEREMOTE,
				toggleSwitchModuleDef_TetR.getIdentity(),
				tetRInverterModuleDef.getFunctionalComponent("TF").getIdentity());

		Model model=document.createModel(
				"toogleswicth",
				URI.create("http://virtualparts.org/part/pIKE_Toggle_1"),
				EDAMOntology.SBML,
				SystemsBiologyOntology.CONTINUOUS_FRAMEWORK);

		//new HashSet<URI>(Arrays.asList(URI.create("http://sbols.org/v2#module_model")))


		toggleSwitchModuleDef.addModel(model.getIdentity());

		//		SBOLWriter.write(document,(System.out));
		runTest("/SBOLTestSuite/SBOL2/ModuleDefinitionOutput.xml", document, true);
	}

	/**
	 * Test SBOLDocument output
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_SBOLDocumentOutput() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		//		SBOLWriter.write(document,(System.out));
		runTest("/SBOLTestSuite/SBOL2/SBOLDocumentOutput.xml", document, true);
	}

	/**
	 * Test SequenceConstraint output
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_SequenceConstraintOutput() throws SBOLValidationException, SBOLConversionException, IOException
	{
		String prURI="http://partsregistry.org/";
		//String prPrefix="pr";
		SBOLDocument document = new SBOLDocument();
		/*
		Sequence seq=document.createSequence(
				URI.create(prURI + "Part:BBa_J23119:Design"),
				 "ttgacagctagctcagtcctaggtataatgctagc",
				URI.create("http://www.chem.qmul.ac.uk/iubmb/misc/naseq.html")
				);
		 */

		document.setDefaultURIprefix(prURI);
		document.setTypesInURIs(true);
		document.setCreateDefaults(true);
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

		promoter.setName("constitutive promoter");
		promoter.setDescription("pspac core promoter region");

		ComponentDefinition operator = document.createComponentDefinition(
				"LacI_operator",
				"",
				new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA_REGION)));

		operator.addRole(SequenceOntology.OPERATOR);

		operator.setName("LacI operator");
		operator.setDescription("LacI binding site");

		promoter.createSequenceConstraint(
				"r1",
				RestrictionType.PRECEDES, constPromoter.getDisplayId(),operator.getDisplayId() );

		//promoter.setSequence(seq.getIdentity());

		//		SBOLWriter.write(document,(System.out));
		runTest("/SBOLTestSuite/SBOL2/SequenceConstraintOutput.xml", document, true);
	}

	/**
	 * Test Sequence output
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_SequenceOutput() throws SBOLValidationException, SBOLConversionException, IOException
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
		//		SBOLWriter.write(document,(System.out));
		runTest("/SBOLTestSuite/SBOL2/SequenceOutput.xml", document, true);
	}

	/**
	 * Test creation of a simple ComponentDefinition
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_SimpleComponentDefinitionExample() throws SBOLValidationException, SBOLConversionException, IOException
	{
		String prURI="http://partsregistry.org/";


		SBOLDocument document = new SBOLDocument();
		document.setDefaultURIprefix(prURI);
		document.setTypesInURIs(true);
		ComponentDefinition promoter = document.createComponentDefinition(
				"BBa_J23119",
				"",
				new HashSet<URI>(Arrays.asList(
						ComponentDefinition.DNA_REGION,
						URI.create("http://identifiers.org/chebi/CHEBI:4705")
						)));
		promoter.addRole(SequenceOntology.PROMOTER);
		promoter.addRole(URI.create("http://identifiers.org/so/SO:0000613"));

		promoter.setName("J23119 promoter");
		promoter.setDescription("Constitutive promoter");
		promoter.addWasDerivedFrom(URI.create("http://partsregistry.org/Part:BBa_J23119"));

		document.setDefaultURIprefix(prURI);
		Sequence seq=document.createSequence(
				"BBa_J23119",
				"",
				"ttgacagctagctcagtcctaggtataatgctagc",
				URI.create("http://www.chem.qmul.ac.uk/iubmb/misc/naseq.html")
				);
		seq.addWasDerivedFrom(URI.create("http://parts.igem.org/Part:BBa_J23119:Design"));
		promoter.addSequence(seq.getIdentity());
		//		SBOLWriter.write(document,(System.out));
		runTest("/SBOLTestSuite/SBOL2_bp/SimpleComponentDefinitionExample.xml", document, true);
	}

	/**
	 * Test creation of a simple ModuleDefinition
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_SimpleModuleDefinition() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();

		setDefaultNameSpace(document, SBOLTestUtils.example.getNamespaceURI());
		document.setTypesInURIs(true);

		ModuleDefinition module=document.createModuleDefinition("GFP_expression");
		//FunctionalComponent  cds=
		module.createFunctionalComponent(
				"Constitutive_GFP",
				AccessType.PUBLIC,
				URI.create("http://sbolstandard.org/example/GFP_generator"),
				DirectionType.IN);


		//FunctionalComponent  protein =
		module.createFunctionalComponent(
				"GFP_protein",
				AccessType.PUBLIC,
				URI.create("http://sbolstandard.org/example/GFP"),
				DirectionType.OUT);

		module.createInteraction("express_GFP", new HashSet<URI>(Arrays.asList(SystemsBiologyOntology.TRANSCRIPTION)));

		//		SBOLWriter.write(document,(System.out));
		runTest("/SBOLTestSuite/SBOL2_ic/SimpleModuleDefinition.xml", document, true);
	}

	private static void setDefaultNameSpace(SBOLDocument document, String uri)
	{
		if (uri.endsWith("/"))
		{
			uri=uri.substring(0,uri.length()-1);
		}
		document.setDefaultURIprefix(uri);
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_BBa_I0462_File() throws SBOLConversionException, IOException
	{
		String fileName = "SBOLTestSuite/SBOL1/BBa_I0462.xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileName, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/BBa_I0462.xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileName, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/BBa_I0462_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileName, e);
		}
	}
	
	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_SBOL1andSBOL2Test_File() throws SBOLConversionException, IOException
	{
		String fileName = "SBOL1and2Test.xml";

		try
		{
			InputStream resourceAsStream = SBOLReaderTest.class.getResourceAsStream(fileName);
			if (resourceAsStream == null)
				resourceAsStream = SBOLReaderTest.class.getResourceAsStream("/SBOLTestSuite/SBOL2_nc/" + fileName);

			assert resourceAsStream != null : "Failed to find test resource '" + fileName + "'";
			SBOLDocument actual = null;
			SBOLReader.setURIPrefix(URIprefix);
			actual = SBOLReader.read(resourceAsStream);
			runTest("/SBOLTestSuite/SBOL2_nc/SBOL1and2Test.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileName, e);
		}
	}

	/**
	 * Test GenBank conversion
	 */
	@Test
	public void test_GenBank_Files() 
	{
		File file_base = null ;
		try {
			file_base = new File(ValidationTest.class.getResource("/SBOLTestSuite/GenBank/").toURI());
		}
		catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		File file; 
		for (File f : file_base.listFiles()){
			
			// ignore sub-directories for the time being
			if(f.isDirectory() || f.getName().equals("manifest")) { continue; }
			
			file = new File(f.getAbsolutePath());
			try
			{
				SBOLReader.setURIPrefix("http://www.async.ece.utah.edu");
				SBOLDocument actual = SBOLReader.read(file);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				SBOLWriter.write(actual, out, SBOLDocument.GENBANK);
				runTest("/SBOLTestSuite/SBOL2/"+f.getName().replace(".gb", ".xml").replace(".gbk", ".xml"), actual, true);
			}
			catch (Exception e)
			{
				throw new AssertionError("Failed for " + f.getName(), e);
			}
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_igem1_File() throws SBOLConversionException, IOException
	{
		String fileName = "SBOLTestSuite/RDF/igem1.xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertRDFTripleStore(fileName, false);
			runTest("/SBOLTestSuite/SBOL2_nc/igem1.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileName, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_igem2_File() throws SBOLConversionException, IOException
	{
		String fileName = "SBOLTestSuite/RDF/igem2.xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertRDFTripleStore(fileName, false);
			runTest("/SBOLTestSuite/SBOL2_nc/igem2.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileName, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_igem3_File() throws SBOLConversionException, IOException
	{
		String fileName = "SBOLTestSuite/RDF/igem3.xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertRDFTripleStore(fileName, false);
			runTest("/SBOLTestSuite/SBOL2_nc/igem3.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileName, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_toggle_File() throws SBOLConversionException, IOException
	{
		String fileName = "SBOLTestSuite/RDF/toggle.xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertRDFTripleStore(fileName, false);
			runTest("/SBOLTestSuite/SBOL2/toggle.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileName, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_BBa_T9002_File() throws SBOLConversionException, IOException
	{
		String fileName = "SBOLTestSuite/SBOL1/BBa_T9002.xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileName, URIprefix, true);
			runTest("/SBOLTestSuite/SBOL2_bp/BBa_T9002.xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileName, null, true);
			runTest("/SBOLTestSuite/SBOL2_nc/BBa_T9002_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileName, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_labhost_All_File() throws SBOLConversionException, IOException
	{
		String fileName = "SBOLTestSuite/SBOL1/labhost_All.xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileName, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/labhost_All.xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileName, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/labhost_All_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileName, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_labhost_Aspergillus_nidulans() throws SBOLConversionException, IOException
	{
		String filename = "labhost_Aspergillus_nidulans";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_labhost_Bacillus_subtilis() throws SBOLConversionException, IOException
	{
		String filename = "labhost_Bacillus_subtilis";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_labhost_Drosophila_melanogaster() throws SBOLConversionException, IOException
	{
		String filename = "labhost_Drosophila_melanogaster";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_labhost_Escherichia_Coli() throws SBOLConversionException, IOException
	{
		String filename = "labhost_Escherichia_Coli";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_labhost_Gramnegative_bacteria() throws SBOLConversionException, IOException
	{
		String filename = "labhost_Gram-negative_bacteria";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_labhost_Insect_Cells() throws SBOLConversionException, IOException
	{
		String filename = "labhost_Insect_Cells";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_labhost_Kluyveromyces_lactis() throws SBOLConversionException, IOException
	{
		String filename = "labhost_Kluyveromyces_lactis";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_labhost_Mammalian_Cells() throws SBOLConversionException, IOException
	{
		String filename = "labhost_Mammalian_Cells";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_labhost_Pichia_pastoris() throws SBOLConversionException, IOException
	{
		String filename = "labhost_Pichia_pastoris";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_labhost_Plant_Cells() throws SBOLConversionException, IOException
	{
		String filename = "labhost_Plant_Cells";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_labhost_Saccharomyces_cerevisiae() throws SBOLConversionException, IOException
	{
		String filename = "labhost_Saccharomyces_cerevisiae";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_labhost_Schizosaccharomyces_pombe() throws SBOLConversionException, IOException
	{
		String filename = "labhost_Schizosaccharomyces_pombe";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_labhost_Unspecified() throws SBOLConversionException, IOException
	{
		String filename = "labhost_Unspecified";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_partial_pIKE_left_cassette() throws SBOLConversionException, IOException
	{
		String filename = "partial_pIKE_left_cassette";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_partial_pIKE_right_casette() throws SBOLConversionException, IOException
	{
		String filename = "partial_pIKE_right_casette";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_partial_pIKE_right_cassette() throws SBOLConversionException, IOException
	{
		String filename = "partial_pIKE_right_cassette";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_partial_pTAK_left_cassette() throws SBOLConversionException, IOException
	{
		String filename = "partial_pTAK_left_cassette";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_partial_pTAK_right_cassette() throws SBOLConversionException, IOException
	{
		String filename = "partial_pTAK_right_cassette";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_pIKE_pTAK_cassettes_2() throws SBOLConversionException, IOException
	{
		String filename = "pIKE_pTAK_cassettes_2";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, true);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, true);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_pIKE_pTAK_cassettes() throws SBOLConversionException, IOException
	{
		String filename = "pIKE_pTAK_cassettes";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, true);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, true);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_pIKE_pTAK_left_right_cassettes() throws SBOLConversionException, IOException
	{
		String filename = "pIKE_pTAK_left_right_cassettes";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, true);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, true);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_pIKE_pTAK_toggle_switches() throws SBOLConversionException, IOException
	{
		String filename = "pIKE_pTAK_toggle_switches";
		String fileDirectory = "SBOLTestSuite/SBOL1/" + filename + ".xml";

		try
		{
			SBOLDocument actual = SBOLTestUtils.convertSBOL1(fileDirectory, URIprefix, false);
			runTest("/SBOLTestSuite/SBOL2/" + filename + ".xml", actual, true);
			actual = SBOLTestUtils.convertSBOL1(fileDirectory, null, false);
			runTest("/SBOLTestSuite/SBOL2_nc/" + filename + "_orig.xml", actual, false);
		}
		catch (SBOLValidationException e)
		{
			throw new AssertionError("Failed for " + fileDirectory, e);
		}
	}

	/**
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_memberAnnotations() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		document.setComplete(true);
		document.setDefaultURIprefix("http://www.async.ece.utah.edu");

		document.addNamespaceBinding(NamespaceBinding("http://myannotation.org/", "annot"));
		document.addNamespaceBinding(NamespaceBinding("urn:bbn.com:tasbe:grn/", "grn"));

		String id    	= "someModel";
		URI source 		= URI.create(id + "_source");

		Collection myParts = document.createCollection("myParts", VERSION_1_0);
		myParts.addAnnotation(new Annotation(NamedProperty(new QName("http://myannotation.org/", "thisAnnotation", "annot"), "turtleString")));

		Model someModel = document.createModel(id, VERSION_1_0, source, EDAMOntology.SBML, SystemsBiologyOntology.CONTINUOUS_FRAMEWORK);
		someModel.addAnnotation(new Annotation(NamedProperty(new QName("http://myannotation.org/", "thisAnnotation", "annot"), "turtleString")));

		ModuleDefinition someModDef = document.createModuleDefinition("someModuleDef", VERSION_1_0);
		someModDef.addAnnotation(new Annotation(NamedProperty(new QName("http://myannotation.org/", "thisAnnotation", "annot"), "turtleString")));
		document.createModuleDefinition("someModuleDefSub", VERSION_1_0);

		Set<URI> interactionType = new HashSet<URI>();
		interactionType.add(SystemsBiologyOntology.NON_COVALENT_BINDING);
		//Interaction someInteraction =
		someModDef.createInteraction("someInteraction", interactionType);
		//		someModDef.createFunctionalComponent("someFunctionalComponent", AccessType.PUBLIC, "componentDef", VERSION_1_0, DirectionType.INOUT); 
		//		someInteraction.createParticipation("someParticipation", "someFunctionalComponent");

		//Module someModule =
		someModDef.createModule("someModule", "someModuleDefSub", VERSION_1_0);
		//String someMapsTo_id = "someMapsTo";
		//		MapsTo someMapsTo = someModule.createMapsTo(someMapsTo_id, RefinementType.USELOCAL, "someModule", someMapsTo_id +"_remote");

		String seq_id = "someSeq";
		Sequence someSeq = document.createSequence(seq_id, VERSION_1_0, "ACGTURYSWKMBDHVN-.", Sequence.IUPAC_DNA);
		someSeq.addAnnotation(new Annotation(NamedProperty(new QName("http://myannotation.org/", "thisAnnotation", "annot"), "turtleString")));

		Set<URI> types = new HashSet<URI>();
		types.add(ComponentDefinition.DNA_REGION);
		ComponentDefinition someCompDef = document.createComponentDefinition("someCompDef", VERSION_1_0, types);
		someCompDef.addAnnotation(new Annotation(NamedProperty(new QName("http://myannotation.org/", "thisAnnotation", "annot"), "turtleString")));
		someCompDef.addRole(SequenceOntology.PROMOTER);
		ComponentDefinition someCompDefCDS = document.createComponentDefinition("someCompDefCDS", VERSION_1_0, types);
		someCompDefCDS.addRole(SequenceOntology.CDS);
		Component someComponent = someCompDef.createComponent("someComponent", AccessType.PUBLIC, "someCompDefCDS", VERSION_1_0);
		someCompDef.createComponent("someOtherComponent", AccessType.PUBLIC, "someCompDefCDS", VERSION_1_0);
		someComponent.addAnnotation(new Annotation(NamedProperty(new QName("http://myannotation.org/", "thisAnnotation", "annot"), "turtleString")));

		SequenceAnnotation someSequenceAnnotation = someCompDef.createSequenceAnnotation("someSequenceAnnotation", "cut", 1, 10);
		someSequenceAnnotation.addAnnotation(new Annotation(NamedProperty(new QName("http://myannotation.org/", "thisAnnotation", "annot"), "turtleString")));
		//SequenceAnnotation someSequenceAnnotation2 =
		someCompDef.createSequenceAnnotation("someSequenceAnnotation2", "cut", 1, OrientationType.INLINE);

		SequenceConstraint someSequenceConstraint = someCompDef.createSequenceConstraint("someSequenceConstraint", RestrictionType.PRECEDES, "someComponent", "someOtherComponent");
		someSequenceConstraint.addAnnotation(new Annotation(NamedProperty(new QName("http://myannotation.org/", "thisAnnotation", "annot"), "turtleString")));

		GenericTopLevel someGenericTopLevel = document.createGenericTopLevel("someGenericTopLevel", VERSION_1_0, new QName("urn:bbn.com:tasbe:grn/", "RegulatoryReaction", "grn"));
		someGenericTopLevel.addAnnotation(new Annotation(NamedProperty(new QName("http://myannotation.org/", "thisAnnotation", "annot"), "turtleString")));


		runTest("/SBOLTestSuite/SBOL2/memberAnnotations.xml", document, true);
	}


	/**
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_CreateAndRemoveCollections() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		document.setComplete(true);
		document.setDefaultURIprefix("http://www.async.ece.utah.edu");

		document.addNamespaceBinding(NamespaceBinding("http://myannotation.org/", "annot"));
		document.addNamespaceBinding(NamespaceBinding("urn:bbn.com:tasbe:grn/", "grn"));

		Collection c = document.createCollection("myParts", VERSION_1_0);
		document.removeCollection(c);

		for(int i = 1; i < 4; i++)
		{
			document.createCollection("myParts" + i, VERSION_1_0);
		}

		document.clearCollections();
		document.createCollection("myParts", VERSION_1_0);
		runTest("/SBOLTestSuite/SBOL2/CreateAndRemoveCollections.xml", document, true);
	}

	/**
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_CreateAndRemoveComponentDefintion() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		document.setComplete(true);
		document.setDefaultURIprefix("http://www.async.ece.utah.edu/");

		document.addNamespaceBinding(NamespaceBinding("http://myannotation.org/", "annot"));
		document.addNamespaceBinding(NamespaceBinding("urn:bbn.com:tasbe:grn/", "grn"));

		Set<URI> types = new HashSet<URI>();
		types.add(ComponentDefinition.PROTEIN);
		ComponentDefinition cd = document.createComponentDefinition("someCompDef", VERSION_1_0, types);
		document.removeComponentDefinition(cd);

		for(int i = 1; i < 4; i++)
		{
			document.createComponentDefinition("someCompDef" + i, VERSION_1_0, types);
		}

		document.clearComponentDefinitions();
		document.createComponentDefinition("someCompDef", VERSION_1_0, types);
		runTest("/SBOLTestSuite/SBOL2/CreateAndRemoveComponentDefinition.xml", document, true);
	}

	/**
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_CreateAndRemoveModuleDefintion() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		document.setComplete(true);
		document.setDefaultURIprefix("http://www.async.ece.utah.edu");

		document.addNamespaceBinding(NamespaceBinding("http://myannotation.org/", "annot"));
		document.addNamespaceBinding(NamespaceBinding("urn:bbn.com:tasbe:grn/", "grn"));

		ModuleDefinition md = document.createModuleDefinition("someModDef", VERSION_1_0);
		document.removeModuleDefinition(md);

		for(int i = 1; i < 4; i++)
		{
			document.createModuleDefinition("someModDef"+i, VERSION_1_0);
		}

		document.clearModuleDefinitions();
		document.createModuleDefinition("someModDef", VERSION_1_0);

		runTest("/SBOLTestSuite/SBOL2/CreateAndRemoveModuleDefinition.xml", document, true);
	}

	/**
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_CreateAndRemoveGenericTopLevel() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		document.setComplete(true);
		document.setDefaultURIprefix("http://www.async.ece.utah.edu");

		document.addNamespaceBinding(NamespaceBinding("http://myannotation.org/", "annot"));
		document.addNamespaceBinding(NamespaceBinding("urn:bbn.com:tasbe:grn/", "grn"));

		GenericTopLevel gen = document.createGenericTopLevel("someGenTopLev", VERSION_1_0, new QName("urn:bbn.com:tasbe:grn/", "RegulatoryReaction", "grn"));
		document.removeGenericTopLevel(gen);

		for(int i = 1; i < 4; i++)
		{
			document.createGenericTopLevel("someGenTopLev"+i, VERSION_1_0, new QName("urn:bbn.com:tasbe:grn/", "RegulatoryReaction", "grn"));
		}

		document.clearGenericTopLevels();
		document.createGenericTopLevel("someGenTopLev", VERSION_1_0, new QName("urn:bbn.com:tasbe:grn/", "RegulatoryReaction", "grn"));

		runTest("/SBOLTestSuite/SBOL2/CreateAndRemoveGenericTopLevel.xml", document, true);
	}

	/**
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_CreateAndRemoveModel() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		document.setComplete(true);
		document.setDefaultURIprefix("http://www.async.ece.utah.edu");

		document.addNamespaceBinding(NamespaceBinding("http://myannotation.org/", "annot"));
		document.addNamespaceBinding(NamespaceBinding("urn:bbn.com:tasbe:grn/", "grn"));

		Sequence s = document.createSequence("someSequence", VERSION_1_0, "ACGTURYSWKMBDHVN-.", Sequence.IUPAC_DNA);
		document.removeSequence(s);


		for(int i = 1; i < 4; i++)
		{
			document.createCollection("someSequence" + i, VERSION_1_0);
		}

		document.clearSequences();
		document.createSequence("someSequence", VERSION_1_0, "ACGTURYSWKMBDHVN-.", Sequence.IUPAC_DNA);
		runTest("/SBOLTestSuite/SBOL2/CreateAndRemoveModel.xml", document, true);
	}

	/**
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_singleCollection() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		document.setComplete(true);
		document.setDefaultURIprefix("http://www.async.ece.utah.edu");

		document.addNamespaceBinding(NamespaceBinding("http://myannotation.org/", "annot"));
		document.addNamespaceBinding(NamespaceBinding("urn:bbn.com:tasbe:grn/", "grn"));

		document.createCollection("myParts", VERSION_1_0);
		runTest("/SBOLTestSuite/SBOL2/singleCollection.xml", document, true);
	}


	/**
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_multipleCollections_no_Members() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		document.setComplete(true);
		document.setDefaultURIprefix("http://www.async.ece.utah.edu");

		document.addNamespaceBinding(NamespaceBinding("http://myannotation.org/", "annot"));
		document.addNamespaceBinding(NamespaceBinding("urn:bbn.com:tasbe:grn/", "grn"));

		document.createCollection("myPart1", VERSION_1_0);
		document.createCollection("myPart2", VERSION_1_0);
		document.createCollection("myPart3", VERSION_1_0);

		runTest("/SBOLTestSuite/SBOL2/multipleCollections_no_Members.xml", document, true);
	}

	/**
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_singleGenericTopLevel() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		document.setComplete(true);
		document.setDefaultURIprefix("http://www.async.ece.utah.edu");

		document.addNamespaceBinding(NamespaceBinding("http://myannotation.org/", "annot"));
		document.addNamespaceBinding(NamespaceBinding("urn:bbn.com:tasbe:grn/", "grn"));

		document.createGenericTopLevel("GenericTopLevel", VERSION_1_0, new QName("urn:bbn.com:tasbe:grn/", "RegulatoryReaction", "grn"));
		runTest("/SBOLTestSuite/SBOL2/singleGenericTopLevel.xml", document, true);
	}

	/**
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_multipleGenericTopLevel() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		document.setComplete(true);
		document.setDefaultURIprefix("http://www.async.ece.utah.edu");

		document.addNamespaceBinding(NamespaceBinding("http://myannotation.org/", "annot"));
		document.addNamespaceBinding(NamespaceBinding("urn:bbn.com:tasbe:grn/", "grn"));

		document.createGenericTopLevel("GenericTopLevel1", VERSION_1_0, new QName("urn:bbn.com:tasbe:grn/", "RegulatoryReaction1", "grn"));
		document.createGenericTopLevel("GenericTopLevel2", VERSION_1_0, new QName("urn:bbn.com:tasbe:grn/", "RegulatoryReaction2", "grn"));
		document.createGenericTopLevel("GenericTopLevel3", VERSION_1_0, new QName("urn:bbn.com:tasbe:grn/", "RegulatoryReaction3", "grn"));

		runTest("/SBOLTestSuite/SBOL2/multipleGenericTopLevel.xml", document, true);
	}

	/**
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_Experiment_ExperimentData() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		document.setComplete(true);
		document.setDefaultURIprefix("http://www.async.ece.utah.edu");

		document.addNamespaceBinding(NamespaceBinding("http://myannotation.org/", "annot"));
		document.addNamespaceBinding(NamespaceBinding("urn:bbn.com:tasbe:grn/", "grn"));

		GenericTopLevel topLevel = document.createGenericTopLevel("GenericTopLevel", VERSION_1_0,
				new QName("urn:bbn.com:tasbe:grn/", "RegulatoryReaction", "grn"));
		
		Experiment expt = document.createExperiment("experiment", VERSION_1_0);

		ExperimentalData expt_data1 = document.createExperimentalData( "experimental_data1", VERSION_1_0 );
		ExperimentalData expt_data2 = document.createExperimentalData( "experimental_data2", VERSION_1_0 );
		/*ExperimentalData expt_data_a = new ExperimentalData(topLevel.getIdentity());
		document.addExperimentalData(expt_data_a);*/

		expt.addExperimentalData(expt_data1.getIdentity());
		expt.addExperimentalData(expt_data2.getIdentity());

		runTest("/SBOLTestSuite/SBOL2/test_Experiment_ExperimentData.xml", document, true);
	}


	/**
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_singleModel() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		document.setComplete(true);
		document.setDefaultURIprefix("http://www.async.ece.utah.edu");

		document.addNamespaceBinding(NamespaceBinding("http://myannotation.org/", "annot"));
		document.addNamespaceBinding(NamespaceBinding("urn:bbn.com:tasbe:grn/", "grn"));

		String id = "ToggleModel";
		document.createModel( id, VERSION_1_0, URI.create(id + "_source"), EDAMOntology.SBML,
				SystemsBiologyOntology.CONTINUOUS_FRAMEWORK);

		runTest("/SBOLTestSuite/SBOL2/singleModel.xml", document, true);
	}


	/**
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_singleSequence() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		document.setComplete(true);
		document.setDefaultURIprefix("http://www.async.ece.utah.edu");

		document.addNamespaceBinding(NamespaceBinding("http://myannotation.org/", "annot"));
		document.addNamespaceBinding(NamespaceBinding("urn:bbn.com:tasbe:grn/", "grn"));

		String id = "pLacSeq";
		document.createSequence(id, VERSION_1_0, "ACGTURYSWKMBDHVN-.", Sequence.IUPAC_DNA);

		runTest("/SBOLTestSuite/SBOL2/singleSequence.xml", document, true);
	}

	/**
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_multipleSquences() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		document.setComplete(true);
		document.setDefaultURIprefix("http://www.async.ece.utah.edu");

		document.addNamespaceBinding(NamespaceBinding("http://myannotation.org/", "annot"));
		document.addNamespaceBinding(NamespaceBinding("urn:bbn.com:tasbe:grn/", "grn"));

		String id = "pLacSeq";
		String id2 = "tetRSeq";
		String id3 = "pLactetRSeq";

		document.createSequence(id, VERSION_1_0, "ACGTURYSWKMBDHVN-.", Sequence.IUPAC_DNA);
		document.createSequence(id2, VERSION_1_0, "ACGTURYSWKMBDHVN-.", Sequence.IUPAC_DNA);
		document.createSequence(id3, VERSION_1_0, "ACGTURYSWKMBDHVN-.", Sequence.IUPAC_DNA);

		runTest("/SBOLTestSuite/SBOL2/multipleSequences.xml", document, true);
	}

	/**
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_single_emptyModuleDefinition() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		document.setComplete(true);
		document.setDefaultURIprefix("http://www.async.ece.utah.edu");

		document.addNamespaceBinding(NamespaceBinding("http://myannotation.org/", "annot"));
		document.addNamespaceBinding(NamespaceBinding("urn:bbn.com:tasbe:grn/", "grn"));

		Set<URI> roles = SBOLTestUtils.getSetPropertyURI("Inverter");
		ModuleDefinition LacI_Inv = document.createModuleDefinition("LacI_Inv", VERSION_1_0);
		LacI_Inv.setRoles(roles);
		//		LacI_Inv.addRole(URI.create("Inverter"));

		runTest("/SBOLTestSuite/SBOL2/singleModuleDefinition.xml", document, true);
	}

	/**
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_singleComponentDefinition() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		document.setComplete(true);
		document.setDefaultURIprefix("http://www.async.ece.utah.edu");

		document.addNamespaceBinding(NamespaceBinding("http://myannotation.org/", "annot"));
		document.addNamespaceBinding(NamespaceBinding("urn:bbn.com:tasbe:grn/", "grn"));

		Set<URI> type = new HashSet<URI>();
		type.add(ComponentDefinition.DNA_REGION);
		Set<URI> role = new HashSet<URI>();
		role.add(SequenceOntology.PROMOTER);
		ComponentDefinition pLac = document.createComponentDefinition("pLac", VERSION_1_0, type);
		pLac.setRoles(role);
		//		pLac.addRole(URI.create("Promoter"));

		runTest("/SBOLTestSuite/SBOL2/singleComponentDefinition.xml", document, true);
	}

	/**
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_singleCompDef_withSeq() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		document.setComplete(true);
		document.setDefaultURIprefix("http://www.async.ece.utah.edu");

		document.addNamespaceBinding(NamespaceBinding("http://myannotation.org/", "annot"));
		document.addNamespaceBinding(NamespaceBinding("urn:bbn.com:tasbe:grn/", "grn"));

		Set<URI> type = new HashSet<URI>();
		type.add(ComponentDefinition.DNA_REGION);
		Set<URI> role = new HashSet<URI>();
		role.add(SequenceOntology.PROMOTER);
		ComponentDefinition pLac = document.createComponentDefinition("pLac", VERSION_1_0, type);
		pLac.setRoles(role);
		document.createSequence("pLacSeq", VERSION_1_0, "ACGTURYSWKMBDHVN-.", Sequence.IUPAC_DNA);
		pLac.addSequence("pLacSeq", VERSION_1_0);

		runTest("/SBOLTestSuite/SBOL2/singleCompDef_withSeq.xml", document, true);
	}

	/**
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_singleFunctionalComponent() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		document.setComplete(true);
		document.setDefaultURIprefix("http://www.async.ece.utah.edu");

		document.addNamespaceBinding(NamespaceBinding("http://myannotation.org/", "annot"));
		document.addNamespaceBinding(NamespaceBinding("urn:bbn.com:tasbe:grn/", "grn"));

		Set<URI> type = new HashSet<URI>();
		type.add(ComponentDefinition.PROTEIN);
		//Set<URI> role = SBOLTestUtils.getSetPropertyURI("Transcriptionfactor");
		ComponentDefinition LacIIn = document.createComponentDefinition("LacIIn", VERSION_1_0, type);
		ComponentDefinition LacIInCDS = document.createComponentDefinition("LacIInCDS", VERSION_1_0, type);
		String compDef_id = LacIInCDS.getDisplayId();
		LacIIn.createComponent("funcComp", AccessType.PUBLIC, compDef_id, VERSION_1_0);


		runTest("/SBOLTestSuite/SBOL2/singleFunctionalComponent.xml", document, true);
	}
	
	
	/**
	 * @throws SBOLValidationException
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	@Test
	public void test_Measure() throws SBOLValidationException, SBOLConversionException, IOException
	{
		SBOLDocument document = new SBOLDocument();
		document.setComplete(true);
		document.setDefaultURIprefix("http://www.async.ece.utah.edu");

		ModuleDefinition md = document.createModuleDefinition("md");
		
		// put a FunctionalComponent into the ModuleDefinition
		ComponentDefinition cd = 
				document.createComponentDefinition("cd", new HashSet<URI>(Arrays.asList(URI.create("http://purl.obolibrary.org/obo/CHEBI_17634"))));
		cd.addType(ComponentDefinition.SMALL_MOLECULE);
		FunctionalComponent fc = 
				md.createFunctionalComponent("fc", AccessType.PUBLIC, cd.getIdentity(), DirectionType.INOUT);
		// put a Module into the ModuleDefinition
		ModuleDefinition emptyMd = document.createModuleDefinition("empty_md");
		Module m = md.createModule("m", emptyMd.getDisplayId());
		// put an Interaction into the ModuleDefinition
		Interaction i =
				md.createInteraction("i", SystemsBiologyOntology.NON_COVALENT_BINDING);
		Participation p = 
				i.createParticipation("p", fc.getDisplayId(), SystemsBiologyOntology.PRODUCT);
		p.createMeasure("pMeasure", 1.23, URI.create("http://purl.obolibrary.org/obo/UO_0000021"));
		
		// add a Measure to the FunctionalComponent
		Measure fcMeasure = 
				fc.createMeasure("fc_measure", 0.04, URI.create("http://purl.obolibrary.org/obo/UO_0000021"));
		fcMeasure.addType(SystemsBiologyOntology.GROWTH_RATE);
		
		// add a Measure to the Module
		Measure mMeasure = 
				m.createMeasure("md_measure", 11.28, URI.create("http://purl.obolibrary.org/obo/UO_0000175"));
		// add a Measure to the Interaction
		Measure iMeasure = 
				i.createMeasure("i_measure", 0.04, URI.create("http://purl.obolibrary.org/obo/UO_0000077"));
		
		runTest("/SBOLTestSuite/SBOL2/Measure.xml", document, true);
	}
	
	/**
	 * Abstract method to run a single test
	 * @param fileName - "golden" file
	 * @param expected - SBOLDocument to check
	 * @param fileType - Type of file format to use for serialization
	 * @param compliant - Flag indicating if the document uses compliant URIs
	 * @throws SBOLValidationException 
	 * @throws SBOLConversionException
	 * @throws IOException
	 */
	public abstract void runTest(final String fileName, final SBOLDocument expected, boolean compliant)
			throws SBOLValidationException, SBOLConversionException, IOException;

}
