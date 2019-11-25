package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;
import static org.sbolstandard.core2.URIcompliance.isChildURIcompliant;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a ModuleDefinition object in the SBOL data model.
 *
 * @author Zhen Zhang
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */

public class ModuleDefinition extends TopLevel {

	private Set<URI>							roles;
	private HashMap<URI, Module>				modules;
	private HashMap<URI, Interaction>			interactions;
	private HashMap<URI, FunctionalComponent>	functionalComponents;
	private Set<URI>							models;

	/**
	 * @param identity
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link TopLevel#TopLevel(URI)}.
	 */
	ModuleDefinition(URI identity) throws SBOLValidationException {
		super(identity);
		this.roles = new HashSet<>();
		this.modules = new HashMap<>();
		this.interactions = new HashMap<>();
		this.functionalComponents = new HashMap<>();
		this.models = new HashSet<>();
	}

	/**
	 * @param moduleDefinition
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of the following
	 * constructors or methods: 
	 * <ul>
	 * <li>{@link TopLevel#TopLevel(TopLevel)},</li>
	 * <li>{@link #addFunctionalComponent(FunctionalComponent)},</li>
	 * <li>{@link #addModule(Module)},</li>
	 * <li>{@link #addInteraction(Interaction)}, or</li>
	 * <li>{@link #setModels(Set)}</li>
	 * </ul>
	 */
	private ModuleDefinition(ModuleDefinition moduleDefinition) throws SBOLValidationException {
		super(moduleDefinition);
		this.roles = new HashSet<>();
		this.modules = new HashMap<>();
		this.interactions = new HashMap<>();
		this.functionalComponents = new HashMap<>();
		this.models = new HashSet<>();
		for (URI role : moduleDefinition.getRoles()) {
			this.addRole(role);
		}
		for (FunctionalComponent component : moduleDefinition.getFunctionalComponents()) {
			this.addFunctionalComponent(component.deepCopy());
		}
		for (Module subModule : moduleDefinition.getModules()) {
			this.addModule(subModule.deepCopy());
		}
		for (Interaction interaction : moduleDefinition.getInteractions()) {
			this.addInteraction(interaction.deepCopy());
		}
		this.setModels(moduleDefinition.getModelURIs());
	}
	
	void copy(ModuleDefinition moduleDefinition) throws SBOLValidationException {
		((TopLevel)this).copy((TopLevel)moduleDefinition);
		for (URI role : moduleDefinition.getRoles()) {
			this.addRole(role);
		}
		for (FunctionalComponent component : moduleDefinition.getFunctionalComponents()) {
			String displayId = URIcompliance.findDisplayId(component);
			FunctionalComponent newComponent = this.createFunctionalComponent(displayId, 
					component.getAccess(), component.getDefinitionURI(), component.getDirection());
			newComponent.copy(component);
		}
		for (FunctionalComponent functionalComponent : moduleDefinition.getFunctionalComponents()) {
			if (!functionalComponent.getMapsTos().isEmpty()) {
				FunctionalComponent copyComponent = this.getFunctionalComponent(functionalComponent.getDisplayId());
				for (MapsTo mapsTo : functionalComponent.getMapsTos()) {
					String displayId = URIcompliance.findDisplayId(mapsTo);
					String localDisplayId = URIcompliance.findDisplayId(mapsTo.getLocal());
					MapsTo newMapsTo = copyComponent.createMapsTo(displayId, mapsTo.getRefinement(), localDisplayId, 
							mapsTo.getRemoteURI());
					newMapsTo.copy(mapsTo);
				}
			}
		}
		for (Module subModule : moduleDefinition.getModules()) {
			String displayId = URIcompliance.findDisplayId(subModule);
			Module newModule = this.createModule(displayId, subModule.getDefinitionURI());
			newModule.copy(subModule);
		}
		for (Interaction interaction : moduleDefinition.getInteractions()) {
			String displayId = URIcompliance.findDisplayId(interaction);
			Interaction newInteraction = this.createInteraction(displayId, 
					interaction.getTypes());
			newInteraction.copy(interaction);
		}
		this.setModels(moduleDefinition.getModelURIs());
	}

	/**
	 * Adds the given role URI to this module definition's set of role URIs.
	 *
	 * @param roleURI the role URI to be added
	 * @return {@code true} if this set did not already contain the specified role; {@code false} otherwise
	 */
	public boolean addRole(URI roleURI) {
		return roles.add(roleURI);
	}

	/**
	 * Removes the given role from the set of roles.
	 *
	 * @param roleURI the URI of the role to be removed
	 * @return {@code true} if the matching role was removed successfully, 
	 * or {@code false} otherwise
	 */
	public boolean removeRole(URI roleURI) {
		return roles.remove(roleURI);
	}

	/**
	 * Clears the existing set of roles first, and then adds the given
	 * set of the roles this module definition.
	 *
	 * @param roles the set of roles to be added
	 */
	public void setRoles(Set<URI> roles) {
		clearRoles();
		if (roles == null)
			return;
		for (URI role : roles) {
			addRole(role);
		}
	}

	/**
	 * Returns the set of role URIs owned by this module definition.
	 *
	 * @return the set of role URIs owned by this module definition.
	 */
	public Set<URI> getRoles() {
		Set<URI> result = new HashSet<>();
		result.addAll(roles);
		return result;
	}

	/**
	 * Checks if the given role URI is included in this module definition's set of role URIs.
	 *
	 * @param roleURI the given role URI to be checked
	 * @return {@code true} if this set contains the given role URI, or {@code false} otherwise
	 */
	public boolean containsRole(URI roleURI) {
		return roles.contains(roleURI);
	}

	/**
	 * Removes all entries of this module definition's set of role URIs.
	 * The set will be empty after this call returns.
	 */
	public void clearRoles() {
		roles.clear();
	}

