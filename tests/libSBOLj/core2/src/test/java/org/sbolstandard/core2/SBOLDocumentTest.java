package org.sbolstandard.core2;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests for SBOLDocument methods.
 * @author Meher Samineni
 * @author Eugene Choe
 *
 */
public class SBOLDocumentTest {

	@Test
	public void Test_Sequence_CopyCreate_Create() throws SBOLValidationException {
		//create copy not adding types to URIs
		String prURI="http://partsregistry.org";
		//String prPrefix="pr";
		SBOLDocument document = new SBOLDocument();
		document.setDefaultURIprefix(prURI);
		document.setTypesInURIs(false);
//		document.addNamespace(URI.create(prURI), prPrefix);
		String prURI1="http://partsregistry.org";
		//String prPrefix1="pr";
		SBOLDocument document2 = new SBOLDocument();
		document.setDefaultURIprefix(prURI1);
		document.setTypesInURIs(false);
//		document.addNamespace(URI.create(prURI1), prPrefix1);
		
		String prURI2="http://partsregistry.org";
		//String prPrefix2="pr";
//		SBOLDocument document3 = new SBOLDocument();
		document.setDefaultURIprefix(prURI2);
		document.setTypesInURIs(false);
		
		//String prURI3="http://partsregistry.org";
		//String prPrefix3="pr";
		//SBOLDocument document4 = new SBOLDocument();
		document.setDefaultURIprefix(prURI2);
		document.setTypesInURIs(false);
		
		String SequenceDisplayID = "ID";
		String SequenceVersion = "1.0";
		String SequenceElements = "Element";
		String SequenceURI = "http://partsregistry.org";
		//String createCopyID = "ID";
		//String createCopyVersion = "1.0";
		//String createCopyURI = "URI";
		//String seq2ID = "ID2";
		String seq2Version = "1.0";
		//String seq2URIPrefix = "http://partsregistry.org";
		//URI seq2URI = URI.create("http://partsregistry.org");
		URI SeqURI = URI.create("www.example.com/name");
		
		
		Sequence seq = document.createSequence(SequenceDisplayID, SequenceVersion, SequenceElements, SeqURI);
		seq.setDescription("description");
		seq.setName("seq");
		Sequence seq2 = (Sequence)document2.createCopy(seq, SequenceURI, SequenceDisplayID, seq2Version);
//		seq2.setDescription("description");
//		Sequence seq3 = (Sequence)document3.createCopy(seq, SequenceDisplayID);
//		Sequence seq4 = (Sequence)document4.createCopy(seq, prPrefix, SequenceDisplayID , SequenceVersion);
		
		
//		seq2.unsetWasDerivedFrom();
//		seq3.unsetWasDerivedFrom();
		
		
//		System.out.println(seq.getIdentity());
//		System.out.println(seq2.getIdentity());

		assertTrue(seq.getVersion().equals(seq2.getVersion()));
		assertTrue(seq.getElements().equals(seq2.getElements()));
		assertTrue(seq.getDisplayId().equals(seq2.getDisplayId()));
		assertTrue(seq.getEncoding().equals(seq2.getEncoding()));
		assertTrue(seq.getClass().equals(seq2.getClass()));
		assertTrue(seq.getAnnotations().equals(seq2.getAnnotations()));
//		assertTrue(seq.getIdentity().equals(seq2.getIdentity()));						//assertion error
		assertTrue(seq.getDescription().equals(seq2.getDescription()));					//assertion error
//		assertTrue(seq.getWasDerivedFrom().equals(seq2.getWasDerivedFrom()));			//null pointer exception
		assertTrue(seq.getPersistentIdentity().equals(seq2.getPersistentIdentity()));	//assertion error
		assertTrue(seq.getName().equals(seq2.getName()));								//assertion error, name is not getting copied  
		assertTrue(seq.equals(seq2));
		
//		assertTrue(seq.equals(seq3));
		
		
		

//		if(seq.equals(seq3)){
//			System.out.println("True");
//		}
//		else{System.out.println("False");}	
	}

