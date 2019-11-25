package org.sbolstandard.core2.examples;

import java.net.URISyntaxException;

import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;

public class MapsToExample {


	public static void main(String[] args) throws URISyntaxException, SBOLValidationException {
		SBOLDocument doc = new SBOLDocument();

		doc.setDefaultURIprefix("http://sbols.org/MapsToExample/");
		doc.setComplete(true);
		doc.setCreateDefaults(true);
		
		String version = "";
		
		ModuleDefinition md1 = doc.createModuleDefinition("md1", version);
		ComponentDefinition fc1_def = doc.createComponentDefinition("fc1_def", version, ComponentDefinition.DNA_REGION);
		ComponentDefinition fc2_def = doc.createComponentDefinition("fc2_def", version, ComponentDefinition.DNA_REGION);
		FunctionalComponent fc1 = md1.createFunctionalComponent(
					"fc1", AccessType.PUBLIC, "fc1_def", version, DirectionType.NONE);
		FunctionalComponent fc2 = md1.createFunctionalComponent(
				"fc2", AccessType.PUBLIC, "fc2_def", version, DirectionType.NONE);
				
		ComponentDefinition cd = doc.createComponentDefinition("cd", version, ComponentDefinition.DNA_REGION);
		fc1_def.createComponent("component", AccessType.PUBLIC, "cd");
		
		fc1.createMapsTo("mapsTo", RefinementType.USELOCAL, "fc2", "component");
		
		SBOLValidate.validateSBOL(doc, true, true, true);
		if (SBOLValidate.getNumErrors() > 0) {
			for (String error : SBOLValidate.getErrors()) {
				System.out.println(error);
			}
			return;
		}

	}



}
