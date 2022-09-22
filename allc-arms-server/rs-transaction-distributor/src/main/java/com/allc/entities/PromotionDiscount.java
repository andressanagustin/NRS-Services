package com.allc.entities;

import java.io.Serializable;

public class PromotionDiscount implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	private Integer promotionSequenceNumber;
	private Double amount;
	private Double amountWithoutTax;
	private String promotionCode;
	private Boolean flvd;
	private Integer ordinalNumber;
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
	 * @return the promotionSequenceNumber
	 */
	public Integer getPromotionSequenceNumber() {
		return promotionSequenceNumber;
	}

	/**
	 * @param promotionSequenceNumber
	 *            the promotionSequenceNumber to set
	 */
	public void setPromotionSequenceNumber(Integer promotionSequenceNumber) {
		this.promotionSequenceNumber = promotionSequenceNumber;
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

	public Double getAmountWithoutTax() {
		if(amountWithoutTax == null)
			amountWithoutTax = Double.valueOf(0);
		return amountWithoutTax;
	}

	public void setAmountWithoutTax(Double amountWithoutTax) {
		this.amountWithoutTax = amountWithoutTax;
	}

	/**
	 * @return the promotionCode
	 */
	public String getPromotionCode() {
		return promotionCode;
	}

	/**
	 * @param promotionCode
	 *            the promotionCode to set
	 */
	public void setPromotionCode(String promotionCode) {
		this.promotionCode = promotionCode;
	}

	/**
	 * @return the flvd
	 */
	public Boolean getFlvd() {
		return flvd;
	}

	/**
	 * @param flvd the flvd to set
	 */
	public void setFlvd(Boolean flvd) {
		this.flvd = flvd;
	}

	/**
	 * @return the ordinalNumber
	 */
	public Integer getOrdinalNumber() {
		return ordinalNumber;
	}

	/**
	 * @param ordinalNumber the ordinalNumber to set
	 */
	public void setOrdinalNumber(Integer ordinalNumber) {
		this.ordinalNumber = ordinalNumber;
	}


}
