package org.sbolstandard.core2;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a TopLevel object in the SBOL data model.
 * 
 * @author Zhen Zhang
 * @author Nicholas Roehner
 * @author Chris Myers
 * @version 2.1
 */

public abstract class TopLevel extends Identified {

	/**
	 * The abbreviation for the Collection type in URI
	 */
	public static final String COLLECTION = "col";
	/**
	 * The abbreviation for the ModuleDefinition type in URI
	 */
	public static final String MODULE_DEFINITION = "md";
	/**
	 * The abbreviation for the Model type in URI
	 */
	public static final String MODEL = "mod";
	/**
	 * The abbreviation for the ComponentDefinition type in URI
	 */
	public static final String COMPONENT_DEFINITION = "cd";
	/**
	 * The abbreviation for the Sequence type in URI
	 */
	public static final String SEQUENCE = "seq";
	/**
	 * The abbreviation for the GenericTopLevel type in URI
	 */
	public static final String GENERIC_TOP_LEVEL = "gen";
	/**
	 * The abbreviation for the Activity type in URI
	 */
	public static final String ACTIVITY = "act";
	/**
	 * The abbreviation for the Agent type in URI
	 */
	public static final String AGENT = "agent";
	/**
	 * The abbreviation for the Plan type in URI
	 */
	public static final String PLAN = "plan";
	/**
	 * The abbreviation for the CombinatorialDerivation type in URI
	 */
	public static final String COMBINATORIAL_DERIVATION = "comb";
	/**
	 * The abbreviation for the Implementation type in URI
	 */
	public static final String IMPLEMENTATION = "impl";
	/**
	 * The abbreviation for the Attachment type in URI
	 */
	public static final String ATTACHMENT = "attach";
	/**
	 * The abbreviation for the Annotation type in URI
	 */
	public static final String ANNOTATION = "anno";
	/**
	 * The abbreviation for the Experiment type in URI
	 */
	public static final String EXPERIMENT = "expt";
	/**
	 * The abbreviation for the Experimental_DATA type in URI
	 */
	public static final String EXPERIMENTAL_DATA = "data";

	private HashSet<URI> attachments;

	/**
	 * @param identity
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link Identified#Identified(URI)}.
	 */
	TopLevel(URI identity) throws SBOLValidationException {
		super(identity);
		attachments = new HashSet<URI>();
	}

	/**
	 * @param topLevel
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in
	 *             {@link Identified#Identified(Identified)}.
	 */
	TopLevel(TopLevel topLevel) throws SBOLValidationException {
		super(topLevel);
		attachments = new HashSet<URI>();
		for (URI attachment : topLevel.getAttachmentURIs()) {
			this.addAttachment(URI.create(attachment.toString()));
		}
	}

