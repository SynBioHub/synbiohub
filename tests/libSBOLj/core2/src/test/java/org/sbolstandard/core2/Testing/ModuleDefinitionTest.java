package org.sbolstandard.core2.Testing;

import static org.junit.Assert.*;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.Model;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SystemsBiologyOntology;

public class ModuleDefinitionTest {
	private SBOLDocument doc = null;
	private ModuleDefinition geneticToggleSwitch = null;
	private ModuleDefinition TetRInverter_MD = null;
	private ModuleDefinition LacIInverter_MD = null;
	private Module TetRInverter = null;
	private Module LacIInverter = null;
	private ComponentDefinition TetR_CD = null;
	private ComponentDefinition LacI_CD = null;
	private FunctionalComponent TetR_FC = null;
	private FunctionalComponent LacI_FC = null;
	private Interaction TetR_promotes_LacI = null;
	@Before
	public void setUp() throws Exception {
		doc = new SBOLDocument();
		doc.setDefaultURIprefix("http://sbols.org/CRISPR_Example/");
		doc.setComplete(true);
		doc.setCreateDefaults(true);
		doc.setComplete(true);
		
		/*add the main ModuleDefinition to the document*/
		geneticToggleSwitch = doc.createModuleDefinition("geneticToggleSwitch");
		
		/*create MD's for each of the Modules*/
		TetRInverter_MD = doc.createModuleDefinition("TetRInverter_MD");
		LacIInverter_MD = doc.createModuleDefinition("LacIInverter_MD");
		
		TetRInverter = geneticToggleSwitch.createModule("TetRInverter", "TetRInverter_MD");
		LacIInverter = geneticToggleSwitch.createModule("LacIInverter", "LacIInverter_MD");
		
		/*create CDs*/
		TetR_CD = doc.createComponentDefinition("TetR_CD", ComponentDefinition.DNA_REGION);
		LacI_CD = doc.createComponentDefinition("LacI_CD", ComponentDefinition.DNA_REGION);
		
		doc.createComponentDefinition("TetR_Promoter_CD", ComponentDefinition.DNA_REGION);
		doc.createComponentDefinition("TetR_Terminator_CD", ComponentDefinition.DNA_REGION);
		doc.createComponentDefinition("TetR_Gene_CD", ComponentDefinition.DNA_REGION);

		doc.createComponentDefinition("LacI_Promoter_CD", ComponentDefinition.DNA_REGION);
		doc.createComponentDefinition("LacI_Terminator_CD", ComponentDefinition.DNA_REGION);
		doc.createComponentDefinition("LacI_Gene_CD", ComponentDefinition.DNA_REGION);
		
		doc.createComponentDefinition("target_gene", ComponentDefinition.DNA_REGION);
		TetR_CD.createComponent("gene", AccessType.PUBLIC, "target_gene");
		
		/*add the corresponding components to the appropriate CD*/
		TetR_CD.createComponent("TetR_promoter", AccessType.PUBLIC, "TetR_Promoter_CD", "");
		TetR_CD.createComponent("TetR_terminator", AccessType.PUBLIC, "TetR_Terminator_CD", "");
		TetR_CD.createComponent("TetR_gene", AccessType.PUBLIC, "TetR_Gene_CD", "");
				
		LacI_CD.createComponent("LacI_promoter", AccessType.PUBLIC, "LacI_Promoter_CD", "");
		LacI_CD.createComponent("LacI_terminator", AccessType.PUBLIC, "LacI_Terminator_CD", "");
		LacI_CD.createComponent("LacI_gene", AccessType.PUBLIC, "LacI_Gene_CD", "");
		
		TetR_FC = TetRInverter_MD.createFunctionalComponent("TetR_FC", AccessType.PUBLIC, "TetR_CD", DirectionType.NONE);
		LacI_FC = LacIInverter_MD.createFunctionalComponent("LacI_FC", AccessType.PUBLIC, "LacI_CD", DirectionType.NONE);
		
		TetRInverter_MD.createFunctionalComponent("TetR_Promoter_FC", AccessType.PUBLIC, "TetR_Promoter_CD", DirectionType.NONE);
		TetRInverter_MD.createFunctionalComponent("TetR_Terminator_FC", AccessType.PUBLIC, "TetR_Terminator_CD", DirectionType.NONE);
		TetRInverter_MD.createFunctionalComponent("TetR_Gene_FC", AccessType.PUBLIC, "TetR_Gene_CD", DirectionType.NONE);

		/*create some Interactions*/
		TetR_promotes_LacI = geneticToggleSwitch.createInteraction("TetR_promotes_LacI", new URI("http://identifiers.org/biomodels.sbo/SBO:0000169"));
		TetR_promotes_LacI.createParticipation("TetR_simulator", "TetR_CD", new URI("http://identifiers.org/biomodels.sbo/SBO:0000459"));
		TetR_promotes_LacI.createParticipation("LacI_product", "LacI_CD", new URI("http://identifiers.org/biomodels.sbo/SBO:0000011"));
		
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test 
	public void test_createModule() throws SBOLValidationException
	{
		assertNotNull(geneticToggleSwitch.getModule("TetRInverter"));
		assertNotNull(geneticToggleSwitch.getModule("LacIInverter"));
	}
	
	@Test
	public void test_getModule() 
	{
		assertTrue(TetRInverter.equals(geneticToggleSwitch.getModule("TetRInverter")));
		assertTrue(LacIInverter.equals(geneticToggleSwitch.getModule("LacIInverter")));
		assertTrue(geneticToggleSwitch.getModule(LacIInverter.getIdentity()).equals(LacIInverter));
		assertTrue(geneticToggleSwitch.getModules().size() == 2);
	}
	
	@Test
	public void test_removeModule()
	{
		geneticToggleSwitch.removeModule(TetRInverter);
		assertTrue(geneticToggleSwitch.getModules().size() == 1);
		assertTrue(geneticToggleSwitch.getModule("TetRInverter") == null);
	}
	
	@Test
	public void test_createFuncComp() throws SBOLValidationException
	{	
		assertNotNull(TetR_FC);
		assertNotNull(LacI_FC);
		assertTrue(TetRInverter_MD.getFunctionalComponent("TetR_FC").equals(TetR_FC));
		assertTrue(LacIInverter_MD.getFunctionalComponent("LacI_FC").equals(LacI_FC));
	}
	
	@Test
	public void test_removeFuncComp() throws SBOLValidationException
	{
		assertTrue(TetRInverter_MD.removeFunctionalComponent(TetR_FC));
		assertTrue(LacIInverter_MD.removeFunctionalComponent(LacI_FC));
		assertNull(TetRInverter_MD.getFunctionalComponent("TetR_FC"));
		assertNull(LacIInverter_MD.getFunctionalComponent("LacI_FC"));
	} 
	
	@Test
	public void test_create_retrieve_Interaction()
	{
		assertNotNull(TetR_promotes_LacI);
		assertNotNull(geneticToggleSwitch.getInteraction("TetR_promotes_LacI"));
		assertTrue(geneticToggleSwitch.getInteraction(TetR_promotes_LacI.getIdentity()).equals(TetR_promotes_LacI));
	}
	
	@Test
	public void test_removeInteraction()
	{
		assertTrue(geneticToggleSwitch.removeInteraction(TetR_promotes_LacI));
		assertNull(geneticToggleSwitch.getInteraction("TetR_promotes_LacI"));
		assertTrue(geneticToggleSwitch.getInteractions().size()== 0);
	}
	
	@Test
	public void test_RoleMethods() throws URISyntaxException
	{
		TetRInverter_MD.addRole(SystemsBiologyOntology.PROMOTER);
		assertTrue(TetRInverter_MD.containsRole(SystemsBiologyOntology.PROMOTER));
		TetRInverter_MD.removeRole(SystemsBiologyOntology.PROMOTER);
		assertFalse(TetRInverter_MD.containsRole(SystemsBiologyOntology.PROMOTER));
	}
	
	@Test
	public void test_toString()
	{
		assertTrue(geneticToggleSwitch.toString().length() != 0);
		assertNotNull(geneticToggleSwitch.toString());
		assertTrue(!geneticToggleSwitch.toString().contains("version="));
	}
	
	@Test
	public void test_moduleDefinitionDeepCopy() throws SBOLValidationException
	{
		SBOLDocument doc_copy = new SBOLDocument();
		doc_copy.setDefaultURIprefix("http://sbols.org/CRISPR_Example/");
		doc_copy.setComplete(false);
		doc_copy.setCreateDefaults(true);
		doc_copy.createCopy(geneticToggleSwitch);
		ModuleDefinition TetRInverter_copy = doc_copy.getModuleDefinition("geneticToggleSwitch", "");
		assertTrue(TetRInverter_copy.getModule("TetRInverter").equals(TetRInverter));	
		
		assertTrue(doc_copy.createRecursiveCopy(geneticToggleSwitch).equals(doc));
		
	}
	
	@Test
	public void test_modelMethods() throws SBOLValidationException
	{
		Model model=doc.createModel(
				"pIKE_Toggle_1",
				"1.0",
				URI.create("http://virtualparts.org/part/pIKE_Toggle_1"),
				URI.create("http://identifiers.org/edam/format_2585"), 
				SystemsBiologyOntology.CONTINUOUS_FRAMEWORK); 
		
		assertTrue(geneticToggleSwitch.addModel("pIKE_Toggle_1"));
		assertTrue(doc.getModuleDefinition("geneticToggleSwitch", "").getModels().contains(model));
		assertTrue(geneticToggleSwitch.removeModel(model.getPersistentIdentity()));
		assertTrue(geneticToggleSwitch.addModel("pIKE_Toggle_1", "1.0"));
		assertTrue(doc.getModuleDefinition("geneticToggleSwitch", "").getModels().contains(model));
		assertTrue(geneticToggleSwitch.containsModel(model.getIdentity()));

	}

	
	
}
