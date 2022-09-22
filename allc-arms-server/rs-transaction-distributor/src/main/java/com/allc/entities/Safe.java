/**
 * 
 */
package com.allc.entities;

/**
 * @author GUSTAVOK
 * 
 */
public class Safe extends TenderRepository {
	private Integer tenderRepositoryID;
	private String description;

	/**
	 * @return the tenderRepositoryID
	 */
	public Integer getTenderRepositoryID() {
		return tenderRepositoryID;
	}

	/**
	 * @param tenderRepositoryID
	 *            the tenderRepositoryID to set
	 */
	public void setTenderRepositoryID(Integer tenderRepositoryID) {
		this.tenderRepositoryID = tenderRepositoryID;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}
