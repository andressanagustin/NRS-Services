/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class PreferredCustomerData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private String customerAccountID;
	private Double points = new Double(0);
	private Double couponAmount = new Double(0);
	private Double couponCount = new Double(0);
	private Double messageCount = new Double(0);
	private Double tranferredTransAmount = new Double(0);
	private Double transferredTransCount = new Double(0);
	private Double bonusPoints = new Double(0);
	private Double redeemedPoints = new Double(0);
	private Double entryMethod = new Double(0);

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
	 * @return the customerAccountID
	 */
	public String getCustomerAccountID() {
		return customerAccountID;
	}

	/**
	 * @param customerAccountID
	 *            the customerAccountID to set
	 */
	public void setCustomerAccountID(String customerAccountID) {
		this.customerAccountID = customerAccountID;
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
	 * @return the couponAmount
	 */
	public Double getCouponAmount() {
		return couponAmount;
	}

	/**
	 * @param couponAmount
	 *            the couponAmount to set
	 */
	public void setCouponAmount(Double couponAmount) {
		this.couponAmount = couponAmount;
	}

	/**
	 * @return the couponCount
	 */
	public Double getCouponCount() {
		return couponCount;
	}

	/**
	 * @param couponCount
	 *            the couponCount to set
	 */
	public void setCouponCount(Double couponCount) {
		this.couponCount = couponCount;
	}

	/**
	 * @return the messageCount
	 */
	public Double getMessageCount() {
		return messageCount;
	}

	/**
	 * @param messageCount
	 *            the messageCount to set
	 */
	public void setMessageCount(Double messageCount) {
		this.messageCount = messageCount;
	}

	/**
	 * @return the tranferredTransAmount
	 */
	public Double getTranferredTransAmount() {
		return tranferredTransAmount;
	}

	/**
	 * @param tranferredTransAmount
	 *            the tranferredTransAmount to set
	 */
	public void setTranferredTransAmount(Double tranferredTransAmount) {
		this.tranferredTransAmount = tranferredTransAmount;
	}

	/**
	 * @return the transferredTransCount
	 */
	public Double getTransferredTransCount() {
		return transferredTransCount;
	}

	/**
	 * @param transferredTransCount
	 *            the transferredTransCount to set
	 */
	public void setTransferredTransCount(Double transferredTransCount) {
		this.transferredTransCount = transferredTransCount;
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
	 * @return the entryMethod
	 */
	public Double getEntryMethod() {
		return entryMethod;
	}

	/**
	 * @param entryMethod
	 *            the entryMethod to set
	 */
	public void setEntryMethod(Double entryMethod) {
		this.entryMethod = entryMethod;
	}

}