	void copy(TopLevel topLevel) throws SBOLValidationException {
		((Identified) this).copy((Identified) topLevel);
		for (URI attachment : topLevel.getAttachmentURIs()) {
			this.addAttachment(URI.create(attachment.toString()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbolstandard.core2.Identified#deepCopy()
	 */
	@Override
	abstract Identified deepCopy() throws SBOLValidationException;
	
	/**
	 * Make a copy of a top-level object whose URI and its descendants' URIs
	 * (children, grandchildren, etc) are all compliant. It first makes a deep copy
	 * of this object, then updates its own identity URI and all of its descendants'
	 * identity URIs according to the given {@code URIprefix, displayId}, and
	 * {@code version}. This method also updates the {@code displayId} and
	 * {@code version} fields for each updated object.
	 * 
	 * @return the copied top-level object if this object and all of its descendants
	 *         have compliant URIs, and {@code null} otherwise.
	 */
	abstract Identified copy(String URIprefix, String displayId, String version) throws SBOLValidationException;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((attachments == null) ? 0 : attachments.hashCode());
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
		TopLevel other = (TopLevel) obj;
		if (attachments == null) {
			if (other.attachments != null)
				return false;
		} else if (!attachments.equals(other.attachments))
			return false;
		return true;
	}
	
	/**
	 * Test if the given object's identity URI is compliant.
	 * 
	 * @param objURI
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in any of the
	 *             following methods:
	 *             <ul>
	 *             <li>{@link URIcompliance#isTopLevelURIformCompliant(URI)},</li>
	 *             <li>{@link URIcompliance#isURIcompliant(Identified)}, or</li>
	 *             <li>{@link #checkDescendantsURIcompliance()}.</li>
	 *             </ul>
	 */
	void isURIcompliant() throws SBOLValidationException {
		// URIcompliance.isTopLevelURIformCompliant(this.getIdentity());
		try {
			URIcompliance.isURIcompliant(this);
		} catch (SBOLValidationException e) {
			throw new SBOLValidationException(e.getRule(), this);
		}
		this.checkDescendantsURIcompliance();
	}

	/**
	 * Check if this top-level object's and all of its descendants' URIs are all
	 * compliant.
	 * 
	 * @throws SBOLValidationException
	 *             validation error
	 */
	abstract void checkDescendantsURIcompliance() throws SBOLValidationException;

	/**
	 * Adds the URI of the given Attachment instance to this top level's 
	 * set of attachment URIs. This method calls {@link #addAttachment(URI)} with this Attachment URI.
	 *
	 * @param attachment the Attachment instance whose identity URI to be added
	 * @return {@code true} if this set did not already contain the identity URI of the given Attachment, {@code false} otherwise.
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: TODO
	 */
	public boolean addAttachment(Attachment attachment) throws SBOLValidationException {
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (this.getSBOLDocument().getAttachment(attachment.getIdentity())==null) {
				throw new SBOLValidationException("sbol-XXXXX", attachment);
			}
		}
		return this.addAttachment(attachment.getIdentity());
	}
	
	/**
	 * Adds the given attachment URI to this top level's list of attachments.
	 * 
	 * @param attachmentURI
	 * @return {@code true} if this set did not already contain the identity URI of the given Attachment, {@code false} otherwise.
	 * @throws SBOLValidationException 
	 *              if the following SBOL validation rule was violated: XXXXX
	 */
	boolean addAttachment(URI attachmentURI) throws SBOLValidationException {
		if (this.getSBOLDocument() != null && this.getSBOLDocument().isComplete()) {
			if (this.getSBOLDocument().getAttachment(attachmentURI) == null) {
				throw new SBOLValidationException("sbol-XXXXX", this);
			}
		}
		return attachments.add(attachmentURI);
	}
	
	/**
	 * Constructs a compliant attachment URI with the given display ID and version, and then adds this URI
	 * to this top level's set of attachment URIs.
	 * <p>
	 * This method creates a compliant sequence URI with the default
	 * URI prefix, which was set in the SBOLDocument instance hosting this top level, the given 
	 * display ID and version. It then calls {@link #addAttachment(URI)} with this Attachment URI.
	 *
	 * @param displayId the display ID of the attachment whose identity URI is to be added
	 * @param version version of the attachment whose identity URI is to be added
	 * @return {@code true} if this set did not already contain the given attachment's URI, {@code false} otherwise. 
	 * @throws SBOLValidationException see {@link #addAttachment(URI)} 
	 */
	public boolean addAttachment(String displayId,String version) throws SBOLValidationException {
		URI attachmentURI = URIcompliance.createCompliantURI(this.getSBOLDocument().getDefaultURIprefix(),
				TopLevel.ATTACHMENT, displayId, version, this.getSBOLDocument().isTypesInURIs());
		return addAttachment(attachmentURI);
	}

	/**
	 * Constructs a compliant sequence URI using the given attachment display ID, and then adds this URI to 
	 * this top level's set of attachment URIs. This method calls {@link #addAttachment(String, String)} with
	 * the given attachment display ID and an empty string as its version. 
	 *
	 * @param displayId the display ID of the attachment whose identity URI is to be added
	 * @return {@code true} if this set did not already contain the given attachment's URI, {@code false} otherwise.
	 * @throws SBOLValidationException see {@link #addAttachment(String, String)}
	 */
	public boolean addAttachment(String displayId) throws SBOLValidationException {
		return addAttachment(displayId,"");
	}

	
	/**
	 * Returns the set of attachments referenced by this top level.
	 *
	 * @return the set of attachments referenced by this top level
	 */
	public Set<Attachment> getAttachments() {
		if (this.getSBOLDocument()==null) return null;
		Set<Attachment> resolved = new HashSet<>();
		for(URI su : attachments) {
			Attachment attachment = this.getSBOLDocument().getAttachment(su);
			if(attachment != null) {
				resolved.add(attachment);
			}
		}
		return resolved;
	}
	
	/**
	 * Returns the set of attachment URIs referenced by this top level.
	 *
	 * @return the set of attachment URIs referenced by this top level
	 */
	public Set<URI> getAttachmentURIs() {
		Set<URI> result = new HashSet<>();
		result.addAll(attachments);
		return result;
	}

	/**
	 * Removes all entries of this top level's list of attachments. The list will be
	 * empty after this call returns.
	 */
	public void clearAttachments() {
		attachments.clear();
	}
	
	/**
	 * Checks if the given attachment URI is included in this top level's
	 * set of attachment URIs.
	 *
	 * @param attachmentURI the attachment URI to be checked
	 * @return {@code true} if this set contains the given attachment URI, {@code false} otherwise.
	 */
	public boolean containsAttachment(URI attachmentURI) {
		return attachments.contains(attachmentURI);
	}
	
	/**
	 * Removes the given attachment from the list of attachments.
	 *
	 * @param attachment
	 *            an attachment uri be removed
	 * @return {@code true} if the matching attachment is removed
	 *         successfully, {@code false} otherwise.
	 */
	public boolean removeAttachment(URI attachment) {
		return attachments.remove(attachment);
	}

	/**
	 * Clears the existing set of attachments first, and then adds the given set of
	 * the attachments to this top level.
	 *
	 * @param attachments
	 *            The set of attachments for this top level.
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in any of the
	 *             following methods:
	 *             <ul>
	 *             <li>{@link #clearAttachments()} or</li>
	 *             <li>{@link #addAttachment(Attachment)}</li>
	 *             </ul>
	 */
	public void setAttachments(Set<URI> attachments) throws SBOLValidationException {
		clearAttachments();
		for (URI attachment : attachments) {
			addAttachment(attachment);
		}
	}

}
