package org.sbolstandard.core2.Testing;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SystemsBiologyOntology;

public class InteractionTest {
	private SBOLDocument doc = null;
	private ComponentDefinition TetR = null;
	private ModuleDefinition TetRInverter_MD = null;

	@Before
	public void setUp() throws Exception {
		doc = new SBOLDocument();
		doc.setDefaultURIprefix("http://sbols.org/CRISPR_Example/");
		doc.setComplete(true);
		doc.setCreateDefaults(true);
		doc.setComplete(true);
		TetRInverter_MD = doc.createModuleDefinition("TetRInverter_MD");

		TetR = doc.createComponentDefinition("TetR", ComponentDefinition.PROTEIN);
		TetRInverter_MD.createFunctionalComponent(
				"TetRInverter_fc", AccessType.PUBLIC, TetR.getIdentity(), DirectionType.INOUT);
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void test_RoleAndParticpantMethods() throws SBOLValidationException
	{
		Interaction TetR_Interaction = TetRInverter_MD.createInteraction("TetR_Interaction", SystemsBiologyOntology.NON_COVALENT_BINDING);
		Participation TetR_part = TetR_Interaction.createParticipation("TetR", "TetR", SystemsBiologyOntology.PRODUCT);
		assertTrue(TetR_Interaction.containsType(SystemsBiologyOntology.NON_COVALENT_BINDING));
		assertTrue(TetR_Interaction.addType(SystemsBiologyOntology.ABSOLUTE_STIMULATION));
		assertTrue(TetR_Interaction.getTypes().size() == 2);
		assertTrue(TetR_Interaction.removeType(SystemsBiologyOntology.NON_COVALENT_BINDING));
		assertTrue(TetR_Interaction.getTypes().size() == 1);
		assertTrue(TetR_Interaction.getParticipation(TetR_part.getIdentity()).equals(TetR_part));
		assertTrue(TetR_Interaction.getParticipations().size() == 1);
		assertTrue(TetR_Interaction.removeParticipation(TetR_part));
		assertTrue(TetR_Interaction.getParticipations().size() == 0);
		assertFalse(TetR_Interaction.removeType(SystemsBiologyOntology.NON_COVALENT_BINDING));

	}
	

}