	@Test
	public void Test_Model_CopyCreate() throws SBOLValidationException{
		
		String prURI="http://partsregistry.org";
//		String prPrefix="pr";
		SBOLDocument document1 = new SBOLDocument();
		document1.setDefaultURIprefix(prURI);
		document1.setTypesInURIs(false);
		
		String prURI2="http://partsregistry.org";
//		String prPrefix2="pr";
		SBOLDocument document2 = new SBOLDocument();
		document2.setDefaultURIprefix(prURI2);
		document2.setTypesInURIs(false);
		
		
		String model1ID = "ID";
		String model1Version = "1.0";
//		String model2Version = "1.2";
		String model1URIPrefix = "http://partsregistry.org";
		String S_model1URI = "http://partsregistry.org/ID";
		String L_model1URI = "http://partsregistry.org/Source";
		String F_model1URI = "http://partsregistry.org/Framework";
		
		URI model1URI_S = URI.create(S_model1URI);
		URI model1URI_L = URI.create(L_model1URI);
		URI model1URI_F = URI.create(F_model1URI);
		
		Model mod1 = document1.createModel(model1ID, model1Version, model1URI_S, model1URI_L, model1URI_F);
		mod1.setName("mod1");
		mod1.setDescription("description");
//		Model mod2 = (Model)document2.createCopy(mod1);
		Model mod3 = (Model)document2.createCopy(mod1, model1URIPrefix, model1ID, model1Version);
		
//		mod2.unsetWasDerivedFrom();
		
//		assertTrue(mod1.equals(mod2));	
		assertTrue(mod1.getAnnotations().equals(mod3.getAnnotations()));
		assertTrue(mod1.getClass().equals(mod3.getClass()));
		assertTrue(mod1.getDescription().equals(mod3.getDescription()));				
		assertTrue(mod1.getDisplayId().equals(mod3.getDisplayId()));
		assertTrue(mod1.getFramework().equals(mod3.getFramework()));
		assertTrue(mod1.getIdentity().equals(mod3.getIdentity()));						
		assertTrue(mod1.getLanguage().equals(mod3.getLanguage()));
		assertTrue(mod1.getName().equals(mod3.getName()));								
		assertTrue(mod1.getPersistentIdentity().equals(mod3.getPersistentIdentity()));	
		assertTrue(mod1.getSource().equals(mod3.getSource()));
		assertTrue(mod1.getVersion().equals(mod3.getVersion()));
//		assertTrue(mod1.getWasDerivedFrom().equals(mod3.getWasDerivedFrom()));			//null pointer exception
		
	}
	
	@Test
	public void Test_GenericTopLevel_CopyCreate() throws SBOLValidationException{
		
		String prURI="http://partsregistry.org/";
		String prPrefix="pr";
		SBOLDocument document1 = new SBOLDocument();
		document1.setDefaultURIprefix(prURI);
		document1.setTypesInURIs(false);
		
		String prURI2="http://partsregistry2.org/";
//		String prPrefix2="pr2";
		SBOLDocument document2 = new SBOLDocument();
		document2.setDefaultURIprefix(prURI2);
		document2.setTypesInURIs(false);
		
		String GTL1ID = "ID";
		String GTL1Name = "GTL1";
		String GTL1Description = "Description";
		String GTL1Version = "1.0";
		GenericTopLevel GTL1 = document1.createGenericTopLevel(GTL1ID, GTL1Version, new QName(prURI, "group", prPrefix));
		GTL1.setDescription(GTL1Description);
		GTL1.setName(GTL1Name);
		GenericTopLevel GTL2 = (GenericTopLevel)document2.createCopy(GTL1);
		
		GTL2.clearWasDerivedFroms();
		
		assertTrue(GTL1.equals(GTL2));	
		assertTrue(GTL1.getAnnotations().equals(GTL2.getAnnotations()));
		assertTrue(GTL1.getClass().equals(GTL2.getClass()));
		assertTrue(GTL1.getDescription().equals(GTL2.getDescription()));			
		assertTrue(GTL1.getDisplayId().equals(GTL2.getDisplayId()));
		assertTrue(GTL1.getIdentity().equals(GTL2.getIdentity()));						
		assertTrue(GTL1.getName().equals(GTL2.getName()));								
		assertTrue(GTL1.getPersistentIdentity().equals(GTL2.getPersistentIdentity()));	
		assertTrue(GTL1.getVersion().equals(GTL2.getVersion()));
//		assertTrue(GTL1.getWasDerivedFrom().equals(mod3.getWasDerivedFrom()));			//null pointer exception
		assertTrue(GTL1.getRDFType().equals(GTL2.getRDFType()));
	}
	
