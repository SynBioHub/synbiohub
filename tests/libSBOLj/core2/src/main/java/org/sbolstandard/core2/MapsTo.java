package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;

import java.net.URI;

/**
 * Represents a MapsTo object in the SBOL data model.
 * 
 * @author Zhen Zhang
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */

public class MapsTo extends Identified{

	private RefinementType refinement;
	
	/**
	 * URI of a local component instantiation.
	 */
	private URI local; 
	
	/**
	 * URI of a remote component instantiation
	 */
	private URI remote; 
	private ModuleDefinition moduleDefinition = null;
	private Module module = null;
	private ComponentDefinition componentDefinition = null;
	private ComponentInstance componentInstance = null;

	/**
	 * @param identity
	 * @param refinement
	 * @param local
	 * @param remote
	 * @throws SBOLValidationException if any of the following condition is satisfied:
	 * <ul>
	 * <li>{@link Identified#Identified(URI)}</li>
	 * <li>{@link #setLocal(URI)}</li>
	 * <li>{@link #setRemote(URI)}</li>
	 * </ul>
	 */
	MapsTo(URI identity, RefinementType refinement,
			URI local, URI remote) throws SBOLValidationException {
		super(identity);
		setRefinement(refinement);
		setLocal(local);
		setRemote(remote);
	}

	/**
	 * @param mapsTo
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in the following 
	 * constructors or methods:
	 * <ul>
	 * <li>{@link Identified#Identified(Identified)},</li>
	 * <li>{@link #setLocal(URI)}, or </li>
	 * <li>{@link #setRemote(URI)}.</li>
	 * </ul> 
	 */
	private MapsTo(MapsTo mapsTo) throws SBOLValidationException {
		super(mapsTo);
		this.setRefinement(mapsTo.getRefinement());
		this.setLocal(mapsTo.getLocalURI());
		this.setRemote(mapsTo.getRemoteURI());
	}
	
	void copy(MapsTo mapsTo) throws SBOLValidationException {
		((Identified)this).copy((Identified)mapsTo);
	}

	/**
	 * Returns the refinement property of this mapsTo.
	 *
	 * @return the refinement property of this mapsTo.
	 */
	public RefinementType getRefinement() {
		return refinement;
	}

	/**
	 * Sets the refinement property of this mapsTo to the given one.	 
	 *
	 * @param refinement the refinement property to set to
	 */
	public void setRefinement(RefinementType refinement) {
		this.refinement = refinement;
	}

	/**
	 * Returns this mapsTo's local component instance's URI.
	 *
	 * @return this mapsTo's local component instance's URI
	 */
	public URI getLocalURI() {
		return local;
	}
	
	/**
	 * Returns this mapsTo's local component instance identity. 
	 *
	 * @return the this mapsTo's local component instance identity
	 */
	public URI getLocalIdentity() {
		if (moduleDefinition!=null) {
			if (moduleDefinition.getFunctionalComponent(local)==null) return null;
			return moduleDefinition.getFunctionalComponent(local).getIdentity();
		} else if (componentDefinition!=null) {
			if (componentDefinition.getComponent(local)==null) return null;
			return componentDefinition.getComponent(local).getIdentity();
		}
		return null;
	}

	/**
	 * Returns this mapsTo's local component instance. 
	 *
	 * @return the this mapsTo's local component instance
	 */
	public ComponentInstance getLocal() {
		if (moduleDefinition!=null) {
			return moduleDefinition.getFunctionalComponent(local);
		} else if (componentDefinition!=null) {
			return componentDefinition.getComponent(local);
		}
		return null;
	}

	/**
	 * Retrieves referenced component definition by this mapsTo's local component instance. 
	 * 
	 * @return the referenced component definition by this mapsTo's local component instance
	 */
	public ComponentDefinition getLocalDefinition() {
		if (moduleDefinition!=null) {
			return moduleDefinition.getFunctionalComponent(local).getDefinition();
		} else if (componentDefinition!=null) {
			return componentDefinition.getComponent(local).getDefinition();
		}
		return null;
	}

	/**
	 * Sets the local property of this mapsTo to the given one.
	 * 
	 * @param local the given local property to set to
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated: 10802, 10803, 10804.
	 */
	public void setLocal(URI local) throws SBOLValidationException {
		if (local==null) {
			throw new SBOLValidationException("sbol-10802", this);
		}
		if (moduleDefinition!=null) {
			if (moduleDefinition.getFunctionalComponent(local)==null) {
				throw new SBOLValidationException("sbol-10804",this);
			}
		} else if (componentDefinition!=null) {
			if (componentDefinition.getComponent(local)==null) {
				throw new SBOLValidationException("sbol-10803",this);
			}
		}
		this.local = local;
	}

	/**
	 * Returns this mapsTo's remote URI. 
	 *
	 * @return this mapsTo's remote URI
	 */

	public URI getRemoteURI() {
		return remote;
	}
	
	/**
	 * Returns this mapsTo's remote component instance identity.
	 *
	 * @return this mapsTo's remote component instance identity
	 */
	public URI getRemoteIdentity() {
		if (module!=null) {
			if (module.getDefinition()==null) return null;
			if (module.getDefinition().getFunctionalComponent(remote)==null) return null;
			return module.getDefinition().getFunctionalComponent(remote).getIdentity();
		} else if (componentInstance!=null) {
			if (componentInstance.getDefinition()==null) return null;
			if (componentInstance.getDefinition().getComponent(remote)==null) return null;
			return componentInstance.getDefinition().getComponent(remote).getIdentity();
		}
		return null;
	}