	/**
	 * Calls the Module constructor to create a new module using
	 * the given parameters, and then adds to the list of modules owned by this
	 * module definition.
	 *
	 * @param identity the identity URI for the module to be created
	 * @param moduleDefinitionURI the module definition referenced by the module to be created
	 * @return the created module
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in
	 * either of the following constructor or method: 
	 * <ul>
	 * <li>{@link Module#Module(URI, URI)}, or</li>
	 * <li>{@link #addModule(Module)}.</li>
	 * </ul>
	 */
	private Module createModule(URI identity, URI moduleDefinitionURI) throws SBOLValidationException {
		Module module = new Module(identity, moduleDefinitionURI);
		addModule(module);
		return module;
	}

	/**
	 * Creates a child module for this module definition with the
	 * given arguments, and then adds to this module definition's list of modules.
	 * <p>
	 * This method creates a compliant module URI first. It starts with
	 * the associated SBOLDocument instance's URI prefix, followed by the the module's 
	 * reference module definition display ID, and ends with the given version. 
	 * It then calls {@link #createModule(String, URI)} the given module's display ID, and the created
	 * reference module definition compliant URI.
	 *
	 * @param displayId the given display ID of the module to be created
	 * @param moduleDefinitionId the display ID of the module definition referenced by the module to be created
	 * @param version the given version for the module to be created
	 * @return the created module
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>either of the following SBOL validation rule violation occurred: 10204, 10206; or </li>
	 * <li>an SBOL validation rule violation occurred in {@link #createModule(String, URI)}.</li>
	 * </ul>
	 */
	public Module createModule(String displayId, String moduleDefinitionId, String version) throws SBOLValidationException {
		URI definitionURI = URIcompliance.createCompliantURI(this.getSBOLDocument().getDefaultURIprefix(),
				TopLevel.MODULE_DEFINITION, moduleDefinitionId, version, this.getSBOLDocument().isTypesInURIs());
		return createModule(displayId, definitionURI);
	}

	/**
	 * Creates a child module for this module definition with the
	 * given arguments, and then adds to this module definition's list of modules.
	 * <p>
	 * This method calls {@link #createModule(String, String, String)} with the given module display ID,
	 * the module's reference module definition display ID, and ends with the empty version string.
	 *
	 * @param displayId the display ID of the module to be created
	 * @param moduleDefinitionId the module definition referenced by the module to be created
	 * @return the created module
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #createModule(String, String, String)} 
	 */
	public Module createModule(String displayId, String moduleDefinitionId) throws SBOLValidationException {
		return createModule(displayId, moduleDefinitionId, "");
	}