	@Test
	public void Test_Collection_CopyCreate() throws SBOLValidationException{
		
		String prURI="http://partsregistry.org";
//		String prPrefix="pr";
		SBOLDocument document1 = new SBOLDocument();
		document1.setDefaultURIprefix(prURI);
		document1.setTypesInURIs(false);
		
		String prURI2="http://partsregistry2.org";
//		String prPrefix2="pr2";
		SBOLDocument document2 = new SBOLDocument();
		document2.setDefaultURIprefix(prURI2);
		document2.setTypesInURIs(false);
		
		String prURI3="http://partsregistry3.org";
//		String prPrefix3="pr3";
		SBOLDocument document3 = new SBOLDocument();
		document3.setDefaultURIprefix(prURI3);
		document3.setTypesInURIs(false);
		
		String Col1ID = "ID";
//		String Col3ID = "ID3";
		String Col1Version = "1.0";
		Collection Col1 = document1.createCollection(Col1ID, Col1Version);
		Col1.setDescription("description");
		Col1.setName("Col1");
		Collection Col2 = (Collection)document2.createCopy(Col1);
//		Collection Col3 = (Collection)document3.createCopy(Col1, Col1ID);
		
//		Col3.unsetWasDerivedFrom();
		Col2.clearWasDerivedFroms();
		
//		assertTrue(Col1.equals(Col3));
		assertTrue(Col1.equals(Col2));
		assertTrue(Col1.getAnnotations().equals(Col2.getAnnotations()));
		assertTrue(Col1.getClass().equals(Col2.getClass()));
		assertTrue(Col1.getDescription().equals(Col2.getDescription())); 
		assertTrue(Col1.getDisplayId().equals(Col2.getDisplayId()));   //display id should be equal
		assertTrue(Col1.getName().equals(Col2.getName()));
		assertTrue(Col1.getPersistentIdentity().equals(Col2.getPersistentIdentity()));
		assertTrue(Col1.getVersion().equals(Col2.getVersion()));
	}
	
	@Test
	public void Test_ComponentDefinition_CopyCreate() throws SBOLValidationException{
		
		String prURI="http://partsregistry.org";
//		String prPrefix="pr";
		SBOLDocument document1 = new SBOLDocument();
		document1.setDefaultURIprefix(prURI);
		document1.setTypesInURIs(false);
		
		String prURI2="http://partsregistry2.org";
//		String prPrefix2="pr2";
		SBOLDocument document2 = new SBOLDocument();
		document2.setDefaultURIprefix(prURI2);
		document2.setTypesInURIs(false);
		
		String CD1_ID = "ID";
		Set<URI> types = new HashSet<URI>();
		types.add(URI.create("www.example.com"));
		ComponentDefinition CD1 = document1.createComponentDefinition(CD1_ID, types);
		CD1.unsetDescription();
		CD1.setDescription("Description");
		CD1.unsetName();
		CD1.setName("CD1");
		ComponentDefinition CD2 = (ComponentDefinition)document2.createCopy(CD1);

		CD2.clearWasDerivedFroms();
		
		assertTrue(CD1.equals(CD2));
		assertTrue(CD1.getAnnotations().equals(CD2.getAnnotations()));
		assertTrue(CD1.getClass().equals(CD2.getClass()));
		assertTrue(CD1.getComponents().equals(CD1.getComponents()));
		assertTrue(CD1.getDescription().equals(CD1.getDescription()));
		assertTrue(CD1.getDisplayId().equals(CD2.getDisplayId()));
		assertTrue(CD1.getIdentity().equals(CD2.getIdentity()));
		assertTrue(CD1.getName().equals(CD2.getName()));
		assertTrue(CD1.getPersistentIdentity().equals(CD2.getPersistentIdentity()));
		assertTrue(CD1.getSequences().equals(CD2.getSequences()));
		assertTrue(CD1.getTypes().equals(CD2.getTypes()));
	}
	
