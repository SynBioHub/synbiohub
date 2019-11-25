package org.sbolstandard.core2;

import static org.sbolstandard.core.datatree.Datatree.DocumentRoot;
import static org.sbolstandard.core.datatree.Datatree.NamedProperties;
import static org.sbolstandard.core.datatree.Datatree.NamedProperty;
import static org.sbolstandard.core.datatree.Datatree.NamespaceBinding;
import static org.sbolstandard.core.datatree.Datatree.NamespaceBindings;
import static org.sbolstandard.core.datatree.Datatree.NestedDocument;
import static org.sbolstandard.core.datatree.Datatree.TopLevelDocument;
import static org.sbolstandard.core.datatree.Datatree.TopLevelDocuments;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.sbolstandard.core.io.turtle.TurtleIo;
import org.sbolstandard.core.datatree.DocumentRoot;
import org.sbolstandard.core.datatree.NamedProperty;
import org.sbolstandard.core.datatree.NamespaceBinding;
import org.sbolstandard.core.datatree.NestedDocument;
import org.sbolstandard.core.datatree.TopLevelDocument;
import org.sbolstandard.core.io.CoreIoException;
import org.sbolstandard.core.io.json.JsonIo;
import org.sbolstandard.core.io.json.StringifyQName;
import org.sbolstandard.core.io.rdf.RdfIo;

/**
 * Provides methods to output SBOL files in XML/RDF format.
 * 
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author Matthew Pocock
 * @author Goksel Misirli
 * @version 2.1
 */

public class SBOLWriter
{
	/**
	 * A {@code true} value of the {@code keepGoing} flag tells the SBOL writer
	 * to continue writing an output file, after it encounters an SBOL conversion exception;
	 * a {@code false} value forces the writer to stop writing after it encounters
	 * an SBOL conversion exception.
	 */
	public static boolean keepGoing = true;
	
	private static List<String> errors = new ArrayList<String>();

	/**
	 * Returns the value of the {@code keepGoing} flag.
	 * @return the value of the {@code keepGoing} flag
	 */
	public static boolean isKeepGoing() {
		return keepGoing;
	}

	/**
	 * Sets the value for the keepGoing flag to the given boolean value.
	 * <p>
	 * A {@code true} value means that reading will keep going after encountering an SBOL validation exception, 
	 * and a {@code false} value means otherwise.
	 * 
	 * @param keepGoing the boolean value for the keepGoing flag
	 */
	public static void setKeepGoing(boolean keepGoing) {
		SBOLWriter.keepGoing = keepGoing;
	}

	/**
	 * Sets the error list that is used to store SBOL conversion exceptions 
	 * during reading to empty. 
	 */
	public static void clearErrors() {
		errors = new ArrayList<String>();
	}

	/**
	 * Returns the error list that is used to store SBOL conversion exceptions.
	 * @return the error list that is used to store SBOL conversion exceptions
	 */
	public static List<String> getErrors() {
		return errors;
	}

	/**
	 * Returns the number of errors in the error list. 
	 * @return the number of errors in the error list
	 */
	public static int getNumErrors() {
		return errors.size();
	}

	/**
	 * Outputs the given SBOL document's data from the RDF/XML serialization to the given file.
	 * <p>
	 * This method first creates a {@link BufferedOutputStream} from the given file, and then
	 * calls {@link #write(SBOLDocument, OutputStream)}.
	 * 
	 * @param doc the given SBOL document
	 * @param file the given output file
	 * @throws IOException see {@link IOException}
	 * @throws SBOLConversionException - problem found during serialization 
	 */
	public static void write(SBOLDocument doc, File file) throws IOException, SBOLConversionException {
		FileOutputStream stream = new FileOutputStream(file);
		BufferedOutputStream buffer = new BufferedOutputStream(stream);
		write(doc, buffer);
		stream.close();
		buffer.close();
	}
	
	/**
	 * Outputs this SBOL document's data from the serialization in the given serialization format 
	 * to the given file.
	 * <p>
	 * This method first creates a {@link BufferedOutputStream} from the given file, and then
	 * calls {@link #write(SBOLDocument, OutputStream, String)}.
	 * 
	 * @param doc the given SBOL document
	 * @param file the given output file
	 * @param fileType the given serialization format
	 * @throws IOException see {@link IOException}
	 * @throws SBOLConversionException - problem found during serialization 
	 */
	public static void write(SBOLDocument doc, File file, String fileType) throws IOException, SBOLConversionException
	{
		FileOutputStream stream = new FileOutputStream(file);
		BufferedOutputStream buffer = new BufferedOutputStream(stream);
		write(doc, buffer, fileType);
		buffer.close();
		stream.close();
	}

	/**
	 *  Outputs this SBOL document's data from the RDF/XML serialization to the given output stream.
	 * 
	 * @param doc the given SBOL document
	 * @param out the given output stream
	 * @throws SBOLConversionException - problem found during serialization 
	 */
	public static void write(SBOLDocument doc, OutputStream out) throws SBOLConversionException
	{
		try {
			writeRDF(new OutputStreamWriter(out),
					DocumentRoot( NamespaceBindings(doc.getNamespaceBindings()),
							TopLevelDocuments(getTopLevelDocument(doc))));
		}
		catch (XMLStreamException e) {
			throw new SBOLConversionException(e);
		}
		catch (FactoryConfigurationError e) {
			throw new SBOLConversionException(e);
		}
		catch (CoreIoException e) {
			throw new SBOLConversionException(e);
		}
	}

	/**
	 * Outputs this SBOL document's data from the RDF/XML serialization to a new file with the given file name.
	 * <p>
	 * This method calls {@link #write(SBOLDocument, File)} by passing this SBOL document, and a 
	 * new file with the given file name.
	 * 
	 * @param doc the given SBOL document
	 * @param filename the given output file name
	 * @throws IOException see {@link IOException}	 
	 * @throws SBOLConversionException - problem found during serialization 
	 */
	public static void write(SBOLDocument doc, String filename) throws IOException, SBOLConversionException
	{
		write(doc, new File(filename));
	}
	
	/**
 	 * Outputs this SBOL document's data from serialization in the given serialization format
	 * to a new file with the given file name.
	 * <p>
	 * This method calls {@link #write(SBOLDocument, File, String)} by passing this SBOL document, and a 
	 * new file with the given file name and type.
	 * 
	 * @param doc the given SBOLDocument object
	 * @param filename the name of the serialized output file
	 * @param fileType the given file format, such as RDF/XML, JSON, or Turtle.
	 * @throws IOException see {@link IOException}
	 * @throws SBOLConversionException - problem found during serialization 
	 */
	public static void write(SBOLDocument doc, String filename, String fileType) throws IOException, SBOLConversionException
	{
		write(doc, new File(filename), fileType);
	}

