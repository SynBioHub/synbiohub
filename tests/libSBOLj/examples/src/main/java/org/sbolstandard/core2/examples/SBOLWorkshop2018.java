package org.sbolstandard.core2.examples;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import org.sbolstandard.core2.Activity;
import org.sbolstandard.core2.Collection;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Implementation;
import org.sbolstandard.core2.Plan;
import org.sbolstandard.core2.RestrictionType;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.TopLevel;
import org.synbiohub.frontend.IdentifiedMetadata;
import org.synbiohub.frontend.SynBioHubException;
import org.synbiohub.frontend.SynBioHubFrontend;

public class SBOLWorkshop2018 {
	
	public static String version = "1";
	
	public static int len(SBOLDocument sbolDocument) {
		int length = 0;
		length += sbolDocument.getActivities().size();
		length += sbolDocument.getAgents().size();
		length += sbolDocument.getAttachments().size();
		length += sbolDocument.getCollections().size();
		length += sbolDocument.getCombinatorialDerivations().size();
		length += sbolDocument.getComponentDefinitions().size();
		length += sbolDocument.getImplementations().size();
		length += sbolDocument.getModels().size();
		length += sbolDocument.getModuleDefinitions().size();
		length += sbolDocument.getPlans().size();
		length += sbolDocument.getSequences().size();

		return length;
	}

	public static void printCounts(SBOLDocument sbolDocument) {
		System.out.println("Activity......................" + sbolDocument.getActivities().size());
		System.out.println("Agents........................" + sbolDocument.getAgents().size());
		System.out.println("Attachment...................." + sbolDocument.getAttachments().size());
		System.out.println("Collection...................." + sbolDocument.getCollections().size());
		System.out.println("CombinatorialDerivation......." + sbolDocument.getCombinatorialDerivations().size());
		System.out.println("ComponentDefinition..........." + sbolDocument.getComponentDefinitions().size());
		System.out.println("Implementation................" + sbolDocument.getImplementations().size());
		System.out.println("Model........................." + sbolDocument.getModels().size());
		System.out.println("ModuleDefinition.............." + sbolDocument.getModuleDefinitions().size());
		System.out.println("Plans........................." + sbolDocument.getPlans().size());
		System.out.println("Sequence......................" + sbolDocument.getSequences().size());
		System.out.println("---");
		System.out.println("Total........................." + len(sbolDocument));
	}

	public static void compile(SBOLDocument sbolDocument,ComponentDefinition componentDefinition) throws SBOLValidationException {
		for (Component component : componentDefinition.getComponents()) {
			SequenceAnnotation sa = componentDefinition.getSequenceAnnotation(component);
			if (sa==null) {
				sa = componentDefinition.createSequenceAnnotation(component.getDisplayId()+"_annot", "location");
				sa.setComponent(component.getIdentity());
			}
		}
		int start = 1;
		String elements = "";
		for (Component component : componentDefinition.getSortedComponents()) {
			Sequence seq = component.getDefinition().getSequenceByEncoding(Sequence.IUPAC_DNA);
			int end = start + seq.getElements().length()-1;
			SequenceAnnotation sa = componentDefinition.getSequenceAnnotation(component);
			componentDefinition.removeSequenceAnnotation(sa);
			sa = componentDefinition.createSequenceAnnotation(component.getDisplayId()+"_annot", "range", 
					start, end);
			start = end + 1;
			sa.setComponent(component.getIdentity());
			elements += seq.getElements();
		}
		Sequence seq = sbolDocument.createSequence(componentDefinition.getDisplayId()+"_seq", version, elements, Sequence.IUPAC_DNA);
		componentDefinition.addSequence(seq);
	}

