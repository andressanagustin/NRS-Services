/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class PriceModificationLineItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	private Integer promotionID;
	private Double percentage;
	private Double amount = new Double(0);
	private Boolean cancelFlag;
	private String priceModificationTypeCode;
	private Boolean proratedFlag;

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
	 * @return the sequenceNumber
	 */
	public Integer getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * @param sequenceNumber
	 *            the sequenceNumber to set
	 */
	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	/**
	 * @return the promotionID
	 */
	public Integer getPromotionID() {
		return promotionID;
	}

	/**
	 * @param promotionID
	 *            the promotionID to set
	 */
	public void setPromotionID(Integer promotionID) {
		this.promotionID = promotionID;
	}

	/**
	 * @return the percentage
	 */
	public Double getPercentage() {
		return percentage;
	}

	/**
	 * @param percentage
	 *            the percentage to set
	 */
	public void setPercentage(Double percentage) {
		this.percentage = percentage;
	}

	/**
	 * @return the amount
	 */
	public Double getAmount() {
		return amount;
	}

	/**
	 * @param amount
	 *            the amount to set
	 */
	public void setAmount(Double amount) {
		this.amount = amount;
	}

	/**
	 * @return the cancelFlag
	 */
	public Boolean getCancelFlag() {
		return cancelFlag;
	}

	/**
	 * @param cancelFlag
	 *            the cancelFlag to set
	 */
	public void setCancelFlag(Boolean cancelFlag) {
		this.cancelFlag = cancelFlag;
	}

	/**
	 * @return the priceModificationTypeCode
	 */
	public String getPriceModificationTypeCode() {
		return priceModificationTypeCode;
	}

	/**
	 * @param priceModificationTypeCode
	 *            the priceModificationTypeCode to set
	 */
	public void setPriceModificationTypeCode(String priceModificationTypeCode) {
		this.priceModificationTypeCode = priceModificationTypeCode;
	}

	/**
	 * @return the proratedFlag
	 */
	public Boolean getProratedFlag() {
		return proratedFlag;
	}

	/**
	 * @param proratedFlag
	 *            the proratedFlag to set
	 */
	public void setProratedFlag(Boolean proratedFlag) {
		this.proratedFlag = proratedFlag;
	}
}
