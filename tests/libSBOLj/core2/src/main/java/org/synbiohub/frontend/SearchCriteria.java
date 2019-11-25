
package org.synbiohub.frontend;

/**
 * Class for search criteria
 * @author James McLaughlin
 * @author Chris Myers
 *
 */
public class SearchCriteria
{
    private String key;
    private String value;
    
	/**
	 * Returns the search key
	 * @return search key
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * Set the search key
	 * @param key the search key
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Return the search value
	 * @return search value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Set the search value
	 * @param value the search value
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