	@Test
	public void Test_ModuleDefinition_CreateCopy() throws SBOLValidationException{
		
		String prURI="http://partsregistry.org";
//		String prPrefix="pr";
		SBOLDocument document1 = new SBOLDocument();
		document1.setDefaultURIprefix(prURI);
		document1.setTypesInURIs(false);
		
		String prURI2="http://partsregistry2.org";
//		String prPrefix2="pr2";
		SBOLDocument document2 = new SBOLDocument();
		document2.setDefaultURIprefix(prURI2);
		document2.setTypesInURIs(false);
		
		String MD1_ID = "ID";
		String MD1_Version = "1.0";
		String FuncID = "FunctionalComponentID";
		String FuncDefID = "FuctionDefinitionID";
		ModuleDefinition MD1 = document1.createModuleDefinition(MD1_ID, MD1_Version);
		MD1.unsetDescription();
		MD1.setDescription("Description");
		MD1.createFunctionalComponent(FuncID, AccessType.PUBLIC, FuncDefID, MD1_Version, DirectionType.IN);
		MD1.setName("MD1");
		ModuleDefinition MD2 = (ModuleDefinition)document2.createCopy(MD1);
		
		MD2.clearWasDerivedFroms();
		
		assertTrue(MD1.equals(MD2));
		assertTrue(MD1.getAnnotations().equals(MD2.getAnnotations()));
		assertTrue(MD1.getClass().equals(MD2.getClass()));
		assertTrue(MD1.getDescription().equals(MD2.getDescription()));
		assertTrue(MD1.getDisplayId().equals(MD2.getDisplayId()));
		assertTrue(MD1.getFunctionalComponent(FuncID).equals(MD2.getFunctionalComponent(FuncID)));   //do functional components not get copied?
		assertTrue(MD1.getModels().equals(MD2.getModels()));
		assertTrue(MD1.getName().equals(MD2.getName()));
		assertTrue(MD1.getPersistentIdentity().equals(MD2.getPersistentIdentity()));
		assertTrue(MD1.getVersion().equals(MD2.getVersion()));
	}
	
	
	@Test
	public void Test_getModuleDefinition() {
		
		String preURI="http://partsregistry.org";
		String displayID = "Anderson";
		String version = "version1.0";
		SBOLDocument document1 = new SBOLDocument();
		document1.setDefaultURIprefix(preURI);
		document1.setTypesInURIs(true);
		document1.setComplete(true);
		document1.setCreateDefaults(true);
		
		ModuleDefinition md = document1.getModuleDefinition(null, version);
		if (md!=null) fail();
		md = document1.getModuleDefinition(displayID, "/");
		if (md!=null) fail();

	}
	
	@Test
	public void Test_getCollection() throws URISyntaxException
	{
		
		String preURI="http://partsregistry.org";
		SBOLDocument document1 = new SBOLDocument();
		document1.setDefaultURIprefix(preURI);
		document1.setTypesInURIs(true);
		document1.setComplete(true);
		document1.setCreateDefaults(true);
		
		URI uri = new URI(document1.getDefaultURIprefix());
		
		try{
		document1.getCollection(uri).getMembers();
		fail();
		}
		catch(NullPointerException e)
		{
			
		}
	}
	
	
	/*The following tests are testing the SBOLDocument methods pertaining 
	 * only to the topLevel Module Definition. 
	 * 
	 * 
	 */
	@Test
	public void test_removeModuleDefinition() throws URISyntaxException, SBOLValidationException
	{
		
		String preURI="http://partsregistry.org";
		SBOLDocument document1 = new SBOLDocument();
		document1.setDefaultURIprefix(preURI);
		document1.setTypesInURIs(true);
		document1.setComplete(true);
		document1.setCreateDefaults(true);
		
		//make a ModuleDefinition 
		ModuleDefinition holdModels = new ModuleDefinition(new URI(preURI));
		
		URI language = new URI("http://identifiers.org/edam/format_2585");
		URI framework = new URI("http://identifiers.org/biomodels.sbo/SBO:0000062");
		URI role = new URI("http://sbols.org/v2#module_model");
		
		//make a Model to add to the ModuleDefinition
		Model firstModel = new Model(new URI(preURI), language, framework, role);
		
		//add a Model into the ModuleDefinition
		holdModels.addModel(firstModel);
		
		document1.addModuleDefinition(holdModels);
		
		document1.removeModuleDefinition(holdModels);
		
		assertTrue(document1.removeModuleDefinition(holdModels) == false);
	    	
	}
	
	
//	@Test 
//	public void test_getModuleDefinitionWithIDAndVersion() throws URISyntaxException
//	{
//		
//		String preURI="http://partsregistry.org";
//		SBOLDocument document1 = new SBOLDocument();
//		document1.setDefaultURIprefix(preURI);
//		document1.setTypesInURIs(true);
//		document1.setComplete(true);
//		document1.setCreateDefaults(true);
//		
//		
//		//make a ModuleDefinition 
//		ModuleDefinition holdModels = new ModuleDefinition(new URI(preURI));
//		
//		try{
//		holdModels.setDisplayId("");
//		fail();
//		}
//		catch(SBOLValidationException e)
//		{
//			System.out.println(e.getMessage());
//		}
//		
//		//holdModels.setDisplayId("Anderson Promoter");
//		holdModels.setVersion("1.0");
//		
//		URI language = new URI("http://identifiers.org/edam/format_2585");
//		URI framework = new URI("http://identifiers.org/biomodels.sbo/SBO:0000062");
//		URI role = new URI("http://sbols.org/v2#module_model");
//		
//		assertTrue(holdModels.getIdentity().toString().equals(preURI));
//		
//		assertTrue(document1.getModuleDefinition(holdModels.getDisplayId(), holdModels.getDisplayId()).equals(holdModels));
//		
//	}
	
