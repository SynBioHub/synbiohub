package org.sbolstandard.core2.Testing;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.MapsTo;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

public class ModuleTest {
	private SBOLDocument doc = null;
	private ModuleDefinition geneticToggleSwitch = null;
	private ModuleDefinition TetRInverter_MD = null;
	private ModuleDefinition LacIInverter_MD = null;
	private ComponentDefinition TetR = null;
	private ComponentDefinition LacI = null;
	private ComponentDefinition TF_TetR = null;
	private ComponentDefinition TF_LacI = null;
	private FunctionalComponent TetRInverter_fc = null;
	private FunctionalComponent LacIInverter_fc = null;
	private FunctionalComponent TF_TetR_fc = null;
	private FunctionalComponent TF_LacI_fc = null;

	private Module TetRInverter = null;
	private Module LacIInverter = null;
	
	@Before
	public void setUp() throws Exception {
		doc = new SBOLDocument();
		doc.setDefaultURIprefix("http://sbols.org/CRISPR_Example/");
		doc.setComplete(true);
		doc.setCreateDefaults(true);
		doc.setComplete(true);
		
		geneticToggleSwitch=doc.createModuleDefinition("toggle_switch");
		LacIInverter_MD = doc.createModuleDefinition("LacIInverter_MD");
		TetRInverter_MD = doc.createModuleDefinition("TetRInverter_MD");
		TetR = doc.createComponentDefinition("TetR", ComponentDefinition.PROTEIN);
		LacI = doc.createComponentDefinition("LacI", ComponentDefinition.PROTEIN);
		TF_TetR = doc.createComponentDefinition("TF_TetR", ComponentDefinition.PROTEIN);
		TF_LacI = doc.createComponentDefinition("TF_LacI", ComponentDefinition.PROTEIN);
		
		TetRInverter_fc = geneticToggleSwitch.createFunctionalComponent(
				"TetRInverter_fc", AccessType.PUBLIC, TetR.getIdentity(), DirectionType.INOUT);
		LacIInverter_fc = geneticToggleSwitch.createFunctionalComponent(
				"LacIInverter_fc", AccessType.PUBLIC, LacI.getIdentity(), DirectionType.INOUT);
	
		TF_TetR_fc = TetRInverter_MD.createFunctionalComponent("TF_TetR_fc", AccessType.PUBLIC, TF_TetR.getIdentity(), DirectionType.NONE);
		TF_LacI_fc = LacIInverter_MD.createFunctionalComponent("TF_LacI_fc", AccessType.PUBLIC, TF_LacI.getIdentity(), DirectionType.NONE);

		TetRInverter = geneticToggleSwitch.createModule("TetRInverter", TetRInverter_MD.getIdentity());
		LacIInverter = geneticToggleSwitch.createModule("LacIInverter", LacIInverter_MD.getIdentity());
	}

	@Test
	public void test_MapsTo() throws SBOLValidationException
	{
		MapsTo TetRInverterMap = TetRInverter.createMapsTo("TetRInverterMap", RefinementType.USEREMOTE, TetRInverter_fc.getDisplayId(),TF_TetR_fc.getDisplayId());
		MapsTo LacIInverterMap = LacIInverter.createMapsTo("LacIInverterMap", RefinementType.USEREMOTE, LacIInverter_fc.getDisplayId(), TF_LacI_fc.getDisplayId());
	
		assertTrue(TetRInverter.getMapsTo("TetRInverterMap").equals(TetRInverterMap));
		assertTrue(TetRInverter.getMapsTo(TetRInverterMap.getIdentity()).equals(TetRInverterMap));
		assertTrue(LacIInverter.getMapsTo("LacIInverterMap").equals(LacIInverterMap));
		assertTrue(LacIInverter.getMapsTo(LacIInverterMap.getIdentity()).equals(LacIInverterMap));
		TetRInverter.removeMapsTo(TetRInverterMap);
		assertTrue(TetRInverter.getMapsTos().size() == 0);

	}
	

}
