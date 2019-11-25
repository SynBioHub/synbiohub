
package org.synbiohub.frontend;

import java.util.ArrayList;

/**
 * Class for search queries
 * @author James McLaughlin
 * @author Chris Myers
 *
 */
public class SearchQuery
{
    private Integer offset;
    private Integer limit;

    private ArrayList<SearchCriteria> criteria
            = new ArrayList<SearchCriteria>();

	/**
	 * Returns the search offset
	 * @return search offset
	 */
	public Integer getOffset() {
		return offset;
	}

	/**
	 * Set the search offset
	 * @param offset the search offset
	 */
	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	/**
	 * Set the search limit
	 * @return the search limit
	 */
	public Integer getLimit() {
		return limit;
	}

	/**
	 * Set the search limit
	 * @param limit the search limit
	 */
	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	/**
	 * Return the search criteria
	 * @return search criteria
	 */
	public ArrayList<SearchCriteria> getCriteria() {
		return criteria;
	}


	/**
	 * Add a search criterion
	 * @param criterion a search criterion
	 */
	public void addCriteria(SearchCriteria criterion) {
		this.criteria.add(criterion);
	}
	
	/**
	 * Set the search criteria
	 * @param criteria the search criteria
	 */
	public void setCriteria(ArrayList<SearchCriteria> criteria) {
		this.criteria = criteria;
	}
}