	@Test
	public void Test_setEncoding() throws SBOLValidationException
	{
		
		String preURI="http://partsregistry.org";
		SBOLDocument document1 = new SBOLDocument();
		document1.setDefaultURIprefix(preURI);
		document1.setTypesInURIs(true);
		document1.setComplete(true);
		document1.setCreateDefaults(true);
		
		Sequence s = document1.createSequence("seq_187", "tcctat", Sequence.IUPAC_DNA);
		
		try
		{
			s.setEncoding(null);
			fail();
			
		}
		catch(SBOLValidationException e){}
		
		try
		{
		 s.setElements(null);
		fail();
		}
		catch(SBOLValidationException e){}
		
	}
	
	/*the following series of tests check the Sequence class*/
	
	@Test
	public void test_SequenceEquals()
	{
		//Sequence s = new Sequence(Sequence.IUPAC_DNA, "", null);
		
			
	}
	
	/*the following tests check ComponentDefinition class*/
	
	/*
	 * Throws SBOLValidationException if ComponentDefinition
	 * has muliple types associated
	 */
/*	@Test
	public void test_AddType() throws URISyntaxException
	{
		String preURI="http://partsregistry.org";
		
		SBOLDocument document1 = new SBOLDocument();
		document1.setDefaultURIprefix(preURI);
		document1.setTypesInURIs(true);
		document1.setComplete(true);
		document1.setCreateDefaults(true);
		
		HashSet <URI> types = new HashSet <URI >(Arrays.asList(ComponentDefinition.RNA, URI.create("http://identifiers.org/chebi/CHEBI:4705")));
		ComponentDefinition TetR_promoter = null;
		try
		{
			//create a ComponentDefinition
			TetR_promoter = document1.createComponentDefinition("BBa_R0040", types);
			document1.addComponentDefinition(TetR_promoter);
			assertTrue(TetR_promoter.addType(new URI(document1.getDefaultURIprefix())) == TetR_promoter.getTypes().contains(ComponentDefinition.RNA));
			
		}
		catch(SBOLValidationException e){}
		
		try
		{
			TetR_promoter.addType(new URI(document1.getDefaultURIprefix()));
			fail();	
		}
		catch(SBOLValidationException e){}
	} 
	@Test
	public void test_removeType() throws URISyntaxException
	{
		String preURI="http://partsregistry.org";
		
		SBOLDocument document1 = new SBOLDocument();
		document1.setDefaultURIprefix(preURI);
		document1.setTypesInURIs(true);
		
		//HashSet <URI> types = new HashSet <URI >(Arrays.asList(ComponentDefinition.RNA, URI.create("http://identifiers.org/chebi/CHEBI:4705")));
		HashSet <URI> types = new HashSet <URI >(Arrays.asList(new URI(document1.getDefaultURIprefix())));
		ComponentDefinition TetR_promoter = document1.createComponentDefinition("BBa_R0040", types);
		document1.addComponentDefinition(TetR_promoter);

		try
		{
			//create a ComponentDefinition
			TetR_promoter.addType(new URI(document1.getDefaultURIprefix()));

			TetR_promoter.removeType(new URI(document1.getDefaultURIprefix()));
			fail();
		}
		catch(SBOLValidationException e){}
	} */
	
