/**
 * 
 */
package com.allc.entities;

/**
 * @author GUSTAVOK
 * 
 */
public class Tender {
	private Integer tenderID;
	private String tenderTypeCode;
	private Integer localCurrency;
	private String description;

	/**
	 * @return the tenderID
	 */
	public Integer getTenderID() {
		return tenderID;
	}

	/**
	 * @param tenderID
	 *            the tenderID to set
	 */
	public void setTenderID(Integer tenderID) {
		this.tenderID = tenderID;
	}

	/**
	 * @return the tenderTypeCode
	 */
	public String getTenderTypeCode() {
		return tenderTypeCode;
	}

	/**
	 * @param tenderTypeCode
	 *            the tenderTypeCode to set
	 */
	public void setTenderTypeCode(String tenderTypeCode) {
		this.tenderTypeCode = tenderTypeCode;
	}

	/**
	 * @return the localCurrency
	 */
	public Integer getLocalCurrency() {
		return localCurrency;
	}

	/**
	 * @param localCurrency
	 *            the localCurrency to set
	 */
	public void setLocalCurrency(Integer localCurrency) {
		this.localCurrency = localCurrency;
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
