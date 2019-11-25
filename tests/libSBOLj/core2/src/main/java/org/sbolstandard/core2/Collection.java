package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an Collection object in the SBOL data model.
 * 
 * @author Zhen Zhang
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */

public class Collection extends TopLevel{

	private Set<URI> members;

	/**
	 * @param identity
	 * @throws SBOLValidationException if an SBOL validation rule was violated in the following constructor:
	 * {@link TopLevel#TopLevel(URI)}. 
	 */
	Collection(URI identity) throws SBOLValidationException {
		super(identity);
		this.members = new HashSet<>();
	}

	/**
	 * @param collection
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in the following 
	 * constructor or method:
	 * <ul>
	 * <li>{@link TopLevel#TopLevel(TopLevel)}, or</li>
	 * <li>{@link #setMembers(Set)}.</li>
	 * </ul>
	 */
	private Collection(Collection collection) throws SBOLValidationException {
		//super(collection.getIdentity());
		super(collection);
		this.members = new HashSet<>();
		for (URI member : collection.getMemberURIs()) {
			this.addMember(member);
		}
	}
	
	void copy(Collection collection) throws SBOLValidationException {
		((TopLevel)this).copy((TopLevel)collection);
		for (URI member : collection.getMemberURIs()) {
			this.addMember(member);
		}
	}

	/**
	 * Adds the given member URI to this Collection instance's
	 * set of reference member URIs.
	 *
	 * @param memberURI References to a TopLevel instance
	 * @return {@code true} if the matching member reference has been added successfully,
	 *         {@code false} otherwise.
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 12103.
	 */
	public boolean addMember(URI memberURI) throws SBOLValidationException {
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (this.getSBOLDocument().getTopLevel(memberURI)==null) {
				throw new SBOLValidationException("sbol-12103", this);
			}
		}
		return members.add(memberURI);
	}

	/**
	 * Removes the given member from this Collection instance's set of members.
	 *
	 * @param memberURI the member identity URI to be removed from this Collection's members
	 * @return {@code true} if the matching member is removed successfully,
	 *         {@code false} otherwise.
	 */
	public boolean removeMember(URI memberURI) {
		return members.remove(memberURI);
	}

	/**
	 * Clears the existing set of members of this Collection instance first, then adds the given
	 * set of the member references to it.
	 * 
	 * @param members a set of identity URIs of zero or more TopLevel instances to be added
	 * @throws SBOLValidationException see SBOL validation rule violation at {@link Collection#addMember(URI)}
	 */
	public void setMembers(Set<URI> members) throws SBOLValidationException {
		clearMembers();
		for (URI member : members) {
			addMember(member);
		}
	}

	/**
	 * Returns the set of member URIs referenced by this Collection instance.
	 *
	 * @return the set of member URIs referenced by this Collection instance.
	 */
	public Set<URI> getMemberURIs() {
		Set<URI> result = new HashSet<>();
		result.addAll(members);
		return result;
	}

	/**
	 * Returns the set of member identities referenced by this Collection instance.
	 *
	 * @return the set of member identities referenced by this Collection instance.
	 */
	public Set<URI> getMemberIdentities() {
		Set<URI> result = new HashSet<>();
		for (URI memberURI : members) {
			TopLevel member = this.getSBOLDocument().getTopLevel(memberURI);
			if(member != null) {
				result.add(member.getIdentity());
			}
		}
		return result;
	}

	/**
	 * Returns the set of objects referenced by this Collection instance's members.
	 *
	 * @return the set of objects referenced by this Collection instance's members.
	 */
	public Set<TopLevel> getMembers() {
		Set<TopLevel> result = new HashSet<>();
		for (URI memberURI : members) {
			TopLevel member = this.getSBOLDocument().getTopLevel(memberURI);
			if(member != null) {
				result.add(member);
			}
		}
		return result;
	}

	/**
	 * Checks if the given URI is included in this Collection
	 * instance's set of member URIs.
	 *
	 * @param memberURI a URI that is checked against this Collection instance's list of member URIs 
	 * @return {@code true} if its members contain the given URI, {@code false} otherwise.
	 */
	public boolean containsMember(URI memberURI) {
		return members.contains(memberURI);
	}

	/**
	 * Removes all entries of this Collection instance's set of reference
	 * member URIs. The set will be empty after this call returns.
	 */
	public void clearMembers() {
		members.clear();
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.abstract_classes.Documented#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((members == null) ? 0 : members.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.abstract_classes.Documented#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Collection other = (Collection) obj;
		if (members == null) {
			if (other.members != null)
				return false;
		} else if (!members.equals(other.members)) {
			if (getMemberIdentities().size()!=getMemberURIs().size() ||
					other.getMemberIdentities().size()!=other.getMemberURIs().size() ||
					!getMemberIdentities().equals(other.getMemberIdentities())) {
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.abstract_classes.TopLevel#deepCopy()
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #Collection(Collection)}.
	 */
	@Override
	Collection deepCopy() throws SBOLValidationException {
		return new Collection(this);
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.abstract_classes.TopLevel#copy(java.lang.String, java.lang.String, java.lang.String)
	 */
	/**
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of the following methods:
	 * <ul>
	 * <li>{@link #deepCopy()},</li>
	 * <li>{@link URIcompliance#createCompliantURI(String, String, String)},</li>
	 * <li>{@link #setDisplayId(String)},</li>
	 * <li>{@link #setVersion(String)},</li>
	 * <li>{@link #setWasDerivedFrom(URI)}, or</li>
	 * <li>{@link #setIdentity(URI)}.</li>
	 * </ul>
	 */
	@Override
	Collection copy(String URIprefix, String displayId, String version) throws SBOLValidationException {
		Collection cloned = this.deepCopy();
		cloned.setPersistentIdentity(createCompliantURI(URIprefix,displayId,""));
		cloned.setDisplayId(displayId);
		cloned.setVersion(version);
		URI newIdentity = createCompliantURI(URIprefix,displayId,version);
		if (!this.getIdentity().equals(newIdentity)) {
			cloned.addWasDerivedFrom(this.getIdentity());
		} else {
			cloned.setWasDerivedFroms(this.getWasDerivedFroms());
		}
		cloned.setIdentity(newIdentity);
		return cloned;
	}

	/* (non-Javadoc)
	 * @see org.sbolstandard.core2.abstract_classes.TopLevel#checkDescendantsURIcompliance()
	 */
	@Override
	void checkDescendantsURIcompliance() {//throws SBOLValidationException {
		//URIcompliance.isTopLevelURIformCompliant(this.getIdentity());
	}

	@Override
	public String toString() {
		return "Collection ["
				+ super.toString()
				+ (members.size()>0?", members=" + members:"")  
				+ "]";
	}

}