	@Test
	public void test_setTypes() throws URISyntaxException, SBOLValidationException
	{
	
		String preURI="http://partsregistry.org";
		HashSet <URI> types = null;
		try
		{
			//create a ComponentDefinition
			ComponentDefinition TetR_promoter = new ComponentDefinition(new URI("http://partsregistry.org"), types);
			TetR_promoter.setTypes(types);
			fail();
		}
		catch(SBOLValidationException e){}
		
		try
		{
			types = new HashSet <URI >();
			//create a ComponentDefinition
			ComponentDefinition TetR_promoter = new ComponentDefinition(new URI("http://partsregistry.org"), types);
			TetR_promoter.setTypes(types);
			fail();
		}
		catch(SBOLValidationException e){}
				
		SBOLDocument document1 = new SBOLDocument();
		document1.setDefaultURIprefix(preURI);
		document1.setTypesInURIs(true);
		document1.setComplete(true);
		document1.setCreateDefaults(true);
		
		types = new HashSet <URI >(Arrays.asList(ComponentDefinition.DNA_REGION, URI.create("http://identifiers.org/chebi/CHEBI:4705")));
		ComponentDefinition TetR_promoter = new ComponentDefinition(new URI("http://partsregistry.org"), types);

		//try and set types
		TetR_promoter.setTypes(types);
		
		//grab types to see if a success
		try{
			assertTrue(TetR_promoter.getTypes().equals(types));
		}
		catch(Exception e){}
	}
	
	/* The following are a series of tests pertaining to ComponentDefinition class */
	
	@Test
	public void addType_CD() throws URISyntaxException, SBOLValidationException
	{
		String preURI="http://partsregistry.org";
		
		SBOLDocument document1 = new SBOLDocument();
		document1.setDefaultURIprefix(preURI);
		document1.setTypesInURIs(true);
		
		HashSet <URI> types = new HashSet <URI >(Arrays.asList(ComponentDefinition.DNA_REGION));
		ComponentDefinition TetR_promoter = null;
		TetR_promoter = new ComponentDefinition(new URI("http://partsregistry.org"), types);

		try 
		{
			 TetR_promoter.addType(ComponentDefinition.DNA_REGION);
			 fail();	
		} 
		catch(SBOLValidationException e){}
		
		types = new HashSet <URI >(Arrays.asList(ComponentDefinition.RNA_REGION));
		TetR_promoter = new ComponentDefinition(new URI("http://partsregistry.org"), types);

		try
		{
			 TetR_promoter.addType(ComponentDefinition.RNA_REGION);
			 fail();
		}
		catch(SBOLValidationException e){}
		
		types = new HashSet <URI >(Arrays.asList(ComponentDefinition.PROTEIN));
		TetR_promoter = new ComponentDefinition(new URI("http://partsregistry.org"), types);

		try
		{
			 TetR_promoter.addType(ComponentDefinition.PROTEIN);
			 fail();
		}
		catch(SBOLValidationException e){}
		
		types = new HashSet <URI >(Arrays.asList(ComponentDefinition.SMALL_MOLECULE));
		TetR_promoter = new ComponentDefinition(new URI("http://partsregistry.org"), types);

		try
		{
			 TetR_promoter.addType(ComponentDefinition.SMALL_MOLECULE);
			 fail();
		}
		catch(SBOLValidationException e){}
		
		types = new HashSet <URI >(Arrays.asList(ComponentDefinition.SMALL_MOLECULE));
		TetR_promoter = new ComponentDefinition(new URI("http://partsregistry.org"), types);

		try
		{
			 assertTrue(TetR_promoter.addType(ComponentDefinition.DNA_REGION));
		}
		catch(SBOLValidationException e){}
		
		//else a SBOLDocument can't be null --> this is checked further up the hierarchy
	}
	
		@Test
		public void removeType_CD() throws URISyntaxException, SBOLValidationException
		{
			HashSet <URI> types = new HashSet <URI >(Arrays.asList(ComponentDefinition.DNA_REGION));
			ComponentDefinition TetR_promoter = null;
			TetR_promoter = new ComponentDefinition(new URI("http://partsregistry.org"), types);
			
			try
			{
				TetR_promoter.removeType(ComponentDefinition.DNA_REGION);
				fail();
			}
			catch(SBOLValidationException e){}
			TetR_promoter.addType( URI.create("http://identifiers.org/chebi/CHEBI:4705"));
			assertTrue(TetR_promoter.removeType(ComponentDefinition.DNA_REGION));
		}
		
