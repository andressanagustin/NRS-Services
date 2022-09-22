/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class PreferredCustomerSecPtsData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Double clubNumber = new Double(0);
	private Double points = new Double(0);
	private Double redeemedPoints = new Double(0);
	private Double bonusPoints = new Double(0);
	private Double sales = new Double(0);

	/**
	 * @return the transactionID
	 */
	public Integer getTransactionID() {
		return transactionID;
	}

	/**
	 * @param transactionID
	 *            the transactionID to set
	 */
	public void setTransactionID(Integer transactionID) {
		this.transactionID = transactionID;
	}

	/**
	 * @return the clubNumber
	 */
	public Double getClubNumber() {
		return clubNumber;
	}

	/**
	 * @param clubNumber
	 *            the clubNumber to set
	 */
	public void setClubNumber(Double clubNumber) {
		this.clubNumber = clubNumber;
	}

	/**
	 * @return the points
	 */
	public Double getPoints() {
		return points;
	}

	/**
	 * @param points
	 *            the points to set
	 */
	public void setPoints(Double points) {
		this.points = points;
	}

	/**
	 * @return the redeemedPoints
	 */
	public Double getRedeemedPoints() {
		return redeemedPoints;
	}

	/**
	 * @param redeemedPoints
	 *            the redeemedPoints to set
	 */
	public void setRedeemedPoints(Double redeemedPoints) {
		this.redeemedPoints = redeemedPoints;
	}

	/**
	 * @return the bonusPoints
	 */
	public Double getBonusPoints() {
		return bonusPoints;
	}

	/**
	 * @param bonusPoints
	 *            the bonusPoints to set
	 */
	public void setBonusPoints(Double bonusPoints) {
		this.bonusPoints = bonusPoints;
	}

	/**
	 * @return the sales
	 */
	public Double getSales() {
		return sales;
	}

	/**
	 * @param sales
	 *            the sales to set
	 */
	public void setSales(Double sales) {
		this.sales = sales;
	}
}