	/**
	 * Serializes a given SBOLDocument and outputs the data from the serialization to the given output stream
	 * in the specified fileType format.
	 * @param doc the given SBOLDocument object
	 * @param out the serialized output stream
	 * @param fileType the given file format, such as RDF/XML, JSON, or Turtle.
	 * @throws SBOLConversionException - problem found during serialization
	 * @throws IOException see {@link IOException}
	 */
	public static void write(SBOLDocument doc, OutputStream out, String fileType) throws SBOLConversionException, IOException
	{
		clearErrors();
		if (fileType.equals(SBOLDocument.FASTAformat)) {
			FASTA.write(doc, out);
		} else if (fileType.equals(SBOLDocument.GFF3format)) {
			GFF3.write(doc, out);
		} else if (fileType.equals(SBOLDocument.GENBANK)) {
			GenBank.write(doc, out);
		} else if (fileType.equals(SBOLDocument.SNAPGENE)) {
			SnapGene.write(doc, out);
		} else if (fileType.equals(SBOLDocument.JSON)) {
			try {
				writeJSON(new OutputStreamWriter(out),
						DocumentRoot( NamespaceBindings(doc.getNamespaceBindings()),
								TopLevelDocuments(getTopLevelDocument(doc))));
			}
			catch (CoreIoException e) {
				throw new SBOLConversionException(e);
			}
		} else if (fileType.equals(SBOLDocument.TURTLE)){
			try {
				writeTurtle(new OutputStreamWriter(out),
						DocumentRoot( NamespaceBindings(doc.getNamespaceBindings()),
								TopLevelDocuments(getTopLevelDocument(doc))));
			}
			catch (CoreIoException e) {
				throw new SBOLConversionException(e);
			}
		} else if (fileType.equals(SBOLDocument.RDFV1)){
			try {
				writeRDF(new OutputStreamWriter(out),
						DocumentRoot( NamespaceBindings(getNamespaceBindingsV1()),
								TopLevelDocuments(convertToV1Document(doc))));
			}
			catch (XMLStreamException e) {
				throw new SBOLConversionException(e);
			}
			catch (FactoryConfigurationError e) {
				throw new SBOLConversionException(e);
			}
			catch (CoreIoException e) {
				throw new SBOLConversionException(e);
			}
		} else {
			try {
				writeRDF(new OutputStreamWriter(out),
						DocumentRoot( NamespaceBindings(doc.getNamespaceBindings()),
								TopLevelDocuments(getTopLevelDocument(doc))));
			}
			catch (XMLStreamException e) {
				throw new SBOLConversionException(e);
			}
			catch (FactoryConfigurationError e) {
				throw new SBOLConversionException(e);
			}
			catch (CoreIoException e) {
				throw new SBOLConversionException(e);
			}
		}
	}

	private static void writeJSON(Writer stream, DocumentRoot<QName> document) throws CoreIoException
	{
		HashMap<String, Object> config = new HashMap<>();
		config.put(JsonGenerator.PRETTY_PRINTING, true);
		JsonGenerator writer = Json.createGeneratorFactory(config).createGenerator(stream);
		JsonIo jsonIo = new JsonIo();
		jsonIo.createIoWriter(writer).write(StringifyQName.qname2string.mapDR(document));
		writer.flush();
		writer.close();
	}

