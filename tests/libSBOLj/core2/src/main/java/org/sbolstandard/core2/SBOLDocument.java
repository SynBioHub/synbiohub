package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;
import static org.sbolstandard.core2.URIcompliance.extractPersistentId;
import static org.sbolstandard.core2.URIcompliance.extractURIprefix;
import static org.sbolstandard.core2.URIcompliance.extractVersion;
import static org.sbolstandard.core2.URIcompliance.isURIprefixCompliant;
import static org.sbolstandard.core2.URIcompliance.keyExistsInAnyMap;
import static org.sbolstandard.core2.Version.isFirstVersionNewer;
import static org.sbolstandard.core.datatree.Datatree.NamespaceBinding;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.synbiohub.frontend.SynBioHubException;
import org.synbiohub.frontend.SynBioHubFrontend;

import org.sbolstandard.core.datatree.NamespaceBinding;

/**
 * Represents the SBOL document where all top-level instances can be created and
 * manipulated.
 * 
 * @author Zhen Zhang
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */

public class SBOLDocument {

	private HashMap<URI, GenericTopLevel> genericTopLevels;
	private HashMap<URI, Collection> collections;
	private HashMap<URI, ComponentDefinition> componentDefinitions;
	private HashMap<URI, Experiment> experiments;
	private HashMap<URI, ExperimentalData> experimentalData;
	private HashMap<URI, Model> models;
	private HashMap<URI, ModuleDefinition> moduleDefinitions;
	private HashMap<URI, Sequence> sequences;
	private HashMap<URI, CombinatorialDerivation> combinatorialDerivations;
	private HashMap<URI, Implementation> implementations;
	private HashMap<URI, Attachment> attachments;
	private HashMap<URI, Activity> activities;
	private HashMap<URI, Plan> plans;
	private HashMap<URI, Agent> agents;
	private HashMap<String, NamespaceBinding> nameSpaces;
	private HashMap<String, SynBioHubFrontend> registries;
	private Set<String> prefixes;
	private String defaultURIprefix;
	private boolean complete = false;
	private boolean compliant = true;
	private boolean typesInURIs = false;
	private boolean createDefaults = false;

	/**
	 * Constant representing TURTLE file format
	 */
	public static final String TURTLE = "TURTLE";
	/**
	 * Constant representing JSON file format
	 */
	public static final String JSON = "JSON";
	/**
	 * Constant representing the format of an SBOL version 1.1 output file as being
	 * RDF format
	 */
	public static final String RDFV1 = "RDFV1";
	/**
	 * Constant representing RDF file format
	 */
	public static final String RDF = "RDF";
	/**
	 * Constant representing FASTA file format
	 */
	public static final String FASTAformat = "FASTA";
	/**
	 * Constant representing GFF3 file format
	 */
	public static final String GFF3format = "GFF3";
	/**
	 * Constant representing GenBank file format
	 */
	public static final String GENBANK = "GENBANK";
	/**
	 * Constant representing SnapGene file format
	 */
	public static final String SNAPGENE = "SNAPGENE";

	/**
	 * Creates a new SBOLDocument instance with one empty list for the namespaces
	 * and one for each top-level instance, and then adds the following namespaces:
	 * {@link Sbol2Terms#sbol2}, {@link Sbol1Terms#rdf}, {@link Sbol2Terms#dc}, and
	 * {@link Sbol2Terms#prov}.
	 */
	public SBOLDocument() {
		genericTopLevels = new HashMap<>();
		collections = new HashMap<>();
		componentDefinitions = new HashMap<>();
		experiments = new HashMap<>();
		experimentalData = new HashMap<>();
		models = new HashMap<>();
		moduleDefinitions = new HashMap<>();
		sequences = new HashMap<>();
		activities = new HashMap<>();
		plans = new HashMap<>();
		agents = new HashMap<>();
		nameSpaces = new HashMap<>();
		combinatorialDerivations = new HashMap<>();
		implementations = new HashMap<>();
		attachments = new HashMap<>();
		try {
			addNamespaceBinding(Sbol2Terms.sbol2);
			addNamespaceBinding(Sbol1Terms.rdf);
			addNamespaceBinding(Sbol2Terms.dc);
			addNamespaceBinding(Sbol2Terms.prov);
			addNamespaceBinding(Sbol2Terms.om);
		} catch (SBOLValidationException e) {
			e.printStackTrace();
		}
		prefixes = new HashSet<>();
		registries = new HashMap<>();
	}

	/**
	 * Creates a module definition, and then adds it to this SBOL document's list of
	 * module definitions.
	 * <p>
	 * This method calls {@link #createModuleDefinition(String, String, String)}
	 * with the default URI prefix of this SBOL document, the given module
	 * definition display ID, and an empty version string.
	 * 
	 * @param displayId
	 *            the display ID of the module definition to be created
	 * @return the created module definition
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule was violated in
	 *             {@link #createModuleDefinition(String, String, String)} .
	 */
	public ModuleDefinition createModuleDefinition(String displayId) throws SBOLValidationException {
		return createModuleDefinition(defaultURIprefix, displayId, "");
	}

	/**
	 * Creates a module definition, and then adds it to this SBOL document's list of
	 * module definitions.
	 * <p>
	 * This method calls {@link #createModuleDefinition(String, String, String)}
	 * with the default URI prefix of this SBOL document, the given module
	 * definition display ID and version.
	 * 
	 * @param displayId
	 *            the display ID of the module definition to be created
	 * @param version
	 *            the version of the module definition to be created
	 * @return the created module definition
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule was violated in
	 *             {@link #createModuleDefinition(String, String, String)} .
	 */
	public ModuleDefinition createModuleDefinition(String displayId, String version) throws SBOLValidationException {
		return createModuleDefinition(defaultURIprefix, displayId, version);
	}

	/**
	 * Creates a module definition with the given arguments, and then adds it to
	 * this SBOL document's list of module definitions.
	 * <p>
	 * This method first creates a compliant URI for the module definition to be
	 * retrieved. It starts with the given URI prefix after its been successfully
	 * validated, optionally followed by its type, namely
	 * {@link TopLevel#MODULE_DEFINITION}, followed by the given display ID, and
	 * ends with the given version.
	 * 
	 * @param URIprefix
	 *            the URI prefix used to construct the compliant URI for the module
	 *            definition to be created
	 * @param displayId
	 *            the display ID of the module definition to be created
	 * @param version
	 *            the version of the module definition to be created
	 * @return the created module definition
	 * @throws SBOLValidationException
	 *             if any of the following SBOL validation rules was violated:
	 *             10201, 10202, 10204, 10206, 10220.
	 */
	public ModuleDefinition createModuleDefinition(String URIprefix, String displayId, String version)
			throws SBOLValidationException {
		URIprefix = URIcompliance.checkURIprefix(URIprefix);
		ModuleDefinition md = new ModuleDefinition(
				createCompliantURI(URIprefix, TopLevel.MODULE_DEFINITION, displayId, version, typesInURIs));
		md.setPersistentIdentity(createCompliantURI(URIprefix, TopLevel.MODULE_DEFINITION, displayId, "", typesInURIs));
		md.setDisplayId(displayId);
		md.setVersion(version);
		addModuleDefinition(md);
		return md;
	}

	// /**
	// * @param identity a given identifier for this object
	// * @return the new module definition
	// * @throws SBOLValidationException if any of the following SBOL validation
	// rules was violated: 10201, 10202, 10220.
	// */
	// private ModuleDefinition createModuleDefinition(URI identity) throws
	// SBOLValidationException {
	// ModuleDefinition newModule = new ModuleDefinition(identity);
	// addModuleDefinition(newModule);
	// return newModule;
	// }

	/**
	 * Appends the specified {@code moduleDefinition} object to the end of the list
	 * of module definitions.
	 *
	 * @param moduleDefinition
	 *            the ModuleDefinition to be added
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #addTopLevel(TopLevel, Map, String, Map...)}.
	 */
	void addModuleDefinition(ModuleDefinition moduleDefinition) throws SBOLValidationException {
		addTopLevel(moduleDefinition, moduleDefinitions, "moduleDefinition", collections, componentDefinitions, experiments, experimentalData,
				genericTopLevels, activities, plans, agents, models, sequences, combinatorialDerivations, implementations, attachments);
		for (FunctionalComponent functionalComponent : moduleDefinition.getFunctionalComponents()) {
			functionalComponent.setSBOLDocument(this);
			for (MapsTo mapsTo : functionalComponent.getMapsTos()) {
				mapsTo.setSBOLDocument(this);
			}
		}
		for (Module module : moduleDefinition.getModules()) {
			module.setSBOLDocument(this);
			module.setModuleDefinition(moduleDefinition);
			for (MapsTo mapsTo : module.getMapsTos()) {
				mapsTo.setSBOLDocument(this);
			}
		}
		for (Interaction interaction : moduleDefinition.getInteractions()) {
			interaction.setSBOLDocument(this);
			interaction.setModuleDefinition(moduleDefinition);
			for (Participation participation : interaction.getParticipations()) {
				participation.setSBOLDocument(this);
				participation.setModuleDefinition(moduleDefinition);
			}
		}
	}

	/**
	 * Removes the given module definition from this SBOL document's list of module
	 * definitions.
	 *
	 * @param moduleDefinition
	 *            The moduleDefinition to be removed
	 * @return {@code true} if the given {@code moduleDefinition} is successfully
	 *         removed, {@code false} otherwise.
	 * @throws SBOLValidationException
	 *             if either of the following SBOL validation rules was violated:
	 *             11703, 12103.
	 */
	public boolean removeModuleDefinition(ModuleDefinition moduleDefinition) throws SBOLValidationException {
		if (complete) {
			for (ModuleDefinition md : moduleDefinitions.values()) {
				for (Module m : md.getModules()) {
					if (m.getDefinitionURI().equals(moduleDefinition.getIdentity())) {
						throw new SBOLValidationException("sbol-11703");
					}
				}
			}
		}
		return removeTopLevel(moduleDefinition, moduleDefinitions);
	}