	/**
	 * Creates a child module for this module definition with the
	 * given arguments, and then adds to this module definition's list of modules.
	 * <p>
	 * This method creates a compliant Module URI with the default URI prefix
	 * for this SBOLDocument instance, the given display ID of the module to be created, and this
	 * module definition's version.
	 * 
	 * @param displayId the display ID for the module to be created
	 * @param moduleDefinitionURI the identity URI of the module definition referenced by the module to be created
	 * @return the created module
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10201, 10202, 10204, 10206, 10804, 11702, 11703, 11704, 11705.
	 */
	public Module createModule(String displayId, URI moduleDefinitionURI) throws SBOLValidationException {
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (this.getSBOLDocument().getModuleDefinition(moduleDefinitionURI) == null) {
				throw new SBOLValidationException("sbol-11703",this);
			}
		}
		String URIprefix = this.getPersistentIdentity().toString();
		String version = this.getVersion();
		URI newModuleURI = createCompliantURI(URIprefix, displayId, version);
		Module m = createModule(newModuleURI, moduleDefinitionURI);
		m.setPersistentIdentity(createCompliantURI(URIprefix, displayId, ""));
		m.setDisplayId(displayId);
		m.setVersion(version);
		return m;
	}

	/**
	 * Adds the given {@code module} argument to this ModuleDefinition's list of
	 * modules, and then associates it with the SBOLDocument instance that also contains
	 * this module definition.
	 * @param module the given module
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated: 10804, 11703, 11704, 11705; or</li>
	 * <li>an SBOL validation rule violation occurred in {@link Identified#addChildSafely(Identified, java.util.Map, String, java.util.Map...)}.</li>
	 * </ul>
	 */
	private void addModule(Module module) throws SBOLValidationException {
		module.setSBOLDocument(this.getSBOLDocument());
		module.setModuleDefinition(this);
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (module.getDefinition() == null) {
				throw new SBOLValidationException("sbol-11703", module);
			}
		}
		if (module.getDefinition()!=null &&
				this.getIdentity().equals(module.getDefinition().getIdentity())) {
			throw new SBOLValidationException("sbol-11704", module);
		}
		Set<URI> visited = new HashSet<>();
		visited.add(this.getIdentity());
		try { 
			SBOLValidate.checkModuleDefinitionCycle(this.getSBOLDocument(), module.getDefinition(), visited);
		} catch (SBOLValidationException e) {
			throw new SBOLValidationException("sbol-11705", module);
		}
		addChildSafely(module, modules, "module", functionalComponents, interactions);
		for (MapsTo mapsTo : module.getMapsTos()) {
			if (this.getFunctionalComponent(mapsTo.getLocalURI())==null) {
				throw new SBOLValidationException("sbol-10804", mapsTo);
			}
			mapsTo.setSBOLDocument(this.getSBOLDocument());
			mapsTo.setModuleDefinition(this);
			mapsTo.setModule(module);
		}
	}

	/**
	 * Removes the given module from the list of modules.
	 *
	 * @param module the module to be removed
	 * @return {@code true} if the matching module was removed successfully, or {@code false} otherwise.
	 *
	 */
	public boolean removeModule(Module module) {
		return removeChildSafely(module, modules);
	}

	/**
	 * Returns the module matching the given display ID from the list of modules.
	 *
	 * @param displayId the display ID of the module to be retrieved
	 * @return the matching module if present, or {@code null} otherwise
	 */
	public Module getModule(String displayId) {
		try {
			return modules.get(createCompliantURI(this.getPersistentIdentity().toString(), displayId,
					this.getVersion()));
		}
		catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the module matching the given identity URI from the list of modules.
	 *
	 * @param moduleURI the identity URI of the module to be retrieved
	 * @return the matching module if present, or {@code null} otherwise
	 */
	public Module getModule(URI moduleURI) {
		return modules.get(moduleURI);
	}

	/**
	 *
	 * Returns the set of modules owned by this module definition.
	 *
	 * @return the set of modules owned by this module definition
	 *
	 */
	public Set<Module> getModules() {
		return new HashSet<>(modules.values());
	}

	/**
	 * Removes all entries of this module definition's list of modules. This set will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeModule(Module)} to iteratively remove each module.
	 */
	public void clearModules()  {
		Object[] valueSetArray = modules.values().toArray();
		for (Object module : valueSetArray) {
			removeModule((Module) module);
		}

	}

	/**
	 * Clears the existing set of modules first, and then adds the given
	 * set of the modules to this module definition.
	 *
	 * @param modules the set of modules to be added
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #addModule(Module)}.
	 *
	 */
	void setModules(Set<Module> modules) throws SBOLValidationException {
		clearModules();
		if (modules == null)
			return;
		for (Module module : modules) {
			addModule(module);
		}
	}

	/**
	 * Calls the Interaction constructor {@link Interaction#Interaction(URI, Set)} to create a new instance 
	 * using the given arguments, and then adds to the list of interactions owned by this
	 * module definition.
	 *
	 * @param identity the identity URI of the interaction to be created
	 * @param types the set of types for the interaction to be created
	 * @return the created interaction
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in
	 * either of the following constructor or method:
	 * <ul>
	 * <li>{@link Interaction#Interaction(URI, Set)}, or</li>
	 * <li>{@link #addInteraction(Interaction)}</li>
	 * </ul>
	 */
	private Interaction createInteraction(URI identity, Set<URI> types) throws SBOLValidationException {
		Interaction interaction = new Interaction(identity, types);
		addInteraction(interaction);
		return interaction;
	}

	/**
	 * Creates a child interaction for this module definition with
	 * the given arguments, and then adds to this module definition's list of interactions.
	 * <p>
	 * This method first creates a compliant interaction URI. It starts with this module definition's
	 * persistent identity, followed by the given display ID of the interaction to be created, and
	 * ends with the version of this module definition. 
	 *
	 * @param displayId The displayId identifier for this interaction
	 * @param types The set of types to be added to the Interaction
	 * @return the created Interaction instance
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated: 
	 * 10201, 10202, 10204, 10206, 11902, 12003. 
	 */

	public Interaction createInteraction(String displayId, Set<URI> types) throws SBOLValidationException {
		String URIprefix = this.getPersistentIdentity().toString();
		String version = this.getVersion();
		URI interactionURI = createCompliantURI(URIprefix, displayId, version);
		Interaction i = createInteraction(interactionURI, types);
		i.setPersistentIdentity(createCompliantURI(URIprefix, displayId, ""));
		i.setDisplayId(displayId);
		i.setVersion(version);
		return i;
	}

	/**
	 * Creates a child interaction for this module definition with
	 * the given arguments, and then adds to this module definition's list of interactions.
	 * <p>
	 * This method first creates a compliant interaction URI. It starts with this module definition's
	 * persistent identity, followed by the given display ID of the interaction to be created, and
	 * ends with the version of this module definition. 
	 *
	 * @param displayId the display ID of the interaction to be created
	 * @param type the type to be added to the interaction to be created
	 * @return the created interaction 
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated: 
	 * 10201, 10202, 10204, 10206, 11902, 12003. 
	 */
	public Interaction createInteraction(String displayId, URI type) throws SBOLValidationException {
		String URIprefix = this.getPersistentIdentity().toString();
		String version = this.getVersion();
		URI newInteractionURI = createCompliantURI(URIprefix, displayId, version);
		HashSet<URI> types = new HashSet<URI>();
		types.add(type);
		Interaction i = createInteraction(newInteractionURI, types);
		i.setPersistentIdentity(createCompliantURI(URIprefix, displayId, ""));
		i.setDisplayId(displayId);
		i.setVersion(version);
		return i;
	}

	/**
	 * Adds the given Interaction instance to the list of Interaction instances.
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul>
	 * <li>the following SBOL validation rule violation occurred: 12003; or </li>
	 * <li>an SBOL validation rule violation occurred in {@link Identified#addChildSafely(Identified, java.util.Map, String, java.util.Map...)}.</li>
	 * </ul>
	 */
	private void addInteraction(Interaction interaction) throws SBOLValidationException {
		addChildSafely(interaction, interactions, "interaction", functionalComponents, modules);
		interaction.setSBOLDocument(this.getSBOLDocument());
		interaction.setModuleDefinition(this);
		for (Participation participation : interaction.getParticipations()) {
			if (this.getFunctionalComponent(participation.getParticipantURI())==null) {
				throw new SBOLValidationException("sbol-12003",participation);
			}
			participation.setSBOLDocument(this.getSBOLDocument());
			participation.setModuleDefinition(this);
		}
	}

	/**
	 * Removes the given interaction from the list of interactions.
	 *
	 * @param interaction the interaction to be removed
	 * @return {@code true} if the matching interaction was removed successfully, or {@code false} otherwise
	 */
	public boolean removeInteraction(Interaction interaction) {
		return removeChildSafely(interaction, interactions);
	}

	/**
	 * Returns the interaction matching the given interaction's identity URI from the
	 * list of interactions.
	 *
	 * @param displayId the display ID of the interaction to be retrieved
	 * @return the matching interaction if present, or {@code null} otherwise
	 */
	public Interaction getInteraction(String displayId) {
		try {
			return interactions.get(createCompliantURI(this.getPersistentIdentity().toString(),
					displayId, this.getVersion()));
		}
		catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the instance matching the given Interaction URI from the list of
	 * Interaction instances.
	 *
	 * @param interactionURI The referenced URI for the interaction
	 * @return the matching instance if present, {@code null} otherwise.
	 */
	public Interaction getInteraction(URI interactionURI) {
		return interactions.get(interactionURI);
	}

	/**
	 * Returns the set of interactions owned by this module definition.
	 *  
	 * @return the set of interactions owned by this module definition 
	 *         
	 */
	public Set<Interaction> getInteractions() {
		return new HashSet<>(interactions.values());
	}

	/**
	 * Removes all entries of this module definition's list of interactions.
	 * The list will be empty after this call returns.
	 * <p>
	 * This method calls {@link #removeInteraction(Interaction)} to iteratively remove each interaction.
	 */
	public void clearInteractions() {
		Object[] valueSetArray = interactions.values().toArray();
		for (Object interaction : valueSetArray) {
			removeInteraction((Interaction) interaction);
		}
	}

	/**
	 * Clears the existing list of interactions first, and then adds the given set of interactions to this 
	 * module definition.
	 * 
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #addInteraction(Interaction)}.
	 */
	void setInteractions(Set<Interaction> interactions) throws SBOLValidationException {
		clearInteractions();
		if (interactions == null)
			return;
		for (Interaction interaction : interactions) {
			addInteraction(interaction);
		}
	}

	/**
	 * Creates a child functional component for this module definition with the given arguments, and then adds to this
	 * module definition's list of functional components.
	 * <p>
	 * This method creates a compliant URI for the functional component to be created. It starts with
	 * this component defintion's persistent identity, followed by the given functional component's display ID,
	 * and ends with this module definition's version.
	 * 
	 * @param identity identity URI for the functional component to be created
	 * @param access the access property of the functional component to be created
	 * @param definitionURI the identity URI of the component definition referenced by the functional component to be created
	 * @param direction the direction property of the functional component to be created
	 * @return the created functional component
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul>
	 * <li>an SBOL validation rule violation occurred in {@link FunctionalComponent#FunctionalComponent(URI, AccessType, URI, DirectionType)}</li>
	 * <li>an SBOL validation rule violation occurred in {@link #addFunctionalComponent(FunctionalComponent)}</li>
	 * </ul>
	 */

	private FunctionalComponent createFunctionalComponent(URI identity, AccessType access,
			URI definitionURI, DirectionType direction) throws SBOLValidationException {
		FunctionalComponent functionalComponent =
				new FunctionalComponent(identity, access, definitionURI, direction);
		addFunctionalComponent(functionalComponent);
		return functionalComponent;
	}

	/**
	 * Creates a child functional component for this module definition with the given arguments, 
	 * and then adds to this module definition's list of functional components.
	 * <p>
	 * This method first creates a compliant component definition URI that is referenced by the functional 
	 * component to be created. This compliant URI is created with the associated SBOLDocument instance's URI prefix, 
	 * followed by the given functional component's component definition's display ID, and ends with the given version.
	 * <p>
	 * This method calls {@link #createFunctionalComponent(String, AccessType, URI, DirectionType)}
	 * with the given functional component's display ID, access type, the created functional component's component definition's
	 * URI, and the given direction.
	 * 
	 * @param displayId the display ID of the functional component to be created
	 * @param access the access property of the functional component to be created
	 * @param definitionId the display ID of the component definition referenced by the functional component to be created
	 * @param version the version of the functional component to be created 
	 * @param direction the direction property of the functional component to be created
	 * @return the created functional component
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul>
	 * <li>if either of the following SBOL validation rules was violated: 10204, 10206; or</li>
	 * <li>an SBOL validation rule violation occurred in {@link #createFunctionalComponent(String, AccessType, URI, DirectionType)}</li>
	 * </ul>
	 */
	public FunctionalComponent createFunctionalComponent(String displayId, AccessType access,
			String definitionId, String version, DirectionType direction) throws SBOLValidationException {
		URI definitionURI = URIcompliance.createCompliantURI(this.getSBOLDocument().getDefaultURIprefix(),
				TopLevel.COMPONENT_DEFINITION, definitionId, version, this.getSBOLDocument().isTypesInURIs());
		return createFunctionalComponent(displayId, access, definitionURI, direction);
	}

	/**
	 * Creates a child functional component for this module definition with the given arguments, 
	 * and then adds to this module definition's list of functional components.
	 * <p>
	 * This method calls {@link #createFunctionalComponent(String, AccessType, String, String, DirectionType)}
	 * with the given functional component's display ID, access type, the functional component's component definition's display ID, 
	 * an empty version string, and the given direction.
	 *
	 * @param displayId the display ID of the functional component to be created
	 * @param access the access type of the functional component to be created
	 * @param definitionId the display ID of the component definition referenced by the functional component to be created
	 * @param direction the direction type of the functional component to be created
	 * @return the created functional component
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #createFunctionalComponent(String, AccessType, String, String, DirectionType)}.
	 */
	public FunctionalComponent createFunctionalComponent(String displayId, AccessType access,
			String definitionId, DirectionType direction) throws SBOLValidationException {
		return createFunctionalComponent(displayId, access, definitionId, "", direction);
	}

	/**
	 * Creates a child functional component for this module definition with the given arguments, and then adds to this
	 * module definition's list of functional components.
	 * <p>
	 * This method creates a compliant URI for the functional component to be created. It starts with
	 * this component defintion's persistent identity, followed by the given functional component's display ID,
	 * and ends with this module definition's version.
	 * 
	 * @param displayId the display ID of the functional component to be created
	 * @param access the access property of the functional component to be created
	 * @param definitionURI the identity URI of the component definition referenced by the functional component to be created
	 * @param direction the direction property of the functional component to be created
	 * @return the created functional component
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated: 
	 * 10201, 10202, 10204, 10206, 10602, 10604, 10607, 11802, 10804.
	 */
	public FunctionalComponent createFunctionalComponent(String displayId, AccessType access,
			URI definitionURI, DirectionType direction) throws SBOLValidationException {
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (this.getSBOLDocument().getComponentDefinition(definitionURI) == null) {
				throw new SBOLValidationException("sbol-10604",this);
			}
		}
		String URIprefix = this.getPersistentIdentity().toString();
		String version = this.getVersion();
		URI functionalComponentURI = createCompliantURI(URIprefix, displayId, version);
		FunctionalComponent fc = createFunctionalComponent(functionalComponentURI, access,
				definitionURI, direction);
		fc.setPersistentIdentity(createCompliantURI(URIprefix, displayId, ""));
		fc.setDisplayId(displayId);
		fc.setVersion(version);
		return fc;
	}

	/**
	 * Adds the given instance to the list of components.
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul>
	 * <li>if either of the following SBOL validation rule was violated: 10604, 10804; or</li>
	 * <li>an SBOL validation rule violation occurred in {@link Identified#addChildSafely(Identified, java.util.Map, String, java.util.Map...)}</li>
	 * </ul>
	 */
	private void addFunctionalComponent(FunctionalComponent functionalComponent) throws SBOLValidationException {
		functionalComponent.setSBOLDocument(this.getSBOLDocument());
		functionalComponent.setModuleDefinition(this);
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (functionalComponent.getDefinition()== null) {
				throw new SBOLValidationException("sbol-10604", functionalComponent);
			}
		}
		addChildSafely(functionalComponent, functionalComponents, "functionalComponent",
				interactions, modules);
		for (MapsTo mapsTo : functionalComponent.getMapsTos()) {
			if (this.getFunctionalComponent(mapsTo.getLocalURI())==null) {
				throw new SBOLValidationException("sbol-10804", mapsTo);
			}
			mapsTo.setSBOLDocument(this.getSBOLDocument());
			mapsTo.setModuleDefinition(this);
			mapsTo.setComponentInstance(functionalComponent);
		}
	}

	private void addFunctionalComponentNoCheck(FunctionalComponent functionalComponent) throws SBOLValidationException {
		functionalComponent.setSBOLDocument(this.getSBOLDocument());
		functionalComponent.setModuleDefinition(this);
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (functionalComponent.getDefinition()== null) {
				throw new SBOLValidationException("sbol-10604", functionalComponent);
			}
		}
		addChildSafely(functionalComponent, functionalComponents, "functionalComponent",
				interactions, modules);
	}

	/**
	 * 
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 10804.
	 */
	private void checkMapsTosLocalURIs() throws SBOLValidationException {
		for (FunctionalComponent functionalComponent : this.getFunctionalComponents()) {
			for (MapsTo mapsTo : functionalComponent.getMapsTos()) {
				if (this.getFunctionalComponent(mapsTo.getLocalURI())==null) {
					throw new SBOLValidationException("sbol-10804", mapsTo);
				}
				mapsTo.setSBOLDocument(this.getSBOLDocument());
				mapsTo.setModuleDefinition(this);
				mapsTo.setComponentInstance(functionalComponent);
			}
		}
	}

	/**
	 * Removes the given functional component from this module definition's list of
	 * functional components.
	 *
	 * @param functionalComponent The FunctionalComponent to be removed
	 * @return {@code true} if the matching functional component was removed successfully,
	 *         or {@code false} otherwise
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10804, 10806, 12003.
	 *
	 */
	public boolean removeFunctionalComponent(FunctionalComponent functionalComponent) throws SBOLValidationException {
		for (Interaction i : interactions.values()) {
			for (Participation p : i.getParticipations()) {
				if (p.getParticipantURI().equals(functionalComponent.getIdentity())) {
					throw new SBOLValidationException("sbol-12003", p);
				}
			}
		}
		for (FunctionalComponent c : functionalComponents.values()) {
			for (MapsTo mt : c.getMapsTos()) {
				if (mt.getLocalURI().equals(functionalComponent.getIdentity())) {
					throw new SBOLValidationException("sbol-10804", mt);
				}
			}
		}
		for (Module m : modules.values()) {
			for (MapsTo mt : m.getMapsTos()) {
				if (mt.getLocalURI().equals(functionalComponent.getIdentity())) {
					throw new SBOLValidationException("sbol-10804", mt);
				}
			}
		}
		if (this.getSBOLDocument() != null) {
			for (ModuleDefinition md : this.getSBOLDocument().getModuleDefinitions()) {
				for (Module m : md.getModules()) {
					for (MapsTo mt : m.getMapsTos()) {
						if (mt.getRemoteURI().equals(functionalComponent.getIdentity())) {
							throw new SBOLValidationException("sbol-10806", functionalComponent);
						}
					}
				}
			}
		}
		return removeChildSafely(functionalComponent, functionalComponents);
	}

	/**
	 * Returns the functional component matching the given display ID from
	 * this module definition's list of functional components.
	 * <p>
	 * This method first creates a compliant URI for the functional component to be retrieved. It starts with
	 * this module definition's persistent identity, followed by the given functional component's display ID,
	 * and ends with this module definition's version.
	 * 
	 * @param displayId the display ID of the functional component to be retrieved
	 * @return the matching functional component if present, or {@code null} otherwise
	 */
	public FunctionalComponent getFunctionalComponent(String displayId) {
		try {
			return functionalComponents.get(createCompliantURI(this.getPersistentIdentity().toString(),
					displayId, this.getVersion()));
		}
		catch (SBOLValidationException e) {
			return null;
		}
	}

	/**
	 * Returns the functional component matching the given functional component identity URI from this
	 * module definition's list of functional components.
	 *
	 * @param functionalComponentURI The URI of the functional component to be retrieved
	 * @return the matching functional component if present, or
	 *         {@code null} otherwise
	 */
	public FunctionalComponent getFunctionalComponent(URI functionalComponentURI) {
		return functionalComponents.get(functionalComponentURI);
	}

	/**
	 * Returns the set of functional components owned by this
	 * module definition.
	 *
	 * @return the set of functional components owned by this
	 *         module definition
	 */
	public Set<FunctionalComponent> getFunctionalComponents() {
		return new HashSet<>(functionalComponents.values());
	}

	/**
	 * Removes all entries of this module definition's list of functional components.
	 * The list will be empty after this call returns.
	 *
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #removeFunctionalComponent(FunctionalComponent)}.
	 */
	public void clearFunctionalComponents() throws SBOLValidationException {
		Object[] valueSetArray = functionalComponents.values().toArray();
		for (Object functionalComponent : valueSetArray) {
			removeFunctionalComponent((FunctionalComponent) functionalComponent);
		}
	}

	/**
	 * Clears the existing list of functional components, then appends
	 * all of the elements in the given collection to the end of this list.
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of the 
	 * following methods:
	 * <ul>
	 * <li>{@link #clearFunctionalComponents()},</li>
	 * <li>{@link #addFunctionalComponent(FunctionalComponent)}, or</li>
	 * <li>{@link #checkMapsTosLocalURIs()}.</li>
	 * </ul>
	 */
	void setFunctionalComponents(Set<FunctionalComponent> components) throws SBOLValidationException {
		clearFunctionalComponents();
		if (components == null)
			return;
		for (FunctionalComponent component : components) {
			addFunctionalComponentNoCheck(component);
		}
		checkMapsTosLocalURIs();
	}

	/**
	 * Adds the URI of the given model to this module definition's
	 * set of model URIs.
	 * <p>
	 * This method calls {@link #addModel(URI)} with this model's URI.
	 *
	 * @param model the given model to this moduleDefinition
	 * @return {@code true} if this set did not already contain the given model, {@code false} otherwise
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul> 
	 * <li>the following SBOL validation rule was violated: 11608; or</li>
	 * <li>an SBOL validation rule violation occurred in {@link #addModel(URI)}.</li>
	 * </ul>
	 */
	public boolean addModel(Model model) throws SBOLValidationException {
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (this.getSBOLDocument().getModel(model.getIdentity()) == null) {
				throw new SBOLValidationException("sbol-11608", model);
			}
		}
		return this.addModel(model.getIdentity());
	}

	/**
  	 * Adds the URI of the model that has the given display ID and version to this module definition's
	 * set of model URIs.
	 * <p>
	 * This method creates a compliant model URI first. It starts with this module definition's persistent
	 * identity, followed by the given model display ID, and ends with the given version.
	 * This method then calls {@link #addModel(URI)} with the created model URI.
	 *
	 * @param modelId the given model display ID
	 * @param version the given version
	 * @return {@code true} if this set did not already contain the model with the given model's display ID
	 * and version, or {@code false} otherwise.
	 * @throws SBOLValidationException if an SBOL validation violation occurred in {@link #addModel(URI)}.
	 */
	public boolean addModel(String modelId, String version) throws SBOLValidationException {
		URI modelURI = URIcompliance.createCompliantURI(this.getSBOLDocument().getDefaultURIprefix(),
				TopLevel.MODEL, modelId, version, this.getSBOLDocument().isTypesInURIs());
		return addModel(modelURI);
	}

	/**
 	 * Adds the URI of the model that has the given display ID to this module definition's
	 * set of model URIs.
	 * <p>
	 * This method then calls {@link #addModel(String, String)} with the given model display ID
	 * and an empty string version.
	 *
	 * @param modelId the given model display ID
	 * @return {@code true} if this set did not already contain the model, {@code false} otherwise
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #addModel(String, String)}.  
	 */
	public boolean addModel(String modelId) throws SBOLValidationException {
		return addModel(modelId,"");
	}

	/**
	 * Adds the given model URI to this module definition's set of model URIs.
	 * 
	 * @param modelURI the given model URI to be added
	 * @return {@code true} if this set did not already contain the given model URI, or {@code false} otherwise
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 11608.
	 */
	public boolean addModel(URI modelURI) throws SBOLValidationException {
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (this.getSBOLDocument().getModel(modelURI) == null) {
				throw new SBOLValidationException("sbol-11608", this);
			}
		}
		return models.add(modelURI);
	}

	/**
	 * Removes the given model URI from the set of model URIs owned by this module definition.
	 *
	 * @param modelURI the given Model URI instance to this ModuleDefinition
	 * @return {@code true} if the matching Model reference is removed successfully,
	 *         or {@code false} otherwise
	 */
	public boolean removeModel(URI modelURI) {
		return models.remove(modelURI);
	}

	/**
	 * Clears the existing set of model URIs first, then adds the given
	 * set of the model URIs to this module definition.
	 *
	 * @param models the given set of URIs of models
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #addModel(URI)}.
	 */
	public void setModels(Set<URI> models) throws SBOLValidationException {
		clearModels();
		if (models == null)
			return;
		for (URI model : models) {
			addModel(model);
		}
	}

	/**
	 * Returns the set of model URIs referenced by this module definition.
	 *
	 * @return the set of model URIs referenced by this module definition.
	 */
	public Set<URI> getModelURIs() {
		Set<URI> result = new HashSet<>();
		result.addAll(models);
		return result;
	}
	
	/**
	 * Returns the set of models identities referenced by this module definition.
	 *
	 * @return the set of models identities referenced by this module definition
	 */
	public Set<URI> getModelIdentities() {
		Set<URI> result = new HashSet<>();
		for (URI modelURI : models) {
			Model model = this.getSBOLDocument().getModel(modelURI);
			result.add(model.getIdentity());
		}
		return result;
	}

	/**
	 * Returns the set of models referenced by this module definition.
	 *
	 * @return the set of models referenced by this module definition
	 */
	public Set<Model> getModels() {
		Set<Model> result = new HashSet<>();
		for (URI modelURI : models) {
			Model model = this.getSBOLDocument().getModel(modelURI);
			result.add(model);
		}
		return result;
	}

	/**
	 * Checks if the given model URI is included in this module definition's set of reference model URIs.
	 *
	 * @param modelURI the given model URI to be checked
	 * @return {@code true} if this set contains the given URI, or {@code false} otherwise
	 */
	public boolean containsModel(URI modelURI) {
		return models.contains(modelURI);
	}

	/**
	 * Removes all entries of this module definition's set of model URIs. The set will be empty after this call returns.
	 */
	public void clearModels() {
		models.clear();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((functionalComponents == null) ? 0 : functionalComponents.hashCode());
		result = prime * result + ((interactions == null) ? 0 : interactions.hashCode());
		result = prime * result + ((models == null) ? 0 : models.hashCode());
		result = prime * result + ((roles == null) ? 0 : roles.hashCode());
		result = prime * result + ((modules == null) ? 0 : modules.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModuleDefinition other = (ModuleDefinition) obj;
		if (functionalComponents == null) {
			if (other.functionalComponents != null)
				return false;
		} else if (!functionalComponents.equals(other.functionalComponents))
			return false;
		if (interactions == null) {
			if (other.interactions != null)
				return false;
		} else if (!interactions.equals(other.interactions))
			return false;
		if (models == null) {
			if (other.models != null)
				return false;
		} else if (!models.equals(other.models)) {
			if (getModelIdentities().size()!=getModelURIs().size() ||
					other.getModelIdentities().size()!=other.getModelURIs().size() ||
					!getModelIdentities().equals(other.getModelIdentities())) {
				return false;
			}
		}
		if (roles == null) {
			if (other.roles != null)
				return false;
		} else if (!roles.equals(other.roles))
			return false;
		if (modules == null) {
			if (other.modules != null)
				return false;
		} else if (!modules.equals(other.modules))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.TopLevel#deepCopy()
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #ModuleDefinition(ModuleDefinition)}.
	 */
	@Override
	ModuleDefinition deepCopy() throws SBOLValidationException {
		return new ModuleDefinition(this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.sbolstandard.core2.abstract_classes.TopLevel#copy(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in
	 * any one of the following methods:
	 * <ul>
	 * <li>{@link #deepCopy()},</li>
	 * <li>{@link URIcompliance#createCompliantURI(String, String, String)},</li>
	 * <li>{@link #setDisplayId(String)},</li>
	 * <li>{@link #setVersion(String)},</li>
	 * <li>{@link #setWasDerivedFrom(URI)},</li>
	 * <li>{@link #setIdentity(URI)},</li>
	 * <li>{@link FunctionalComponent#setDisplayId(String)}</li>
	 * <li>{@link FunctionalComponent#updateCompliantURI(String, String, String)},</li>
	 * <li>{@link #addFunctionalComponent(FunctionalComponent)},</li>
	 * <li>{@link Module#setDisplayId(String)}</li>
	 * <li>{@link Module#updateCompliantURI(String, String, String)},</li>
	 * <li>{@link #addModule(Module)},</li>
	 * <li>{@link Interaction#setDisplayId(String)},</li>
	 * <li>{@link Interaction#updateCompliantURI(String, String, String)}, or</li>
	 * <li>{@link #addInteraction(Interaction)}.</li>
	 * </ul> 
	 */
	@Override
	ModuleDefinition copy(String URIprefix, String displayId, String version) throws SBOLValidationException {
		ModuleDefinition cloned = this.deepCopy();
		cloned.setPersistentIdentity(createCompliantURI(URIprefix, displayId, ""));
		cloned.setDisplayId(displayId);
		cloned.setVersion(version);
		URI newIdentity = createCompliantURI(URIprefix, displayId, version);
		if (!this.getIdentity().equals(newIdentity)) {
			cloned.addWasDerivedFrom(this.getIdentity());
		} else {
			cloned.setWasDerivedFroms(this.getWasDerivedFroms());
		}
		cloned.setIdentity(newIdentity);
		int count = 0;
		for (FunctionalComponent component : cloned.getFunctionalComponents()) {
			if (!component.isSetDisplayId())
				component.setDisplayId("functionalComponent" + ++count);
			component.updateCompliantURI(cloned.getPersistentIdentity().toString(),
					component.getDisplayId(), version);
			cloned.removeChildSafely(component, cloned.functionalComponents);
			cloned.addFunctionalComponent(component);
		}
		count = 0;
		for (Module module : cloned.getModules()) {
			if (!module.isSetDisplayId())
				module.setDisplayId("module" + ++count);
			module.updateCompliantURI(cloned.getPersistentIdentity().toString(),
					module.getDisplayId(), version);
			cloned.removeChildSafely(module, cloned.modules);
			cloned.addModule(module);
		}
		count = 0;
		for (Interaction interaction : cloned.getInteractions()) {
			if (!interaction.isSetDisplayId())
				interaction.setDisplayId("interaction" + ++count);
			interaction.updateCompliantURI(cloned.getPersistentIdentity().toString(),
					interaction.getDisplayId(), version);
			cloned.removeChildSafely(interaction, cloned.interactions);
			cloned.addInteraction(interaction);
		}
		return cloned;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.sbolstandard.core2.abstract_classes.TopLevel#updateCompliantURI(java
	 * .lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	void checkDescendantsURIcompliance() throws SBOLValidationException {
		//isTopLevelURIformCompliant(this.getIdentity());
		if (!this.getModules().isEmpty()) {
			for (Module module : this.getModules()) {
				try {
					isChildURIcompliant(this, module);
				}
				catch (SBOLValidationException e) {
					throw new SBOLValidationException(e.getRule(),module);
				}
				if (!module.getMapsTos().isEmpty()) {
					// Check compliance of Module's children
					for (MapsTo mapsTo : module.getMapsTos()) {
						try {
							isChildURIcompliant(module, mapsTo);
						}
						catch (SBOLValidationException e) {
							throw new SBOLValidationException(e.getRule(),mapsTo);
						}
					}
				}
			}
		}
		if (!this.getFunctionalComponents().isEmpty()) {
			for (FunctionalComponent functionalComponent : this.getFunctionalComponents()) {
				try {
					isChildURIcompliant(this, functionalComponent);
				}
				catch (SBOLValidationException e) {
					throw new SBOLValidationException(e.getRule(),functionalComponent);
				}
				if (!functionalComponent.getMapsTos().isEmpty()) {
					// Check compliance of Component's children
					for (MapsTo mapsTo : functionalComponent.getMapsTos()) {
						try {
							isChildURIcompliant(functionalComponent, mapsTo);
						}
						catch (SBOLValidationException e) {
							throw new SBOLValidationException(e.getRule(),mapsTo);
						}
					}
				}
			}
		}
		if (!this.getInteractions().isEmpty()) {
			for (Interaction interaction : this.getInteractions()) {
				try {
					isChildURIcompliant(this, interaction);
				}
				catch (SBOLValidationException e) {
					throw new SBOLValidationException(e.getRule(),interaction);
				}
				for (Participation participation : interaction.getParticipations()) {
					try {
						isChildURIcompliant(interaction, participation);
					}
					catch (SBOLValidationException e) {
						throw new SBOLValidationException(e.getRule(),participation);
					}
				}
			}
		}
	}

	// TODO: "flatten" is half-written method, needs to be changed to public once completed.
	/**
	 * Returns a flattened copy of the module definition matching the given arguments, which has all internal
	 * hierarchy removed.   
	 * 
	 * @param URIprefix The default URI prefix for the ModuleDefinition to be flattened
	 * @param displayId The displayId identifier for the ModuleDefinition to be flattened
	 * @param version The version for the ModuleDefinition to be flattened
	 * @return The flattened copy of this ModuleDefinition
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in either of the following method:
	 * <ul>
	 * <li>{@link #flattenRecurse()}, or</li>
	 * <li>{@link #copy(String, String, String)}</li>
	 * </ul>
	 */
	ModuleDefinition flatten(String URIprefix,String displayId,String version) throws SBOLValidationException {
		return flattenRecurse().copy(URIprefix, displayId, version);
	}

	/**
	 * @return flattened ModuleDefinition
	 * @throws SBOLValidationException if either of the following condition is satisfied:
	 * <ul>
	 * <li>The following SBOL validation rule was violated: 10811; or</li> 
	 * 
	 * <li>if an SBOL validation rule violation occurred in any of the following methods:</li>
	 *	 <ul>
	 *     	<li>{@link #deepCopy()},</li>
	 * 		<li>{@link #flattenRecurse()},</li>
	 * 		<li>{@link FunctionalComponent#setDefinition(URI)},</li>
	 * 		<li>{@link FunctionalComponent#deepCopy()},</li>
	 * 		<li>{@link FunctionalComponent#updateCompliantURI(String, String, String)},</li>
	 * 		<li>{@link #addFunctionalComponent(FunctionalComponent)},</li>
	 * 		<li>{@link Participation#setParticipant(URI)},</li>
	 * 		<li>{@link #addInteraction(Interaction)}, or</li>
	 * 		<li>{@link Interaction#deepCopy()}.</li>
	 * 	</ul>
	 * </ul> 
	 */
	private ModuleDefinition flattenRecurse() throws SBOLValidationException {
		ModuleDefinition flatModuleDefinition = this.deepCopy();
		for (Module module : this.getModules()) {
			ModuleDefinition flatModule = module.getDefinition().flattenRecurse();
			for (FunctionalComponent fc : flatModule.getFunctionalComponents()) {
				boolean foundIt = false;
				URI oldURI = fc.getIdentity();
				URI newURI = null;
				for (MapsTo mapsTo : module.getMapsTos()) {
					if (mapsTo.getRemoteURI().equals(fc.getIdentity())) {
						newURI = mapsTo.getLocalURI();
						FunctionalComponent topFc = flatModuleDefinition.getFunctionalComponent(newURI);
						if (mapsTo.getRefinement()==RefinementType.USEREMOTE) {
							topFc.setDefinition(fc.getDefinitionURI());
						} else if (mapsTo.getRefinement()==RefinementType.VERIFYIDENTICAL) {
							if (!topFc.getDefinitionURI().equals(fc.getDefinitionURI())) {
								//								throw new SBOLValidationException("Component definitions in mapsTo '" + mapsTo.getIdentity()
								//										+ "' are not identical.");
								throw new SBOLValidationException("sbol-10811", mapsTo);
							}
						} else if (mapsTo.getRefinement()==RefinementType.MERGE) {
							// TODO: merge?
						}
						foundIt = true;
						break;
					}
				}
				if (!foundIt) {
					FunctionalComponent newFC = fc.deepCopy();
					newFC.updateCompliantURI(this.getPersistentIdentity().toString(),
							module.getDisplayId() + "__" + fc.getDisplayId(), this.getVersion());
					newURI = newFC.getIdentity();
					flatModuleDefinition.addFunctionalComponent(newFC);
				}
				for (Interaction i : flatModule.getInteractions()) {
					for (Participation p : i.getParticipations()) {
						if (p.getParticipantURI().equals(oldURI)) {
							p.setParticipant(newURI);
						}
					}
				}
			}
			for (Interaction i : flatModule.getInteractions()) {
				flatModuleDefinition.addInteraction(i.deepCopy());
			}
		}
		flatModuleDefinition.clearModules();
		return flatModuleDefinition;
	}

	@Override
	public String toString() {
		return "ModuleDefinition ["
				+ super.toString()
				+ (this.getRoles().size()>0?", roles=" + this.getRoles():"") 
				+ (this.getFunctionalComponents().size()>0?", functionalComponents=" + this.getFunctionalComponents():"") 
				+ (this.getModules().size()>0?", modules=" + this.getModules():"") 
				+ (this.getInteractions().size()>0?", interactions=" + this.getInteractions():"") 
				+ (this.getModels().size()>0?", models=" + this.getModelURIs():"") 
				+ "]";
	}
}