	private static void writeRDF(Writer stream, DocumentRoot<QName> document) throws XMLStreamException, FactoryConfigurationError, CoreIoException
	{
		XMLStreamWriter xmlWriter = new IndentingXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(stream));
		RdfIo rdfIo = new RdfIo();
		rdfIo.createIoWriter(xmlWriter).write(document);
		xmlWriter.flush();
		xmlWriter.close();
	}

	private static void writeTurtle(Writer stream, DocumentRoot<QName> document) throws CoreIoException
	{
		PrintWriter printWriter = new PrintWriter(stream);
		TurtleIo turtleIo = new TurtleIo();
		turtleIo.createIoWriter(printWriter).write(document);
		printWriter.flush();
	}

	private static void formatCollections (Set<Collection> collections, List<TopLevelDocument<QName>> topLevelDoc)
	{
		for(Collection c : collections)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();
			formatCommonTopLevelData(list, c);
			for (URI member : c.getMemberURIs())
			{
				list.add(NamedProperty(Sbol2Terms.Collection.hasMembers, member));
			}
			topLevelDoc.add(TopLevelDocument(Sbol2Terms.Collection.Collection, c.getIdentity(), NamedProperties(list)));
		}
	}

	private static void formatExperiments (Set<Experiment> experiments, List<TopLevelDocument<QName>> topLevelDoc)
	{
		for(Experiment expt : experiments)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();
			formatCommonTopLevelData(list, expt);
			for (URI experimentalData : expt.getExperimentalDataURIs())
			{
				list.add(NamedProperty(Sbol2Terms.Experiment.hasExperimentalData, experimentalData));
			}
			topLevelDoc.add(TopLevelDocument(Sbol2Terms.Experiment.Experiment, expt.getIdentity(), NamedProperties(list)));
		}
	}

	private static void formatExperimentalData (Set<ExperimentalData> experimentalData, List<TopLevelDocument<QName>> topLevelDoc)
	{
		for(ExperimentalData exptData : experimentalData)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();
			formatCommonTopLevelData(list, exptData);
			topLevelDoc.add(TopLevelDocument(Sbol2Terms.ExperimentalData.ExperimentalData, exptData.getIdentity(), NamedProperties(list)));
		}
	}

	private static void formatCommonIdentifiedData (List<NamedProperty<QName>> list, Identified t)
	{
		if(t.isSetPersistentIdentity())
			list.add(NamedProperty(Sbol2Terms.Identified.persistentIdentity, t.getPersistentIdentity()));
		if(t.isSetDisplayId())
			list.add(NamedProperty(Sbol2Terms.Identified.displayId, t.getDisplayId()));
		if(t.isSetVersion())
			list.add(NamedProperty(Sbol2Terms.Identified.version, t.getVersion()));
		for (URI wasDerivedFrom : t.getWasDerivedFroms()) {
			list.add(NamedProperty(Sbol2Terms.Identified.wasDerivedFrom, wasDerivedFrom));
		}
		for (URI wasGeneratedBy : t.getWasGeneratedBys()) {
			list.add(NamedProperty(Sbol2Terms.Identified.wasGeneratedBy, wasGeneratedBy));
		}
		if(t.isSetName())
			list.add(NamedProperty(Sbol2Terms.Identified.title, t.getName()));
		if(t.isSetDescription())
			list.add(NamedProperty(Sbol2Terms.Identified.description, t.getDescription()));
		for(Annotation annotation : t.getAnnotations())
		{
			if (!annotation.getValue().getName().getPrefix().equals("sbol"))
				list.add(annotation.getValue());
		}
	}

	private static void formatCommonMeasuredData (List<NamedProperty<QName>> comlist, Measured m)
	{
		formatCommonIdentifiedData(comlist,m);
		for(Measure measure : m.getMeasures()) {
			List<NamedProperty<QName>> list = new ArrayList<>();
			formatCommonIdentifiedData(list, measure);
			list.add(NamedProperty(Sbol2Terms.Measure.hasNumericalValue, measure.getNumericalValue()));
			list.add(NamedProperty(Sbol2Terms.Measure.hasUnit, measure.getUnitURI()));
			for(URI t : measure.getTypes())
				list.add(NamedProperty(Sbol2Terms.Measure.type, t));
			comlist.add(NamedProperty(Sbol2Terms.Measured.hasMeasure, 
					NestedDocument(Sbol2Terms.Measure.Measure, measure.getIdentity(), NamedProperties(list))));
		}
	}

	private static void formatCommonTopLevelData (List<NamedProperty<QName>> list, TopLevel t)
	{
		formatCommonIdentifiedData(list,t);
		for(URI attachment : t.getAttachmentURIs()) {
			list.add(NamedProperty(Sbol2Terms.TopLevel.hasAttachment, attachment));
		}
	}
	
	private static void formatWasInformedByProperties(Set<URI> wasInformedBys, List<NamedProperty<QName>> list)
	{
		for(URI wib : wasInformedBys)
		{
			list.add(NamedProperty(Sbol2Terms.Activity.wasInformedBy, wib));
		}
	}

	private static void formatActivities (Set<Activity> activities, List<TopLevelDocument<QName>> topLevelDoc)
	{

		for(Activity activity : activities)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();

			formatCommonTopLevelData(list,activity);
			for(URI types : activity.getTypes())
			{
				list.add(NamedProperty(Sbol2Terms.Activity.type, types));
			}
			if (activity.isSetStartedAtTime()) {
				list.add(NamedProperty(Sbol2Terms.Activity.startedAtTime, activity.getStartedAtTime().toString()));
			}
			if (activity.isSetEndedAtTime()) {
				list.add(NamedProperty(Sbol2Terms.Activity.endedAtTime, activity.getEndedAtTime().toString()));
			}
			formatAssociations(activity.getAssociations(),list);
			formatUsages(activity.getUsages(),list);
			formatWasInformedByProperties(activity.getWasInformedByURIs(),list);
			topLevelDoc.add(TopLevelDocument(Sbol2Terms.Activity.Activity, activity.getIdentity(), NamedProperties(list)));
		}
	}
	
	private static void formatAssociations(Set<Association> associations, List<NamedProperty<QName>> properties)
	{
		for(Association association : associations)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();
			formatCommonIdentifiedData(list, association);
			for (URI role : association.getRoles())
			{
				list.add(NamedProperty(Sbol2Terms.Association.role, role));
			}
			list.add(NamedProperty(Sbol2Terms.Association.agent, association.getAgentURI()));
			if (association.isSetPlan()) {
				list.add(NamedProperty(Sbol2Terms.Association.plan, association.getPlanURI()));
			}
			properties.add(NamedProperty(Sbol2Terms.Activity.qualifiedAssociation,
					NestedDocument( Sbol2Terms.Association.Association,
							association.getIdentity(), NamedProperties(list))));
		}
	}
	
	private static void formatUsages(Set<Usage> usages, List<NamedProperty<QName>> properties)
	{
		for(Usage usage : usages)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();
			formatCommonIdentifiedData(list, usage);
			for (URI role : usage.getRoles())
			{
				list.add(NamedProperty(Sbol2Terms.Usage.role, role));
			}
			list.add(NamedProperty(Sbol2Terms.Usage.entity, usage.getEntityURI()));
			properties.add(NamedProperty(Sbol2Terms.Activity.qualifiedUsage,
					NestedDocument( Sbol2Terms.Usage.Usage,
							usage.getIdentity(), NamedProperties(list))));
		}
	}
	
	private static void formatPlans (Set<Plan> plans, List<TopLevelDocument<QName>> topLevelDoc)
	{

		for(Plan plan : plans)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();

			formatCommonTopLevelData(list,plan);
			topLevelDoc.add(TopLevelDocument(Sbol2Terms.Plan.Plan, plan.getIdentity(), NamedProperties(list)));
		}
	}
	
	private static void formatAgents (Set<Agent> agents, List<TopLevelDocument<QName>> topLevelDoc)
	{

		for(Agent agent : agents)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();

			formatCommonTopLevelData(list,agent);
			topLevelDoc.add(TopLevelDocument(Sbol2Terms.Agent.Agent, agent.getIdentity(), NamedProperties(list)));
		}
	}
	
	private static void formatComponentDefinitions (Set<ComponentDefinition> componentDefinitions, List<TopLevelDocument<QName>> topLevelDoc)
	{

		for(ComponentDefinition c : componentDefinitions)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();

			formatCommonTopLevelData(list,c);
			for(URI types : c.getTypes())
			{
				list.add(NamedProperty(Sbol2Terms.ComponentDefinition.type, types));
			}
			for (URI roles : c.getRoles())
			{
				list.add(NamedProperty(Sbol2Terms.ComponentDefinition.roles, roles));
			}
			formatComponents(c.getComponents(),list);
			formatSequenceAnnotations(c.getSequenceAnnotations(),list);
			formatSequenceConstraints(c.getSequenceConstraints(),list);
			for(URI sUri: c.getSequenceURIs())
				formatSequence(sUri, list);

			topLevelDoc.add(TopLevelDocument(Sbol2Terms.ComponentDefinition.ComponentDefinition, c.getIdentity(), NamedProperties(list)));
		}
	}
	
	private static void formatCombinatorialDerivation(Set<CombinatorialDerivation> combinatorialDerivations, List<TopLevelDocument<QName>> topLevelDoc) {
		for(CombinatorialDerivation combinatorialDerivation : combinatorialDerivations) {
			List<NamedProperty<QName>> list = new ArrayList<>();
			
			formatCommonTopLevelData(list, combinatorialDerivation);
						
			list.add(NamedProperty(Sbol2Terms.CombinatorialDerivation.template, combinatorialDerivation.getTemplateURI()));
			if (combinatorialDerivation.isSetStrategy()) {
				list.add(NamedProperty(Sbol2Terms.CombinatorialDerivation.strategy, StrategyType.convertToURI(combinatorialDerivation.getStrategy())));
			}
			formatVariableComponents(combinatorialDerivation.getVariableComponents(), list);
			
			topLevelDoc.add(TopLevelDocument(Sbol2Terms.CombinatorialDerivation.CombinatorialDerivation, 
					combinatorialDerivation.getIdentity(), NamedProperties(list)));
		}
	}
	
	private static void formatImplementation(Set<Implementation> implementations, List<TopLevelDocument<QName>> topLevelDoc) {
		for(Implementation implementation : implementations) {
			List<NamedProperty<QName>> list = new ArrayList<>();
			
			formatCommonTopLevelData(list, implementation);
						
			if (implementation.isSetBuilt()) {
				list.add(NamedProperty(Sbol2Terms.Implementation.built, implementation.getBuiltURI()));
			}
			
			topLevelDoc.add(TopLevelDocument(Sbol2Terms.Implementation.Implementation, 
					implementation.getIdentity(), NamedProperties(list)));
		}
	}

	/**
	 * formatFunctionalComponents for Module
	 * @param functionalInstantiation
	 * @param properties
	 */
	private static void formatFunctionalComponents(Set<FunctionalComponent> functionalInstantiation,
			List<NamedProperty<QName>> properties)
	{
		for(FunctionalComponent f : functionalInstantiation)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();

			formatCommonMeasuredData(list, f);

			list.add(NamedProperty(Sbol2Terms.ComponentInstance.hasComponentDefinition, f.getDefinitionURI()));
			list.add(NamedProperty(Sbol2Terms.ComponentInstance.access, AccessType.convertToURI(f.getAccess())));
			list.add(NamedProperty(Sbol2Terms.FunctionalComponent.direction, DirectionType.convertToURI(f.getDirection())));
			List<NestedDocument<QName>> referenceList = getMapsTo(f.getMapsTos());
			for(NestedDocument<QName> n : referenceList)
			{
				list.add(NamedProperty(Sbol2Terms.ComponentInstance.hasMapsTo, n));
			}

			properties.add(NamedProperty(Sbol2Terms.ModuleDefinition.hasfunctionalComponent,
					NestedDocument( Sbol2Terms.FunctionalComponent.FunctionalComponent,
							f.getIdentity(), NamedProperties(list))));
		}
	}

	/**
	 * formatInteractions for Module
	 * @param interactions
	 * @param properties
	 */
	private static void formatInteractions (Set<Interaction> interactions,
			List<NamedProperty<QName>> properties)
	{
		for(Interaction i : interactions)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();
			formatCommonMeasuredData(list, i);
			for(URI type : i.getTypes())
			{
				list.add(NamedProperty(Sbol2Terms.Interaction.type, type));
			}
			List<NestedDocument<QName>> participantList = formatParticipations(i.getParticipations());
			for(NestedDocument<QName> n : participantList)
			{
				list.add(NamedProperty(Sbol2Terms.Interaction.hasParticipations, n));
			}

			properties.add(NamedProperty(Sbol2Terms.ModuleDefinition.hasInteractions,
					NestedDocument( Sbol2Terms.Interaction.Interaction,
							i.getIdentity(), NamedProperties(list))));
		}
	}

	private static void formatModels (Set<Model> models, List<TopLevelDocument<QName>> topLevelDoc)
	{
		for(Model m : models)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();
			formatCommonTopLevelData(list,m);
			list.add(NamedProperty(Sbol2Terms.Model.source, m.getSource()));
			list.add(NamedProperty(Sbol2Terms.Model.language, m.getLanguage()));
			list.add(NamedProperty(Sbol2Terms.Model.framework, m.getFramework()));
			topLevelDoc.add(TopLevelDocument(Sbol2Terms.Model.Model, m.getIdentity(), NamedProperties(list)));
		}
	}

	private static void formatAttachments (Set<Attachment> attachments, List<TopLevelDocument<QName>> topLevelDoc)
	{
		for(Attachment attachment : attachments)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();
			formatCommonTopLevelData(list,attachment);
			list.add(NamedProperty(Sbol2Terms.Attachment.source, attachment.getSource()));
			if (attachment.isSetFormat()) {
				list.add(NamedProperty(Sbol2Terms.Attachment.format, attachment.getFormat()));
			}
			if (attachment.isSetSize()) {
				list.add(NamedProperty(Sbol2Terms.Attachment.size, String.valueOf(attachment.getSize())));
			}
			if (attachment.isSetHash()) {
				list.add(NamedProperty(Sbol2Terms.Attachment.hash, attachment.getHash()));
			}
			topLevelDoc.add(TopLevelDocument(Sbol2Terms.Attachment.Attachment, attachment.getIdentity(), NamedProperties(list)));
		}
	}

	private static void formatModelProperties(Set<URI> models, List<NamedProperty<QName>> list)
	{
		for(URI m : models)
		{
			list.add(NamedProperty(Sbol2Terms.ModuleDefinition.hasModels, m));
		}
	}

	/**
	 * getModule for Module
	 * @param module
	 * @param properties
	 */
	private static void formatModule (Set<Module> module,
			List<NamedProperty<QName>> properties)
	{
		for(Module m : module)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();
			formatCommonMeasuredData(list, m);
			list.add(NamedProperty(Sbol2Terms.Module.hasDefinition, m.getDefinitionURI()));
			List<NestedDocument<QName>> referenceList = getMapsTo(m.getMapsTos());
			for(NestedDocument<QName> n : referenceList)
			{
				list.add(NamedProperty(Sbol2Terms.Module.hasMapsTo, n));
			}
			properties.add(NamedProperty(Sbol2Terms.ModuleDefinition.hasModule,
					NestedDocument( Sbol2Terms.Module.Module,
							m.getIdentity(), NamedProperties(list))));
		}
	}

	private static void formatModuleDefinitions(Set<ModuleDefinition> module, List<TopLevelDocument<QName>> topLevelDoc)
	{
		for (ModuleDefinition m : module)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();
			formatCommonTopLevelData(list,m);
			for (URI role : m.getRoles())
			{
				list.add(NamedProperty(Sbol2Terms.ModuleDefinition.roles, role));
			}
			formatFunctionalComponents(m.getFunctionalComponents(),list);
			formatInteractions(m.getInteractions(),list);
			formatModelProperties(m.getModelURIs(),list);
			formatModule(m.getModules(),list);
			topLevelDoc.add(TopLevelDocument(Sbol2Terms.ModuleDefinition.ModuleDefinition, m.getIdentity(), NamedProperties(list)));
		}
	}

	private static List<NestedDocument<QName>> formatParticipations(Set<Participation> participations)
	{
		List<NestedDocument<QName>> nestedDoc = new ArrayList<>();
		for(Participation p : participations)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();
			formatCommonMeasuredData(list, p);
			for(URI r : p.getRoles())
				list.add(NamedProperty(Sbol2Terms.Participation.role, r));
			list.add(NamedProperty(Sbol2Terms.Participation.hasParticipant, p.getParticipantURI()));
			nestedDoc.add(NestedDocument(Sbol2Terms.Participation.Participation, p.getIdentity(), NamedProperties(list)));
		}
		return nestedDoc;
	}

	private static void formatSequence(URI sequence, List<NamedProperty<QName>> list)
	{
		list.add(NamedProperty(Sbol2Terms.ComponentDefinition.hasSequence, sequence));
	}


	private static void formatSequenceAnnotations(Set<SequenceAnnotation> sequenceAnnotations,
			List<NamedProperty<QName>> properties)
	{
		for(SequenceAnnotation s : sequenceAnnotations)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();
			formatCommonIdentifiedData(list, s);
			for (URI roles : s.getRoles())
			{
				list.add(NamedProperty(Sbol2Terms.SequenceAnnotation.roles, roles));
			}
//			if (s.isSetRoleIntegration()) {
//				list.add(NamedProperty(Sbol2Terms.Component.roleIntegration, RoleIntegrationType.convertToURI(s.getRoleIntegration())));
//			}
			for (Location location : s.getLocations()) {
				list.add(getLocation(location));
			}
			if(s.isSetComponent())
				list.add(NamedProperty(Sbol2Terms.SequenceAnnotation.hasComponent, s.getComponentURI()));
			properties.add(NamedProperty(Sbol2Terms.ComponentDefinition.hasSequenceAnnotations,
					NestedDocument( Sbol2Terms.SequenceAnnotation.SequenceAnnotation,
							s.getIdentity(), NamedProperties(list))));
		}

	}

	private static void formatSequenceConstraints(Set<SequenceConstraint> sequenceConstraint,
			List<NamedProperty<QName>> properties)
	{
		for(SequenceConstraint s : sequenceConstraint)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();
			formatCommonIdentifiedData(list, s);
			list.add(NamedProperty(Sbol2Terms.SequenceConstraint.restriction, s.getRestrictionURI()));
			list.add(NamedProperty(Sbol2Terms.SequenceConstraint.hasSubject, s.getSubjectURI()));
			list.add(NamedProperty(Sbol2Terms.SequenceConstraint.hasObject, s.getObjectURI()));
			properties.add(NamedProperty(Sbol2Terms.ComponentDefinition.hasSequenceConstraints,
					NestedDocument( Sbol2Terms.SequenceConstraint.SequenceConstraint,
							s.getIdentity(), NamedProperties(list))));
		}

	}

	private static void formatSequences (Set<Sequence> sequences, List<TopLevelDocument<QName>> topLevelDoc)
	{
		for(Sequence s : sequences)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();
			formatCommonTopLevelData(list, s);
			list.add(NamedProperty(Sbol2Terms.Sequence.elements, s.getElements()));
			list.add(NamedProperty(Sbol2Terms.Sequence.encoding, s.getEncoding()));
			topLevelDoc.add(TopLevelDocument(Sbol2Terms.Sequence.Sequence, s.getIdentity(), NamedProperties(list)));
		}
	}

	private static void formatComponents(Set<Component> components,
			List<NamedProperty<QName>> properties)
	{
		for(Component s : components)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();
			formatCommonMeasuredData(list, s);
			for (URI roles : s.getRoles())
			{ 
				list.add(NamedProperty(Sbol2Terms.Component.roles, roles));
			}
			if (s.isSetRoleIntegration()) {
				list.add(NamedProperty(Sbol2Terms.Component.roleIntegration, RoleIntegrationType.convertToURI(s.getRoleIntegration())));
			}
			list.add(NamedProperty(Sbol2Terms.ComponentInstance.access, AccessType.convertToURI(s.getAccess())));
			list.add(NamedProperty(Sbol2Terms.ComponentInstance.hasComponentDefinition, s.getDefinitionURI()));
			for (Location location : s.getLocations()) {
				list.add(getLocation(location));
			}
			for (Location location : s.getSourceLocations()) {
				list.add(getSourceLocation(location));
			}
			List<NestedDocument<QName>> referenceList = getMapsTo(s.getMapsTos());
			for(NestedDocument<QName> n : referenceList)
			{
				list.add(NamedProperty(Sbol2Terms.ComponentInstance.hasMapsTo, n));
			}
			properties.add(NamedProperty(Sbol2Terms.ComponentDefinition.hasComponent,
					NestedDocument( Sbol2Terms.Component.Component,
							s.getIdentity(), NamedProperties(list))));
		}
	}
	
	private static void formatVariableComponents(Set<VariableComponent> variableComponents,
			List<NamedProperty<QName>> properties)
	{
		for(VariableComponent variableComponent : variableComponents)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();
			formatCommonIdentifiedData(list, variableComponent);

			list.add(NamedProperty(Sbol2Terms.VariableComponent.hasVariable, variableComponent.getVariableURI()));
			list.add(NamedProperty(Sbol2Terms.VariableComponent.hasOperator, OperatorType.convertToURI(variableComponent.getOperator())));
			
			for(URI variant : variableComponent.getVariantURIs()) {
				list.add(NamedProperty(Sbol2Terms.VariableComponent.hasVariants, variant));
			}
			
			for(URI variantCollection : variableComponent.getVariantCollectionURIs()) {
				list.add(NamedProperty(Sbol2Terms.VariableComponent.hasVariantCollections, variantCollection));
			}
			
			for(URI variantDerivation : variableComponent.getVariantDerivationURIs()) {
				list.add(NamedProperty(Sbol2Terms.VariableComponent.hasVariantDerivations, variantDerivation));
			}
			
			properties.add(NamedProperty(Sbol2Terms.CombinatorialDerivation.hasVariableComponent,
					NestedDocument(Sbol2Terms.VariableComponent.VariableComponent,
							variableComponent.getIdentity(), NamedProperties(list))));
		}
	}

	private static void formatGenericTopLevel(Set<GenericTopLevel> topLevels, List<TopLevelDocument<QName>> topLevelDoc)
	{
		for(GenericTopLevel t : topLevels)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();
			formatCommonTopLevelData(list, t);
			topLevelDoc.add(TopLevelDocument(t.getRDFType(), t.getIdentity(), NamedProperties(list)));
		}
	}

	private static NamedProperty<QName> getLocation(Location location)
	{
		List<NamedProperty<QName>> property = new ArrayList<>();
		formatCommonIdentifiedData(property, location);

		if(location instanceof Range)
		{
			Range range = (Range) location;
			property.add(NamedProperty(Sbol2Terms.Range.start, range.getStart()));
			property.add(NamedProperty(Sbol2Terms.Range.end, range.getEnd()));
			if(range.isSetOrientation())
				property.add(NamedProperty(Sbol2Terms.Range.orientation, OrientationType.convertToURI(range.getOrientation())));
			if(range.isSetSequence())
				property.add(NamedProperty(Sbol2Terms.Location.sequence, range.getSequenceURI()));
			return NamedProperty(Sbol2Terms.Location.Location,
					NestedDocument(Sbol2Terms.Range.Range, range.getIdentity(), NamedProperties(property)));
		}
		else if(location instanceof Cut)
		{
			Cut cut = (Cut) location;
			property.add(NamedProperty(Sbol2Terms.Cut.at, cut.getAt()));
			if (cut.isSetOrientation())
				property.add(NamedProperty(Sbol2Terms.Cut.orientation, OrientationType.convertToURI(cut.getOrientation())));
			if(cut.isSetSequence())
				property.add(NamedProperty(Sbol2Terms.Location.sequence, cut.getSequenceURI()));
			return NamedProperty(Sbol2Terms.Location.Location,
					NestedDocument(Sbol2Terms.Cut.Cut, cut.getIdentity(), NamedProperties(property)));
		}
		else 
		{
			GenericLocation genericLocation = (GenericLocation) location;
			if (genericLocation.isSetOrientation())
				property.add(NamedProperty(Sbol2Terms.GenericLocation.orientation, OrientationType.convertToURI(genericLocation.getOrientation())));
			if(genericLocation.isSetSequence())
				property.add(NamedProperty(Sbol2Terms.Location.sequence, genericLocation.getSequenceURI()));
			return NamedProperty(Sbol2Terms.Location.Location,
					NestedDocument(Sbol2Terms.GenericLocation.GenericLocation, genericLocation.getIdentity(), NamedProperties(property)));
		}
	}
	
	private static NamedProperty<QName> getSourceLocation(Location location)
	{
		List<NamedProperty<QName>> property = new ArrayList<>();
		formatCommonIdentifiedData(property, location);

		if(location instanceof Range)
		{
			Range range = (Range) location;
			property.add(NamedProperty(Sbol2Terms.Range.start, range.getStart()));
			property.add(NamedProperty(Sbol2Terms.Range.end, range.getEnd()));
			if(range.isSetOrientation())
				property.add(NamedProperty(Sbol2Terms.Range.orientation, OrientationType.convertToURI(range.getOrientation())));
			if(range.isSetSequence())
				property.add(NamedProperty(Sbol2Terms.Location.sequence, range.getSequenceURI()));
			return NamedProperty(Sbol2Terms.Component.sourceLocation,
					NestedDocument(Sbol2Terms.Range.Range, range.getIdentity(), NamedProperties(property)));
		}
		else if(location instanceof Cut)
		{
			Cut cut = (Cut) location;
			property.add(NamedProperty(Sbol2Terms.Cut.at, cut.getAt()));
			if (cut.isSetOrientation())
				property.add(NamedProperty(Sbol2Terms.Cut.orientation, OrientationType.convertToURI(cut.getOrientation())));
			if(cut.isSetSequence())
				property.add(NamedProperty(Sbol2Terms.Location.sequence, cut.getSequenceURI()));
			return NamedProperty(Sbol2Terms.Component.sourceLocation,
					NestedDocument(Sbol2Terms.Cut.Cut, cut.getIdentity(), NamedProperties(property)));
		}
		else 
		{
			GenericLocation genericLocation = (GenericLocation) location;
			if (genericLocation.isSetOrientation())
				property.add(NamedProperty(Sbol2Terms.GenericLocation.orientation, OrientationType.convertToURI(genericLocation.getOrientation())));
			if(genericLocation.isSetSequence())
				property.add(NamedProperty(Sbol2Terms.Location.sequence, genericLocation.getSequenceURI()));
			return NamedProperty(Sbol2Terms.Component.sourceLocation,
					NestedDocument(Sbol2Terms.GenericLocation.GenericLocation, genericLocation.getIdentity(), NamedProperties(property)));
		}
	}

	private static List<NestedDocument<QName>> getMapsTo(Set<MapsTo> references)
	{
		List<NestedDocument<QName>> nestedDoc = new ArrayList<>();
		for(MapsTo m : references)
		{
			List<NamedProperty<QName>> list = new ArrayList<>();
			formatCommonIdentifiedData(list, m);
			list.add(NamedProperty(Sbol2Terms.MapsTo.refinement, RefinementType.convertToURI(m.getRefinement())));
			list.add(NamedProperty(Sbol2Terms.MapsTo.hasRemote, m.getRemoteURI()));
			list.add(NamedProperty(Sbol2Terms.MapsTo.hasLocal, m.getLocalURI()));
			nestedDoc.add(NestedDocument(Sbol2Terms.MapsTo.MapsTo, m.getIdentity(), NamedProperties(list)));
		}
		return nestedDoc;
	}
	
	private static NestedDocument<QName> getSequenceV1(Sequence sequence)
	{
		List<NamedProperty<QName>> list = new ArrayList<>();
		list.add(NamedProperty(Sbol1Terms.DNASequence.nucleotides, sequence.getElements()));
		return NestedDocument(Sbol1Terms.DNASequence.DNASequence, sequence.getIdentity(), NamedProperties(list));
	}
	
	private static NestedDocument<QName> getSequenceAnnotationV1(SequenceAnnotation sequenceAnnotation, 
			ComponentDefinition componentDefinition) throws SBOLConversionException
	{
		List<NamedProperty<QName>> list = new ArrayList<>();
		for (SequenceConstraint sequenceConstraint : componentDefinition.getSequenceConstraints()) {
			if (sequenceConstraint.getRestriction().equals(RestrictionType.PRECEDES)) {
				if (sequenceConstraint.getSubjectURI().equals(sequenceAnnotation.getComponentURI())) {
					for (SequenceAnnotation annotation : componentDefinition.getSequenceAnnotations()) {
						if (sequenceConstraint.getObjectURI().equals(annotation.getComponentURI())) {
							list.add(NamedProperty(Sbol1Terms.SequenceAnnotations.precedes,annotation.getIdentity()));
						}
					}
				}
			}
		}
		if (sequenceAnnotation.getLocations().size()!=1) {
			if (keepGoing) {
				errors.add("SBOL 1.1 only allows a single location.\n:"+sequenceAnnotation.getIdentity());
			} else {
				throw new SBOLConversionException("SBOL 1.1 only allows a single location.\n:"+sequenceAnnotation.getIdentity());
			}
		}
		for (Location location : sequenceAnnotation.getLocations()) {
			if (location instanceof Range) {
				Range range = (Range)location;
				list.add(NamedProperty(Sbol1Terms.SequenceAnnotations.bioStart, range.getStart()));
				list.add(NamedProperty(Sbol1Terms.SequenceAnnotations.bioEnd, range.getEnd()));
				if (range.isSetOrientation()) {
					if (range.getOrientation()==OrientationType.INLINE) {
						list.add(NamedProperty(Sbol1Terms.SequenceAnnotations.strand, "+"));
					} else if (range.getOrientation()==OrientationType.REVERSECOMPLEMENT) {
						list.add(NamedProperty(Sbol1Terms.SequenceAnnotations.strand, "-"));
					} 
				} 
			} else if (location instanceof GenericLocation) {
				GenericLocation genericLocation = (GenericLocation)location;
				if (genericLocation.isSetOrientation()) {
					if (genericLocation.getOrientation()==OrientationType.INLINE) {
						list.add(NamedProperty(Sbol1Terms.SequenceAnnotations.strand, "+"));
					} else if (genericLocation.getOrientation()==OrientationType.REVERSECOMPLEMENT) {
						list.add(NamedProperty(Sbol1Terms.SequenceAnnotations.strand, "-"));
					} 
				} 
			} else {
				if (keepGoing) {
					errors.add("SBOL 1.1 only supports Ranges and GenericLocations.\n:"+sequenceAnnotation.getIdentity());
				} else {
					throw new SBOLConversionException("SBOL 1.1 only supports Ranges and GenericLocations."+sequenceAnnotation.getIdentity());
				}				
			}
		}
		if (sequenceAnnotation.isSetComponent()) {
			list.add(NamedProperty(Sbol1Terms.SequenceAnnotations.subComponent, 
					getSubComponent(sequenceAnnotation.getComponent().getDefinition())));
		}
		return NestedDocument(Sbol1Terms.SequenceAnnotations.SequenceAnnotation, 
				sequenceAnnotation.getIdentity(), NamedProperties(list));
	}
	
	
	private static NestedDocument<QName> getComponentV1(Component component, 
			ComponentDefinition componentDefinition) throws SBOLConversionException
	{
		List<NamedProperty<QName>> list = new ArrayList<>();
		for (SequenceConstraint sequenceConstraint : componentDefinition.getSequenceConstraints()) {
			if (sequenceConstraint.getRestriction().equals(RestrictionType.PRECEDES)) {
				if (sequenceConstraint.getSubjectURI().equals(component.getIdentity())) {
					SequenceAnnotation annotation = componentDefinition.getSequenceAnnotation(sequenceConstraint.getObject());
					if (annotation!=null) {
						list.add(NamedProperty(Sbol1Terms.SequenceAnnotations.precedes,annotation.getIdentity()));
					} else {
						list.add(NamedProperty(Sbol1Terms.SequenceAnnotations.precedes,sequenceConstraint.getObjectURI()));
					}
				}
			}
		}
		list.add(NamedProperty(Sbol1Terms.SequenceAnnotations.subComponent, 
				getSubComponent(component.getDefinition())));
		return NestedDocument(Sbol1Terms.SequenceAnnotations.SequenceAnnotation, 
				component.getIdentity(), NamedProperties(list));
	}
	
	private static NestedDocument<QName> getSubComponent(ComponentDefinition componentDefinition) throws SBOLConversionException {
		List<NamedProperty<QName>> list = new ArrayList<>();
		if (componentDefinition==null) {
			throw new SBOLConversionException("ComponentDefinition not found.\n:");
		}
		if (!componentDefinition.getTypes().contains(ComponentDefinition.DNA_REGION)) {
			throw new SBOLConversionException("SBOL 1.1 only supports DNA ComponentDefinitions.\n:"+componentDefinition.getIdentity());
		}
		if(componentDefinition.isSetDisplayId())
			list.add(NamedProperty(Sbol1Terms.DNAComponent.displayId, componentDefinition.getDisplayId()));
		if(componentDefinition.isSetName())
			list.add(NamedProperty(Sbol1Terms.DNAComponent.name, componentDefinition.getName()));
		if(componentDefinition.isSetDescription())
			list.add(NamedProperty(Sbol1Terms.DNAComponent.description, componentDefinition.getDescription()));
		for(Annotation annotation : componentDefinition.getAnnotations())
		{
			if (!annotation.getValue().getName().getPrefix().equals("sbol"))
				list.add(annotation.getValue());
		}
		for (URI role : componentDefinition.getRoles())
		{
			URI purlRole = URI.create(role.toString().replace("http://identifiers.org/so/SO:", "http://purl.obolibrary.org/obo/SO_"));
			list.add(NamedProperty(Sbol1Terms.DNAComponent.type, purlRole));
		}
		Sequence sequence = componentDefinition.getSequenceByEncoding(Sequence.IUPAC_DNA);
		if ((sequence==null && componentDefinition.getSequences().size()>0) || 
				(componentDefinition.getSequences().size()>1)) {
			if (keepGoing) {
				errors.add("SBOL 1.1 only supports a single IUPAC_DNA Sequence.\n:"+componentDefinition.getIdentity());
			} else {
				throw new SBOLConversionException("SBOL 1.1 only supports a single IUPAC_DNA Sequence.\n:"+componentDefinition.getIdentity());
			}
		}
		if (sequence!=null) {
			list.add(NamedProperty(Sbol1Terms.DNAComponent.dnaSequence, getSequenceV1(sequence)));
		}
		for (Component component : componentDefinition.getComponents()) {
			SequenceAnnotation sequenceAnnotation = componentDefinition.getSequenceAnnotation(component);
			if (sequenceAnnotation!=null) {
				list.add(NamedProperty(Sbol1Terms.DNAComponent.annotations, getSequenceAnnotationV1(sequenceAnnotation,componentDefinition)));
			} else {
				list.add(NamedProperty(Sbol1Terms.DNAComponent.annotations, getComponentV1(component,componentDefinition)));
			}
		}
		for (SequenceAnnotation sequenceAnnotation : componentDefinition.getSequenceAnnotations()) {
			if (!sequenceAnnotation.isSetComponent()) {
				if (keepGoing) {
					errors.add("Dropping SequenceAnnotation without a Component.\n:"+sequenceAnnotation.getIdentity());
				} else {
					throw new SBOLConversionException("Dropping SequenceAnnotation without a Component.\n:"+sequenceAnnotation.getIdentity());
				}				
			}
		}

		return NestedDocument(Sbol1Terms.DNAComponent.DNAComponent, 
				componentDefinition.getIdentity(), NamedProperties(list));		
	}
	
	private static void formatDNAComponent(ComponentDefinition componentDefinition, List<TopLevelDocument<QName>> topLevelDoc) throws SBOLConversionException {
		List<NamedProperty<QName>> list = new ArrayList<>();

		if(componentDefinition.isSetDisplayId())
			list.add(NamedProperty(Sbol1Terms.DNAComponent.displayId, componentDefinition.getDisplayId()));
		if(componentDefinition.isSetName())
			list.add(NamedProperty(Sbol1Terms.DNAComponent.name, componentDefinition.getName()));
		if(componentDefinition.isSetDescription())
			list.add(NamedProperty(Sbol1Terms.DNAComponent.description, componentDefinition.getDescription()));
		for(Annotation annotation : componentDefinition.getAnnotations())
		{
			if (!annotation.getValue().getName().getPrefix().equals("sbol"))
				list.add(annotation.getValue());
		}
		for (URI role : componentDefinition.getRoles())
		{
			URI purlRole = URI.create(role.toString().replace("http://identifiers.org/so/SO:", "http://purl.obolibrary.org/obo/SO_"));
			list.add(NamedProperty(Sbol1Terms.DNAComponent.type, purlRole));
		}
		Sequence sequence = componentDefinition.getSequenceByEncoding(Sequence.IUPAC_DNA);
		if ((sequence==null && componentDefinition.getSequences().size()>0) || 
				(componentDefinition.getSequences().size()>1)) {
			if (keepGoing) {
				errors.add("SBOL 1.1 only supports a single IUPAC_DNA Sequence.\n:"+componentDefinition.getIdentity());
			} else {
				throw new SBOLConversionException("SBOL 1.1 only supports a single IUPAC_DNA Sequence.\n:"+componentDefinition.getIdentity());
			}
		}
		if (sequence!=null) {
			list.add(NamedProperty(Sbol1Terms.DNAComponent.dnaSequence, getSequenceV1(sequence)));
		}
		for (Component component : componentDefinition.getComponents()) {
			SequenceAnnotation sequenceAnnotation = componentDefinition.getSequenceAnnotation(component);
			if (sequenceAnnotation!=null) {
				list.add(NamedProperty(Sbol1Terms.DNAComponent.annotations, getSequenceAnnotationV1(sequenceAnnotation,componentDefinition)));
			} else {
				list.add(NamedProperty(Sbol1Terms.DNAComponent.annotations, getComponentV1(component,componentDefinition)));
			}
		}
		for (SequenceAnnotation sequenceAnnotation : componentDefinition.getSequenceAnnotations()) {
			if (!sequenceAnnotation.isSetComponent()) {
				if (keepGoing) {
					errors.add("Dropping SequenceAnnotation without a Component.\n:"+sequenceAnnotation.getIdentity());
				} else {
					throw new SBOLConversionException("Dropping SequenceAnnotation without a Component.\n:"+sequenceAnnotation.getIdentity());
				}				
			}
		}

		topLevelDoc.add(TopLevelDocument(Sbol1Terms.DNAComponent.DNAComponent, 
				componentDefinition.getIdentity(), NamedProperties(list)));
	}
	
	
	private static void formatCollectionV1(Collection collection, List<TopLevelDocument<QName>> topLevelDoc) throws SBOLConversionException {
		List<NamedProperty<QName>> list = new ArrayList<>();

		if(collection.isSetDisplayId())
			list.add(NamedProperty(Sbol1Terms.Collection.displayId, collection.getDisplayId()));
		if(collection.isSetName())
			list.add(NamedProperty(Sbol1Terms.Collection.name, collection.getName()));
		if(collection.isSetDescription())
			list.add(NamedProperty(Sbol1Terms.Collection.description, collection.getDescription()));
		for(Annotation annotation : collection.getAnnotations())
		{
			if (!annotation.getValue().getName().getPrefix().equals("sbol"))
				list.add(annotation.getValue());
		}
		for (TopLevel topLevel : collection.getMembers()) {
			if (topLevel instanceof ComponentDefinition) {
				ComponentDefinition componentDefinition = (ComponentDefinition) topLevel;
				list.add(NamedProperty(Sbol1Terms.Collection.component, getSubComponent(componentDefinition)));
			} else {
				if (keepGoing) {
					errors.add("SBOL 1.1 only supports Collections of DNA ComponentDefinitions.\n:"+topLevel.getIdentity());
				} else {
					throw new SBOLConversionException("SBOL 1.1 only supports Collections of DNA ComponentDefinitions.\n:"+topLevel.getIdentity());
				}	
			}
		}

		topLevelDoc.add(TopLevelDocument(Sbol1Terms.Collection.Collection, 
				collection.getIdentity(), NamedProperties(list)));
	}
	
	private static void formatDNASequence(Sequence sequence, List<TopLevelDocument<QName>> topLevelDoc)
	{
		List<NamedProperty<QName>> list = new ArrayList<>();
		list.add(NamedProperty(Sbol1Terms.DNASequence.nucleotides, sequence.getElements()));
		topLevelDoc.add(TopLevelDocument(Sbol1Terms.DNASequence.DNASequence, sequence.getIdentity(), NamedProperties(list)));
	}
	
	private static List<NamespaceBinding> getNamespaceBindingsV1() {
		List<NamespaceBinding> bindings = new ArrayList<>();
		bindings.add(NamespaceBinding("http://sbols.org/v1#",""));
		bindings.add(NamespaceBinding("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf"));
		return bindings;
	}

	private static List<TopLevelDocument<QName>> convertToV1Document(SBOLDocument doc) throws SBOLConversionException {
		List<TopLevelDocument<QName>> topLevelDoc = new ArrayList<>();
		if (doc.getModuleDefinitions().size()>0) {
			if (keepGoing) {
				errors.add("SBOL 1.1 does not support ModuleDefinitions.");
			} else {
				throw new SBOLConversionException("SBOL 1.1 does not support ModuleDefinitions.\n");
			}	
		}
		if (doc.getModels().size()>0) {
			if (keepGoing) {
				errors.add("SBOL 1.1 does not support Models.");
			} else {
				throw new SBOLConversionException("SBOL 1.1 does not support Models.\n");
			}	
		}
		if (doc.getGenericTopLevels().size()>0) {
			if (keepGoing) {
				errors.add("SBOL 1.1 does not support GenericTopLevels.");
			} else {
				throw new SBOLConversionException("SBOL 1.1 does not support GenericTopLevels.\n");
			}	
		}
		for (Collection collection : doc.getCollections()) {
			formatCollectionV1(collection, topLevelDoc);
		}
		for (ComponentDefinition componentDefinition : doc.getRootComponentDefinitions()) {
			if (componentDefinition.getTypes().contains(ComponentDefinition.DNA_REGION)) {
				boolean skip = false;
				for (Collection collection : doc.getCollections()) {
					if (collection.getMemberURIs().contains(componentDefinition.getIdentity())) {
						skip = true;
						break;
					}
				}
				if (!skip) {
					formatDNAComponent(componentDefinition, topLevelDoc);
				}
			} else {
				if (keepGoing) {
					errors.add("SBOL 1.1 only supports DNA ComponentDefinitions.\n:"+componentDefinition.getIdentity());
				} else {
					throw new SBOLConversionException("SBOL 1.1 only supports DNA ComponentDefinitions.\n:"+componentDefinition.getIdentity());
				}	
			}
		}
		for (Sequence sequence : doc.getSequences()) {
			boolean skip = false;
			for (ComponentDefinition componentDefinition : doc.getComponentDefinitions()) {
				if (componentDefinition.getSequenceURIs().contains(sequence.getIdentity())) {
					skip = true;
					break;
				}
			}
			if (!skip) {
				formatDNASequence(sequence, topLevelDoc);
			}
		}
		return topLevelDoc;
	}

	private static List<TopLevelDocument<QName>> getTopLevelDocument(SBOLDocument doc) {
		List<TopLevelDocument<QName>> topLevelDoc = new ArrayList<>();
		formatCollections(doc.getCollections(), topLevelDoc);
		formatModuleDefinitions(doc.getModuleDefinitions(), topLevelDoc);
		formatModels(doc.getModels(), topLevelDoc);
		formatComponentDefinitions(doc.getComponentDefinitions(), topLevelDoc);
		formatSequences(doc.getSequences(), topLevelDoc);
		formatActivities(doc.getActivities(), topLevelDoc);
		formatAgents(doc.getAgents(), topLevelDoc);
		formatPlans(doc.getPlans(), topLevelDoc);
		formatGenericTopLevel(doc.getGenericTopLevels(), topLevelDoc);
		formatCombinatorialDerivation(doc.getCombinatorialDerivations(), topLevelDoc);
		formatImplementation(doc.getImplementations(), topLevelDoc);
		formatAttachments(doc.getAttachments(), topLevelDoc);
		formatExperiments(doc.getExperiments(), topLevelDoc);
		formatExperimentalData(doc.getExperimentalData(), topLevelDoc);
		return topLevelDoc;
	}

}