		@Test
		public void removeRole_CD() throws URISyntaxException, SBOLValidationException
		{
			HashSet <URI> types = new HashSet <URI >(Arrays.asList(ComponentDefinition.DNA_REGION));
			ComponentDefinition TetR_promoter = null;
			TetR_promoter = new ComponentDefinition(new URI("http://partsregistry.org"), types);
			URI promoter_role = new URI("http://identifiers.org/so/SO:0000167");
			assertTrue(TetR_promoter.addRole(promoter_role));
			assertTrue(TetR_promoter.removeRole(promoter_role));
			
		}
		
		@Test
		public void containsRole_CD() throws URISyntaxException, SBOLValidationException
		{
			HashSet <URI> types = new HashSet <URI >(Arrays.asList(ComponentDefinition.DNA_REGION));
			ComponentDefinition TetR_promoter = new ComponentDefinition(new URI("http://partsregistry.org"), types);
			URI promoter_role = new URI("http://identifiers.org/so/SO:0000167");
			assertTrue(TetR_promoter.addRole(promoter_role));
			assertTrue(TetR_promoter.containsRole(promoter_role));
		}
		
		@Test 
		public void addSeq_CD() throws URISyntaxException, SBOLValidationException 
		{
			String preURI="http://doesnotexist.com";
			SBOLDocument document1 = new SBOLDocument();
			document1.setDefaultURIprefix(preURI);
			document1.setTypesInURIs(true);
			document1.setComplete(true);
			document1.setCreateDefaults(true);
			
			HashSet <URI> types = new HashSet <URI >(Arrays.asList(ComponentDefinition.DNA_REGION));
			ComponentDefinition TetR_promoter = new ComponentDefinition(new URI("http://partsregistry.org"), types);
			
			document1.addComponentDefinition(TetR_promoter);
			
			Sequence s = new Sequence(Sequence.IUPAC_DNA, "", Sequence.IUPAC_DNA);
			try
			{
				assertTrue(TetR_promoter.addSequence(s));
			}
			catch(SBOLValidationException e){}
			
			try
			{
				TetR_promoter.addSequence(new Sequence(null, null, null));
				fail();	
			}
			catch(SBOLValidationException e){}
		
			ComponentDefinition TetR_promoter2 = new ComponentDefinition(new URI("http://partsregistry.org"), types);
			try
			{
			  	assertTrue(TetR_promoter2.addSequence(s));
			}
			catch(SBOLValidationException e){}
			
		} 
		
		@Test
		public void removeSeq_CD() throws URISyntaxException, SBOLValidationException
		{		
			//create a CD and add sequence to it. 
			HashSet <URI> types = new HashSet <URI >(Arrays.asList(ComponentDefinition.DNA_REGION));
			ComponentDefinition TetR_promoter = new ComponentDefinition(new URI("http://partsregistry.org"), types);
			Sequence s = new Sequence(Sequence.IUPAC_DNA, "", Sequence.IUPAC_DNA);
			TetR_promoter.addSequence(s);
			
			//case 1: document1 is null
			assertTrue(TetR_promoter.containsSequence(Sequence.IUPAC_DNA));
			assertTrue(TetR_promoter.removeSequence(Sequence.IUPAC_DNA));	
			TetR_promoter.clearSequences();
			
			//case 2: document is not null
			String preURI="http://doesnotexist.com";
			SBOLDocument document1 = new SBOLDocument();
			document1.setDefaultURIprefix(preURI);
			document1.setTypesInURIs(true);
			document1.setComplete(true);
			document1.setCreateDefaults(true);
			
			Sequence s2 = new Sequence(Sequence.IUPAC_PROTEIN, "", Sequence.IUPAC_DNA);
			TetR_promoter.addSequence(s2);
			document1.addComponentDefinition(TetR_promoter);
			assertTrue(TetR_promoter.containsSequence(Sequence.IUPAC_PROTEIN));
			assertFalse(TetR_promoter.containsSequence(Sequence.IUPAC_RNA));
			assertTrue(TetR_promoter.removeSequence(Sequence.IUPAC_PROTEIN));	
			//assertTrue(TetR_promoter.getSequences().size() == 0);
		}
		
		
		
	
	
	
	
}