	/**
	 * Returns the module definition matching the given display ID and version from
	 * this SBOLDocument object's list of module definitions.
	 * <p>
	 * This method first creates a compliant URI for the module definition to be
	 * retrieved. It starts with the given URI prefix after its been successfully
	 * validated, optionally followed by its type, namely
	 * {@link TopLevel#MODULE_DEFINITION}, followed by the given display ID, and
	 * ends with the given version. This URI is used to look up the module
	 * definition in this SBOL document.
	 * 
	 * @param displayId
	 *            the display ID of the module definition to be retrieved
	 * @param version
	 *            the version of the module definition to be retrieved
	 * @return the matching module definition if present, or {@code null} otherwise
	 */
	public ModuleDefinition getModuleDefinition(String displayId, String version) {
		try {
			return getModuleDefinition(
					createCompliantURI(defaultURIprefix, TopLevel.MODULE_DEFINITION, displayId, version, typesInURIs));
		} catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the module definition matching the given identity URI from this SBOL
	 * document object's list of module definitions.
	 *
	 * @param moduleDefinitionURI
	 *            the give identity URI of the module definition to be retrieved
	 * @return the matching module definition if present, or {@code null} otherwise
	 */
	public ModuleDefinition getModuleDefinition(URI moduleDefinitionURI) {
		ModuleDefinition moduleDefinition = moduleDefinitions.get(moduleDefinitionURI);
		if (moduleDefinition == null) {
			for (SynBioHubFrontend frontend : getRegistries()) {
				try {
					SBOLDocument document = frontend.getSBOL(moduleDefinitionURI);
					if (document != null) {
						moduleDefinition = document.getModuleDefinition(moduleDefinitionURI);
						createCopy(document);
					}
				} catch (SynBioHubException | SBOLValidationException e) {
				}
			}
		}
		return moduleDefinition;
	}

	/**
	 * Returns the set of module definitions owned by this SBOLDocument object.
	 *
	 * @return the set of module definitions owned by this SBOLDocument object.
	 */
	public Set<ModuleDefinition> getModuleDefinitions() {
		Set<ModuleDefinition> moduleDefinitions = new HashSet<>();
		moduleDefinitions.addAll(this.moduleDefinitions.values());
		return moduleDefinitions;
	}

	/**
	 * Removes all entries in the list of module definitions owned by this SBOL
	 * document. The list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeModuleDefinition(ModuleDefinition)} to
	 * iteratively remove each module definition.
	 * 
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #removeModuleDefinition(ModuleDefinition)}.
	 */
	public void clearModuleDefinitions() throws SBOLValidationException {
		Object[] valueSetArray = moduleDefinitions.values().toArray();
		for (Object moduleDefinition : valueSetArray) {
			removeModuleDefinition((ModuleDefinition) moduleDefinition);
		}
	}

	/**
	 * Creates a collection first, and then adds to this SBOL document's list of
	 * collections.
	 * <p>
	 * This method calls {@link #createCollection(String, String, String)} with the
	 * default URI prefix for this SOBL document, the given display ID of the
	 * collection to be created, and an empty version string.
	 *
	 * @param displayId
	 *            the display ID of the collection to be created
	 * @return the created collection
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createCollection(String, String, String)}.
	 */
	public Collection createCollection(String displayId) throws SBOLValidationException {
		return createCollection(defaultURIprefix, displayId, "");
	}

	/**
	 * Creates a collection first, and then adds to this SBOL document's list of
	 * collections.
	 * <p>
	 * This method calls {@link #createCollection(String, String, String)} with the
	 * default URI prefix for this SOBL document, the given display ID and version
	 * of the collection to be created.
	 *
	 * @param displayId
	 *            the display ID of the collection to be created
	 * @param version
	 *            the version of the collection to be created
	 * @return the created collection
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createCollection(String, String, String)}.
	 */
	public Collection createCollection(String displayId, String version) throws SBOLValidationException {
		return createCollection(defaultURIprefix, displayId, version);
	}

	/**
	 * Creates a collection first, and then adds to this SBOL document's list of
	 * collections.
	 * <p>
	 * This method creates a compliant URI for the collection to be created first.
	 * It starts with the given URI prefix after its been successfully validated,
	 * followed by the given display ID, and ends with the given version.
	 * 
	 * @param URIprefix
	 *            the URI prefix for the collection to be created
	 * @param displayId
	 *            the display ID of the collection to be created
	 * @param version
	 *            the version of the collection to be created
	 * @return the created collection
	 * @throws SBOLValidationException
	 *             if any of the following SBOL validation rules was violated:
	 *             10201, 10204, 10206.
	 */
	public Collection createCollection(String URIprefix, String displayId, String version)
			throws SBOLValidationException {
		URIprefix = URIcompliance.checkURIprefix(URIprefix);
		Collection c = new Collection(
				createCompliantURI(URIprefix, TopLevel.COLLECTION, displayId, version, typesInURIs));
		c.setDisplayId(displayId);
		c.setPersistentIdentity(createCompliantURI(URIprefix, TopLevel.COLLECTION, displayId, "", typesInURIs));
		c.setVersion(version);
		addCollection(c);
		return c;
	}

	/**
	 * Adds the given collection to this SBOL document's list of collections.
	 *
	 * @param collection
	 *            the collection object to be added
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #addTopLevel(TopLevel, Map, String, Map...)}
	 */
	void addCollection(Collection collection) throws SBOLValidationException {
		addTopLevel(collection, collections, "collection", componentDefinitions, genericTopLevels, activities, plans,
				agents, models, moduleDefinitions, sequences, combinatorialDerivations, implementations, attachments);
	}

	/**
	 * Removes the given collection from this SBOL document's list of collections.
	 *
	 * @param collection
	 *            the given collection to be removed
	 * @return {@code true} if the given collection was successfully removed,
	 *         {@code false} otherwise
	 * @throws SBOLValidationException
	 *             if the following SBOL validation rule was violated: 12103.
	 */
	public boolean removeCollection(Collection collection) throws SBOLValidationException {
		return removeTopLevel(collection, collections);
	}

	/**
	 * Returns the collection matching the given display ID and version from this
	 * SBOL document's list of collections.
	 * <p>
	 * A compliant Collection URI is created first. It starts with the given URI
	 * prefix after its been successfully validated, optionally followed by its
	 * type, namely {@link TopLevel#COLLECTION}, followed by the given display ID,
	 * and ends with the given version. This URI is used to look up the module
	 * definition in this SBOL document.
	 *
	 * @param displayId
	 *            the display ID of the collection to be retrieved
	 * @param version
	 *            the version of the collection to be retrieved
	 * @return the matching collection if present, or {@code null} otherwise
	 */
	public Collection getCollection(String displayId, String version) {
		try {
			return getCollection(
					createCompliantURI(defaultURIprefix, TopLevel.COLLECTION, displayId, version, typesInURIs));
		} catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the collection matching the given identity URI from this SBOL
	 * document's list of collections.
	 *
	 * @param collectionURI
	 *            the given identity URI of the collection to be retrieved
	 * @return the matching collection if present, or {@code null} otherwise
	 *
	 */
	public Collection getCollection(URI collectionURI) {
		Collection collection = collections.get(collectionURI);
		if (collection == null) {
			for (SynBioHubFrontend frontend : getRegistries()) {
				try {
					SBOLDocument document = frontend.getSBOL(collectionURI);
					if (document != null) {
						collection = document.getCollection(collectionURI);
						createCopy(document);
					}
				} catch (SynBioHubException | SBOLValidationException e) {
					collection = null;
				}
			}
		}
		return collection;
	}

	/**
	 * Returns the set of {@code Collection} instances owned by this SBOL document.
	 *
	 * @return the set of {@code Collection} instances owned by this SBOL document.
	 */
	public Set<Collection> getCollections() {
		Set<Collection> collections = new HashSet<>();
		collections.addAll(this.collections.values());
		return collections;
	}

	/**
	 * Removes all entries in the list of collections owned by this SBOL document.
	 * The list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeCollection(Collection)} to iteratively remove
	 * each collection.
	 * 
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #removeCollection(Collection)}.
	 */
	public void clearCollections() throws SBOLValidationException {
		Object[] valueSetArray = collections.values().toArray();
		for (Object collection : valueSetArray) {
			removeCollection((Collection) collection);
		}
	}
	
	/**
	 * Creates a experiment first, and then adds to this SBOL document's list of
	 * experiments.
	 * <p>
	 * This method calls {@link #createExperiment(String, String, String)} with the
	 * default URI prefix for this SOBL document, the given display ID of the
	 * experiment to be created, and an empty version string.
	 *
	 * @param displayId
	 *            the display ID of the experiment to be created
	 * @return the created experiment
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createExperiment(String, String, String)}.
	 */
	public Experiment createExperiment(String displayId) throws SBOLValidationException {
		return createExperiment(defaultURIprefix, displayId, "");
	}

	/**
	 * Creates a experiment first, and then adds to this SBOL document's list of
	 * experiments.
	 * <p>
	 * This method calls {@link #createExperiment(String, String, String)} with the
	 * default URI prefix for this SOBL document, the given display ID and version
	 * of the experiment to be created.
	 *
	 * @param displayId
	 *            the display ID of the experiment to be created
	 * @param version
	 *            the version of the experiment to be created
	 * @return the created experiment
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createExperiment(String, String, String)}.
	 */
	public Experiment createExperiment(String displayId, String version) throws SBOLValidationException {
		return createExperiment(defaultURIprefix, displayId, version);
	}

	/**
	 * Creates a experiment first, and then adds to this SBOL document's list of
	 * experiments.
	 * <p>
	 * This method creates a compliant URI for the experiment to be created first.
	 * It starts with the given URI prefix after its been successfully validated,
	 * followed by the given display ID, and ends with the given version.
	 * 
	 * @param URIprefix
	 *            the URI prefix for the experiment to be created
	 * @param displayId
	 *            the display ID of the experiment to be created
	 * @param version
	 *            the version of the experiment to be created
	 * @return the created experiment
	 * @throws SBOLValidationException
	 *             if any of the following SBOL validation rules was violated:
	 *             10201, 10204, 10206.
	 */
	public Experiment createExperiment(String URIprefix, String displayId, String version)
			throws SBOLValidationException {
		URIprefix = URIcompliance.checkURIprefix(URIprefix);
		Experiment c = new Experiment(
				createCompliantURI(URIprefix, TopLevel.EXPERIMENT, displayId, version, typesInURIs));
		c.setDisplayId(displayId);
		c.setPersistentIdentity(createCompliantURI(URIprefix, TopLevel.EXPERIMENT, displayId, "", typesInURIs));
		c.setVersion(version);
		addExperiment(c);
		return c;
	}

	/**
	 * Adds the given experiment to this SBOL document's list of experiments.
	 *
	 * @param experiment
	 *            the experiment object to be added
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #addTopLevel(TopLevel, Map, String, Map...)}
	 */
	void addExperiment(Experiment experiment) throws SBOLValidationException {
		addTopLevel(experiment, experiments, "experiment", collections, experimentalData, componentDefinitions, genericTopLevels, activities, plans,
				agents, models, moduleDefinitions, sequences, combinatorialDerivations, implementations, attachments);
	}

	/**
	 * Removes the given experiment from this SBOL document's list of experiments.
	 *
	 * @param experiment
	 *            the given experiment to be removed
	 * @return {@code true} if the given experiment was successfully removed,
	 *         {@code false} otherwise
	 * @throws SBOLValidationException
	 *             if the following SBOL validation rule was violated: 12103.
	 */
	public boolean removeExperiment(Experiment experiment) throws SBOLValidationException {
		return removeTopLevel(experiment, experiments);
	}

	/**
	 * Returns the experiment matching the given display ID and version from this
	 * SBOL document's list of experiments.
	 * <p>
	 * A compliant Experiment URI is created first. It starts with the given URI
	 * prefix after its been successfully validated, optionally followed by its
	 * type, namely {@link TopLevel#EXPERIMENT}, followed by the given display ID,
	 * and ends with the given version. This URI is used to look up the module
	 * definition in this SBOL document.
	 *
	 * @param displayId
	 *            the display ID of the experiment to be retrieved
	 * @param version
	 *            the version of the experiment to be retrieved
	 * @return the matching experiment if present, or {@code null} otherwise
	 */
	public Experiment getExperiment(String displayId, String version) {
		try {
			return getExperiment(
					createCompliantURI(defaultURIprefix, TopLevel.EXPERIMENT, displayId, version, typesInURIs));
		} catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the experiment matching the given identity URI from this SBOL
	 * document's list of experiments.
	 *
	 * @param experimentURI
	 *            the given identity URI of the experiment to be retrieved
	 * @return the matching experiment if present, or {@code null} otherwise
	 *
	 */
	public Experiment getExperiment(URI experimentURI) {
		Experiment experiment = experiments.get(experimentURI);
		if (experiment == null) {
			for (SynBioHubFrontend frontend : getRegistries()) {
				try {
					SBOLDocument document = frontend.getSBOL(experimentURI);
					if (document != null) {
						experiment = document.getExperiment(experimentURI);
						createCopy(document);
					}
				} catch (SynBioHubException | SBOLValidationException e) {
					experiment = null;
				}
			}
		}
		return experiment;
	}

	/**
	 * Returns the set of {@code Experiment} instances owned by this SBOL document.
	 *
	 * @return the set of {@code Experiment} instances owned by this SBOL document.
	 */
	public Set<Experiment> getExperiments() {
		Set<Experiment> experiments = new HashSet<>();
		experiments.addAll(this.experiments.values());
		return experiments;
	}

	/**
	 * Removes all entries in the list of experiments owned by this SBOL document.
	 * The list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeExperiment(Experiment)} to iteratively remove
	 * each experiment.
	 * 
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #removeExperiment(Experiment)}.
	 */
	public void clearExperiments() throws SBOLValidationException {
		Object[] valueSetArray = experiments.values().toArray();
		for (Object experiment : valueSetArray) {
			removeExperiment((Experiment) experiment);
		}
	}
	
	/**
	 * Creates a experimentalData first, and then adds to this SBOL document's list of
	 * experimentalDatas.
	 * <p>
	 * This method calls {@link #createExperimentalData(String, String, String)} with the
	 * default URI prefix for this SOBL document, the given display ID of the
	 * experimentalData to be created, and an empty version string.
	 *
	 * @param displayId
	 *            the display ID of the experimentalData to be created
	 * @return the created experimentalData
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createExperimentalData(String, String, String)}.
	 */
	public ExperimentalData createExperimentalData(String displayId) throws SBOLValidationException {
		return createExperimentalData(defaultURIprefix, displayId, "");
	}

	/**
	 * Creates a experimentalData first, and then adds to this SBOL document's list of
	 * experimentalDatas.
	 * <p>
	 * This method calls {@link #createExperimentalData(String, String, String)} with the
	 * default URI prefix for this SOBL document, the given display ID and version
	 * of the experimentalData to be created.
	 *
	 * @param displayId
	 *            the display ID of the experimentalData to be created
	 * @param version
	 *            the version of the experimentalData to be created
	 * @return the created experimentalData
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createExperimentalData(String, String, String)}.
	 */
	public ExperimentalData createExperimentalData(String displayId, String version) throws SBOLValidationException {
		return createExperimentalData(defaultURIprefix, displayId, version);
	}

	/**
	 * Creates a experimentalData first, and then adds to this SBOL document's list of
	 * experimentalDatas.
	 * <p>
	 * This method creates a compliant URI for the experimentalData to be created first.
	 * It starts with the given URI prefix after its been successfully validated,
	 * followed by the given display ID, and ends with the given version.
	 * 
	 * @param URIprefix
	 *            the URI prefix for the experimentalData to be created
	 * @param displayId
	 *            the display ID of the experimentalData to be created
	 * @param version
	 *            the version of the experimentalData to be created
	 * @return the created experimentalData
	 * @throws SBOLValidationException
	 *             if any of the following SBOL validation rules was violated:
	 *             10201, 10204, 10206.
	 */
	public ExperimentalData createExperimentalData(String URIprefix, String displayId, String version)
			throws SBOLValidationException {
		URIprefix = URIcompliance.checkURIprefix(URIprefix);
		ExperimentalData c = new ExperimentalData(
				createCompliantURI(URIprefix, TopLevel.EXPERIMENTAL_DATA, displayId, version, typesInURIs));
		c.setDisplayId(displayId);
		c.setPersistentIdentity(createCompliantURI(URIprefix, TopLevel.EXPERIMENTAL_DATA, displayId, "", typesInURIs));
		c.setVersion(version);
		addExperimentalData(c);
		return c;
	}

	/**
	 * Adds the given experimentalData to this SBOL document's list of experimentalData.
	 *
	 * @param experimentalDatum
	 *            the experimentalData object to be added
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #addTopLevel(TopLevel, Map, String, Map...)}
	 */
	void addExperimentalData(ExperimentalData experimentalDatum) throws SBOLValidationException {
		addTopLevel(experimentalDatum, experimentalData, "experimentalData", collections, experiments, componentDefinitions, genericTopLevels, activities, plans,
				agents, models, moduleDefinitions, sequences, combinatorialDerivations, implementations, attachments);
	}

	/**
	 * Removes the given experimentalData from this SBOL document's list of experimentalData.
	 *
	 * @param experimentalDatum
	 *            the given experimentalData to be removed
	 * @return {@code true} if the given experimentalData was successfully removed,
	 *         {@code false} otherwise
	 * @throws SBOLValidationException
	 *             if the following SBOL validation rule was violated: 12103.
	 */
	public boolean removeExperimentalData(ExperimentalData experimentalDatum) throws SBOLValidationException {
		return removeTopLevel(experimentalDatum, experimentalData);
	}

	/**
	 * Returns the experimentalData matching the given display ID and version from this
	 * SBOL document's list of experimentalData.
	 * <p>
	 * A compliant ExperimentalData URI is created first. It starts with the given URI
	 * prefix after its been successfully validated, optionally followed by its
	 * type, namely {@link TopLevel#EXPERIMENTAL_DATA}, followed by the given display ID,
	 * and ends with the given version. This URI is used to look up the module
	 * definition in this SBOL document.
	 *
	 * @param displayId
	 *            the display ID of the experimentalData to be retrieved
	 * @param version
	 *            the version of the experimentalData to be retrieved
	 * @return the matching experimentalData if present, or {@code null} otherwise
	 */
	public ExperimentalData getExperimentalData(String displayId, String version) {
		try {
			return getExperimentalData(
					createCompliantURI(defaultURIprefix, TopLevel.EXPERIMENTAL_DATA, displayId, version, typesInURIs));
		} catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the experimentalData matching the given identity URI from this SBOL
	 * document's list of experimentalDatas.
	 *
	 * @param experimentalDataURI
	 *            the given identity URI of the experimentalData to be retrieved
	 * @return the matching experimentalData if present, or {@code null} otherwise
	 *
	 */
	public ExperimentalData getExperimentalData(URI experimentalDataURI) {
		ExperimentalData experimentalDatum = experimentalData.get(experimentalDataURI);
		if (experimentalDatum == null) {
			for (SynBioHubFrontend frontend : getRegistries()) {
				try {
					SBOLDocument document = frontend.getSBOL(experimentalDataURI);
					if (document != null) {
						experimentalDatum = document.getExperimentalData(experimentalDataURI);
						createCopy(document);
					}
				} catch (SynBioHubException | SBOLValidationException e) {
					experimentalDatum = null;
				}
			}
		}
		return experimentalDatum;
	}

	/**
	 * Returns the set of {@code ExperimentalData} instances owned by this SBOL document.
	 *
	 * @return the set of {@code ExperimentalData} instances owned by this SBOL document.
	 */
	public Set<ExperimentalData> getExperimentalData() {
		Set<ExperimentalData> experimentalDatas = new HashSet<>();
		experimentalDatas.addAll(this.experimentalData.values());
		return experimentalDatas;
	}

	/**
	 * Removes all entries in the list of experimentalData owned by this SBOL document.
	 * The list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeExperimentalData(ExperimentalData)} to iteratively remove
	 * each experimentalData.
	 * 
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #removeExperimentalData(ExperimentalData)}.
	 */
	public void clearExperimentalData() throws SBOLValidationException {
		Object[] valueSetArray = experimentalData.values().toArray();
		for (Object experimentalData : valueSetArray) {
			removeExperimentalData((ExperimentalData) experimentalData);
		}
	}

	/**
	 * Creates a model, and then adds it to this SBOL document's list of models.
	 * <p>
	 * This method calls {@link #createModel(String, String, String, URI, URI, URI)}
	 * with this SBOL document's default URI prefix, an empty version string, and
	 * all given arguments.
	 * 
	 * @param displayId
	 *            the display ID of the model to be created
	 * @param source
	 *            the source of the model to be created
	 * @param language
	 *            the language of the model to be created
	 * @param framework
	 *            the framework of the model to be created
	 * @return the created model
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createModel(String, String, String, URI, URI, URI)};
	 */
	public Model createModel(String displayId, URI source, URI language, URI framework) throws SBOLValidationException {
		return createModel(defaultURIprefix, displayId, "", source, language, framework);
	}

	/**
	 * Creates a model, and then adds it to this SBOL document's list of models.
	 * <p>
	 * This method calls {@link #createModel(String, String, String, URI, URI, URI)}
	 * with this SBOL document's default URI prefix, and all given arguments.
	 * 
	 * @param displayId
	 *            the display ID of the model to be created
	 * @param version
	 *            the version of the model to be created
	 * @param source
	 *            the source of the model to be created
	 * @param language
	 *            the language of the model to be created
	 * @param framework
	 *            the framework of the model to be created
	 * @return the created model
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createModel(String, String, String, URI, URI, URI)};
	 * 
	 */
	public Model createModel(String displayId, String version, URI source, URI language, URI framework)
			throws SBOLValidationException {
		return createModel(defaultURIprefix, displayId, version, source, language, framework);
	}

	/**
	 * Creates a model, and then adds it to this SBOL document's list of models.
	 * <p>
	 * This method first creates a compliant URI for the model to be created. It
	 * starts with the given URI prefix, followed by the given display ID, and ends
	 * with the given version.
	 *
	 * @param URIprefix
	 *            the URI prefix used to construct the compliant URI for the model
	 *            to be created
	 * @param displayId
	 *            the display ID of the model to be created
	 * @param version
	 *            the version of the model to be created
	 * @param source
	 *            the source of the model to be created
	 * @param language
	 *            the language of the model to be created
	 * @param framework
	 *            the framework of the model to be created
	 * @return the created model
	 * @throws SBOLValidationException
	 *             if any of the following SBOL validation rules was violated:
	 *             10201, 10202, 10204, 10206, 10220, 10303, 10304, 10305, 10401,
	 *             10501, 10701, 10801, 10901, 11101, 11201, 11301, 11401, 11501,
	 *             11502, 11504, 11508, 11601, 11701, 11801, 11901, 12001, 12301.
	 */
	public Model createModel(String URIprefix, String displayId, String version, URI source, URI language,
			URI framework) throws SBOLValidationException {
		URIprefix = URIcompliance.checkURIprefix(URIprefix);
		Model model = new Model(createCompliantURI(URIprefix, TopLevel.MODEL, displayId, version, typesInURIs), source,
				language, framework);
		model.setPersistentIdentity(createCompliantURI(URIprefix, TopLevel.MODEL, displayId, "", typesInURIs));
		model.setDisplayId(displayId);
		model.setVersion(version);
		addModel(model);
		return model;
	}

	/**
	 *
	 * @param model
	 *            The model to be added to the document
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #addTopLevel(TopLevel, Map, String, Map...)}.
	 */
	void addModel(Model model) throws SBOLValidationException {
		addTopLevel(model, models, "model", collections, componentDefinitions, experiments, experimentalData, genericTopLevels, activities, plans,
				agents, moduleDefinitions, sequences, combinatorialDerivations, implementations, attachments);
	}

	/**
	 * Removes the given model from this SBOL document's list of models.
	 *
	 * @param model
	 *            the given model to be removed
	 * @return {@code true} if the given {@code model} was successfully removed,
	 *         {@code false} otherwise
	 * @throws SBOLValidationException
	 *             if either of the following SBOL validation rules was violated:
	 *             11608, 12103.
	 */
	public boolean removeModel(Model model) throws SBOLValidationException {
		if (complete) {
			for (ModuleDefinition md : moduleDefinitions.values()) {
				if (md.containsModel(model.getIdentity())) {
					throw new SBOLValidationException("sbol-11608", md);
				}
			}
		}
		return removeTopLevel(model, models);
	}

	/**
	 * Removes the given attachment from this SBOL document's list of attachments.
	 *
	 * @param attachment
	 *            the given attachment to be removed
	 * @return {@code true} if the given {@code attachment} was successfully removed,
	 *         {@code false} otherwise
	 * @throws SBOLValidationException
	 *             if either of the following SBOL validation rules was violated:
	 *             11608, 12103. // TODO
	 */
	public boolean removeAttachment(Attachment attachment) throws SBOLValidationException {
		if (complete) {
			for (TopLevel top : attachments.values()) {
				if (top.containsAttachment(attachment.getIdentity())) {
					throw new SBOLValidationException("sbol-XXXXX", top);
				}
			}
		}
		return removeTopLevel(attachment, attachments);
	}
	
	/**
	 * Returns the model matching the given display ID and version from this SBOL
	 * document's list of models.
	 * <p>
	 * This method first creates a compliant URI for the model to be retrieved. It
	 * starts with the given URI prefix after its been successfully validated,
	 * optionally followed by its type, namely {@link TopLevel#MODEL}, followed by
	 * the given display ID, and ends with the given version. This URI is used to
	 * look up the model in this SBOL document.
	 *
	 * @param displayId
	 *            the display ID of the model to be retrieved
	 * @param version
	 *            the version of the model to be retrieved
	 * @return the matching model if present, or {@code null} otherwise
	 */
	public Model getModel(String displayId, String version) {
		try {
			return getModel(createCompliantURI(defaultURIprefix, TopLevel.MODEL, displayId, version, typesInURIs));
		} catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the model matching the given identity URI from this SBOL document's
	 * list of models.
	 *
	 * @param modelURI
	 *            the identity URI of the model to be retrieved
	 * @return the matching model if present, or {@code null} otherwise
	 */
	public Model getModel(URI modelURI) {
		Model model = models.get(modelURI);
		if (model == null) {
			for (SynBioHubFrontend frontend : getRegistries()) {
				try {
					SBOLDocument document = frontend.getSBOL(modelURI);
					if (document != null) {
						model = document.getModel(modelURI);
						createCopy(document);
					}
				} catch (SynBioHubException | SBOLValidationException e) {
				}
			}
		}
		return model;
	}

	/**
	 * Returns the set of models owned by this SBOL document.
	 *
	 * @return the set of models owned by this SBOL document.
	 */
	public Set<Model> getModels() {
		// return (List<Model>) models.values();
		Set<Model> models = new HashSet<>();
		models.addAll(this.models.values());
		return models;
	}

	/**
	 * Removes all entries in the list of models owned by this SBOL document. The
	 * list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeModel(Model)} to iteratively remove each
	 * model.
	 * 
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #removeModel(Model)}.
	 */
	public void clearModels() throws SBOLValidationException {
		Object[] valueSetArray = models.values().toArray();
		for (Object model : valueSetArray) {
			removeModel((Model) model);
		}
	}

	/**
	 * Creates an attachment, and then adds it to this SBOL document's list of attachments.
	 * <p>
	 * This method calls {@link #createAttachment(String, String, String, URI)}
	 * with this SBOL document's default URI prefix, an empty version string, and
	 * all given arguments.
	 * 
	 * @param displayId
	 *            the display ID of the attachment to be created
	 * @param source
	 *            the source of the attachment to be created
	 * @return the created attachment
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createAttachment(String, String, String, URI)};
	 */
	public Attachment createAttachment(String displayId, URI source) throws SBOLValidationException {
		return createAttachment(defaultURIprefix, displayId, "", source);
	}

	/**
	 * Creates an attachment, and then adds it to this SBOL document's list of attachments.
	 * <p>
	 * This method calls {@link #createAttachment(String, String, String, URI)}
	 * with this SBOL document's default URI prefix, and all given arguments.
	 * 
	 * @param displayId
	 *            the display ID of the attachment to be created
	 * @param version
	 *            the version of the attachment to be created
	 * @param source
	 *            the source of the attachment to be created
	 * @return the created attachment
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createAttachment(String, String, String, URI)};
	 * 
	 */
	public Attachment createAttachment(String displayId, String version, URI source)
			throws SBOLValidationException {
		return createAttachment(defaultURIprefix, displayId, version, source);
	}

	/**
	 * Creates an attachment, and then adds it to this SBOL document's list of attachments.
	 * <p>
	 * This method first creates a compliant URI for the attachment to be created. It
	 * starts with the given URI prefix, followed by the given display ID, and ends
	 * with the given version.
	 *
	 * @param URIprefix
	 *            the URI prefix used to construct the compliant URI for the attachment
	 *            to be created
	 * @param displayId
	 *            the display ID of the attachment to be created
	 * @param version
	 *            the version of the attachment to be created
	 * @param source
	 *            the source of the attachment to be created
	 * @return the created attachment
	 * @throws SBOLValidationException
	 *             if any of the following SBOL validation rules was violated:
	 *             10201, 10202, 10204, 10206, 10220, 10303, 10304, 10305, 10401,
	 *             10501, 10701, 10801, 10901, 11101, 11201, 11301, 11401, 11501,
	 *             11502, 11504, 11508, 11601, 11701, 11801, 11901, 12001, 12301. // TODO
	 */
	public Attachment createAttachment(String URIprefix, String displayId, String version, URI source) throws SBOLValidationException {
		URIprefix = URIcompliance.checkURIprefix(URIprefix);
		Attachment attachment = new Attachment(createCompliantURI(URIprefix, TopLevel.ATTACHMENT, displayId, version, typesInURIs), source);
		attachment.setPersistentIdentity(createCompliantURI(URIprefix, TopLevel.ATTACHMENT, displayId, "", typesInURIs));
		attachment.setDisplayId(displayId);
		attachment.setVersion(version);
		addAttachment(attachment);
		return attachment;
	}

	/**
	 *
	 * @param attachment
	 *            The attachment to be added to the document
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #addTopLevel(TopLevel, Map, String, Map...)}.
	 */
	void addAttachment(Attachment attachment) throws SBOLValidationException {
		addTopLevel(attachment, attachments, "attachment", collections, componentDefinitions, experiments, experimentalData, genericTopLevels, activities, plans,
				agents, moduleDefinitions, sequences, combinatorialDerivations, implementations);
	}
	
	/**
	 * Returns the attachment matching the given display ID and version from this SBOL
	 * document's list of attachments.
	 * <p>
	 * This method first creates a compliant URI for the attachment to be retrieved. It
	 * starts with the given URI prefix after its been successfully validated,
	 * optionally followed by its type, namely {@link TopLevel#ATTACHMENT}, followed by
	 * the given display ID, and ends with the given version. This URI is used to
	 * look up the attachment in this SBOL document.
	 *
	 * @param displayId
	 *            the display ID of the attachment to be retrieved
	 * @param version
	 *            the version of the attachment to be retrieved
	 * @return the matching attachment if present, or {@code null} otherwise
	 */
	public Attachment getAttachment(String displayId, String version) {
		try {
			return getAttachment(createCompliantURI(defaultURIprefix, TopLevel.ATTACHMENT, displayId, version, typesInURIs));
		} catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the attachment matching the given identity URI from this SBOL document's
	 * list of attachments.
	 *
	 * @param attachmentURI
	 *            the identity URI of the attachment to be retrieved
	 * @return the matching attachment if present, or {@code null} otherwise
	 */
	public Attachment getAttachment(URI attachmentURI) {
		Attachment attachment = attachments.get(attachmentURI);
		if (attachment == null) {
			for (SynBioHubFrontend frontend : getRegistries()) {
				try {
					SBOLDocument document = frontend.getSBOL(attachmentURI);
					if (document != null) {
						attachment = document.getAttachment(attachmentURI);
						createCopy(document);
					}
				} catch (SynBioHubException | SBOLValidationException e) {
					attachment = null;
				}
			}
		}
		return attachment;
	}

	/**
	 * Returns the set of attachments owned by this SBOL document.
	 *
	 * @return the set of attachments owned by this SBOL document.
	 */
	public Set<Attachment> getAttachments() {
		Set<Attachment> attachments = new HashSet<>();
		attachments.addAll(this.attachments.values());
		return attachments;
	}

	/**
	 * Removes all entries in the list of attachments owned by this SBOL document. The
	 * list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeAttachment(Attachment)} to iteratively remove each
	 * attachment.
	 * 
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #removeAttachment(Attachment)}.
	 */
	public void clearAttachments() throws SBOLValidationException {
		Object[] valueSetArray = attachments.values().toArray();
		for (Object attachment : valueSetArray) {
			removeAttachment((Attachment) attachment);
		}
	}

	/**
	 * Creates a component definition, and then adds it to this SBOL document's list
	 * of component definitions.
	 * <p>
	 * This method calls
	 * {@link #createComponentDefinition(String, String, String, Set)} with the
	 * default URI prefix of this SBOL document, the given component definition
	 * display ID, and an empty version string. given types.
	 *
	 * @param displayId
	 *            the display ID of the component definition to be created
	 * @param types
	 *            the types of the component definition to be created
	 * @return the created component definition
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule was violated in
	 *             {@link #createComponentDefinition(String, String, String, Set)}.
	 */
	public ComponentDefinition createComponentDefinition(String displayId, Set<URI> types)
			throws SBOLValidationException {
		return createComponentDefinition(defaultURIprefix, displayId, "", types);
	}

	/**
	 * Creates a component definition, and then adds it to this SBOL document's list
	 * of component definitions.
	 * <p>
	 * This method first creates an empty set of types, and then adds to this set
	 * the given type. It then calls
	 * {@link #createComponentDefinition(String, String, String, Set)} with the
	 * given display ID, and version, and the created types.
	 *
	 * @param displayId
	 *            the display ID of the component definition to be created
	 * @param type
	 *            the type to be added to the component definition to be created
	 * @return the created component definition
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createComponentDefinition(String, String, String, Set)}.
	 */
	public ComponentDefinition createComponentDefinition(String displayId, URI type) throws SBOLValidationException {
		HashSet<URI> types = new HashSet<URI>();
		types.add(type);
		return createComponentDefinition(defaultURIprefix, displayId, "", types);
	}

	/**
	 * Creates a component definition, and then adds it to this SBOL document's list
	 * of component definitions.
	 * <p>
	 * This method calls
	 * {@link #createComponentDefinition(String, String, String, Set)} with the
	 * given component definition display ID, version, and the given types.
	 *
	 * @param displayId
	 *            the display ID of the component definition to be created
	 * @param version
	 *            the version of the component definition to be created
	 * @param types
	 *            the types of the component definition to be created
	 * @return the created component definition
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule was violated in
	 *             {@link #createComponentDefinition(String, String, String, Set)}.
	 */
	public ComponentDefinition createComponentDefinition(String displayId, String version, Set<URI> types)
			throws SBOLValidationException {
		return createComponentDefinition(defaultURIprefix, displayId, version, types);
	}

	/**
	 * Creates a component definition, and then adds it to this SBOL document's list
	 * of component definitions.
	 * <p>
	 * This method first creates an empty set of types, and then adds to this set
	 * the given type. It then calls
	 * {@link #createComponentDefinition(String, String, String, Set)} with the
	 * default URI prefix of this SBOL document, display ID, and version, and the
	 * created types.
	 *
	 * @param displayId
	 *            the display ID of the component definition to be created
	 * @param version
	 *            the version of the component definition to be created
	 * @param type
	 *            the type to be added to the component definition to be created
	 * @return the created component definition
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createComponentDefinition(String, String, String, Set)}.
	 */
	public ComponentDefinition createComponentDefinition(String displayId, String version, URI type)
			throws SBOLValidationException {
		HashSet<URI> types = new HashSet<URI>();
		types.add(type);
		return createComponentDefinition(defaultURIprefix, displayId, version, types);
	}

	/**
	 * Creates a component definition, and then adds it to this SBOL document's list
	 * of component definitions.
	 * <p>
	 * This method creates a compliant URI for the component definition to be
	 * created first. It starts with the given URI prefix after its been
	 * successfully validated, followed by the given display ID, and ends with the
	 * given version.
	 *
	 * @param URIprefix
	 *            the URI prefix used to construct the compliant URI for the
	 *            component definition to be created
	 * @param displayId
	 *            the display ID of the component definition to be created
	 * @param version
	 *            the version of the component definition to be created
	 * @param types
	 *            the types of the component definition to be created
	 * @return the created component definition
	 * @throws SBOLValidationException
	 *             if any of the following SBOL validation rules was violated:
	 *             10201, 10202, 10204, 10206, 10220, 10502, 10503.
	 */
	public ComponentDefinition createComponentDefinition(String URIprefix, String displayId, String version,
			Set<URI> types) throws SBOLValidationException {
		URIprefix = URIcompliance.checkURIprefix(URIprefix);
		ComponentDefinition cd = new ComponentDefinition(
				createCompliantURI(URIprefix, TopLevel.COMPONENT_DEFINITION, displayId, version, typesInURIs), types);
		cd.setDisplayId(displayId);
		cd.setPersistentIdentity(
				createCompliantURI(URIprefix, TopLevel.COMPONENT_DEFINITION, displayId, "", typesInURIs));
		cd.setVersion(version);
		addComponentDefinition(cd);
		return cd;
	}

	/**
	 * Creates a component definition, and then adds it to this SBOL document's list
	 * of component definitions.
	 * <p>
	 * This method first creates an empty set of types, and then adds to this set
	 * the given type. It then calls
	 * {@link #createComponentDefinition(String, String, String, Set)} with the
	 * given URI prefix, display ID, and version, and the created types.
	 *
	 * @param URIprefix
	 *            the URI prefix used to construct the compliant URI for the
	 *            component definition to be created
	 * @param displayId
	 *            the display ID of the component definition to be created
	 * @param version
	 *            the version of the component definition to be created
	 * @param type
	 *            the type to be added to the component definition to be created
	 * @return the created component definition
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createComponentDefinition(String, String, String, Set)}.
	 */
	public ComponentDefinition createComponentDefinition(String URIprefix, String displayId, String version, URI type)
			throws SBOLValidationException {
		HashSet<URI> types = new HashSet<URI>();
		types.add(type);
		return createComponentDefinition(URIprefix, displayId, version, types);
	}

	/**
	 * Adds the given component definition to this SBOL document's list of component
	 * definitions.
	 *
	 * @param componentDefinition
	 *            the component definition to be added
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #addTopLevel(TopLevel, Map, String, Map...)}.
	 */
	void addComponentDefinition(ComponentDefinition componentDefinition) throws SBOLValidationException {
		addTopLevel(componentDefinition, componentDefinitions, "componentDefinition", collections, experiments, experimentalData, genericTopLevels,
				activities, plans, agents, models, moduleDefinitions, sequences, combinatorialDerivations, implementations, attachments);
		for (Component component : componentDefinition.getComponents()) {
			component.setSBOLDocument(this);
			for (MapsTo mapsTo : component.getMapsTos()) {
				mapsTo.setSBOLDocument(this);
			}
		}
		for (SequenceAnnotation sa : componentDefinition.getSequenceAnnotations()) {
			sa.setSBOLDocument(this);
			sa.setComponentDefinition(componentDefinition);
			for (Location location : sa.getLocations()) {
				location.setSBOLDocument(this);
				location.setComponentDefinition(componentDefinition);
			}
		}
		for (SequenceConstraint sc : componentDefinition.getSequenceConstraints()) {
			sc.setSBOLDocument(this);
			sc.setComponentDefinition(componentDefinition);
		}
	}

	/**
	 * Removes the given component definition from this SBOL document's list of
	 * component definitions.
	 *
	 * @param componentDefinition
	 *            the component definition to be removed
	 * @return {@code true} if the given component definition was successfully
	 *         removed, {@code false} otherwise
	 * @throws SBOLValidationException
	 *             if either of the following SBOL validation rules was violated:
	 *             10604, 12103.
	 */
	public boolean removeComponentDefinition(ComponentDefinition componentDefinition) throws SBOLValidationException {
		if (complete) {
			for (ComponentDefinition cd : componentDefinitions.values()) {
				for (Component c : cd.getComponents()) {
					if (c.getDefinitionURI().equals(componentDefinition.getIdentity())) {
						throw new SBOLValidationException("sbol-10604", c);
					}
				}
			}
			for (ModuleDefinition md : moduleDefinitions.values()) {
				for (FunctionalComponent c : md.getFunctionalComponents()) {
					if (c.getDefinitionURI().equals(componentDefinition.getIdentity())) {
						throw new SBOLValidationException("sbol-10604", c);
					}
				}
			}
		}
		return removeTopLevel(componentDefinition, componentDefinitions);
	}

	/**
	 * Returns the component definition matching the given display ID and version
	 * from this SBOL document's list of component definitions.
	 * <p>
	 * A compliant ComponentDefinition URI is created first. It starts with this
	 * SBOL document's default URI prefix after its been successfully validated,
	 * optionally followed by its type, namely
	 * {@link TopLevel#COMPONENT_DEFINITION}, followed by the given display ID, and
	 * ends with the given version. This URI is used to look up the module
	 * definition in this SBOL document.
	 *
	 * @param displayId
	 *            the display ID of the component definition to be retrieved
	 * @param version
	 *            the version of the component definition to be retrieved
	 * @return the matching component definition if present, or {@code null}
	 *         otherwise
	 */
	public ComponentDefinition getComponentDefinition(String displayId, String version) {
		try {
			return getComponentDefinition(createCompliantURI(defaultURIprefix, TopLevel.COMPONENT_DEFINITION, displayId,
					version, typesInURIs));
		} catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the component definition matching the given identity URI from this
	 * SBOL document's list of component definitions.
	 *
	 * @param componentDefinitionURI
	 *            the given identity URI of the component definition to be retrieved
	 * @return the matching component definition if present, or {@code null}
	 *         otherwise.
	 */
	public ComponentDefinition getComponentDefinition(URI componentDefinitionURI) {
		ComponentDefinition componentDefinition = componentDefinitions.get(componentDefinitionURI);
		if (componentDefinition == null) {
			for (SynBioHubFrontend frontend : getRegistries()) {
				try {
					SBOLDocument document = frontend.getSBOL(componentDefinitionURI);
					if (document != null) {
						componentDefinition = document.getComponentDefinition(componentDefinitionURI);
						createCopy(document);
					}
				} catch (SynBioHubException | SBOLValidationException e) {
				}
			}
		}
		return componentDefinition;
	}

	/**
	 * Returns the combinatorial derivation matching the given display ID and
	 * version from this SBOL document's list of combinatorial derivations.
	 * <p>
	 * A compliant combinatorialDerivation URI is created first. It starts with this
	 * SBOL document's default URI prefix after its been successfully validated,
	 * optionally followed by its type, namely
	 * {@link TopLevel#COMBINATORIAL_DERIVATION}, followed by the given display ID,
	 * and ends with the given version. This URI is used to look up the module
	 * definition in this SBOL document.
	 *
	 * @param displayId
	 *            the display ID of the combinatorial derivation to be retrieved
	 * @param version
	 *            the version of the combinatorial derivation to be retrieved
	 * @return the matching combinatorial derivation if present, or {@code null}
	 *         otherwise
	 * @throws SBOLValidationException
	 *             validation error
	 */
	public CombinatorialDerivation getCombinatorialDerivation(String displayId, String version)
			throws SBOLValidationException {
		URI uri = URIcompliance.createCompliantURI(defaultURIprefix, TopLevel.COMBINATORIAL_DERIVATION, displayId,
				version, typesInURIs);

		return getCombinatorialDerivation(uri);
	}

	/**
	 * Returns the combinatorial derivation matching the given identity URI from
	 * this SBOL document's list of combinatorial derivations.
	 *
	 * @param combinatorialDerivationURI
	 *            the given identity URI of the combinatorial derivation to be
	 *            retrieved
	 * @return the matching combinatorial derivation if present, or {@code null}
	 *         otherwise.
	 */
	public CombinatorialDerivation getCombinatorialDerivation(URI combinatorialDerivationURI) {
		CombinatorialDerivation combinatorialDerivation = combinatorialDerivations.get(combinatorialDerivationURI);

		if (combinatorialDerivation == null) {
			for (SynBioHubFrontend frontend : getRegistries()) {
				try {
					SBOLDocument document = frontend.getSBOL(combinatorialDerivationURI);
					if (document != null) {
						combinatorialDerivation = document.getCombinatorialDerivation(combinatorialDerivationURI);
						createCopy(document);
					}
				} catch (SynBioHubException | SBOLValidationException e) {
				}
			}
		}

		return combinatorialDerivation;
	}

	/**
	 * Adds the given combinatorial derivation to this SBOL document's list of
	 * combinatorial derivations
	 *
	 * @param combinatorialDerivation
	 *            the combinatorial derivation to be added
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #addTopLevel(TopLevel, Map, String, Map...)}.
	 */
	void addCombinatorialDerivation(CombinatorialDerivation combinatorialDerivation) throws SBOLValidationException {
		addTopLevel(combinatorialDerivation, combinatorialDerivations, "combinatorialDerivation", collections, experiments, experimentalData, 
				genericTopLevels, activities, plans, agents, models, moduleDefinitions, sequences, implementations, attachments);
		for (VariableComponent variableComponent : combinatorialDerivation.getVariableComponents()) {
			variableComponent.setSBOLDocument(this);
		}
	}

	/**
	 * Removes the given combinatorial derivation from this SBOL document's list of
	 * combinatorial derivations.
	 *
	 * @param combinatorialDerivation
	 *            the combinatorialDerivation to be removed
	 * @return {@code true} if the given combinatorial derivation was successfully
	 *         removed, {@code false} otherwise
	 * @throws SBOLValidationException
	 *             if either of the following SBOL validation rules was violated:
	 *             TODO: 10604, 12103.
	 */
	public boolean removeCombinatorialDerivation(CombinatorialDerivation combinatorialDerivation)
			throws SBOLValidationException {
		if (complete) {
			for (CombinatorialDerivation cd : combinatorialDerivations.values()) {
				for (VariableComponent vc : cd.getVariableComponents()) {
					for (URI variantURI : vc.getVariantURIs())
						if (variantURI.equals(combinatorialDerivation.getIdentity())) {
							// TODO;
							throw new SBOLValidationException("sbol-XXXXX", vc);
						}
				}
			}
		}

		return removeTopLevel(combinatorialDerivation, combinatorialDerivations);
	}

	/**
	 * Creates a combinatorial derivation, and then adds it to this SBOL document's
	 * list of combinatorial derivations.
	 * <p>
	 * {@link #createCombinatorialDerivation(String, String, URI)}
	 * with the default URI prefix of this SBOL document, display ID, and version.
	 *
	 * @param displayId
	 *            the display ID of the combinatorial derivation to be created
	 * @param version
	 *            the version of the combinatorial derivation to be created
	 * @param templateDisplayId
	 *            the display ID of the template of the combinatorial derivation to
	 *            be created
	 * @param templateVersion
	 *            the version of the template of the combinatorial derivation to be
	 *            created
	 * @return the created combinatorial derivation
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createCombinatorialDerivation(String, String, URI)}.
	 */
	public CombinatorialDerivation createCombinatorialDerivation(String displayId, String version,
			String templateDisplayId, String templateVersion) throws SBOLValidationException {
		ComponentDefinition templateCD = this.getComponentDefinition(templateDisplayId, templateVersion);

		CombinatorialDerivation combinatorialDerivation = createCombinatorialDerivation(displayId, version,
				templateCD.getIdentity());
		this.addCombinatorialDerivation(combinatorialDerivation);

		return combinatorialDerivation;

	}

	/**
	 * Creates a combinatorial derivation, and then adds it to this SBOL document's
	 * list of combinatorial derivations.
	 * <p>
	 * {@link #createCombinatorialDerivation(String, URI)} with the
	 * default URI prefix of this SBOL document, display ID, and version.
	 *
	 * @param displayId
	 *            the display ID of the combinatorial derivation to be created
	 * @param template
	 *            the URI of the template of the combinatorial derivation to be
	 *            created
	 * @return the created combinatorial derivation
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createCombinatorialDerivation(String, URI)}.
	 */
	public CombinatorialDerivation createCombinatorialDerivation(String displayId, URI template)
			throws SBOLValidationException {
		return createCombinatorialDerivation(defaultURIprefix, displayId, "", template);
	}

	/**
	 * Creates a combinatorial derivation, and then adds it to this SBOL document's
	 * list of combinatorial derivations.
	 * <p>
	 * {@link #createCombinatorialDerivation(String, String, String, URI)}
	 * with the default URI prefix of this SBOL document, display ID, and version.
	 *
	 * @param displayId
	 *            the display ID of the combinatorial derivation to be created
	 * @param version
	 *            the version of the combinatorial derivation to be created
	 * @param template
	 *            the URI of the template of the combinatorial derivation to be
	 *            created
	 * @return the created combinatorial derivation
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createCombinatorialDerivation(String, String, String, URI)}.
	 */
	public CombinatorialDerivation createCombinatorialDerivation(String displayId, String version, URI template) throws SBOLValidationException {
		return createCombinatorialDerivation(defaultURIprefix, displayId, version, template);
	}

	/**
	 * Creates a combinatorial derivation, and then adds it to this SBOL document's
	 * list of combinatorial derivations.
	 * <p>
	 * This method creates a compliant URI for the combinatorial derivation to be
	 * created first. It starts with the given URI prefix after its been
	 * successfully validated, followed by the given display ID, and ends with the
	 * given version.
	 *
	 * @param URIprefix
	 *            the URI prefix used to construct the compliant URI for the
	 *            combinatorial derivation to be created
	 * @param displayId
	 *            the display ID of the combinatorial derivation to be created
	 * @param version
	 *            the version of the combinatorial derivation to be created
	 * @param template
	 *            the template URI of the combinatorial derivation to be created
	 * @return the created combinatorial derivation
	 * @throws SBOLValidationException
	 *             if any of the following SBOL validation rules was violated: TODO:
	 *             10201, 10202, 10204, 10206, 10220, 10502, 10503.
	 */
	public CombinatorialDerivation createCombinatorialDerivation(String URIprefix, String displayId, String version,
			URI template) throws SBOLValidationException {
		URIprefix = URIcompliance.checkURIprefix(URIprefix);
		CombinatorialDerivation cd = new CombinatorialDerivation(
				createCompliantURI(URIprefix, TopLevel.COMBINATORIAL_DERIVATION, displayId, version, typesInURIs),
				template);
		cd.setDisplayId(displayId);
		cd.setPersistentIdentity(
				createCompliantURI(URIprefix, TopLevel.COMBINATORIAL_DERIVATION, displayId, "", typesInURIs));
		cd.setVersion(version);
		addCombinatorialDerivation(cd);
		return cd;
	}

	/**
	 * Returns the set of combinatorial derivations owned by this SBOL document.
	 *
	 * @return the set of combinatorial derivation owned by this SBOL document.
	 */
	public Set<CombinatorialDerivation> getCombinatorialDerivations() {
		Set<CombinatorialDerivation> combinatorialDerivations = new HashSet<>();
		combinatorialDerivations.addAll(this.combinatorialDerivations.values());
		return combinatorialDerivations;
	}

	/**
	 * Removes all entries in the list of combinatorial derivations owned by this SBOL document. The
	 * list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeCombinatorialDerivation(CombinatorialDerivation)} to iteratively remove each
	 * combinatorialDerivation.
	 * 
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #removeCombinatorialDerivation(CombinatorialDerivation)}.
	 */
	public void clearCombinatorialDerivations() throws SBOLValidationException {
		Object[] valueSetArray = combinatorialDerivations.values().toArray();
		for (Object combinatorialDerivation : valueSetArray) {
			removeCombinatorialDerivation((CombinatorialDerivation) combinatorialDerivation);
		}
	}
	
	/**
	 * Returns the implementation matching the given display ID and
	 * version from this SBOL document's list of implementations.
	 * <p>
	 * A compliant implementation URI is created first. It starts with this
	 * SBOL document's default URI prefix after its been successfully validated,
	 * optionally followed by its type, namely
	 * {@link TopLevel#IMPLEMENTATION}, followed by the given display ID,
	 * and ends with the given version. This URI is used to look up the module
	 * definition in this SBOL document.
	 *
	 * @param displayId
	 *            the display ID of the implementation to be retrieved
	 * @param version
	 *            the version of the implementation to be retrieved
	 * @return the matching implementation if present, or {@code null}
	 *         otherwise
	 * @throws SBOLValidationException
	 *             validation error
	 */
	public Implementation getImplementation(String displayId, String version)
			throws SBOLValidationException {
		URI uri = URIcompliance.createCompliantURI(defaultURIprefix, TopLevel.IMPLEMENTATION, displayId,
				version, typesInURIs);

		return getImplementation(uri);
	}

	/**
	 * Returns the implementation matching the given identity URI from
	 * this SBOL document's list of implementations.
	 *
	 * @param implementationURI
	 *            the given identity URI of the implementation to be
	 *            retrieved
	 * @return the matching implementation if present, or {@code null}
	 *         otherwise.
	 */
	public Implementation getImplementation(URI implementationURI) {
		Implementation implementation = implementations.get(implementationURI);

		if (implementation == null) {
			for (SynBioHubFrontend frontend : getRegistries()) {
				try {
					SBOLDocument document = frontend.getSBOL(implementationURI);
					if (document != null) {
						implementation = document.getImplementation(implementationURI);
						createCopy(document);
					}
				} catch (SynBioHubException | SBOLValidationException e) {
				}
			}
		}

		return implementation;
	}
	
	/**
	 * Adds the given implementation to this SBOL document's list of
	 * implementations
	 *
	 * @param implementation
	 *            the implementation to be added
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #addTopLevel(TopLevel, Map, String, Map...)}.
	 */
	void addImplementation(Implementation implementation) throws SBOLValidationException {
		addTopLevel(implementation, implementations, "implementation", collections, experiments, experimentalData, 
				genericTopLevels, activities, plans, agents, models, moduleDefinitions, sequences, combinatorialDerivations, attachments);
	}
	
	/**
	 * Removes the given implementation from this SBOL document's list of
	 * implementations.
	 *
	 * @param implementation
	 *            the implementation to be removed
	 * @return {@code true} if the given implementation was successfully
	 *         removed, {@code false} otherwise
	 * @throws SBOLValidationException
	 *             if either of the following SBOL validation rules was violated:
	 *             TODO: 10604, 12103.
	 */
	public boolean removeImplementation(Implementation implementation)
			throws SBOLValidationException {

		return removeTopLevel(implementation, implementations);
	}

	/**
	 * Creates an implementation, and then adds it to this SBOL document's
	 * list of implementations.
	 * <p>
	 * {@link #createImplementation(String)} with the
	 * default URI prefix of this SBOL document, display ID, and version.
	 *
	 * @param displayId
	 *            the display ID of the implementation to be created
	 * @return the created implementation
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createImplementation(String, String, String)}.
	 */
	public Implementation createImplementation(String displayId)
			throws SBOLValidationException {
		return createImplementation(defaultURIprefix, displayId, "");
	}

	/**
	 * Creates an implementation, and then adds it to this SBOL document's
	 * list of implementations.
	 * <p>
	 * {@link #createImplementation(String, String)}
	 * with the default URI prefix of this SBOL document, display ID, and version.
	 *
	 * @param displayId
	 *            the display ID of the implementation to be created
	 * @param version
	 *            the version of the implementation to be created
	 * @return the created implementation
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createImplementation(String, String, String)}.
	 */
	public Implementation createImplementation(String displayId, String version) throws SBOLValidationException {
		return createImplementation(defaultURIprefix, displayId, version);
	}
	
	/**
	 * Creates an implementation, and then adds it to this SBOL document's
	 * list of implementations.
	 * <p>
	 * This method creates a compliant URI for the implementation to be
	 * created first. It starts with the given URI prefix after its been
	 * successfully validated, followed by the given display ID, and ends with the
	 * given version.
	 *
	 * @param URIprefix
	 *            the URI prefix used to construct the compliant URI for the
	 *            implementation to be created
	 * @param displayId
	 *            the display ID of the implementation to be created
	 * @param version
	 *            the version of the implementation to be created
	 * @return the created implementation
	 * @throws SBOLValidationException
	 *             if any of the following SBOL validation rules was violated: TODO:
	 *             10201, 10202, 10204, 10206, 10220, 10502, 10503.
	 */
	public Implementation createImplementation(String URIprefix, String displayId, String version) throws SBOLValidationException {
		URIprefix = URIcompliance.checkURIprefix(URIprefix);
		Implementation cd = new Implementation(
				createCompliantURI(URIprefix, TopLevel.IMPLEMENTATION, displayId, version, typesInURIs));
		cd.setDisplayId(displayId);
		cd.setPersistentIdentity(
				createCompliantURI(URIprefix, TopLevel.IMPLEMENTATION, displayId, "", typesInURIs));
		cd.setVersion(version);
		addImplementation(cd);
		return cd;
	}
	
	/**
	 * Returns the set of implementations owned by this SBOL document.
	 *
	 * @return the set of implementations owned by this SBOL document.
	 */
	public Set<Implementation> getImplementations() {
		Set<Implementation> implementations = new HashSet<>();
		implementations.addAll(this.implementations.values());
		return implementations;
	}

	/**
	 * Removes all entries in the list of implementations owned by this SBOL document. The
	 * list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeImplementation(Implementation)} to iteratively remove each
	 * implementation.
	 * 
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #removeImplementation(Implementation)}.
	 */
	public void clearImplementations() throws SBOLValidationException {
		Object[] valueSetArray = implementations.values().toArray();
		for (Object implementation : valueSetArray) {
			removeImplementation((Implementation) implementation);
		}
	}

	/**
	 * Returns the set of component definitions owned by this SBOL document.
	 *
	 * @return the set of component definitions owned by this SBOL document.
	 */
	public Set<ComponentDefinition> getComponentDefinitions() {
		Set<ComponentDefinition> components = new HashSet<>();
		components.addAll(this.componentDefinitions.values());
		return components;
	}

	/**
	 * Returns the set of root component definitions. A root component definition is
	 * a component definition that is not referenced by a child component.
	 * 
	 * @return the set of root component definitions
	 */
	public Set<ComponentDefinition> getRootComponentDefinitions() {
		Set<ComponentDefinition> componentDefs = getComponentDefinitions();
		for (ComponentDefinition componentDefinition : getComponentDefinitions()) {
			for (Component component : componentDefinition.getComponents()) {
				ComponentDefinition childDefinition = component.getDefinition();
				if (childDefinition != null && componentDefs.contains(childDefinition)) {
					componentDefs.remove(childDefinition);
				}
			}
		}
		return componentDefs;
	}

	/**
	 * Returns the set of root module definitions. A root module definition is a
	 * module definition that is not referenced by a child module.
	 * 
	 * @return the set of root module definitions
	 */
	public Set<ModuleDefinition> getRootModuleDefinitions() {
		Set<ModuleDefinition> moduleDefs = getModuleDefinitions();
		for (ModuleDefinition moduleDefinition : getModuleDefinitions()) {
			for (Module module : moduleDefinition.getModules()) {
				ModuleDefinition childDefinition = module.getDefinition();
				if (childDefinition != null && moduleDefs.contains(childDefinition)) {
					moduleDefs.remove(childDefinition);
				}
			}
		}
		return moduleDefs;
	}

	/**
	 * Removes all entries in the list of component definitions owned by this SBOL
	 * document. The list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeComponentDefinition(ComponentDefinition)} to
	 * iteratively remove each component definition.
	 * 
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #removeComponentDefinition(ComponentDefinition)}.
	 */
	public void clearComponentDefinitions() throws SBOLValidationException {
		Object[] valueSetArray = componentDefinitions.values().toArray();
		for (Object componentDefinition : valueSetArray) {
			removeComponentDefinition((ComponentDefinition) componentDefinition);
		}
	}

	/**
	 * @param componentDefinitions
	 *            The given set of ComponentDefinitions to be added
	 * @throws SBOLValidationException
	 *             see {@link SBOLValidationException}
	 */
	/*
	 * void setComponentDefinitions(Set<ComponentDefinition> componentDefinitions)
	 * throws SBOLValidationException { clearComponentDefinitions(); for
	 * (ComponentDefinition componentDefinition : componentDefinitions) {
	 * addComponentDefinition(componentDefinition); } }
	 */

	// /**
	// * @param identity a given identifier for this object
	// * @param elements characters that represents the constituents of a biological
	// or chemical molecule (i.e. nucleotide bases of a molecule of DNA, the amino
	// acid residues of a protein, or the atoms and chemical bonds of a small
	// molecule)
	// * @param encoding Indicate how the elements property of a Sequence must be
	// formed and interpreted
	// * @return the created Sequence instance
	// * @throws SBOLValidationException if an SBOL validation rule violation
	// occurred in any of the following constructors or methods:
	// * <ul>
	// * <li>{@link Sequence#Sequence(URI, String, URI)}, or </li>
	// * <li>{@link #addSequence(Sequence)}.</li>
	// * </ul>
	// */
	// private Sequence createSequence(URI identity, String elements, URI encoding)
	// throws SBOLValidationException {
	// Sequence newSequence = new Sequence(identity, elements, encoding);
	// addSequence(newSequence);
	// return newSequence;
	// }

	/**
	 * Creates a Sequence instance with this SBOL document's
	 * {@code defaultURIprefix}, the given arguments, and an empty version string,
	 * and then adds it to this SBOL document's list of Sequence instances.
	 * <p>
	 * This method calls
	 * {@link #createSequence(String, String, String, String, URI)} to do the
	 * following validity checks and create a Sequence instance.
	 * <p>
	 * If the {@code defaultURIprefix} does not end with one of the following
	 * delimiters: "/", ":", or "#", then "/" is appended to the end of it.
	 * <p>
	 * This method requires the {@code defaultURIprefix} field to be set, and the
	 * given display ID is not {@code null} and is valid.
	 * <p>
	 * A Sequence instance is created with a compliant URI. This URI is composed
	 * from the this SBOL document's {@code defaultURIprefix}, the optional type
	 * {@link TopLevel#SEQUENCE}, the given display ID, and an empty version string.
	 * The display ID, persistent identity, and version fields of this instance are
	 * then set accordingly.
	 *
	 * @param displayId
	 *            an intermediate between name and identity that is machine-readable
	 * @param elements
	 *            characters that represents the constituents of a biological or
	 *            chemical molecule (i.e. nucleotide bases of a molecule of DNA, the
	 *            amino acid residues of a protein, or the atoms and chemical bonds
	 *            of a small molecule)
	 * @param encoding
	 *            Indicate how the elements property of a Sequence must be formed
	 *            and interpreted
	 * @return the created Sequence instance
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule was violated in
	 *             {@link SBOLDocument#createSequence(String, String, String, String, URI)}.
	 */
	public Sequence createSequence(String displayId, String elements, URI encoding) throws SBOLValidationException {
		return createSequence(defaultURIprefix, displayId, "", elements, encoding);
	}

	/**
	 * Creates a Sequence instance with this SBOL document's
	 * {@code defaultURIprefix} and the given arguments, and then adds it to this
	 * SBOL document's list of Sequence instances.
	 * <p>
	 * This method calls
	 * {@link #createSequence(String, String, String, String, URI)} to do the
	 * following validity checks and create a Sequence instance.
	 * <p>
	 * If the {@code defaultURIprefix} does not end with one of the following
	 * delimiters: "/", ":", or "#", then "/" is appended to the end of it.
	 * <p>
	 * This method requires the {@code defaultURIprefix} field to be set, and the
	 * given display ID and version arguments are not {@code null} and are both
	 * valid.
	 * <p>
	 * A Sequence instance is created with a compliant URI. This URI is composed
	 * from the this SBOL document's {@code defaultURIprefix}, the optional type
	 * {@link TopLevel#SEQUENCE}, the given display ID, and version. The display ID,
	 * persistent identity, and version fields of this instance are then set
	 * accordingly.
	 *
	 *
	 * @param displayId
	 *            an intermediate between name and identity that is machine-readable
	 * @param version
	 *            The given version for this object
	 * @param elements
	 *            characters that represents the constituents of a biological or
	 *            chemical molecule (i.e. nucleotide bases of a molecule of DNA, the
	 *            amino acid residues of a protein, or the atoms and chemical bonds
	 *            of a small molecule)
	 * @param encoding
	 *            Indicate how the elements property of a Sequence must be formed
	 *            and interpreted
	 * @return the created Sequence instance
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule was violated in
	 *             {@link SBOLDocument#createSequence(String, String, String, String, URI)}.
	 */
	public Sequence createSequence(String displayId, String version, String elements, URI encoding)
			throws SBOLValidationException {
		return createSequence(defaultURIprefix, displayId, version, elements, encoding);
	}

	/**
	 * Creates a Sequence instance with the given arguments, and then adds it to
	 * this SBOLDocument object's list of Sequence instances.
	 * <p>
	 * If the given {@code URIprefix} does not end with one of the following
	 * delimiters: "/", ":", or "#", then "/" is appended to the end of it.
	 * <p>
	 * This method requires that the given {@code URIprefix}, display ID, and
	 * version are not {@code null} and are valid.
	 * <p>
	 * A Sequence instance is created with a compliant URI. This URI is composed
	 * from the given {@code URIprefix}, the optional type
	 * {@link TopLevel#SEQUENCE}, the given display ID, and version. The display ID,
	 * persistent identity, and version fields of this instance are then set
	 * accordingly.
	 *
	 * @param URIprefix
	 *            maps to a domain over which the user has control
	 * @param displayId
	 *            an intermediate between name and identity that is machine-readable
	 * @param version
	 *            The given version for this object
	 * @param elements
	 *            characters that represents the constituents of a biological or
	 *            chemical molecule (i.e. nucleotide bases of a molecule of DNA, the
	 *            amino acid residues of a protein, or the atoms and chemical bonds
	 *            of a small molecule)
	 * @param encoding
	 *            Indicate how the elements property of a Sequence must be formed
	 *            and interpreted
	 * @return the created Sequence instance
	 * @throws SBOLValidationException
	 *             if if any of the following SBOL validation rules was violated:
	 *             10201, 10202, 10204, 10206, 10220, 10402, 10403, 10405.
	 */
	public Sequence createSequence(String URIprefix, String displayId, String version, String elements, URI encoding)
			throws SBOLValidationException {
		URIprefix = URIcompliance.checkURIprefix(URIprefix);
		Sequence s = new Sequence(createCompliantURI(URIprefix, TopLevel.SEQUENCE, displayId, version, typesInURIs),
				elements, encoding);
		s.setPersistentIdentity(createCompliantURI(URIprefix, TopLevel.SEQUENCE, displayId, "", typesInURIs));
		s.setDisplayId(displayId);
		s.setVersion(version);
		addSequence(s);
		return s;
	}

	/**
	 * Creates an identical copy of each top-level element of a document, and then
	 * adds the created top-level to the corresponding list of top-levels in this
	 * SBOL document.
	 * <p>
	 * This method calls {@link #createCopy(TopLevel)} for each top-level instance.
	 *
	 * @param document
	 *            the document to be copied from
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createCopy(TopLevel)}.
	 */
	public void createCopy(SBOLDocument document) throws SBOLValidationException {
		for (TopLevel topLevel : document.getTopLevels()) {
			createCopy(topLevel);
		}
	}

	// private SBOLDocument createCopy(String URIPrefix, String displayId, String
	// version) throws SBOLValidationException {
	// SBOLDocument document = new SBOLDocument();
	// for (TopLevel topLevel : getTopLevels()) {
	// document.createCopy(topLevel, URIPrefix, displayId, version);
	// }
	// return document;
	// }

	/**
	 * Creates an identical copy of the given top-level, and then adds the created
	 * top-level to the corresponding list of top-levels in this SBOL document.
	 * <p>
	 * This method calls {@link #createCopy(TopLevel, String, String, String)} with
	 * the given top-level instance, and {@code null} for other arguments.
	 *
	 * @param topLevel
	 *            the top-level to be copied from
	 * @return the copied top-level instance
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createCopy(TopLevel, String, String, String)}.
	 */
	public TopLevel createCopy(TopLevel topLevel) throws SBOLValidationException {
		return createCopy(topLevel, null, null, null);
	}

	/**
	 * Creates a copy of the given top-level with the given display ID, and then
	 * adds the created top-level to the corresponding list of top-levels in this
	 * SBOL document.
	 * <p>
	 * This method calls {@link #createCopy(TopLevel, String, String, String)} with
	 * the given top-level instance, the default URI prefix for this SBOL document,
	 * the given display ID, and the empty version string.
	 * 
	 * @param topLevel
	 *            the top-level to be copied from
	 * @param displayId
	 *            the display ID of the created copy
	 * @return the copied top-level instance
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createCopy(TopLevel, String, String, String)}.
	 *
	 */
	public TopLevel createCopy(TopLevel topLevel, String displayId) throws SBOLValidationException {
		return createCopy(topLevel, defaultURIprefix, displayId, "");
	}

	/**
	 * Renames the given top-level's display ID with the given one.
	 * <p>
	 * This method first calls {@link #createCopy(TopLevel, String, String, String)}
	 * to make a copy of the given top-level with this SBOL document's default URI
	 * prefix, the given display, and an empty string for version. It then removes
	 * the given top-level and then returns the newly-copied top-level.
	 *
	 * @param topLevel
	 *            the top-level to be renamed
	 * @param displayId
	 *            the given display ID to be renamed to
	 * @return the renamed top-level
	 * @throws SBOLValidationException
	 *             if either of the following conditions is satisfied:
	 *             <ul>
	 *             <li>any of the following SBOL validation rules was violated:
	 *             10513, 10604, 11608, 11703, 12103; or</li>
	 *             <li>an SBOL validation rule violation occurred in the following
	 *             method:
	 *             {@link #createCopy(TopLevel, String, String, String)}.</li>
	 *             </ul>
	 */
	public TopLevel rename(TopLevel topLevel, String displayId) throws SBOLValidationException {
		return rename(topLevel, defaultURIprefix, displayId, "");
	}

	/**
	 * Creates a copy of the given top-level with the given display ID and version,
	 * and then adds the created top-level to the corresponding list of top-levels
	 * in this SBOL document.
	 * <p>
	 * This method calls {@link #createCopy(TopLevel, String, String, String)} with
	 * the given top-level instance, the default URI prefix for this SBOL document,
	 * the given display ID, and the empty version string.
	 * 
	 * @param topLevel
	 *            the top-level to be copied from
	 * @param displayId
	 *            the display ID of the created copy
	 * @param version
	 *            the version of the created copy
	 * @return the copied top-level instance
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createCopy(TopLevel, String, String, String)}
	 */
	public TopLevel createCopy(TopLevel topLevel, String displayId, String version) throws SBOLValidationException {
		return createCopy(topLevel, defaultURIprefix, displayId, version);
	}

	/**
	 * Renames the given top-level's display ID and version with the given ones.
	 * <p>
	 * This method first calls {@link #createCopy(TopLevel, String, String, String)}
	 * to make a copy of the given top-level with this SBOL document's default URI
	 * prefix, the given display ID and version. It then removes the given top-level
	 * and then returns the newly-copied top-level.
	 *
	 * @param topLevel
	 *            the top-level to be renamed
	 * @param displayId
	 *            the given display ID to be renamed to
	 * @param version
	 *            the given version to be renamed to
	 * @return the renamed top-level
	 * @throws SBOLValidationException
	 *             if either of the following conditions is satisfied:
	 *             <ul>
	 *             <li>any of the following SBOL validation rules was violated:
	 *             10513, 10604, 11608, 11703, 12103; or</li>
	 *             <li>an SBOL validation rule violation occurred in the following
	 *             method:
	 *             {@link #createCopy(TopLevel, String, String, String)}.</li>
	 *             </ul>
	 */
	public TopLevel rename(TopLevel topLevel, String displayId, String version) throws SBOLValidationException {
		return rename(topLevel, defaultURIprefix, displayId, version);
	}

	/**
	 * Creates a copy of the given top-level with the given URI prefix, display ID
	 * and version, and then adds the created top-level to the corresponding list of
	 * top-levels in this SBOL document.
	 * <p>
	 * This method creates a compliant URI for the copied top-level with the given
	 * default URI prefix, display ID, and version.
	 * 
	 * @param topLevel
	 *            the top-level to be copied from
	 * @param URIprefix
	 *            the URI prefix used to create the compliant URI for the created
	 *            copy
	 * @param displayId
	 *            the display ID of the created copy
	 * @param version
	 *            the version of the created copy
	 * @return the copied top-level instance
	 * @throws SBOLValidationException
	 *             if any of the following SBOL validation rules was violated:
	 *             10201, 10202, 10204, 10206, 10220, 10303, 10304, 10305, 10401,
	 *             10402, 10403, 10405, 10501, 10503, 10513, 10522, 10526, 10602,
	 *             10604, 10605, 10607, 10701, 10801, 10802, 10803, 10804, 10805,
	 *             10807, 10808, 10809, 10811, 10901, 10905, 11101, 11102, 11103,
	 *             11104, 11201, 11202, 11301, 11401, 11402, 11403, 11404, 11405,
	 *             11406, 11501, 11502, 11504, 11508, 11601, 11608, 11609, 11701,
	 *             11703, 11704, 11705, 11801, 11901, 12001, 12002, 12003, 12103,
	 *             12301, 12302.
	 */
	public TopLevel createCopy(TopLevel topLevel, String URIprefix, String displayId, String version)
			throws SBOLValidationException {
		// topLevel.isURIcompliant();
		if (URIprefix == null) {
			URIprefix = extractURIprefix(topLevel.getIdentity());
			if (URIprefix == null) {
				URIprefix = this.getDefaultURIprefix();
			}
			URIprefix = URIcompliance.checkURIprefix(URIprefix);
		} else {
			URIprefix = URIcompliance.checkURIprefix(URIprefix);
		}
		if (displayId == null) {
			// displayId = topLevel.getDisplayId();
			// if (displayId == null) {
			displayId = URIcompliance.extractDisplayId(topLevel.getIdentity());
			if (displayId == null) {
				displayId = URIcompliance.findDisplayId(topLevel.getIdentity().toString());
			} else {
				displayId = URIcompliance.fixDisplayId(displayId);
			}
			// }
		}
		if (version == null) {
			version = topLevel.getVersion();
		}
		TopLevel oldTopLevel = this.getTopLevelLocalOnly(URIcompliance.createCompliantURI(URIprefix, displayId, version));
		if (oldTopLevel != null) {
			if (oldTopLevel.equals(topLevel)) {
				return oldTopLevel;
			} else if (oldTopLevel instanceof GenericTopLevel) {
				// TODO: need to make sure they are okay enough
				// This is done because copy makes compliant which changes object
				// See AnnotationOutput example and sequenceX examples
				return oldTopLevel;
			} else {
				throw new SBOLValidationException("sbol-10202",oldTopLevel.getIdentity());
			}
		}
		if (topLevel instanceof Collection) {
			Collection newCollection = this.createCollection(URIprefix, displayId, version);
			newCollection.copy((Collection) topLevel);
			return newCollection;
		} else if (topLevel instanceof Experiment) {
			Experiment newExperiment = this.createExperiment(URIprefix, displayId, version);
			newExperiment.copy((Experiment) topLevel);
			return newExperiment;
		} else if (topLevel instanceof ExperimentalData) {
			ExperimentalData newExperimentalData = this.createExperimentalData(URIprefix, displayId, version);
			newExperimentalData.copy((ExperimentalData) topLevel);
			return newExperimentalData;
		} else if (topLevel instanceof ComponentDefinition) {
			ComponentDefinition newComponentDefinition = this.createComponentDefinition(URIprefix, displayId, version,
					((ComponentDefinition) topLevel).getTypes());
			newComponentDefinition.copy((ComponentDefinition) topLevel);
			return newComponentDefinition;
		} else if (topLevel instanceof CombinatorialDerivation) {
			CombinatorialDerivation newCombinatorialDerivation = this.createCombinatorialDerivation(URIprefix,
					displayId, version, ((CombinatorialDerivation) topLevel).getTemplateURI());
			newCombinatorialDerivation.copy((CombinatorialDerivation) topLevel);
			return newCombinatorialDerivation;
		} else if (topLevel instanceof Attachment) {
			Attachment newAttachment = this.createAttachment(URIprefix,	displayId, version, ((Attachment) topLevel).getSource());
			newAttachment.copy((Attachment) topLevel);
			return newAttachment;
		} else if (topLevel instanceof Model) {
			Model newModel = this.createModel(URIprefix, displayId, version, ((Model) topLevel).getSource(),
					((Model) topLevel).getLanguage(), ((Model) topLevel).getFramework());
			newModel.copy((Model) topLevel);
			return newModel;
		} else if (topLevel instanceof ModuleDefinition) {
			ModuleDefinition newModuleDefinition = this.createModuleDefinition(URIprefix, displayId, version);
			newModuleDefinition.copy((ModuleDefinition) topLevel);
			return newModuleDefinition;
		} else if (topLevel instanceof Implementation) {
			Implementation newImplementation = this.createImplementation(URIprefix,	displayId, version);
			newImplementation.copy((Implementation) topLevel);
			return newImplementation;
		} else if (topLevel instanceof Sequence) {
			Sequence newSequence = this.createSequence(URIprefix, displayId, version,
					((Sequence) topLevel).getElements(), ((Sequence) topLevel).getEncoding());
			newSequence.copy((Sequence) topLevel);
			return newSequence;
		} else if (topLevel instanceof GenericTopLevel) {
			GenericTopLevel newGenericTopLevel = this.createGenericTopLevel(URIprefix, displayId, version,
					((GenericTopLevel) topLevel).getRDFType());
			newGenericTopLevel.copy((GenericTopLevel) topLevel);
			return newGenericTopLevel;
		} else if (topLevel instanceof Activity) {
			Activity newActivity = this.createActivity(URIprefix, displayId, version);
			newActivity.copy((Activity) topLevel);
			return newActivity;
		} else if (topLevel instanceof Agent) {
			Agent newAgent = this.createAgent(URIprefix, displayId, version);
			newAgent.copy((Agent) topLevel);
			return newAgent;
		} else if (topLevel instanceof Plan) {
			Plan newPlan = this.createPlan(URIprefix, displayId, version);
			newPlan.copy((Plan) topLevel);
			return newPlan;
		} else {
			throw new IllegalArgumentException("Unable to copy " + topLevel.getIdentity());
		}
	}

	/**
	 * Creates an identical copy of the given top-level and returns it in a new
	 * SBOLDocument.
	 *
	 * @param topLevel
	 *            The topLevel object to be recursively copied from this
	 *            SBOLDocument
	 * @return the created SBOLDocument with this top-level instance and all its
	 *         dependencies
	 * @throws SBOLValidationException
	 *             if any of the following SBOL validation rules was violated:
	 *             10201, 10202, 10204, 10206, 10220, 10303, 10304, 10305, 10401,
	 *             10402, 10403, 10405, 10501, 10503, 10522, 10526, 10602, 10604,
	 *             10605, 10607, 10701, 10801, 10802, 10803, 10804, 10805, 10807,
	 *             10808, 10809, 10811, 10901, 10905, 11101, 11201, 11301, 11401,
	 *             11402, 11403, 11404, 11405, 11406, 11501, 11502, 11504, 11508,
	 *             11601, 11608, 11609, 11701, 11703, 11704, 11705, 11801, 10802,
	 *             10803, 10804, 10807, 10808, 10809, 10811, 11901, 12001, 12002,
	 *             12003, 12101, 12103, 12301, 12302.
	 */
	public SBOLDocument createRecursiveCopy(TopLevel topLevel) throws SBOLValidationException {
		SBOLDocument document = new SBOLDocument();
		createRecursiveCopy(document, topLevel);
		return document;
	}

	private void createRecursiveCopy(SBOLDocument document, Annotation annotation) throws SBOLValidationException {
		if (annotation.isURIValue()) {
			TopLevel gtl = getTopLevelLocalOnly(annotation.getURIValue());
			if (gtl != null)
				createRecursiveCopy(document, gtl);
		} else if (annotation.isNestedAnnotations()) {
			for (Annotation nestedAnnotation : annotation.getAnnotations()) {
				createRecursiveCopy(document, nestedAnnotation);
			}
		}
	}

	/**
	 * @param document
	 *            document to copy recursively into
	 * @param topLevel
	 *            topLevel that is being recursively copied from
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link SBOLDocument#createCopy(TopLevel)}.
	 */
	public void createRecursiveCopy(SBOLDocument document, TopLevel topLevel) throws SBOLValidationException {
		if (topLevel == null || document.getTopLevelLocalOnly(topLevel.getIdentity()) != null)
			return;
		document.createCopy(topLevel);
		for (URI wasDerivedFromURI : topLevel.getWasDerivedFroms()) {
			TopLevel wasDerivedFrom = getTopLevel(wasDerivedFromURI);
			if (wasDerivedFrom != null) {
				createRecursiveCopy(document, wasDerivedFrom);
			}
		}
		for (URI wasGeneratedByURI : topLevel.getWasGeneratedBys()) {
			TopLevel wasGeneratedBy = getTopLevel(wasGeneratedByURI);
			if (wasGeneratedBy != null) {
				createRecursiveCopy(document, wasGeneratedBy);
			}
		}
		for (URI attachmentURI : topLevel.getAttachmentURIs()) {
			TopLevel attachment = getTopLevel(attachmentURI);
			if (attachment != null) {
				createRecursiveCopy(document, attachment);
			}
		}
		if (topLevel instanceof GenericTopLevel || topLevel instanceof Sequence || topLevel instanceof Model
				|| topLevel instanceof Plan || topLevel instanceof Agent || topLevel instanceof Implementation
				|| topLevel instanceof Attachment || topLevel instanceof ExperimentalData) {
			// Do nothing
		} else if (topLevel instanceof Collection) {
			for (TopLevel member : ((Collection) topLevel).getMembers()) {
				createRecursiveCopy(document, member);
			}
		} else if (topLevel instanceof Experiment) {
			for (TopLevel experimentalDatum : ((Experiment) topLevel).getExperimentalData()) {
				createRecursiveCopy(document, experimentalDatum);
			}
		} else if (topLevel instanceof ComponentDefinition) {
			for (Component component : ((ComponentDefinition) topLevel).getComponents()) {
				if (component.getDefinition() != null) {
					createRecursiveCopy(document, component.getDefinition());
				}
			}
			for (TopLevel sequence : ((ComponentDefinition) topLevel).getSequences()) {
				createRecursiveCopy(document, sequence);
			}
		} else if (topLevel instanceof CombinatorialDerivation) {
			if(((CombinatorialDerivation) topLevel).getTemplate() != null) {
				createRecursiveCopy(document, ((CombinatorialDerivation) topLevel).getTemplate());
			}
			for (VariableComponent variableComponent : ((CombinatorialDerivation) topLevel).getVariableComponents()) {
				for(Collection collection: variableComponent.getVariantCollections()) {
					createRecursiveCopy(document, collection);
				}
				for(ComponentDefinition componentDefinition: variableComponent.getVariants()) {
					createRecursiveCopy(document, componentDefinition);
				}
				for(CombinatorialDerivation combinatorialDerivation: variableComponent.getVariantDerivations()) {
					createRecursiveCopy(document, combinatorialDerivation);
				}
			}
		}
		else if (topLevel instanceof ModuleDefinition) {
			for (FunctionalComponent functionalComponent : ((ModuleDefinition) topLevel).getFunctionalComponents()) {
				if (functionalComponent.getDefinition() != null) {
					createRecursiveCopy(document, functionalComponent.getDefinition());
				}
			}
			for (Module module : ((ModuleDefinition) topLevel).getModules()) {
				if (module.getDefinition() != null) {
					createRecursiveCopy(document, module.getDefinition());
				}
			}
			for (Model model : ((ModuleDefinition) topLevel).getModels()) {
				createRecursiveCopy(document,model);
			}
		} else if (topLevel instanceof Activity) {
			for (Association association : ((Activity) topLevel).getAssociations()) {
				if (association.getAgent() != null) {
					createRecursiveCopy(document, association.getAgent());
				}
				if (association.isSetPlan() && association.getPlan() != null) {
					createRecursiveCopy(document, association.getPlan());
				}
			}
			for (Usage usage : ((Activity) topLevel).getUsages()) {
				if (usage.getEntity() != null) {
					createRecursiveCopy(document, usage.getEntity());
				}
			}
		}
		for (Annotation annotation : topLevel.getAnnotations()) {
			if (annotation.isURIValue()) {
				TopLevel gtl = getTopLevelLocalOnly(annotation.getURIValue());
				if (gtl != null)
					createRecursiveCopy(document, gtl);
			} else if (annotation.isNestedAnnotations()) {
				for (Annotation nestedAnnotation : annotation.getAnnotations()) {
					createRecursiveCopy(document, nestedAnnotation);
				}
			}
		}
	}

	private String extractDocumentURIPrefix() {
		String documentURIPrefix = null;
		for (TopLevel topLevel : getTopLevels()) {
			if (documentURIPrefix == null || documentURIPrefix.equals("")) {
				documentURIPrefix = URIcompliance.extractURIprefix(topLevel.getIdentity());
			} else {
				for (int i = 0; i < documentURIPrefix.length(); i++) {
					if (i >= topLevel.getIdentity().toString().length()
							|| documentURIPrefix.charAt(i) != topLevel.getIdentity().toString().charAt(i)) {
						if (i == 0) {
							documentURIPrefix = "";
						} else {
							documentURIPrefix = documentURIPrefix.substring(0, i);
						}
						break;
					}
				}
				if (documentURIPrefix.equals(""))
					break;
			}
		}
		return documentURIPrefix;
	}

	private void fixDocumentURIPrefix() throws SBOLValidationException {
		String documentURIPrefixAll = extractDocumentURIPrefix();
		if (documentURIPrefixAll != null && documentURIPrefixAll.length() >= 9) {
			setDefaultURIprefix(documentURIPrefixAll);
		}
		String documentURIPrefix = documentURIPrefixAll;
		for (TopLevel topLevel : getTopLevels()) {
			if (documentURIPrefixAll.length() < 9) {
				documentURIPrefix = URIcompliance.extractURIprefix(topLevel.getIdentity());
				String simpleNamespace = URIcompliance.extractSimpleNamespace(topLevel.getIdentity());
				if (simpleNamespace != null) {
					String documentURIPrefix2 = URIcompliance.extractURIprefix(URI.create(simpleNamespace));
					if (documentURIPrefix2 != null) {
						documentURIPrefix = documentURIPrefix2;
					}
				}
				if (documentURIPrefix == null) {
					documentURIPrefix = this.getDefaultURIprefix();
				}
			}
			if (!topLevel.getIdentity().equals(URIcompliance.createCompliantURI(documentURIPrefix,
					URIcompliance.findDisplayId(topLevel), topLevel.getVersion()))) {
				String newDisplayId = topLevel.getIdentity().toString().replaceAll(documentURIPrefix, "");
				String newVersion = "";
				if (topLevel.isSetVersion()) {
					newDisplayId = newDisplayId.replace("/" + topLevel.getVersion(), "");
					newVersion = topLevel.getVersion();
				}
				newDisplayId = URIcompliance.fixDisplayId(newDisplayId);
				while (getTopLevelLocalOnly(
						URIcompliance.createCompliantURI(documentURIPrefix, newDisplayId, newVersion)) != null) {
					newDisplayId = newDisplayId.replaceAll("_", "__");
				}
				TopLevel newTopLevel = this.createCopy(topLevel, newDisplayId, newVersion);
				removeTopLevel(topLevel);
				updateReferences(topLevel.getIdentity(), newTopLevel.getIdentity());
				// TODO: should this be changing to newTopLevel.getIdentity(), rather than persistent identity
				updateReferences(topLevel.getPersistentIdentity(), newTopLevel.getPersistentIdentity());
			}
		}
	}

	private void updateReferences(List<Annotation> annotations, URI originalIdentity, URI newIdentity)
			throws SBOLValidationException {
		for (Annotation annotation : annotations) {
			if (annotation.isURIValue()) {
				if (annotation.getURIValue().equals(originalIdentity)) {
					annotation.setURIValue(newIdentity);
				}
			} else if (annotation.isNestedAnnotations()) {
				updateReferences(annotation.getAnnotations(), originalIdentity, newIdentity);
			}
		}
	}

	private void updateReferences(Identified identified, URI originalIdentity, URI newIdentity)
			throws SBOLValidationException {
		updateReferences(identified.getAnnotations(), originalIdentity, newIdentity);
	}

	private void updateReferences(URI originalIdentity, URI newIdentity) throws SBOLValidationException {
		for (TopLevel topLevel : getTopLevels()) {
			for (URI wasDerivedFrom : topLevel.getWasDerivedFroms()) {
				if (wasDerivedFrom.equals(originalIdentity)) {
					topLevel.removeWasDerivedFrom(originalIdentity);
					topLevel.addWasDerivedFrom(newIdentity);	
				}
			}
			for (URI wasGeneratedBy : topLevel.getWasGeneratedBys()) {
				if (wasGeneratedBy.equals(originalIdentity)) {
					topLevel.removeWasGeneratedBy(originalIdentity);
					topLevel.addWasGeneratedBy(newIdentity);	
				}
			}
			for (URI attachmentURI : topLevel.getAttachmentURIs()) {
				if (attachmentURI.equals(originalIdentity)) {
					topLevel.removeAttachment(originalIdentity);
					topLevel.addAttachment(newIdentity);	
				}
			}
		}
		for (Collection collection : getCollections()) {
			for (URI memberURI : collection.getMemberURIs()) {
				if (memberURI.equals(originalIdentity)) {
					collection.removeMember(originalIdentity);
					collection.addMember(newIdentity);
				}
			}
			updateReferences(collection, originalIdentity, newIdentity);
		}
		for (Experiment experiment : getExperiments()) {
			for (URI experimentalDataURI : experiment.getExperimentalDataURIs()) {
				if (experimentalDataURI.equals(originalIdentity)) {
					experiment.removeExperimentalData(originalIdentity);
					experiment.addExperimentalData(newIdentity);
				}
			}
			updateReferences(experiment, originalIdentity, newIdentity);
		}
		for (ComponentDefinition componentDefinition : getComponentDefinitions()) {
			updateReferences(componentDefinition, originalIdentity, newIdentity);
			for (URI sequenceURI : componentDefinition.getSequenceURIs()) {
				if (sequenceURI.equals(originalIdentity)) {
					componentDefinition.removeSequence(originalIdentity);
					componentDefinition.addSequence(newIdentity);
				}
			}
			for (Component component : componentDefinition.getComponents()) {
				if (component.getDefinitionURI().equals(originalIdentity)) {
					component.setDefinition(newIdentity);
					for (MapsTo mapsTo : component.getMapsTos()) {
						ComponentDefinition cd = getComponentDefinition(newIdentity);
						if (cd != null) {
							String displayId = URIcompliance.extractDisplayId(mapsTo.getRemoteURI());
							URI newURI = URIcompliance.createCompliantURI(cd.getPersistentIdentity().toString(),
									displayId, cd.getVersion());
							mapsTo.setRemote(newURI);
						}
					}
				}
				updateReferences(component, originalIdentity, newIdentity);
				for (MapsTo mapsTo : component.getMapsTos()) {
					updateReferences(mapsTo, originalIdentity, newIdentity);
				}
				for (Location sourceLocation : component.getSourceLocations()) {
					if (sourceLocation.isSetSequence() && sourceLocation.getSequenceURI().equals(originalIdentity)) {
						sourceLocation.setSequence(newIdentity);
					}
				}
			}
			for (SequenceAnnotation sa : componentDefinition.getSequenceAnnotations()) {
				for (Location loc : sa.getLocations()) {
					if (loc.isSetSequence() && loc.getSequenceURI().equals(originalIdentity)) {
						loc.setSequence(newIdentity);
					}
					updateReferences(loc, originalIdentity, newIdentity);
				}
				updateReferences(sa, originalIdentity, newIdentity);
			}
			for (SequenceConstraint sc : componentDefinition.getSequenceConstraints()) {
				updateReferences(sc, originalIdentity, newIdentity);
			}
		}
		for (ModuleDefinition moduleDefinition : getModuleDefinitions()) {
			updateReferences(moduleDefinition, originalIdentity, newIdentity);
			for (FunctionalComponent functionalComponent : moduleDefinition.getFunctionalComponents()) {
				if (functionalComponent.getDefinitionURI().equals(originalIdentity)) {
					functionalComponent.setDefinition(newIdentity);
					for (MapsTo mapsTo : functionalComponent.getMapsTos()) {
						ComponentDefinition cd = getComponentDefinition(newIdentity);
						if (cd != null) {
							String displayId = URIcompliance.extractDisplayId(mapsTo.getRemoteURI());
							URI newURI = URIcompliance.createCompliantURI(cd.getPersistentIdentity().toString(),
									displayId, cd.getVersion());
							mapsTo.setRemote(newURI);
						}
					}
				}
				updateReferences(functionalComponent, originalIdentity, newIdentity);
				for (MapsTo mapsTo : functionalComponent.getMapsTos()) {
					updateReferences(mapsTo, originalIdentity, newIdentity);
				}
			}
			for (Module module : moduleDefinition.getModules()) {
				if (module.getDefinitionURI().equals(originalIdentity)) {
					module.setDefinition(newIdentity);
					for (MapsTo mapsTo : module.getMapsTos()) {
						ModuleDefinition md = getModuleDefinition(newIdentity);
						if (md != null) {
							String displayId = URIcompliance.extractDisplayId(mapsTo.getRemoteURI());
							URI newURI = URIcompliance.createCompliantURI(md.getPersistentIdentity().toString(),
									displayId, md.getVersion());
							mapsTo.setRemote(newURI);
						}
					}
				}
				updateReferences(module, originalIdentity, newIdentity);
				for (MapsTo mapsTo : module.getMapsTos()) {
					updateReferences(mapsTo, originalIdentity, newIdentity);
				}
			}
			for (Interaction interaction : moduleDefinition.getInteractions()) {
				updateReferences(interaction, originalIdentity, newIdentity);
				for (Participation participation : interaction.getParticipations()) {
					updateReferences(participation, originalIdentity, newIdentity);
				}
			}
			for (URI modelURI : moduleDefinition.getModelURIs()) {
				if (modelURI.equals(originalIdentity)) {
					moduleDefinition.removeModel(originalIdentity);
					moduleDefinition.addModel(newIdentity);
				}
			}
		}
		for (Model model : getModels()) {
			if (model.getSource().equals(originalIdentity)) {
				model.setSource(newIdentity);
			}
 			updateReferences(model, originalIdentity, newIdentity);
		}
		for (Attachment attachment : getAttachments()) {
			updateReferences(attachment, originalIdentity, newIdentity);
		}
		for (Implementation implementation : getImplementations()) {
			if (implementation.isSetBuilt() && implementation.getBuiltURI().equals(originalIdentity)) {
				implementation.setBuilt(newIdentity);
			}
			updateReferences(implementation, originalIdentity, newIdentity);
		}
		for (Sequence sequence : getSequences()) {
			updateReferences(sequence, originalIdentity, newIdentity);
		}
		for (GenericTopLevel genericTopLevel : getGenericTopLevels()) {
			updateReferences(genericTopLevel, originalIdentity, newIdentity);
		}
		for (CombinatorialDerivation combinatorialDerivation : getCombinatorialDerivations()) {
			updateReferences(combinatorialDerivation, originalIdentity, newIdentity);
			if (combinatorialDerivation.getTemplateURI().equals(originalIdentity)) {
				combinatorialDerivation.setTemplate(newIdentity);
				ComponentDefinition cd = getComponentDefinition(newIdentity);
				if (cd != null) {
					for (VariableComponent variableComponent : combinatorialDerivation.getVariableComponents()) {
						String displayId = URIcompliance.extractDisplayId(variableComponent.getVariableURI());
						URI newURI = URIcompliance.createCompliantURI(cd.getPersistentIdentity().toString(),
								displayId, cd.getVersion());
						variableComponent.setVariable(newURI);
					}
					
				}
			}
			for (VariableComponent variableComponent : combinatorialDerivation.getVariableComponents()) {
				for (URI variantURI : variableComponent.getVariantURIs()) {
					if (variantURI.equals(originalIdentity)) {
						variableComponent.removeVariant(variantURI);
						variableComponent.addVariant(newIdentity);
					}
				}
				for (URI variantCollectionURI : variableComponent.getVariantCollectionURIs()) {
					if (variantCollectionURI.equals(originalIdentity)) {
						variableComponent.removeVariantCollection(variantCollectionURI);
						variableComponent.addVariantCollection(newIdentity);
					}
				}
				for (URI variantDerivationURI : variableComponent.getVariantDerivationURIs()) {
					if (variantDerivationURI.equals(originalIdentity)) {
						variableComponent.removeVariantDerivation(variantDerivationURI);
						variableComponent.addVariantDerivation(newIdentity);
					}
				}
				updateReferences(variableComponent,originalIdentity,newIdentity);
			}
		}
		for (Activity activity : getActivities()) {
			updateReferences(activity, originalIdentity, newIdentity);
			for (Association association : activity.getAssociations()) {
				if (association.getAgentURI().equals(originalIdentity)) {
					association.setAgent(newIdentity);
				}
				if (association.isSetPlan() && 
						association.getPlanURI().equals(originalIdentity)) {
					association.setPlan(newIdentity);
				}
				updateReferences(association, originalIdentity, newIdentity);
			}
			for (Usage usage : activity.getUsages()) {
				if (usage.getEntityURI().equals(originalIdentity)) {
					usage.setEntity(newIdentity);
				}
				updateReferences(usage, originalIdentity, newIdentity);
			}
		}
		for (Agent agent : getAgents()) {
			updateReferences(agent, originalIdentity, newIdentity);
		}
		for (Plan plan : getPlans()) {
			updateReferences(plan, originalIdentity, newIdentity);
		}
	}

	private void updateReferences(List<Annotation> annotations, HashMap<URI, URI> uriMap)
			throws SBOLValidationException {
		for (Annotation annotation : annotations) {
			if (annotation.isURIValue()) {
				if (uriMap.get(annotation.getURIValue()) != null) {
					annotation.setURIValue(uriMap.get(annotation.getURIValue()));
				}
			} else if (annotation.isNestedAnnotations()) {
				updateReferences(annotation.getAnnotations(), uriMap);
			}
		}
	}

	private void updateReferences(Identified identified, HashMap<URI, URI> uriMap) throws SBOLValidationException {
		updateReferences(identified.getAnnotations(), uriMap);
	}

	private void updateReferences(HashMap<URI, URI> uriMap) throws SBOLValidationException {
		for (TopLevel topLevel : getTopLevels()) {
			for (URI wasDerivedFrom : topLevel.getWasDerivedFroms()) {
				if (uriMap.get(wasDerivedFrom) != null) {
					topLevel.removeWasDerivedFrom(wasDerivedFrom);
					topLevel.addWasDerivedFrom(uriMap.get(wasDerivedFrom));	
				}
			}
			for (URI wasGeneratedBy : topLevel.getWasGeneratedBys()) {
				if (uriMap.get(wasGeneratedBy) != null) {
					topLevel.removeWasGeneratedBy(wasGeneratedBy);
					topLevel.addWasGeneratedBy(uriMap.get(wasGeneratedBy));	
				}
			}
			for (URI attachmentURI : topLevel.getAttachmentURIs()) {
				if (uriMap.get(attachmentURI) != null) {
					topLevel.removeAttachment(attachmentURI);
					topLevel.addAttachment(uriMap.get(attachmentURI));	
				}
			}
		}
		for (Collection collection : getCollections()) {
			for (URI memberURI : collection.getMemberURIs()) {
				if (uriMap.get(memberURI) != null) {
					collection.removeMember(memberURI);
					collection.addMember(uriMap.get(memberURI));
				}
			}
			updateReferences(collection, uriMap);
		}
		for (Experiment experiment : getExperiments()) {
			for (URI experimentalDatumURI : experiment.getExperimentalDataURIs()) {
				if (uriMap.get(experimentalDatumURI) != null) {
					experiment.removeExperimentalData(experimentalDatumURI);
					experiment.addExperimentalData(uriMap.get(experimentalDatumURI));
				}
			}
			updateReferences(experiment, uriMap);
		}
		for (ComponentDefinition componentDefinition : getComponentDefinitions()) {
			updateReferences(componentDefinition, uriMap);
			for (URI sequenceURI : componentDefinition.getSequenceURIs()) {
				if (uriMap.get(sequenceURI) != null) {
					componentDefinition.removeSequence(sequenceURI);
					componentDefinition.addSequence(uriMap.get(sequenceURI));
				}
			}
			for (Component component : componentDefinition.getComponents()) {
				if (uriMap.get(component.getDefinitionURI()) != null) {
					component.setDefinition(uriMap.get(component.getDefinitionURI()));
					for (MapsTo mapsTo : component.getMapsTos()) {
						ComponentDefinition cd = getComponentDefinition(component.getDefinitionURI());
						if (cd != null) {
							String displayId = URIcompliance.extractDisplayId(mapsTo.getRemoteURI());
							URI newURI = URIcompliance.createCompliantURI(cd.getPersistentIdentity().toString(),
									displayId, cd.getVersion());
							mapsTo.setRemote(newURI);
						}
					}
				}
				updateReferences(component, uriMap);
				for (MapsTo mapsTo : component.getMapsTos()) {
					updateReferences(mapsTo, uriMap);
				}
				for (Location sourceLocation : component.getSourceLocations()) {
					if (sourceLocation.isSetSequence() && uriMap.get(sourceLocation.getSequenceURI())!=null) {
						sourceLocation.setSequence(uriMap.get(sourceLocation.getSequenceURI()));
					}
				}
			}
			for (SequenceAnnotation sa : componentDefinition.getSequenceAnnotations()) {
				for (Location loc : sa.getLocations()) {
					if (loc.isSetSequence() && uriMap.get(loc.getSequenceURI())!=null) {
						loc.setSequence(uriMap.get(loc.getSequenceURI()));
					}
					updateReferences(loc, uriMap);
				}
				updateReferences(sa, uriMap);
			}
			for (SequenceConstraint sc : componentDefinition.getSequenceConstraints()) {
				updateReferences(sc, uriMap);
			}
		}
		for (ModuleDefinition moduleDefinition : getModuleDefinitions()) {
			updateReferences(moduleDefinition, uriMap);
			for (FunctionalComponent functionalComponent : moduleDefinition.getFunctionalComponents()) {
				if (uriMap.get(functionalComponent.getDefinitionURI()) != null) {
					functionalComponent.setDefinition(uriMap.get(functionalComponent.getDefinitionURI()));
					for (MapsTo mapsTo : functionalComponent.getMapsTos()) {
						ComponentDefinition cd = getComponentDefinition(functionalComponent.getDefinitionURI());
						if (cd != null) {
							String displayId = URIcompliance.extractDisplayId(mapsTo.getRemoteURI());
							URI newURI = URIcompliance.createCompliantURI(cd.getPersistentIdentity().toString(),
									displayId, cd.getVersion());
							mapsTo.setRemote(newURI);
						}
					}
				}
				updateReferences(functionalComponent, uriMap);
				for (MapsTo mapsTo : functionalComponent.getMapsTos()) {
					updateReferences(mapsTo, uriMap);
				}
			}
			for (Module module : moduleDefinition.getModules()) {
				if (uriMap.get(module.getDefinitionURI()) != null) {
					module.setDefinition(uriMap.get(module.getDefinitionURI()));
					for (MapsTo mapsTo : module.getMapsTos()) {
						ModuleDefinition md = getModuleDefinition(module.getDefinitionURI());
						if (md != null) {
							String displayId = URIcompliance.extractDisplayId(mapsTo.getRemoteURI());
							URI newURI = URIcompliance.createCompliantURI(md.getPersistentIdentity().toString(),
									displayId, md.getVersion());
							mapsTo.setRemote(newURI);
						}
					}
				}
				updateReferences(module, uriMap);
				for (MapsTo mapsTo : module.getMapsTos()) {
					updateReferences(mapsTo, uriMap);
				}
			}
			for (Interaction interaction : moduleDefinition.getInteractions()) {
				updateReferences(interaction, uriMap);
				for (Participation participation : interaction.getParticipations()) {
					updateReferences(participation, uriMap);
				}
			}
			for (URI modelURI : moduleDefinition.getModelURIs()) {
				if (uriMap.get(modelURI) != null) {
					moduleDefinition.removeModel(modelURI);
					moduleDefinition.addModel(uriMap.get(modelURI));
				}
			}
		}
		for (Model model : getModels()) {
			if (uriMap.get(model.getSource()) != null) {
				model.setSource(uriMap.get(model.getSource()));
			}
			updateReferences(model, uriMap);
		}
		for (Sequence sequence : getSequences()) {
			updateReferences(sequence, uriMap);
		}
		for (Attachment attachment : getAttachments()) {
			updateReferences(attachment, uriMap);
		}
		for (Implementation implementation : getImplementations()) {
			if (implementation.isSetBuilt()) {
				URI built = implementation.getBuiltURI();
				if (uriMap.get(built) != null) {
					implementation.setBuilt(uriMap.get(built));
				}
			}
			updateReferences(implementation, uriMap);
		}
		for (GenericTopLevel genericTopLevel : getGenericTopLevels()) {
			updateReferences(genericTopLevel, uriMap);
		}
		for (CombinatorialDerivation combinatorialDerivation : getCombinatorialDerivations()) {
			updateReferences(combinatorialDerivation, uriMap);
			if (uriMap.get(combinatorialDerivation.getTemplateURI())!=null) {
				combinatorialDerivation.setTemplate(uriMap.get(combinatorialDerivation.getTemplateURI()));
				ComponentDefinition cd = getComponentDefinition(combinatorialDerivation.getTemplateURI());
				if (cd != null) {
					for (VariableComponent variableComponent : combinatorialDerivation.getVariableComponents()) {
						String displayId = URIcompliance.extractDisplayId(variableComponent.getVariableURI());
						URI newURI = URIcompliance.createCompliantURI(cd.getPersistentIdentity().toString(),
								displayId, cd.getVersion());
						variableComponent.setVariable(newURI);
					}
					
				}
			}
			for (VariableComponent variableComponent : combinatorialDerivation.getVariableComponents()) {
				for (URI variantURI : variableComponent.getVariantURIs()) {
					if (uriMap.get(variantURI) != null) {
						variableComponent.removeVariant(variantURI);
						variableComponent.addVariant(uriMap.get(variantURI));
					}
				}
				for (URI variantCollectionURI : variableComponent.getVariantCollectionURIs()) {
					if (uriMap.get(variantCollectionURI) != null) {
						variableComponent.removeVariantCollection(variantCollectionURI);
						variableComponent.addVariantCollection(uriMap.get(variantCollectionURI));
					}
				}
				for (URI variantDerivationURI : variableComponent.getVariantDerivationURIs()) {
					if (uriMap.get(variantDerivationURI) != null) {
						variableComponent.removeVariantDerivation(variantDerivationURI);
						variableComponent.addVariantDerivation(uriMap.get(variantDerivationURI));
					}
				}
				updateReferences(variableComponent,uriMap);
			}
		}
		for (Activity activity : getActivities()) {
			updateReferences(activity, uriMap);
			for (Association association : activity.getAssociations()) {
				if (uriMap.get(association.getAgentURI()) != null) {
					association.setAgent(uriMap.get(association.getAgentURI()));
				}
				if (uriMap.get(association.getPlanURI()) != null) {
					association.setPlan(uriMap.get(association.getPlanURI()));
				}
				updateReferences(association, uriMap);
			}
			for (Usage usage : activity.getUsages()) {
				if (uriMap.get(usage.getEntityURI()) != null) {
					usage.setEntity(uriMap.get(usage.getEntityURI()));
				}
				updateReferences(usage, uriMap);
			}
		}
		for (Agent agent : getAgents()) {
			updateReferences(agent, uriMap);
		}
		for (Plan plan : getPlans()) {
			updateReferences(plan, uriMap);
		}
	}

	private void changeURIPrefixVersion(List<Annotation> annotations, String URIPrefix, String version,
			String documentURIPrefix) throws SBOLValidationException {
		for (Annotation annotation : annotations) {
			if (annotation.isURIValue()) {
				TopLevel topLevel = getTopLevel(annotation.getURIValue());
				if (topLevel != null) {
					annotation.setURIValue(URIcompliance.createCompliantURI(URIPrefix,
							topLevel.getDisplayId() != null ? topLevel.getDisplayId()
									: URIcompliance.extractDisplayId(topLevel.getIdentity()),
							version != null ? version : topLevel.getVersion()));
				}
			} else if (annotation.isNestedAnnotations()) {
				URI nestedURI = annotation.getNestedIdentity();
				URI newURI;
				if (nestedURI.toString().startsWith(URIPrefix)) {
					newURI = URI.create(nestedURI.toString() + "/" + version);
				} else {
					String displayId = nestedURI.toString().replace(documentURIPrefix, "").replace("http://", "")
							.replace("https://", "");
					displayId = displayId.replace("/", "_");
					newURI = URI.create(URIPrefix + displayId + "/" + version);
				}
				annotation.setNestedIdentity(newURI);
				List<Annotation> nestedAnnotations = annotation.getAnnotations();
				changeURIPrefixVersion(nestedAnnotations, URIPrefix, version, documentURIPrefix);
				annotation.setAnnotations(nestedAnnotations);
			}
		}
	}

	private void changeURIPrefixVersion(Identified identified, String URIPrefix, String version,
			String documentURIPrefix) throws SBOLValidationException {
		if (URIPrefix == null) {
			URIPrefix = extractURIprefix(identified.getIdentity());
			URIPrefix = URIcompliance.checkURIprefix(URIPrefix);
		}
		changeURIPrefixVersion(identified.getAnnotations(), URIPrefix, version, documentURIPrefix);
	}

	/**
	 * Copy all objects to an a new SBOL Document and change the URI prefix/version
	 * of each object
	 * 
	 * @param URIPrefix
	 *            new URI prefix
	 * @param version
	 *            new version for all objects
	 * @param defaultVersion 
	 *            if version is null, then this version is used for only for objects without a version
	 * @return new SBOL document with changed URI prefix
	 * @throws SBOLValidationException
	 *             if URIprefix or version provided is invalid
	 */
	public SBOLDocument changeURIPrefixVersion(String URIPrefix, String version, String defaultVersion) throws SBOLValidationException {
		SBOLDocument document = new SBOLDocument();
		SBOLDocument fixed = new SBOLDocument();
		fixed.setDefaultURIprefix(URIPrefix);
		this.fixDocumentURIPrefix();
		fixed.createCopy(this);
		String documentURIPrefix = fixed.extractDocumentURIPrefix();
		HashMap<URI, URI> uriMap = new HashMap<URI, URI>();
		for (TopLevel topLevel : fixed.getTopLevels()) {
			String newVersion = version;
			if (newVersion==null) {
				if (topLevel.isSetVersion()) {
					newVersion = topLevel.getVersion();
				} else {
					newVersion = defaultVersion;
				}
			}
			fixed.rename(topLevel, URIPrefix, null, newVersion, false);
			TopLevel newTL = document.createCopy(topLevel, URIPrefix, null, newVersion);
			uriMap.put(topLevel.getIdentity(), newTL.getIdentity());
			if (!topLevel.getIdentity().equals(topLevel.getPersistentIdentity())) {
				// TODO: This means persistent identity references change to actual identity references,
				// This was needed for SBH, but is it okay in general
				uriMap.put(topLevel.getPersistentIdentity(), newTL.getIdentity());
			}
		}
		document.updateReferences(uriMap);
		for (TopLevel topLevel : document.getTopLevels()) {
			// TODO: this is stop-gap, needs to be over all identified
			String newVersion = version;
			if (newVersion==null) {
				if (topLevel.isSetVersion()) {
					newVersion = topLevel.getVersion();
				} else {
					newVersion = defaultVersion;
				}
			}
			document.changeURIPrefixVersion(topLevel, URIPrefix, newVersion, documentURIPrefix);
		}
		document.setDefaultURIprefix(URIPrefix);
		return document;
	}
	
	private TopLevel rename(TopLevel topLevel, String URIprefix, String displayId, String version, boolean updateRefs)
			throws SBOLValidationException {
		if ((URIprefix == null || URIprefix.equals(URIcompliance.extractURIprefix(topLevel.getIdentity())))
				&& (displayId == null || displayId.equals(topLevel.getDisplayId()))
				&& (version == null || version.equals(topLevel.getVersion()))) {
			return topLevel;
		}
		TopLevel renamedTopLevel = createCopy(topLevel, URIprefix, displayId, version);
		removeTopLevel(topLevel);
		if (updateRefs) {
			updateReferences(topLevel.getIdentity(), renamedTopLevel.getIdentity());
			// TODO: should this be changing to newTopLevel.getIdentity(), rather than persistent identity
			updateReferences(topLevel.getPersistentIdentity(), renamedTopLevel.getPersistentIdentity());
		}
		return renamedTopLevel;
	}

	/**
	 * Renames the given top-level's URI prefix, display ID, and version with the
	 * given ones.
	 * <p>
	 * This method first calls {@link #createCopy(TopLevel, String, String, String)}
	 * to make a copy of the given top-level with the URI prefix, display ID, and
	 * version. It then removes the given top-level and then returns the
	 * newly-copied top-level.
	 *
	 * @param topLevel
	 *            the top-level to be renamed
	 * @param URIprefix
	 *            the given URI prefix to be rename to
	 * @param displayId
	 *            the given display ID to be renamed to
	 * @param version
	 *            the given version to be renamed to
	 * @return the renamed top-level
	 * @throws SBOLValidationException
	 *             if either of the following conditions is satisfied:
	 *             <ul>
	 *             <li>any of the following SBOL validation rules was violated:
	 *             10513, 10604, 11608, 11703, 12103; or</li>
	 *             <li>an SBOL validation rule violation occurred in the following
	 *             method:
	 *             {@link #createCopy(TopLevel, String, String, String)}.</li>
	 *             </ul>
	 */
	public TopLevel rename(TopLevel topLevel, String URIprefix, String displayId, String version)
			throws SBOLValidationException {
		return rename(topLevel,URIprefix,displayId,version,true);
	}

	/**
	 * Appends the specified {@code sequence} object to the end of the list of
	 * sequences.
	 *
	 * @param sequence
	 *            The given sequence to be added
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #addTopLevel(TopLevel, Map, String, Map...)}.
	 */
	void addSequence(Sequence sequence) throws SBOLValidationException {
		addTopLevel(sequence, sequences, "sequence", collections, componentDefinitions, experiments, experimentalData, genericTopLevels, activities,
				plans, agents, models, moduleDefinitions, combinatorialDerivations, implementations, attachments);
	}

	/**
	 * Removes the given sequence from this SBOL document's list of sequences.
	 *
	 * @param sequence
	 *            the given sequence to be removed
	 * @return {@code true} if the given sequence was successfully removed,
	 *         {@code false} otherwise
	 * @throws SBOLValidationException
	 *             if either of the following SBOL validation rules was violated:
	 *             10513, 12103.
	 */
	public boolean removeSequence(Sequence sequence) throws SBOLValidationException {
		if (complete) {
			for (ComponentDefinition cd : componentDefinitions.values()) {
				if (cd.containsSequence(sequence.getIdentity())) {
					throw new SBOLValidationException("sbol-10513", cd);
				}
			}
		}
		return removeTopLevel(sequence, sequences);
	}

	/**
	 * Returns the sequence matching the given display ID and version from this SBOL
	 * document's list of sequences.
	 * <p>
	 * This method first creates a compliant URI for the sequence to be retrieved.
	 * It starts with the given URI prefix after its been successfully validated,
	 * optionally followed by its type, namely {@link TopLevel#SEQUENCE}, followed
	 * by the given display ID, and ends with the given version. This URI is used to
	 * look up the sequence in this SBOL document.
	 *
	 * @param displayId
	 *            an intermediate between name and identity that is machine-readable
	 * @param version
	 *            The given version for this object
	 * @return the matching sequence if present, or {@code null} otherwise.
	 */
	public Sequence getSequence(String displayId, String version) {
		try {
			return getSequence(
					createCompliantURI(defaultURIprefix, TopLevel.SEQUENCE, displayId, version, typesInURIs));
		} catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the sequence matching the given {@code modelURI} from this SBOL
	 * document's list of sequences.
	 *
	 * @param sequenceURI
	 *            takes the given SequenceURI to retrieve the sequence from this
	 *            SBOL document
	 * @return the matching sequence if present, or {@code null} otherwise.
	 */
	public Sequence getSequence(URI sequenceURI) {
		Sequence sequence = sequences.get(sequenceURI);
		if (sequence == null) {
			for (SynBioHubFrontend frontend : getRegistries()) {
				try {
					SBOLDocument document = frontend.getSBOL(sequenceURI);
					if (document != null) {
						sequence = document.getSequence(sequenceURI);
						createCopy(document);
					}
				} catch (SynBioHubException | SBOLValidationException e) {
				}
			}
		}
		return sequence;
	}

	/**
	 * Returns the set of sequences owned by this SBOL document.
	 *
	 * @return the set of sequences owned by this SBOL document.
	 */
	public Set<Sequence> getSequences() {
		// return (List<Structure>) structures.values();
		Set<Sequence> structures = new HashSet<>();
		structures.addAll(this.sequences.values());
		return structures;
	}

	/**
	 * Removes all entries in the list of sequences owned by this SBOL document. The
	 * list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeSequence(Sequence)} to iteratively remove
	 * each sequence.
	 * 
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #removeSequence(Sequence)}.
	 */
	public void clearSequences() throws SBOLValidationException {
		Object[] valueSetArray = sequences.values().toArray();
		for (Object sequence : valueSetArray) {
			removeSequence((Sequence) sequence);
		}
	}

	/**
	 * Creates a generic top-level, and then adds it to this SBOL document's list of
	 * generic top-levels.
	 * <p>
	 * This method calls
	 * {@link #createGenericTopLevel(String, String, String, QName)} with the
	 * default URI prefix of this SBOL document, the given component definition
	 * display ID, an empty version string, and the given RDF type.
	 *
	 * @param displayId
	 *            the display ID of the generic top-level to be created
	 * @param rdfType
	 *            the type of the generic top-level to be created
	 * @return the created generic top-level
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createGenericTopLevel(String, String, String, QName)}.
	 */
	public GenericTopLevel createGenericTopLevel(String displayId, QName rdfType) throws SBOLValidationException {
		return createGenericTopLevel(defaultURIprefix, displayId, "", rdfType);
	}

	/**
	 * Creates a generic top-level, and then adds it to this SBOL document's list of
	 * generic top-levels.
	 * <p>
	 * This method calls
	 * {@link #createGenericTopLevel(String, String, String, QName)} with the
	 * default URI prefix of this SBOL document, the given component definition
	 * display ID and version, and the given RDF type.
	 *
	 * @param displayId
	 *            the display ID of the generic top-level to be created
	 * @param version
	 *            the version of the generic top-level to be created
	 * @param rdfType
	 *            the type of the generic top-level to be created
	 * @return the created generic top-level
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createGenericTopLevel(String, String, String, QName)}.
	 */
	public GenericTopLevel createGenericTopLevel(String displayId, String version, QName rdfType)
			throws SBOLValidationException {
		return createGenericTopLevel(defaultURIprefix, displayId, version, rdfType);
	}

	/**
	 * Creates a generic top-level, and then adds it to this SBOL document's list of
	 * generic top-levels.
	 * <p>
	 * This method calls
	 * {@link #createGenericTopLevel(String, String, String, QName)} with the
	 * default URI prefix of this SBOL document, the given component definition
	 * display ID and version, and the given RDF type.
	 * 
	 * @param URIprefix
	 *            the given URI prefix used to create a compliant URI for the
	 *            generic top-level to be created
	 * @param displayId
	 *            the display ID of the generic top-level to be created
	 * @param version
	 *            the version of the generic top-level to be created
	 * @param rdfType
	 *            the type of the generic top-level to be created
	 * @return the created generic top-level
	 * @throws SBOLValidationException
	 *             if an SBOL validation rules was violated: 10201, 10202, 10204,
	 *             10206, 10220, 10303, 10304, 10305, 10401, 10501, 10701, 10801,
	 *             10901, 11101, 11201, 11301, 11401, 11501, 11601, 11701, 11801,
	 *             11901, 12001, 12301, 12302.
	 */
	public GenericTopLevel createGenericTopLevel(String URIprefix, String displayId, String version, QName rdfType)
			throws SBOLValidationException {
		URIprefix = URIcompliance.checkURIprefix(URIprefix);
		if (rdfType.getNamespaceURI().equals(Sbol2Terms.sbol2
				.getNamespaceURI())/*
									 * || rdfType.getNamespaceURI().equals(Sbol1Terms.sbol1.getNamespaceURI())
									 */) {
			throw new SBOLValidationException("sbol-12302");
		}
		// QName qNameInNamespace = getNamespace(URI.create(rdfType.getNamespaceURI()));
		// if (qNameInNamespace==null ||
		// rdfType.getPrefix()!=qNameInNamespace.getPrefix()) {
		// addNamespace(URI.create(rdfType.getNamespaceURI()), rdfType.getPrefix());
		// }
		GenericTopLevel g = new GenericTopLevel(
				createCompliantURI(URIprefix, TopLevel.GENERIC_TOP_LEVEL, displayId, version, typesInURIs), rdfType);
		g.setPersistentIdentity(createCompliantURI(URIprefix, TopLevel.GENERIC_TOP_LEVEL, displayId, "", typesInURIs));
		g.setDisplayId(displayId);
		g.setVersion(version);
		addGenericTopLevel(g);
		return g;
	}

	/**
	 * Appends the specified {@code genericTopLevel} object to the end of the list
	 * of generic top levels.
	 *
	 * @param genericTopLevel
	 *            Adds the given TopLevel object to this document
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #addTopLevel(TopLevel, Map, String, Map...)}.
	 */
	void addGenericTopLevel(GenericTopLevel genericTopLevel) throws SBOLValidationException {
		QName qNameInNamespace = getNamespace(URI.create(genericTopLevel.getRDFType().getNamespaceURI()));
		if (qNameInNamespace == null) {
			String prefix = genericTopLevel.getRDFType().getPrefix();
			if (getNamespace(prefix) != null) {
				prefix = getNamespacePrefix(URI.create(genericTopLevel.getRDFType().getNamespaceURI()));
				genericTopLevel.setRDFType(new QName(genericTopLevel.getRDFType().getNamespaceURI(),
						genericTopLevel.getRDFType().getLocalPart(), prefix));
			} else {
				addNamespace(URI.create(genericTopLevel.getRDFType().getNamespaceURI()),
						genericTopLevel.getRDFType().getPrefix());
			}
		} else if (genericTopLevel.getRDFType().getPrefix() != qNameInNamespace.getPrefix()) {
			genericTopLevel.setRDFType(new QName(genericTopLevel.getRDFType().getNamespaceURI(),
					genericTopLevel.getRDFType().getLocalPart(), qNameInNamespace.getPrefix()));
		}
		addTopLevel(genericTopLevel, genericTopLevels, "genericTopLevel", collections, componentDefinitions, experiments, experimentalData, models,
				activities, plans, agents, moduleDefinitions, sequences, combinatorialDerivations, implementations, attachments);
	}

	/**
	 * Removes the given generic top-level from this SBOL document's list of generic
	 * top-levels.
	 *
	 * @param genericTopLevel
	 *            the given generic top-level to be removed
	 * @return {@code true} if the given generic top-level was successfully removed,
	 *         {@code false} otherwise
	 * @throws SBOLValidationException
	 *             if the following SBOL validation rule was violated: 12103.
	 */
	public boolean removeGenericTopLevel(GenericTopLevel genericTopLevel) throws SBOLValidationException {
		return removeTopLevel(genericTopLevel, genericTopLevels);
	}

	/**
	 * Returns the generic top-level matching the given display ID and version from
	 * this SBOL document's list of generic top-levels.
	 * <p>
	 * A compliant generic top-level URI is created first. It starts with this SBOL
	 * document's default URI prefix after its been successfully validated,
	 * optionally followed by its type, namely {@link TopLevel#GENERIC_TOP_LEVEL},
	 * followed by the given display ID, and ends with the given version. This URI
	 * is used to look up the generic top-level in this SBOL document.
	 *
	 * @param displayId
	 *            the display ID of the generic top-level to be retrieved
	 * @param version
	 *            the version of the generic top-level to be retrieved
	 * @return the matching generic top-level if present, or {@code null} otherwise.
	 */
	public GenericTopLevel getGenericTopLevel(String displayId, String version) {
		try {
			return getGenericTopLevel(
					createCompliantURI(defaultURIprefix, TopLevel.GENERIC_TOP_LEVEL, displayId, version, typesInURIs));
		} catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the generic top-level matching the given display identity URI from
	 * this SBOL document's list of generic top-levels.
	 *
	 * @param genericTopLevelURI
	 *            the identity URI of the top-level to be retrieved
	 * @return the matching generic top-level if present, or {@code null} otherwise.
	 */
	public GenericTopLevel getGenericTopLevel(URI genericTopLevelURI) {
		GenericTopLevel genericTopLevel = genericTopLevels.get(genericTopLevelURI);
		if (genericTopLevel == null) {
			for (SynBioHubFrontend frontend : getRegistries()) {
				try {
					SBOLDocument document = frontend.getSBOL(genericTopLevelURI);
					if (document != null) {
						genericTopLevel = document.getGenericTopLevel(genericTopLevelURI);
						createCopy(document);
					}
				} catch (SynBioHubException | SBOLValidationException e) {
				}
			}
		}
		return genericTopLevel;
	}

	/**
	 * Returns the set of generic top-levels owned by this SBOL document.
	 *
	 * @return the set of generic top-levels owned by this SBOL document.
	 */
	public Set<GenericTopLevel> getGenericTopLevels() {
		// return (List<GenericTopLevel>) topLevels.values();
		Set<GenericTopLevel> topLevels = new HashSet<>();
		topLevels.addAll(this.genericTopLevels.values());
		return topLevels;
	}

	/**
	 * Removes all entries in the list of generic top-levels owned by this SBOL
	 * document. The list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeGenericTopLevel(GenericTopLevel)} to
	 * iteratively remove each generic top-level.
	 * 
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #removeGenericTopLevel(GenericTopLevel)}.
	 */
	public void clearGenericTopLevels() throws SBOLValidationException {
		Object[] valueSetArray = genericTopLevels.values().toArray();
		for (Object genericTopLevel : valueSetArray) {
			removeGenericTopLevel((GenericTopLevel) genericTopLevel);
		}
	}

	/**
	 * Creates an activity, and then adds it to this SBOL document's list of
	 * activities.
	 * <p>
	 * This method calls {@link #createActivity(String, String, String)} with the
	 * default URI prefix of this SBOL document, the given display ID, and an empty
	 * version string.
	 *
	 * @param displayId
	 *            the display ID of the activity to be created
	 * @return the created activity
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createActivity(String, String, String)}.
	 */
	public Activity createActivity(String displayId) throws SBOLValidationException {
		return createActivity(defaultURIprefix, displayId, "");
	}

	/**
	 * Creates an activity, and then adds it to this SBOL document's list of
	 * activities.
	 * <p>
	 * This method calls {@link #createActivity(String, String, String)} with the
	 * default URI prefix of this SBOL document, the given display ID, and version.
	 *
	 * @param displayId
	 *            the display ID of the activity to be created
	 * @param version
	 *            the version of the activity to be created
	 * @return the created activity
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createActivity(String, String, String)}.
	 */
	public Activity createActivity(String displayId, String version) throws SBOLValidationException {
		return createActivity(defaultURIprefix, displayId, version);
	}

	/**
	 * Creates an activity, and then adds it to this SBOL document's list of
	 * activities.
	 * <p>
	 * This method calls {@link #createActivity(String, String, String)} with the
	 * default URI prefix of this SBOL document, the given display ID, and version.
	 * 
	 * @param URIprefix
	 *            the given URI prefix used to create a compliant URI for the
	 *            activity to be created
	 * @param displayId
	 *            the display ID of the activity to be created
	 * @param version
	 *            the version of the activity to be created
	 * @return the created activity
	 * @throws SBOLValidationException
	 *             if an SBOL validation rules was violated: 10201, 10202, 10204,
	 *             10206, 10220.
	 */
	public Activity createActivity(String URIprefix, String displayId, String version) throws SBOLValidationException {
		URIprefix = URIcompliance.checkURIprefix(URIprefix);
		Activity g = new Activity(createCompliantURI(URIprefix, TopLevel.ACTIVITY, displayId, version, typesInURIs));
		g.setPersistentIdentity(createCompliantURI(URIprefix, TopLevel.ACTIVITY, displayId, "", typesInURIs));
		g.setDisplayId(displayId);
		g.setVersion(version);
		addActivity(g);
		return g;
	}

	/**
	 * Appends the specified {@code activity} object to the end of the list of
	 * activity top levels.
	 *
	 * @param activity
	 *            Adds the given Activity object to this document
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #addTopLevel(TopLevel, Map, String, Map...)}.
	 */
	void addActivity(Activity activity) throws SBOLValidationException {
		addTopLevel(activity, activities, "activity", collections, componentDefinitions, experiments, experimentalData, models, genericTopLevels,
				plans, agents, moduleDefinitions, sequences, combinatorialDerivations, implementations, attachments);
		for (Usage usage : activity.getUsages()) {
			usage.setSBOLDocument(this);
		}
		for (Association association: activity.getAssociations()) {
			association.setSBOLDocument(this);
		}
	}

	/**
	 * Removes the given activity from this SBOL document's list of activities.
	 *
	 * @param activity
	 *            the given activity to be removed
	 * @return {@code true} if the given activity was successfully removed,
	 *         {@code false} otherwise
	 * @throws SBOLValidationException
	 *             if the following SBOL validation rule was violated: ?????.
	 */
	public boolean removeActivity(Activity activity) throws SBOLValidationException {
		return removeTopLevel(activity, activities);
	}

	/**
	 * Returns the activity matching the given display ID and version from this SBOL
	 * document's list of activities.
	 * <p>
	 * A compliant activity URI is created first. It starts with this SBOL
	 * document's default URI prefix after its been successfully validated,
	 * optionally followed by its type, namely {@link TopLevel#ACTIVITY}, followed
	 * by the given display ID, and ends with the given version. This URI is used to
	 * look up the activity in this SBOL document.
	 *
	 * @param displayId
	 *            the display ID of the activity to be retrieved
	 * @param version
	 *            the version of the activity to be retrieved
	 * @return the matching activity if present, or {@code null} otherwise.
	 */
	public Activity getActivity(String displayId, String version) {
		try {
			return getActivity(
					createCompliantURI(defaultURIprefix, TopLevel.ACTIVITY, displayId, version, typesInURIs));
		} catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the activity matching the given display identity URI from this SBOL
	 * document's list of activities.
	 *
	 * @param activityURI
	 *            the identity URI of the top-level to be retrieved
	 * @return the matching activity if present, or {@code null} otherwise.
	 */
	public Activity getActivity(URI activityURI) {
		Activity activity = activities.get(activityURI);
		if (activity == null) {
			for (SynBioHubFrontend frontend : getRegistries()) {
				try {
					SBOLDocument document = frontend.getSBOL(activityURI);
					if (document != null) {
						activity = document.getActivity(activityURI);
						createCopy(document);
					}
				} catch (SynBioHubException | SBOLValidationException e) {
					activity = null;
				}
			}
		}
		return activity;
	}

	/**
	 * Returns the set of activities owned by this SBOL document.
	 *
	 * @return the set of activities owned by this SBOL document.
	 */
	public Set<Activity> getActivities() {
		Set<Activity> topLevels = new HashSet<>();
		topLevels.addAll(this.activities.values());
		return topLevels;
	}

	/**
	 * Removes all entries in the list of activities owned by this SBOL document.
	 * The list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeActivity(Activity)} to iteratively remove
	 * each activity.
	 * 
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #removeActivity(Activity)}.
	 */
	public void clearActivitys() throws SBOLValidationException {
		Object[] valueSetArray = activities.values().toArray();
		for (Object activity : valueSetArray) {
			removeActivity((Activity) activity);
		}
	}

	/**
	 * Creates an agent, and then adds it to this SBOL document's list of agents.
	 * <p>
	 * This method calls {@link #createAgent(String, String, String)} with the
	 * default URI prefix of this SBOL document, the given display ID, and an empty
	 * version string.
	 *
	 * @param displayId
	 *            the display ID of the agent to be created
	 * @return the created agent
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createAgent(String, String, String)}.
	 */
	public Agent createAgent(String displayId) throws SBOLValidationException {
		return createAgent(defaultURIprefix, displayId, "");
	}

	/**
	 * Creates an agent, and then adds it to this SBOL document's list of agents.
	 * <p>
	 * This method calls {@link #createAgent(String, String, String)} with the
	 * default URI prefix of this SBOL document, the given display ID, and version.
	 *
	 * @param displayId
	 *            the display ID of the agent to be created
	 * @param version
	 *            the version of the agent to be created
	 * @return the created agent
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createAgent(String, String, String)}.
	 */
	public Agent createAgent(String displayId, String version) throws SBOLValidationException {
		return createAgent(defaultURIprefix, displayId, version);
	}

	/**
	 * Creates an agent, and then adds it to this SBOL document's list of agents.
	 * <p>
	 * This method calls {@link #createAgent(String, String, String)} with the
	 * default URI prefix of this SBOL document, the given display ID, and version.
	 * 
	 * @param URIprefix
	 *            the given URI prefix used to create a compliant URI for the agent
	 *            to be created
	 * @param displayId
	 *            the display ID of the agent to be created
	 * @param version
	 *            the version of the agent to be created
	 * @return the created agent
	 * @throws SBOLValidationException
	 *             if an SBOL validation rules was violated: 10201, 10202, 10204,
	 *             10206, 10220.
	 */
	public Agent createAgent(String URIprefix, String displayId, String version) throws SBOLValidationException {
		URIprefix = URIcompliance.checkURIprefix(URIprefix);
		Agent g = new Agent(createCompliantURI(URIprefix, TopLevel.AGENT, displayId, version, typesInURIs));
		g.setPersistentIdentity(createCompliantURI(URIprefix, TopLevel.AGENT, displayId, "", typesInURIs));
		g.setDisplayId(displayId);
		g.setVersion(version);
		addAgent(g);
		return g;
	}

	/**
	 * Appends the specified {@code agentTopLevel} object to the end of the list of
	 * agent top levels.
	 *
	 * @param agent
	 *            Adds the given Agent object to this document
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #addTopLevel(TopLevel, Map, String, Map...)}.
	 */
	void addAgent(Agent agent) throws SBOLValidationException {
		addTopLevel(agent, agents, "agent", collections, componentDefinitions, experiments, experimentalData, models, genericTopLevels, plans,
				activities, moduleDefinitions, sequences, combinatorialDerivations, implementations, attachments);
	}

	/**
	 * Removes the given agent from this SBOL document's list of agents.
	 *
	 * @param agent
	 *            the given agent to be removed
	 * @return {@code true} if the given agent was successfully removed,
	 *         {@code false} otherwise
	 * @throws SBOLValidationException
	 *             if the following SBOL validation rule was violated: ?????.
	 */
	public boolean removeAgent(Agent agent) throws SBOLValidationException {
		return removeTopLevel(agent, agents);
	}

	/**
	 * Returns the agent matching the given display ID and version from this SBOL
	 * document's list of agents.
	 * <p>
	 * A compliant agent URI is created first. It starts with this SBOL document's
	 * default URI prefix after its been successfully validated, optionally followed
	 * by its type, namely {@link TopLevel#AGENT}, followed by the given display ID,
	 * and ends with the given version. This URI is used to look up the agent in
	 * this SBOL document.
	 *
	 * @param displayId
	 *            the display ID of the agent to be retrieved
	 * @param version
	 *            the version of the agent to be retrieved
	 * @return the matching agent if present, or {@code null} otherwise.
	 */
	public Agent getAgent(String displayId, String version) {
		try {
			return getAgent(createCompliantURI(defaultURIprefix, TopLevel.AGENT, displayId, version, typesInURIs));
		} catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the agent matching the given display identity URI from this SBOL
	 * document's list of agents.
	 *
	 * @param agentURI
	 *            the identity URI of the top-level to be retrieved
	 * @return the matching agent if present, or {@code null} otherwise.
	 */
	public Agent getAgent(URI agentURI) {
		Agent agent = agents.get(agentURI);
		if (agent == null) {
			for (SynBioHubFrontend frontend : getRegistries()) {
				try {
					SBOLDocument document = frontend.getSBOL(agentURI);
					if (document != null) {
						agent = document.getAgent(agentURI);
						createCopy(document);
					}
				} catch (SynBioHubException | SBOLValidationException e) {
					agent = null;
				}
			}
		}
		return agent;
	}

	/**
	 * Returns the set of agents owned by this SBOL document.
	 *
	 * @return the set of agents owned by this SBOL document.
	 */
	public Set<Agent> getAgents() {
		Set<Agent> topLevels = new HashSet<>();
		topLevels.addAll(this.agents.values());
		return topLevels;
	}

	/**
	 * Removes all entries in the list of agents owned by this SBOL document. The
	 * list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeAgent(Agent)} to iteratively remove each
	 * agent.
	 * 
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #removeAgent(Agent)}.
	 */
	public void clearAgents() throws SBOLValidationException {
		Object[] valueSetArray = agents.values().toArray();
		for (Object agent : valueSetArray) {
			removeAgent((Agent) agent);
		}
	}

	/**
	 * Creates an plan, and then adds it to this SBOL document's list of plans.
	 * <p>
	 * This method calls {@link #createPlan(String, String, String)} with the
	 * default URI prefix of this SBOL document, the given display ID, and an empty
	 * version string.
	 *
	 * @param displayId
	 *            the display ID of the plan to be created
	 * @return the created plan
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createPlan(String, String, String)}.
	 */
	public Plan createPlan(String displayId) throws SBOLValidationException {
		return createPlan(defaultURIprefix, displayId, "");
	}

	/**
	 * Creates an plan, and then adds it to this SBOL document's list of plans.
	 * <p>
	 * This method calls {@link #createPlan(String, String, String)} with the
	 * default URI prefix of this SBOL document, the given display ID, and version.
	 *
	 * @param displayId
	 *            the display ID of the plan to be created
	 * @param version
	 *            the version of the plan to be created
	 * @return the created plan
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #createPlan(String, String, String)}.
	 */
	public Plan createPlan(String displayId, String version) throws SBOLValidationException {
		return createPlan(defaultURIprefix, displayId, version);
	}

	/**
	 * Creates an plan, and then adds it to this SBOL document's list of plans.
	 * <p>
	 * This method calls {@link #createPlan(String, String, String)} with the
	 * default URI prefix of this SBOL document, the given display ID, and version.
	 * 
	 * @param URIprefix
	 *            the given URI prefix used to create a compliant URI for the plan
	 *            to be created
	 * @param displayId
	 *            the display ID of the plan to be created
	 * @param version
	 *            the version of the plan to be created
	 * @return the created plan
	 * @throws SBOLValidationException
	 *             if an SBOL validation rules was violated: 10201, 10202, 10204,
	 *             10206, 10220.
	 */
	public Plan createPlan(String URIprefix, String displayId, String version) throws SBOLValidationException {
		URIprefix = URIcompliance.checkURIprefix(URIprefix);
		Plan g = new Plan(createCompliantURI(URIprefix, TopLevel.PLAN, displayId, version, typesInURIs));
		g.setPersistentIdentity(createCompliantURI(URIprefix, TopLevel.PLAN, displayId, "", typesInURIs));
		g.setDisplayId(displayId);
		g.setVersion(version);
		addPlan(g);
		return g;
	}

	/**
	 * Appends the specified {@code planTopLevel} object to the end of the list of
	 * plan top levels.
	 *
	 * @param plan
	 *            Adds the given Plan object to this document
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #addTopLevel(TopLevel, Map, String, Map...)}.
	 */
	void addPlan(Plan plan) throws SBOLValidationException {
		addTopLevel(plan, plans, "plan", collections, componentDefinitions, experiments, experimentalData, models, genericTopLevels, agents,
				activities, moduleDefinitions, sequences, combinatorialDerivations, implementations, attachments);
	}

	/**
	 * Removes the given plan from this SBOL document's list of plans.
	 *
	 * @param plan
	 *            the given plan to be removed
	 * @return {@code true} if the given plan was successfully removed,
	 *         {@code false} otherwise
	 * @throws SBOLValidationException
	 *             if the following SBOL validation rule was violated: ?????.
	 */
	public boolean removePlan(Plan plan) throws SBOLValidationException {
		return removeTopLevel(plan, plans);
	}

	/**
	 * Returns the plan matching the given display ID and version from this SBOL
	 * document's list of plans.
	 * <p>
	 * A compliant plan URI is created first. It starts with this SBOL document's
	 * default URI prefix after its been successfully validated, optionally followed
	 * by its type, namely {@link TopLevel#PLAN}, followed by the given display ID,
	 * and ends with the given version. This URI is used to look up the plan in this
	 * SBOL document.
	 *
	 * @param displayId
	 *            the display ID of the plan to be retrieved
	 * @param version
	 *            the version of the plan to be retrieved
	 * @return the matching plan if present, or {@code null} otherwise.
	 */
	public Plan getPlan(String displayId, String version) {
		try {
			return getPlan(createCompliantURI(defaultURIprefix, TopLevel.PLAN, displayId, version, typesInURIs));
		} catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the plan matching the given display identity URI from this SBOL
	 * document's list of plans.
	 *
	 * @param planURI
	 *            the identity URI of the top-level to be retrieved
	 * @return the matching plan if present, or {@code null} otherwise.
	 */
	public Plan getPlan(URI planURI) {
		Plan plan = plans.get(planURI);
		if (plan == null) {
			for (SynBioHubFrontend frontend : getRegistries()) {
				try {
					SBOLDocument document = frontend.getSBOL(planURI);
					if (document != null) {
						plan = document.getPlan(planURI);
						createCopy(document);
					}
				} catch (SynBioHubException | SBOLValidationException e) {
				}
			}
		}
		return plan;
	}

	/**
	 * Returns the set of plans owned by this SBOL document.
	 *
	 * @return the set of plans owned by this SBOL document.
	 */
	public Set<Plan> getPlans() {
		Set<Plan> topLevels = new HashSet<>();
		topLevels.addAll(this.plans.values());
		return topLevels;
	}

	/**
	 * Removes all entries in the list of plans owned by this SBOL document. The
	 * list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removePlan(Plan)} to iteratively remove each plan.
	 * 
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #removePlan(Plan)}.
	 */
	public void clearPlans() throws SBOLValidationException {
		Object[] valueSetArray = plans.values().toArray();
		for (Object plan : valueSetArray) {
			removePlan((Plan) plan);
		}
	}
	
	Identified getIdentified(URI identifiedURI) {
		Identified identified = getTopLevelLocalOnly(identifiedURI);
		return identified;
	}
	
	private TopLevel getTopLevelLocalOnly(URI topLevelURI) {
		TopLevel topLevel = collections.get(topLevelURI);
		if (topLevel != null) {
			return topLevel;
		}
		topLevel = experiments.get(topLevelURI);
		if (topLevel != null) {
			return topLevel;
		}
		topLevel = experimentalData.get(topLevelURI);
		if (topLevel != null) {
			return topLevel;
		}
		topLevel = moduleDefinitions.get(topLevelURI);
		if (topLevel != null) {
			return topLevel;
		}
		topLevel = models.get(topLevelURI);
		if (topLevel != null) {
			return topLevel;
		}
		topLevel = componentDefinitions.get(topLevelURI);
		if (topLevel != null) {
			return topLevel;
		}
		topLevel = sequences.get(topLevelURI);
		if (topLevel != null) {
			return topLevel;
		}
		topLevel = genericTopLevels.get(topLevelURI);
		if (topLevel != null) {
			return topLevel;
		}
		topLevel = activities.get(topLevelURI);
		if (topLevel != null) {
			return topLevel;
		}
		topLevel = agents.get(topLevelURI);
		if (topLevel != null) {
			return topLevel;
		}
		topLevel = plans.get(topLevelURI);
		if (topLevel != null) {
			return topLevel;
		}
		topLevel = combinatorialDerivations.get(topLevelURI);
		if (topLevel != null) {
			return topLevel;
		}
		topLevel = implementations.get(topLevelURI);
		if(topLevel != null) {
			return topLevel;
		}
		topLevel = attachments.get(topLevelURI);
		if(topLevel != null) {
			return topLevel;
		}
		return null;
	}

	/**
	 * Returns the top-level matching the given identity URI from this SBOL
	 * document's lists of top-levels.
	 *
	 * @param topLevelURI
	 *            the identity URI of the top-level to be retrieved
	 * @return the matching top-level if present, or {@code null} otherwise.
	 */
	public TopLevel getTopLevel(URI topLevelURI) {
		TopLevel topLevel = getTopLevelLocalOnly(topLevelURI);
		if (topLevel == null) {
			for (SynBioHubFrontend frontend : getRegistries()) {
				try {
					SBOLDocument document = frontend.getSBOL(topLevelURI);
					if (document != null) {
						topLevel = document.getTopLevel(topLevelURI);
						createCopy(document);
					}
				} catch (SynBioHubException | SBOLValidationException e) {
				}
			}
		} 
		return topLevel;
	}

	/**
	 * Returns the set of all top-levels owned by this SBOL document.
	 *
	 * @return the set of all top-level owned by this SBOL document
	 */
	public Set<TopLevel> getTopLevels() {
		Set<TopLevel> topLevels = new HashSet<>();
		for (Collection topLevel : collections.values()) {
			topLevels.add(topLevel);
		}
		for (Experiment topLevel : experiments.values()) {
			topLevels.add(topLevel);
		}
		for (ExperimentalData topLevel : experimentalData.values()) {
			topLevels.add(topLevel);
		}
		for (Sequence topLevel : sequences.values()) {
			topLevels.add(topLevel);
		}
		for (Model topLevel : models.values()) {
			topLevels.add(topLevel);
		}
		for (GenericTopLevel topLevel : genericTopLevels.values()) {
			topLevels.add(topLevel);
		}
		for (Activity topLevel : activities.values()) {
			topLevels.add(topLevel);
		}
		for (Agent topLevel : agents.values()) {
			topLevels.add(topLevel);
		}
		for (Plan topLevel : plans.values()) {
			topLevels.add(topLevel);
		}
		for (ComponentDefinition topLevel : componentDefinitions.values()) {
			topLevels.add(topLevel);
		}
		for (ModuleDefinition topLevel : moduleDefinitions.values()) {
			topLevels.add(topLevel);
		}
		for (CombinatorialDerivation topLevel : combinatorialDerivations.values()) {
			topLevels.add(topLevel);
		}
		for (Implementation topLevel : implementations.values()) {
			topLevels.add(topLevel);
		}
		for (Attachment topLevel : attachments.values()) {
			topLevels.add(topLevel);
		}
		return topLevels;
	}

	/**
	 * Retrieves a set of top-levels in this SBOL document whose
	 * {@code wasDerivedFrom} field matches the given one.
	 * 
	 * @param wasDerivedFrom
	 *            the {@code wasDerivedFrom} field of which all matching top-levels
	 *            to be retrieved
	 * @return a set of top-levels whose with the matching {@code wasDerivedFrom}
	 *         field
	 */
	public Set<TopLevel> getByWasDerivedFrom(URI wasDerivedFrom) {
		Set<TopLevel> topLevels = new HashSet<>();
		for (TopLevel topLevel : getTopLevels()) {
			for (URI wdf : topLevel.getWasDerivedFroms()) {
				if (wdf.equals(wasDerivedFrom)) {
					topLevels.add(topLevel);
				}
			}
		}
		return topLevels;
	}

	/**
	 * Adds the given registry to this SBOL document.
	 * 
	 * @param registryURL
	 *            URL of the registry to add
	 * @return The StackFrontend object that has been created
	 */
	public SynBioHubFrontend addRegistry(String registryURL) {
		SynBioHubFrontend stackFrontend = new SynBioHubFrontend(registryURL);
		registries.put(registryURL, stackFrontend);
		return stackFrontend;
	}

	/**
	 * Adds the given registry to this SBOL document.
	 * 
	 * @param registryURL
	 *            URL of the registry to add
	 * @param uriPrefix
	 *            URI prefix for everything stored in this repository
	 * @return The StackFrontend object that has been created
	 */
	public SynBioHubFrontend addRegistry(String registryURL, String uriPrefix) {
		SynBioHubFrontend stackFrontend = new SynBioHubFrontend(registryURL, uriPrefix);
		registries.put(registryURL, stackFrontend);
		return stackFrontend;
	}

	/**
	 * Adds the given namespace URI and its prefix to this SBOL document.
	 *
	 * @param nameSpaceURI
	 *            the URI used to construct a new namespace
	 * @param prefix
	 *            the prefix used to construct a new namespace
	 * @throws SBOLValidationException
	 *             if namespace URI does not end with a delimeter
	 */
	public void addNamespace(URI nameSpaceURI, String prefix) throws SBOLValidationException {

		// if (!URIcompliance.isURIprefixCompliant(nameSpaceURI.toString())) {
		// throw new SBOLException("Namespace URI " + nameSpaceURI.toString() + " is not
		// valid.");
		// }
		addNamespaceBinding(NamespaceBinding(nameSpaceURI.toString(), prefix));

	}

	/**
	 * Adds the given namespace QName to this SBOL document.
	 *
	 * @param qName
	 *            the QName to be added
	 * @throws SBOLValidationException
	 *             when namespace URI does not end with a delimiter
	 */
	public void addNamespace(QName qName) throws SBOLValidationException {

		addNamespaceBinding(NamespaceBinding(qName.getNamespaceURI(), qName.getPrefix()));
	}

	void addNamespaceBinding(NamespaceBinding namespaceBinding) throws SBOLValidationException {
		if (!namespaceBinding.getNamespaceURI().endsWith("#") && !namespaceBinding.getNamespaceURI().endsWith(":")
				&& !namespaceBinding.getNamespaceURI().endsWith("/")) {
			throw new SBOLValidationException("sbol-10106");
		}
		nameSpaces.put(namespaceBinding.getPrefix(), namespaceBinding);
	}

	/**
	 * Removes all non-required namespaces from this SBOL document.
	 * <p>
	 * This method calls {@link #removeNamespace(URI)} to iteratively remove each
	 * non-required namespace.
	 */
	public void clearNamespaces() {
		Object[] keySetArray = nameSpaces.keySet().toArray();
		for (Object key : keySetArray) {
			if (isRequiredNamespaceBinding(URI.create((String) key)))
				continue;
			nameSpaces.remove((String) key);
		}
	}

	/**
	 * Removes all registries from this SBOL document.
	 * <p>
	 * This method calls {@link #removeRegistry(String)} to iteratively remove each
	 * registry.
	 */
	public void clearRegistries() {
		Object[] keySetArray = registries.keySet().toArray();
		for (Object key : keySetArray) {
			removeRegistry((String) key);
		}
	}

	String getNamespacePrefix(URI namespaceURI) {
		QName qName = getNamespace(namespaceURI);
		int nsNum = 0;
		if (qName == null) {
			boolean foundIt;
			do {
				foundIt = false;
				if (nameSpaces.keySet().contains("ns" + nsNum)) {
					nsNum++;
					foundIt = true;
					break;
				}
			} while (foundIt);
			nameSpaces.put("ns" + nsNum, NamespaceBinding(namespaceURI.toString(), "ns" + nsNum));
			return "ns" + nsNum;
		} else {
			return qName.getPrefix();
		}
	}

	/**
	 * Returns the QName matching the given namespace prefix from this SBOL
	 * document's list of QNames.
	 *
	 * @param namespacePrefix
	 *            the prefix of the namespace to be retrieved
	 * @return the matching QName if present, or {@code null} otherwise
	 */
	public QName getNamespace(String namespacePrefix) {
		NamespaceBinding namespaceBinding = nameSpaces.get(namespacePrefix);
		if (namespaceBinding == null)
			return null;
		return new QName(namespaceBinding.getNamespaceURI(), "", namespaceBinding.getPrefix());
	}

	/**
	 * Returns the QName matching the given namespace URI from this SBOL document's
	 * list of QNames.
	 *
	 * @param namespaceURI
	 *            the identity URI of the namespace to be retrieved
	 * @return the matching QName if present, or {@code null} otherwise
	 */
	public QName getNamespace(URI namespaceURI) {
		// if (nameSpaces.get(namespaceURI)==null) return null;
		for (NamespaceBinding namespaceBinding : nameSpaces.values()) {
			if (namespaceBinding.getNamespaceURI().equals(namespaceURI.toString())) {
				return new QName(namespaceBinding.getNamespaceURI(), "", namespaceBinding.getPrefix());
			}
		}
		return null;
	}

	/**
	 * Returns the list of namespaces owned by this SBOL document.
	 *
	 * @return the list of namespaces owned by this SBOL document
	 */
	public List<QName> getNamespaces() {
		List<QName> bindings = new ArrayList<>();
		for (NamespaceBinding namespaceBinding : this.nameSpaces.values()) {
			bindings.add(new QName(namespaceBinding.getNamespaceURI(), "", namespaceBinding.getPrefix()));
		}
		return bindings;
	}

	/**
	 * Returns the StackFrontend for the registry specified by its URL
	 * 
	 * @param registryURL
	 *            URL for the registry to return
	 * @return a StackFrontend for the registry specified by its URL
	 */
	public SynBioHubFrontend getRegistry(String registryURL) {
		return registries.get(registryURL);
	}

	/**
	 * Returns the list of registries used by this SBOL document.
	 *
	 * @return the list of registries used by this SBOL document
	 */
	public List<SynBioHubFrontend> getRegistries() {
		List<SynBioHubFrontend> registries = new ArrayList<>();
		for (SynBioHubFrontend registry : this.registries.values()) {
			registries.add(registry);
		}
		return registries;
	}

	/**
	 * Returns the namespace bindings for this SBOL document
	 * 
	 * @return the list of namespace bindings for this SBOL document
	 */
	List<NamespaceBinding> getNamespaceBindings() {
		List<NamespaceBinding> bindings = new ArrayList<>();
		bindings.addAll(this.nameSpaces.values());
		return bindings;
	}

	/**
	 * Removes the given namespace URI from this SBOL document's list of namespaces.
	 *
	 * @param namespaceURI
	 *            the namespaceURI to be removed
	 */
	public void removeNamespace(URI namespaceURI) {
		if (isRequiredNamespaceBinding(namespaceURI)) {
			throw new IllegalStateException("Cannot remove required namespace " + namespaceURI.toString());
		}
		String prefix = getNamespace(namespaceURI).getPrefix();
		nameSpaces.remove(prefix);
	}

	/**
	 * Removes the given registry id from this SBOL document's list of registries.
	 *
	 * @param registryId
	 *            the URL of the registry to be removed
	 */
	public void removeRegistry(String registryId) {
		registries.remove(registryId);
	}

	// /**
	// * Clears the existing list of <code>namespaces</code>, then appends all of
	// the namespaces to the end of this list.
	// */
	/*
	 * void setNameSpaceBindings(List<NamespaceBinding> namespaceBinding) {
	 * clearNamespaces(); for (NamespaceBinding namespace : namespaceBinding) {
	 * addNamespaceBinding(namespace); } }
	 */

	private boolean isRequiredNamespaceBinding(URI namespaceURI) {
		if (namespaceURI.toString().equals(Sbol2Terms.sbol2.getNamespaceURI()))
			return true;
		if (namespaceURI.toString().equals(Sbol2Terms.dc.getNamespaceURI()))
			return true;
		if (namespaceURI.toString().equals(Sbol2Terms.prov.getNamespaceURI()))
			return true;
		if (namespaceURI.toString().equals(Sbol1Terms.rdf.getNamespaceURI()))
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((collections == null) ? 0 : collections.hashCode());
		result = prime * result + ((experiments == null) ? 0 : experiments.hashCode());
		result = prime * result + ((experimentalData == null) ? 0 : experimentalData.hashCode());
		result = prime * result + ((componentDefinitions == null) ? 0 : componentDefinitions.hashCode());
		result = prime * result + ((genericTopLevels == null) ? 0 : genericTopLevels.hashCode());
		result = prime * result + ((activities == null) ? 0 : activities.hashCode());
		result = prime * result + ((agents == null) ? 0 : agents.hashCode());
		result = prime * result + ((plans == null) ? 0 : plans.hashCode());
		result = prime * result + ((models == null) ? 0 : models.hashCode());
		result = prime * result + ((moduleDefinitions == null) ? 0 : moduleDefinitions.hashCode());
		result = prime * result + ((nameSpaces == null) ? 0 : nameSpaces.hashCode());
		result = prime * result + ((sequences == null) ? 0 : sequences.hashCode());
		result = prime * result + ((combinatorialDerivations == null) ? 0 : combinatorialDerivations.hashCode());
		result = prime * result + ((implementations == null) ? 0 : implementations.hashCode());
		result = prime * result + ((attachments == null) ? 0 : attachments.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SBOLDocument other = (SBOLDocument) obj;
		if (collections == null) {
			if (other.collections != null)
				return false;
		} else if (!collections.equals(other.collections))
			return false;
		if (experiments == null) {
			if (other.experiments != null)
				return false;
		} else if (!experiments.equals(other.experiments))
			return false;
		if (experimentalData == null) {
			if (other.experimentalData != null)
				return false;
		} else if (!experimentalData.equals(other.experimentalData))
			return false;
		if (componentDefinitions == null) {
			if (other.componentDefinitions != null)
				return false;
		} else if (!componentDefinitions.equals(other.componentDefinitions))
			return false;
		if (genericTopLevels == null) {
			if (other.genericTopLevels != null)
				return false;
		} else if (!genericTopLevels.equals(other.genericTopLevels))
			return false;
		if (activities == null) {
			if (other.activities != null)
				return false;
		} else if (!activities.equals(other.activities))
			return false;
		if (agents == null) {
			if (other.agents != null)
				return false;
		} else if (!agents.equals(other.agents))
			return false;
		if (plans == null) {
			if (other.plans != null)
				return false;
		} else if (!plans.equals(other.plans))
			return false;
		if (models == null) {
			if (other.models != null)
				return false;
		} else if (!models.equals(other.models))
			return false;
		if (moduleDefinitions == null) {
			if (other.moduleDefinitions != null)
				return false;
		} else if (!moduleDefinitions.equals(other.moduleDefinitions))
			return false;
		if (nameSpaces == null) {
			if (other.nameSpaces != null)
				return false;
		} else if (!nameSpaces.equals(other.nameSpaces))
			return false;
		if (sequences == null) {
			if (other.sequences != null)
				return false;
		} else if (!sequences.equals(other.sequences))
			return false;
		if (combinatorialDerivations == null) {
			if (other.combinatorialDerivations != null)
				return false;
		} else if (!combinatorialDerivations.equals(other.combinatorialDerivations))
			return false;
		if (attachments == null) {
			if (other.attachments != null)
				return false;
		} else if (!attachments.equals(other.attachments))
			return false;
		if (implementations == null) {
			if (other.implementations != null)
				return false;
		} else if (!implementations.equals(other.implementations))
			return false;
		return true;
	}

	/**
	 * @param newTopLevel
	 * @param instancesMap
	 * @param typeName
	 * @param maps
	 * @throws SBOLValidationException
	 *             if either of the following SBOL validation rules was violated:
	 *             10202, 10220.
	 */
	@SafeVarargs
	private final <TL extends TopLevel> void addTopLevel(TL newTopLevel, Map<URI, TL> instancesMap, String typeName,
			Map<URI, ? extends Identified>... maps) throws SBOLValidationException {
		boolean childrenCompliant = true;
		try {
			URIcompliance.isURIcompliant(newTopLevel);
			// newTopLevel.checkDescendantsURIcompliance();
		} catch (SBOLValidationException e) {
			childrenCompliant = false;
		}
		if (compliant && childrenCompliant) {
			URI persistentId = URI.create(extractPersistentId(newTopLevel.getIdentity()));
			if (keyExistsInAnyMap(persistentId, maps))
				throw new SBOLValidationException("sbol-10220", newTopLevel);
			if (instancesMap.containsKey(newTopLevel.getIdentity()))
				throw new SBOLValidationException("sbol-10202", newTopLevel);
			String prefix = extractURIprefix(persistentId);
			while (prefix != null) {
				if (keyExistsInAnyMap(URI.create(prefix), maps))
					throw new SBOLValidationException("sbol-10202", newTopLevel);
				if (instancesMap.containsKey(URI.create(prefix)))
					throw new SBOLValidationException("sbol-10202", newTopLevel);
				prefix = extractURIprefix(URI.create(prefix));
			}
			if (prefixes.contains(persistentId.toString())) {
				throw new IllegalArgumentException(
						"Persistent identity `" + persistentId.toString() + "' matches URI prefix in document.");
			}
			prefix = extractURIprefix(persistentId);
			while (prefix != null) {
				prefixes.add(prefix);
				prefix = extractURIprefix(URI.create(prefix));
			}
			instancesMap.put(newTopLevel.getIdentity(), newTopLevel);
			Identified latest = instancesMap.get(persistentId);
			if (latest == null) {
				instancesMap.put(persistentId, newTopLevel);
			} else {
				if (isFirstVersionNewer(extractVersion(newTopLevel.getIdentity()),
						extractVersion(latest.getIdentity()))) {
					instancesMap.put(persistentId, newTopLevel);
				}
			}
		} else { // Only check if URI exists in all maps.
			if (keyExistsInAnyMap(newTopLevel.getIdentity()))
				throw new SBOLValidationException("sbol-10202", newTopLevel);
			if (instancesMap.containsKey(newTopLevel.getIdentity()))
				throw new SBOLValidationException("sbol-10202", newTopLevel);
			instancesMap.put(newTopLevel.getIdentity(), newTopLevel);
			if (newTopLevel.isSetPersistentIdentity()) {
				Identified latest = instancesMap.get(newTopLevel.getPersistentIdentity());
				if (latest == null) {
					instancesMap.put(newTopLevel.getPersistentIdentity(), newTopLevel);
				} else {
					if (isFirstVersionNewer(extractVersion(newTopLevel.getIdentity()),
							extractVersion(latest.getIdentity()))) {
						instancesMap.put(newTopLevel.getPersistentIdentity(), newTopLevel);
					}
				}
			}
		}
		newTopLevel.setSBOLDocument(this);
	}

	/**
	 * Removes the given top-level from this SBOL document's list of top-levels.
	 *
	 * @param topLevel
	 *            the top-level to be removed
	 * @param instancesMap
	 *            map of toplevel instances
	 * @return {@code true} if the given top-level was successfully removed,
	 *         {@code false} otherwise
	 * @throws SBOLValidationException
	 *             if the following SBOL validation rule was violated: 12103.
	 */
	private final <TL extends TopLevel> boolean removeTopLevel(TopLevel topLevel, Map<URI, TL> instancesMap)
			throws SBOLValidationException {
		if (complete) {
			for (Collection c : collections.values()) {
				if (c.containsMember(topLevel.getIdentity())) {
					throw new SBOLValidationException("sbol-12103", c);
				}
			}
			for (Experiment c : experiments.values()) {
				if (c.containsExperimentalData(topLevel.getIdentity())) {
					throw new SBOLValidationException("sbol-1xxxx", c);
				}
			}
		}
		Set<TopLevel> setToRemove = new HashSet<>();
		setToRemove.add(topLevel);
		boolean changed = instancesMap.values().removeAll(setToRemove);
		URI latestVersion = null;
		for (TL tl : instancesMap.values()) {
			if (topLevel.getPersistentIdentity().toString().equals(tl.getPersistentIdentity().toString())) {
				if (latestVersion == null) {
					latestVersion = tl.getIdentity();
				} else if (isFirstVersionNewer(extractVersion(tl.getIdentity()), extractVersion(latestVersion))) {
					latestVersion = tl.getIdentity();
				}
			}
		}
		if (latestVersion != null) {
			instancesMap.put(topLevel.getPersistentIdentity(), instancesMap.get(latestVersion));
		}
		return changed;
	}

	/**
	 * Method to remove a TopLevel object
	 * 
	 * @param topLevel
	 *            the TopLevel object to remove
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in any of the
	 *             following methods:
	 *             <ul>
	 *             <li>{@link #removeActivity(Activity)},</li>
	 *             <li>{@link #removeAgent(Agent)},</li>
	 *             <li>{@link #removePlan(Plan)},</li>
	 *             <li>{@link #removeGenericTopLevel(GenericTopLevel)},</li>
	 *             <li>{@link #removeCollection(Collection)},</li>
	 *             <li>{@link #removeExperiment(Experiment)},</li>
	 *             <li>{@link #removeExperimentalData(ExperimentalData)},</li>
	 *             <li>{@link #removeSequence(Sequence)},</li>
	 *             <li>{@link #removeComponentDefinition(ComponentDefinition)},</li>
	 *             <li>{@link #removeModel(Model)}, or</li>
	 *             <li>{@link #removeModuleDefinition(ModuleDefinition)}.</li>
	 *             <li>{@link #removeImplementation(Implementation)}.</li>
	 *             </ul>
	 */
	public void removeTopLevel(TopLevel topLevel) throws SBOLValidationException {
		if (topLevel instanceof GenericTopLevel)
			removeGenericTopLevel((GenericTopLevel) topLevel);
		else if (topLevel instanceof Activity)
			removeActivity((Activity) topLevel);
		else if (topLevel instanceof Agent)
			removeAgent((Agent) topLevel);
		else if (topLevel instanceof Plan)
			removePlan((Plan) topLevel);
		else if (topLevel instanceof Collection)
			removeCollection((Collection) topLevel);
		else if (topLevel instanceof Experiment)
			removeExperiment((Experiment) topLevel);
		else if (topLevel instanceof ExperimentalData)
			removeExperimentalData((ExperimentalData) topLevel);
		else if (topLevel instanceof Sequence)
			removeSequence((Sequence) topLevel);
		else if (topLevel instanceof ComponentDefinition)
			removeComponentDefinition((ComponentDefinition) topLevel);
		else if (topLevel instanceof Model)
			removeModel((Model) topLevel);
		else if (topLevel instanceof ModuleDefinition)
			removeModuleDefinition((ModuleDefinition) topLevel);
		else if (topLevel instanceof CombinatorialDerivation)
			removeCombinatorialDerivation((CombinatorialDerivation) topLevel);
		else if (topLevel instanceof Implementation)
			removeImplementation((Implementation) topLevel);
		else if (topLevel instanceof Attachment)
			removeAttachment((Attachment) topLevel);
	}

	/**
	 * Sets the default URI prefix of this SBOL document to the given one. This
	 * means that any SBOL instances created subsequently in this document will have
	 * the given URI prefix as the beginning of its compliant identity URI.
	 *
	 * @param defaultURIprefix
	 *            the given default URI prefix
	 * @throws IllegalArgumentException
	 *             if the given URI prefix is not compliant
	 */

	public void setDefaultURIprefix(String defaultURIprefix) throws IllegalArgumentException {
		if (!defaultURIprefix.endsWith("/") && !defaultURIprefix.endsWith(":") && !defaultURIprefix.endsWith("#")) {
			defaultURIprefix += "/";
		}
		if (isURIprefixCompliant(defaultURIprefix)) {
			this.defaultURIprefix = defaultURIprefix;
		} else {
			throw new IllegalArgumentException(
					"Unable to set default URI prefix to non-compliant value `" + defaultURIprefix + "'");
		}
	}

	/**
	 * Returns the default URI prefix of this SBOL document.
	 *
	 * @return the default URI prefix of this SBOL document
	 */
	public String getDefaultURIprefix() {
		return defaultURIprefix;
	}

	/**
	 * Returns the value of the complete flag for this SBOL document.
	 * <p>
	 * A {@code true} value means that all identity URI references should be able to
	 * dereference to instances in the this document, and a {@code false} value
	 * means otherwise.
	 *
	 * @return the value of the complete flag for this SBOL document
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * Sets the complete flag to the given value.
	 * <p>
	 * A {@code true} value indicates this SBOL document is complete: any identity
	 * URIs should be able to dereference to an instance in this document.
	 *
	 * @param complete
	 *            the given boolean value for the complete flag
	 */
	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	/**
	 * Returns the value of the compliant flag in this SBOL document.
	 * <p>
	 * A {@code true} value means that all identity URIs in this SBOL document
	 * should be compliant, and a {@code false} value means otherwise.
	 *
	 * @return the value of the compliant flag in this SBOL document
	 */
	public boolean isCompliant() {
		return compliant;
	}

	void setCompliant(boolean compliant) {
		this.compliant = compliant;
	}

	/**
	 * Returns the value of the typesInURI flag in this SBOL document.
	 * <p>
	 * A {@code true} value means that types will be inserted into this document's
	 * each top-level's compliant URI when it is created, and a {@code false} value
	 * means otherwise.
	 *
	 * @return the value of the typesInURI flag in this SBOL document
	 */
	public boolean isTypesInURIs() {
		return typesInURIs;
	}

	/**
	 * Sets the typesInURIs flag to the given value.
	 * <p>
	 * A {@code true} value means that types are inserted into top-level identity
	 * URIs when they are created.
	 *
	 * @param typesInURIs
	 *            the given boolean value for the typesInURIs flag
	 */
	public void setTypesInURIs(boolean typesInURIs) {
		this.typesInURIs = typesInURIs;
	}

	/**
	 * Returns the value of the createDefaults flag in this SBOL document.
	 * <p>
	 * A {@code true} value means that default components and/or functional
	 * components instances should be created when not present, and a {@code false}
	 * value means otherwise.
	 * 
	 * @return the value of the createDefaults flag in this SBOL document.
	 */
	public boolean isCreateDefaults() {
		return createDefaults;
	}

	/**
	 * Sets the createDefaults flag to the given value. A {@code true} value means
	 * that default component instances should be created when not present.
	 *
	 * @param createDefaults
	 *            the given boolean value for the createDefaults flag
	 */
	public void setCreateDefaults(boolean createDefaults) {
		this.createDefaults = createDefaults;
	}

	/**
	 * Takes in a given RDF file name and adds the data read to this SBOLDocument.
	 *
	 * @param fileName
	 *            a given RDF file name
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #read(File)}.
	 * @throws SBOLConversionException
	 *             see {@link SBOLConversionException}
	 * @throws IOException
	 *             see {@link IOException}
	 */
	public void read(String fileName) throws SBOLValidationException, IOException, SBOLConversionException {
		read(new File(fileName));
	}

	/**
	 * Takes in a given RDF file and adds the data read to this SBOL document.
	 *
	 * @param file
	 *            a given RDF file
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link #read(InputStream)}.
	 * @throws SBOLConversionException
	 *             see {@link SBOLConversionException}
	 * @throws IOException
	 *             see {@link IOException}
	 */
	public void read(File file) throws SBOLValidationException, IOException, SBOLConversionException {
		FileInputStream stream = new FileInputStream(file);
		BufferedInputStream buffer = new BufferedInputStream(stream);
		read(buffer);
	}

	/**
	 * Takes in a given RDF input stream and adds the data read to this SBOL
	 * document.
	 *
	 * @param in
	 *            a given RDF input stream
	 * @throws SBOLValidationException
	 *             if any of the following SBOL validation rules was violated:
	 *             10101, 10102, 10105, 10201, 10202, 10203, 10204, 10206, 10208,
	 *             10212, 10213, 10220, 10303, 10304, 10305, 10401, 10402, 10403,
	 *             10405, 10501, 10502, 10503, 10504, 10507, 10508, 10512, 10513,
	 *             10519, 10522, 10526, 10602, 10603, 10604, 10605, 10606, 10607,
	 *             10701, 10801, 10802, 10803, 10804, 10805, 10806, 10807, 10808,
	 *             10809, 10810, 10811, 10901, 10902, 10904, 10905, 11002, 11101,
	 *             11102, 11103, 11104, 11201, 11202, 11301, 11401, 11402, 11403,
	 *             11404, 11405, 11406, 11407, 11412, 11501, 11502, 11504, 11508,
	 *             11601, 11602, 11604, 11605, 11606, 11607, 11608, 11609, 11701,
	 *             11702, 11703, 11704, 11705, 11706, 11801, 11802, 11901, 11902,
	 *             11906, 12001, 12002, 12003, 12004, 12101, 12102, 12103, 12301,
	 *             12302.
	 * @throws SBOLConversionException
	 *             see {@link SBOLConversionException}
	 * @throws IOException
	 *             see {@link IOException}
	 */
	public void read(InputStream in) throws SBOLValidationException, IOException, SBOLConversionException {
		SBOLReader.read(this, in, SBOLDocument.RDF);
	}

	/**
	 * Outputs this SBOL document's data from the RDF/XML serialization to a new
	 * file with the given file name.
	 * <p>
	 * This method calls {@link SBOLWriter#write(SBOLDocument, File)} by passing
	 * this SBOL document, and a new file with the given file name.
	 * 
	 * @param filename
	 *            the given output file name
	 * @throws IOException
	 *             see {@link IOException}
	 * @throws SBOLConversionException
	 *             see {@link SBOLConversionException}
	 */
	public void write(String filename) throws IOException, SBOLConversionException {
		SBOLWriter.write(this, new File(filename));
	}

	/**
	 * Outputs this SBOL document's data from serialization in the given
	 * serialization format to a new file with the given file name.
	 * <p>
	 * This method calls {@link SBOLWriter#write(SBOLDocument, File, String)} by
	 * passing this SBOL document, and a new file with the given file name and type.
	 * 
	 * @param filename
	 *            the given output file name
	 * @param fileType
	 *            the serialization format
	 * @throws IOException
	 *             see {@link IOException}
	 * @throws SBOLConversionException
	 *             see {@link SBOLConversionException}
	 */
	public void write(String filename, String fileType) throws IOException, SBOLConversionException {
		SBOLWriter.write(this, new File(filename), fileType);
	}

	/**
	 * Outputs this SBOL document's data from the RDF/XML serialization to the given
	 * file.
	 * <p>
	 * This method calls {@link SBOLWriter#write(SBOLDocument, File)}.
	 * 
	 * @param file
	 *            the given output file
	 * @throws IOException
	 *             see {@link IOException}
	 * @throws SBOLConversionException
	 *             see {@link SBOLConversionException}
	 */
	public void write(File file) throws IOException, SBOLConversionException {
		FileOutputStream stream = new FileOutputStream(file);
		BufferedOutputStream buffer = new BufferedOutputStream(stream);
		SBOLWriter.write(this, buffer);
		stream.close();
		buffer.close();
	}

	/**
	 * Outputs this SBOL document's data from the serialization in the given
	 * serialization format to the given file.
	 * <p>
	 * This method calls
	 * {@link SBOLWriter#write(SBOLDocument, OutputStream, String)}.
	 * 
	 * @param file
	 *            the given output file
	 * @param fileType
	 *            the given serialization format
	 * @throws IOException
	 *             see {@link IOException}
	 * @throws SBOLConversionException
	 *             see {@link SBOLConversionException}
	 */
	public void write(File file, String fileType) throws IOException, SBOLConversionException {
		FileOutputStream stream = new FileOutputStream(file);
		BufferedOutputStream buffer = new BufferedOutputStream(stream);
		SBOLWriter.write(this, buffer, fileType);
		stream.close();
		buffer.close();
	}

	/**
	 * Outputs this SBOL document's data from the RDF/XML serialization to the given
	 * output stream.
	 * <p>
	 * This method calls {@link SBOLWriter#write(SBOLDocument, OutputStream)} by
	 * passing this SBOL document and the given output stream.
	 * 
	 * @param out
	 *            the given output stream
	 * @throws SBOLConversionException
	 *             see {@link SBOLConversionException}
	 */
	public void write(OutputStream out) throws SBOLConversionException {
		SBOLWriter.write(this, out);
	}

	/**
	 * Outputs this SBOL document's data from the serialization in the given
	 * serialization format to the given output stream.
	 * <p>
	 * This method calls
	 * {@link SBOLWriter#write(SBOLDocument, OutputStream, String)} by passing this
	 * SBOL document, the given output stream and file type.
	 * 
	 * @param out
	 *            the given output stream
	 * @param fileType
	 *            the serialization format
	 * @throws SBOLConversionException
	 *             see {@link SBOLConversionException}
	 * @throws IOException
	 *             see {@link IOException}
	 */
	public void write(OutputStream out, String fileType) throws SBOLConversionException, IOException {
		SBOLWriter.write(this, out, fileType);
	}

	@Override
	public String toString() {
		return "SBOLDocument [activities=" + activities + ", agents=" + agents + ", plans=" + plans + ", implementations=" + implementations
				+ ", attachments=" + attachments + ", combinatorialDerivations=" + combinatorialDerivations
				+ ", genericTopLevels=" + genericTopLevels + ", collections=" + collections + ", experiments=" + experiments + ", experimentalData =" + experimentalData 
				+ ", componentDefinitions=" + componentDefinitions
				+ ", models=" + models + ", moduleDefinitions=" + moduleDefinitions + ", sequences=" + sequences
				+ ", nameSpaces=" + nameSpaces + ", defaultURIprefix=" + defaultURIprefix + ", complete=" + complete
				+ ", compliant=" + compliant + ", typesInURIs=" + typesInURIs + ", createDefaults=" + createDefaults
				+ "]";

	}

}