	public static void main(String[] args) throws SBOLValidationException, IOException, SBOLConversionException, SynBioHubException {	

		/* Getting a Device from an SBOL Compliant XML */

		// Start a new SBOL Document to hold the device
		SBOLDocument doc = new SBOLDocument();
		doc.setCreateDefaults(true);

		// Set your Homespace. All new SBOL objects will be created in this namespace
		String my_namespace = "http://my_namespace.org/";
		doc.setDefaultURIprefix(my_namespace);

		// Create a new device
		ComponentDefinition my_device = doc.createComponentDefinition("my_device", version, ComponentDefinition.DNA_REGION);
		System.out.println(my_device.getIdentity());
		System.out.println("");

		// Load some genetic parts taken from the Cello paper
		// TODO: need to fill in path
		SBOLDocument cello_parts = SBOLReader.read("/Users/myers/Downloads/parts.xml");
		System.out.println(len(cello_parts));
		printCounts(cello_parts);
		System.out.println("");

		// Explore document contents. Notice it is composed of
		// componentDefinitions and sequences
		for (TopLevel topLevel : cello_parts.getTopLevels()) {
			System.out.println(topLevel.getIdentity());
		}
		System.out.println("");

		// Import these objects into your Document
		doc.createCopy(cello_parts);
		for (TopLevel topLevel : doc.getTopLevels()) {
			System.out.println(topLevel.getIdentity());
		}
		System.out.println("");

		// Retrieve an object from the Document using its uniform resource identifier (URI)
		Collection promoter_collection = doc.getCollection(URI.create("http://examples.org/Collection/promoters/1"));

		// A Collection contains a list of URI references to objects, not the object themselves
		for (TopLevel topLevel : promoter_collection.getMembers()) {
			System.out.println(topLevel.getIdentity());
		}
		System.out.println("");

		// Retrieve a component, using its full URI
		ComponentDefinition promoter = doc.getComponentDefinition(URI.create("http://examples.org/ComponentDefinition/pPhlF/1"));

		// Review the BioPAX and Sequence Ontology terms that describe this component
		System.out.println(promoter.getTypes());
		System.out.println(promoter.getRoles());
		System.out.println("");

		/* Getting a Device from Synbiohub */

		// Start an interface to the part shop
		SynBioHubFrontend part_shop = new SynBioHubFrontend("https://synbiohub.org");

		// Search for records from the interlab study
		ArrayList<IdentifiedMetadata> records = part_shop.getMatchingComponentDefinitionMetadata("interlab", null, null, null, 0, 50);
		for (IdentifiedMetadata record : records) {
			System.out.println(record.getDisplayId()+": "+record.getUri());
		}
		System.out.println("");

		// Import the medium device into the user's Document
		doc.createCopy(part_shop.getSBOL(URI.create("https://synbiohub.org/public/iGEM_2016_interlab/Medium_2016Interlab/1")));

		// Explore the new parts
		for (TopLevel topLevel : doc.getTopLevels()) {
			System.out.println(topLevel.getClass().getSimpleName()+": "+topLevel.getIdentity());
		}
		System.out.println("");

		/* Extracting a ComponentDefinition from a Pre-existing Device */

		// Extract the medium strength promoter
		ComponentDefinition medium_strength_promoter = doc.getComponentDefinition(URI.create("https://synbiohub.org/public/igem/BBa_J23106/1"));

		// Get parts for a new circuit
		ComponentDefinition rbs = doc.getComponentDefinition(URI.create("http://examples.org/ComponentDefinition/Q2/1"));
		ComponentDefinition cds = doc.getComponentDefinition(URI.create("http://examples.org/ComponentDefinition/LuxR/1"));
		ComponentDefinition terminator = doc.getComponentDefinition(URI.create("http://examples.org/ComponentDefinition/ECK120010818/1"));

		// Assemble a new gene
		my_device.createSequenceConstraint("constraint1", RestrictionType.PRECEDES, medium_strength_promoter.getIdentity(), rbs.getIdentity());
		my_device.createSequenceConstraint("constraint2", RestrictionType.PRECEDES, rbs.getIdentity(), cds.getIdentity());
		my_device.createSequenceConstraint("constraint3", RestrictionType.PRECEDES, cds.getIdentity(), terminator.getIdentity());

		// Annotate the target construct with a Sequence Ontology term
		my_device.addRole(SequenceOntology.ENGINEERED_REGION);

		// Explore the newly assembled gene
		for (Component component : my_device.getComponents()) {
			System.out.println(component.getDisplayId());
		}
		System.out.println("");

		compile(doc,my_device);
		Sequence seq = my_device.getSequenceByEncoding(Sequence.IUPAC_DNA);
		System.out.println(seq.getElements());
		System.out.println("");

		/* Managing a Design-Build-Test-Learn workflow */

		Activity workflow_step_1 = doc.createActivity("build_1",version);
		Activity workflow_step_2 = doc.createActivity("build_2",version);
		Activity workflow_step_3 = doc.createActivity("test_1",version);
		Activity workflow_step_4 = doc.createActivity("analysis_1",version);

		Plan workflow_step_1_plan = doc.createPlan("gibson_assembly",version);
		Plan workflow_step_2_plan = doc.createPlan("transformation",version);
		Plan workflow_step_3_plan = doc.createPlan("promoter_characterization",version);
		Plan workflow_step_4_plan = doc.createPlan("parameter_optimization",version);

		workflow_step_1.createAssociation("association",URI.create("mailto:jdoe@my_namespace.org")).setPlan(workflow_step_1_plan.getIdentity());
		workflow_step_2.createAssociation("association",URI.create("mailto:jdoe@my_namespace.org")).setPlan(workflow_step_2_plan.getIdentity());
		workflow_step_3.createAssociation("association",URI.create("http://sys-bio.org/plate_reader_1")).setPlan(workflow_step_3_plan.getIdentity());
		workflow_step_4.createAssociation("association",URI.create("http://tellurium.analogmachine.org")).setPlan(workflow_step_4_plan.getIdentity());

		Implementation gibson_mix = doc.createImplementation("gibson_mix", version);
		gibson_mix.setBuilt(my_device);
		gibson_mix.addWasGeneratedBy(workflow_step_1.getIdentity());
		workflow_step_1.createUsage("usage", my_device.getIdentity());

		Collection clones = doc.createCollection("clones",version);
		Implementation clone1 = doc.createImplementation("clone1", version);
		clone1.setBuilt(my_device);
		clones.addMember(clone1.getIdentity());
		Implementation clone2 = doc.createImplementation("clone2", version);
		clone2.setBuilt(my_device);
		clones.addMember(clone2.getIdentity());
		Implementation clone3 = doc.createImplementation("clone3", version);
		clone3.setBuilt(my_device);
		clones.addMember(clone3.getIdentity());
		clones.addWasGeneratedBy(workflow_step_2.getIdentity());
		workflow_step_2.createUsage("usage", gibson_mix.getIdentity());

		Collection experiment1 = doc.createCollection("experiment1", version);
		experiment1.addWasGeneratedBy(workflow_step_3.getIdentity());
		workflow_step_3.createUsage("usage", clones.getIdentity());

		Collection analysis1 = doc.createCollection("analysis1", version);
		analysis1.addWasGeneratedBy(workflow_step_4.getIdentity());
		workflow_step_4.createUsage("usage", experiment1.getIdentity());

		// Validate the Document
		SBOLValidate.validateSBOL(doc, false, false, false);
		for (String error : SBOLValidate.getErrors()) {
			System.out.println(error);
		}
		System.out.println(analysis1.getIdentity());
		System.out.println("");

		/* Uploading the Device back to SynBioHub */

		// TODO: Need to provide your credentials
		String user_name = "myers";
		String password = "MaWen69!";
		part_shop.login(user_name, password);

		// Upon submission, the Document will be converted to a Collection with the following properties
		// The new Collection will have a URI that conforms to the following pattern:
		// https://synbiohub.org/user/<USERNAME>/<DOC.DISPLAYID>/<DOC.DISPLAYID>_collection
		String displayId = "my_device";
		String name = "my device";
		String description = "a description of the cassette";
		part_shop.createCollection(displayId, version, name, description, "", true, doc);

		// TODO: need to fill in your path
		String attachment_path = "/Users/myers/Downloads/results.txt";

		// Attach raw experimental data to the Test object here. Note the pattern
		URI test_uri = URI.create("https://synbiohub.org/user/" + user_name + "/" + displayId + "/experiment1/1");
		part_shop.attachFile(test_uri, attachment_path);

		// Attach processed experimental data here
		// TODO: need to fill in your path
		String other_attachement_path = "/Users/myers/Downloads/results.txt";
		URI analysis_uri = URI.create("https://synbiohub.org/user/" + user_name + "/" + displayId + "/analysis1/1");
		part_shop.attachFile(analysis_uri, other_attachement_path);

		System.out.println("Successfully uploaded");

	}
}
