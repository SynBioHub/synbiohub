package org.synbiohub.frontend;

/**
 * The metadata common to all SBOL data objects
 * @author James McLaughlin
 *
 */
public class IdentifiedMetadata
{
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IdentifiedMetadata [type =" + type + ", uri=" + uri + ", name=" + name + ", displayId=" + displayId
				+ ", description=" + description + ", version=" + version + "]";
	}

	private String type;
	private String uri;
	private String name;
	private String displayId;
	private String description;
    private String version;
    
	/**
	 * @return URI 
	 */
	public String getUri() {
		return uri;
	}
	
	/**
	 * @param uri set the URI
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name set the name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return displayId
	 */
	public String getDisplayId() {
		return displayId;
	}

	/**
	 * @param displayId set the displayId
	 */
	public void setDisplayId(String displayId) {
		this.displayId = displayId;
	}

	/**
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description set the description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version set the version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Return the type
	 * @return type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Set the type
	 * @param type set the type
	 */
	public void setType(String type) {
		this.type = type;
	}
}