	/**
	 * Returns this mapsTo's remote component instance.
	 *
	 * @return this mapsTo's remote component instance
	 */
	public ComponentInstance getRemote() {
		if (module!=null) {
			if (module.getDefinition()==null) return null;
			return module.getDefinition().getFunctionalComponent(remote);
		} else if (componentInstance!=null) {
			if (componentInstance.getDefinition()==null) return null;
			return componentInstance.getDefinition().getComponent(remote);
		}
		return null;
	}

	/**
	 * Returns the remote component definition referenced by mapsTo.
	 * 
	 * @return the remote component definition referenced by mapsTo
	 */
	public ComponentDefinition getRemoteDefinition() {
		if (module!=null) {
			if (module.getDefinition()==null) return null;
			return module.getDefinition().getFunctionalComponent(remote).getDefinition();
		} else if (componentInstance!=null) {
			if (componentInstance.getDefinition()==null) return null;
			return componentInstance.getDefinition().getComponent(remote).getDefinition();
		}
		return null;
	}

	/**
	 * Sets the remote property of this mapsTo to the given one.
	 *
	 * @param remote the remote property to set to
	 * @throws SBOLValidationException if any of the following SBOL validation rule was violated:
	 * 10805, 10807, 10808, 10809.
	 */
	public void setRemote(URI remote) throws SBOLValidationException {
		if (remote==null) {
			throw new SBOLValidationException("sbol-10805", this);
		}
		if (module!=null) {
			if (module.getDefinition()!=null) {
				if (module.getDefinition().getFunctionalComponent(remote)==null) {
					throw new SBOLValidationException("sbol-10809",this);
				}
				if (module.getDefinition().getFunctionalComponent(remote).getAccess().equals(AccessType.PRIVATE)) {
					throw new SBOLValidationException("sbol-10807",this);
				}
			}
		} else if (componentInstance!=null) {
			if (componentInstance.getDefinition()!=null) {
				if (componentInstance.getDefinition().getComponent(remote)==null) {
					throw new SBOLValidationException("sbol-10808",this);
				}
				if (componentInstance.getDefinition().getComponent(remote).getAccess().equals(AccessType.PRIVATE)) {
					throw new SBOLValidationException("sbol-10807",this);
				}
			}
		}
		this.remote = remote;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((local == null) ? 0 : local.hashCode());
		result = prime * result + ((refinement == null) ? 0 : refinement.hashCode());
		result = prime * result + ((remote == null) ? 0 : remote.hashCode());
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
		MapsTo other = (MapsTo) obj;
		if (local == null) {
			if (other.local != null)
				return false;
		} else if (!local.equals(other.local)) {
			if (getLocalIdentity() == null || other.getLocalIdentity() == null 
					|| !getLocalIdentity().equals(other.getLocalIdentity())) {
				return false;
			}
		}
		if (remote == null) {
			if (other.remote != null)
				return false;
		} else if (!remote.equals(other.remote)) {
			if (getRemoteIdentity() == null || other.getRemoteIdentity() == null 
					|| !getRemoteIdentity().equals(other.getRemoteIdentity())) {
				return false;
			}
		}
		if (refinement != other.refinement)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.Identified#deepCopy()
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #MapsTo(MapsTo)}.
	 */
	@Override
	MapsTo deepCopy() throws SBOLValidationException {
		return new MapsTo(this);
	}

	/**
	 * Assume this mapsTo has compliant URI, and all given parameters have compliant forms.
	 * This method is called by {@link Component#updateCompliantURI(String, String, String)}.
	 * 
  	 * @throws SBOLValidationException if any of the following is true:
  	 * <ul> 
	 * <li>an SBOL validation exception occurred in {@link URIcompliance#createCompliantURI(String, String, String)};</li>
	 * <li>an SBOL validation exception occurred in {@link #setWasDerivedFrom(URI)};
	 * <li>an SBOL validation exception occurred in {@link #setIdentity(URI)};
	 * <li>an SBOL validation exception occurred in {@link #setDisplayId(String)}; or
	 * <li>an SBOL validation exception occurred in {@link #setVersion(String)}.
	 * </ul>
	 */
	void updateCompliantURI(String URIprefix, String displayId, String version) throws SBOLValidationException {
		if (!this.getIdentity().equals(createCompliantURI(URIprefix,displayId,version))) {
			this.addWasDerivedFrom(this.getIdentity());
		}
		this.setIdentity(createCompliantURI(URIprefix,displayId,version));
		this.setPersistentIdentity(createCompliantURI(URIprefix,displayId,""));
		this.setDisplayId(displayId);
		this.setVersion(version);
	}

	/**
	 * @param moduleDefinition the moduleDefinition to set
	 */
	void setModuleDefinition(ModuleDefinition moduleDefinition) {
		this.moduleDefinition = moduleDefinition;
	}

	void setModule(Module module) {
		this.module = module;
	}

	void setComponentDefinition(ComponentDefinition componentDefinition) {
		this.componentDefinition = componentDefinition;
	}

	void setComponentInstance(ComponentInstance componentInstance) {
		this.componentInstance = componentInstance;
	}

	@Override
	public String toString() {
		return "MapsTo ["
				+ super.toString()
				+ ", refinement=" + refinement 
				+ ", local=" + local 
				+ ", remote=" + remote
				+ "]";
	}

}
