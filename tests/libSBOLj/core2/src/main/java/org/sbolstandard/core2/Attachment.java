package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;

import java.net.URI;

/**
 * Represents a Attachment object in the SBOL data model.
 * 
 * @author Igor Durovic
 * @author Chris Myers
 * @version 2.3
 */

public class Attachment extends TopLevel {
	
	/**
	 * The source is a link to the external file and is REQUIRED.
	 */
	private URI source;
	
	/** 
	 * The format is an ontology term indicating the format of the file. 
	 * It is RECOMMENDED to use the EDAM ontology for file formats, which
	 * includes a variety of experimental data formats. The format is an 
	 * OPTIONAL field.
	 */
	private URI format;
	
	/**
	 * The size is a long integer indicating the file size in bytes. This
	 * may be used by client applications accessing files over RESTful APIs.
	 * This field is OPTIONAL.
	 * 
	 */
	private Long size;
	
	/**
	 * The hash is a string used to retrieve files from a cache. This field
	 * is OPTIONAL.
	 */
	private String hash;

	Attachment(URI identity, URI source) throws SBOLValidationException {
		super(identity);
		setSource(source);
	}
	
	/**
	 * @param attachment
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in any of the
	 *             following constructors or methods:
	 *             <ul>
	 *             <li>{@link TopLevel#TopLevel(TopLevel)},</li>
	 *             </ul>
	 */
	private Attachment(Attachment attachment) throws SBOLValidationException {
		super(attachment);

		this.setSource(attachment.getSource());
		this.setFormat(attachment.getFormat());
		this.setSize(attachment.getSize());
		this.setHash(attachment.getHash());
	}
	
	/**
	 * Returns this attachment's source property
	 *
	 * @return the URI representing the source property
	 */
	public URI getSource() {
		return this.source;
	}
	
	/**
	 * Sets the source property to the given one.
	 * 
	 * @param source the source property to set
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 11502.
	 */
	public void setSource(URI source) throws SBOLValidationException {
		if (source==null) {
			throw new SBOLValidationException("sbol-13202", this);
		}
		this.source = source;
	}
	
	/**
	 * Checks if the format property is set.
	 * 
	 * @return {@code true} if it is not {@code null}, {@code false} otherwise
	 */
	public boolean isSetFormat() {
		return format != null;
	}
	
	/**
	 * Returns this attachment's format property
	 *
	 * @return the URI representing the format property
	 */
	public URI getFormat() {
		return this.format;
	}

	/**
	 * Sets the URI of this attachment's format property
	 *
	 * @param format
	 *            the given URI to set to
	 */
	public void setFormat(URI format) {
		this.format = format;
	}
	
	/**
	 * Sets the format property of the attachment to {@code null}.
	 */
	public void unsetFormat() {
		this.format = null;
	}
	
	/**
	 * Checks if the size property is set.
	 * 
	 * @return {@code true} if it is not {@code null}, {@code false} otherwise
	 */
	public boolean isSetSize() {
		return (size != null);
	}
	
	/**
	 * Returns this attachment's size property
	 *
	 * @return the size property
	 */
	public Long getSize() {
		return this.size;
	}

	/**
	 * Sets the value of this attachment's size property
	 *
	 * @param size
	 *            the given size to set to
	 */
	public void setSize(long size) {
		this.size = size;
	}
	
	/**
	 * Sets the size property of the attachment to -1.
	 */
	public void unsetSize() {
		this.size = null;
	}
	
	/**
	 * Checks if the hash property is set.
	 * 
	 * @return {@code true} if it is not {@code null}, {@code false} otherwise
	 */
	public boolean isSetHash() {
		return hash != null;
	}
	
	/**
	 * Returns this attachment's hash property
	 *
	 * @return the hash property
	 */
	public String getHash() {
		return this.hash;
	}

	/**
	 * sets this attachment's hash property
	 *
	 * @param hash
	 *            the given hash to set to
	 */
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	/**
	 * Sets the hash property of the attachment to {@code null}.
	 */
	public void unsetHash() {
		this.hash = null;
	}

	@Override
	Attachment deepCopy() throws SBOLValidationException {
		return new Attachment(this);
	}
	
	void copy(Attachment attachment) throws SBOLValidationException {
		((TopLevel) this).copy((TopLevel) attachment);
		
		if (attachment.isSetFormat()) {
			this.setFormat(attachment.getFormat());
		}
		if (attachment.isSetSize()) {
			this.setSize(attachment.getSize());
		}
		if (attachment.isSetHash()) {
			this.setHash(attachment.getHash());
		}
		
		this.setSource(attachment.getSource());
	}

	/**
	 * @throws SBOLValidationException
	 *             if an SBOL validation rule violation occurred in any of the
	 *             following constructors or methods:
	 *             <ul>
	 *             <li>{@link #deepCopy()},</li>
	 *             <li>{@link URIcompliance#createCompliantURI(String, String, String)},</li>
	 *             <li>{@link #setDisplayId(String)},</li>
	 *             <li>{@link #setVersion(String)},</li>
	 *             <li>{@link #setWasDerivedFrom(URI)},</li>
	 *             <li>{@link #setIdentity(URI)}</li>
	 *             </ul>
	 */
	@Override
	Attachment copy(String URIprefix, String displayId, String version) throws SBOLValidationException {
		Attachment cloned = this.deepCopy();
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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode() * prime;

		result = prime * result + this.source.hashCode();
		result = prime * result + (this.isSetFormat() ? this.format.hashCode() : 0);
		result = prime * result + (this.isSetSize() ? this.size.hashCode() : 0);
		result = prime * result + (this.isSetHash() ? this.hash.hashCode() : 0);

		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbolstandard.core2.abstract_classes.Documented#equals(java.lang.Instance)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		Attachment other = (Attachment) obj;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (format == null) {
			if (other.format != null)
				return false;
		} else if (!format.equals(other.format))
			return false;
		if (size == null) {
			if (other.size != null)
				return false;
		} else if (!size.equals(other.size))
			return false;
		if (hash == null) {
			if (other.hash != null)
				return false;
		} else if (!hash.equals(other.hash))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Attachment [" + super.toString()
				+ ("source=" + this.getSource())
				+ (this.isSetFormat() ? ", format=" + this.getFormat() : "")
				+ (this.isSetSize() ? ", size=" + this.getSize() : "")
				+ (this.isSetHash() ? ", hash=" + this.getHash() : "")
				+ "]";
	}

}
