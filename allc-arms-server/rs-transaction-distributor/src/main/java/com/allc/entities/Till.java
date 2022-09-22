/**
 * 
 */
package com.allc.entities;

import java.util.Date;

/**
 * @author GUSTAVOK
 * 
 */
public class Till extends TenderRepository {
	private Integer tenderRepositoryID;
	private String statusCode;
	private Double defaultOpeningCashBalanceAmount = new Double(0);
	private Date statusDateTime;

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
	 * @return the statusCode
	 */
	public String getStatusCode() {
		return statusCode;
	}

	/**
	 * @param statusCode
	 *            the statusCode to set
	 */
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * @return the defaultOpeningCashBalanceAmount
	 */
	public Double getDefaultOpeningCashBalanceAmount() {
		return defaultOpeningCashBalanceAmount;
	}

	/**
	 * @param defaultOpeningCashBalanceAmount
	 *            the defaultOpeningCashBalanceAmount to set
	 */
	public void setDefaultOpeningCashBalanceAmount(
			Double defaultOpeningCashBalanceAmount) {
		this.defaultOpeningCashBalanceAmount = defaultOpeningCashBalanceAmount;
	}

	/**
	 * @return the statusDateTime
	 */
	public Date getStatusDateTime() {
		return statusDateTime;
	}

	/**
	 * @param statusDateTime
	 *            the statusDateTime to set
	 */
	public void setStatusDateTime(Date statusDateTime) {
		this.statusDateTime = statusDateTime;
	}
}
