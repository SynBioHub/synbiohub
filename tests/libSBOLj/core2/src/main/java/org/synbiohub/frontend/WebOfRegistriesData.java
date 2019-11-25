package org.synbiohub.frontend;

/**
 * The metadata common to all SBOL data objects
 * @author James McLaughlin
 *
 */
public class WebOfRegistriesData
{
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Registry [id=" + id + ", name=" + name + ", description=" + description +
				", uriPrefix=" + uriPrefix + ", instanceUrl=" + instanceUrl + ", approved=" + approved +
				", administratorEmail=" + administratorEmail + ", updateWorking=" + updateWorking + "]";
	}

	private String id;
	private String uriPrefix;
	private String instanceUrl;
	private String description;
	private String name;
	private String approved;
    private String administratorEmail;
    private String updateWorking;
    
	/**
	 * @return uriPrefix
	 */
	public String getUriPrefix() {
		return uriPrefix;
	}
	
	/**
	 * @param uriPrefix set the uriPrefix
	 */
	public void setUriPrefix(String uriPrefix) {
		this.uriPrefix = uriPrefix;
	}
	
	/**
	 * @return instanceUrl
	 */
	public String getInstanceUrl() {
		return instanceUrl;
	}

	/**
	 * @param instanceUrl set the instanceUrl
	 */
	public void setInstanceUrl(String instanceUrl) {
		this.instanceUrl = instanceUrl;
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
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id set the id
	 */
	public void setId(String id) {
		this.id = id;
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
	 * @return approved
	 */
	public String getApproved() {
		return approved;
	}

	/**
	 * @param approved set approved
	 */
	public void setApproved(String approved) {
		this.approved = approved;
	}
	
	/**
	 * @return administratorEmail
	 */
	public String getAdministratorEmail() {
		return administratorEmail;
	}

	/**
	 * @param administratorEmail set administratorEmail
	 */
	public void setAdministratorEmail(String administratorEmail) {
		this.administratorEmail = administratorEmail;
	}
	
	/**
	 * @return updateWorking
	 */
	public String getUpdateWorking() {
		return updateWorking;
	}

	/**
	 * @param updateWorking set updateWorking
	 */
	public void setUpdateWorking(String updateWorking) {
		this.updateWorking = updateWorking;
	}
}

