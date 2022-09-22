/**
 * 
 */
package com.allc.entities;

/**
 * @author GUSTAVOK
 * 
 */
public class RetailPriceModifier {
	private Integer transactionID;
	private Integer retailTransactionLineItemSequenceNumber;
	private Integer retailPriceModifierSequenceNumber;
	private String calculationMethodCode;
	private String priceModificationTypeCode;
	private Integer promotionID;
	private String reasonCode;
	private Double previousPrice;
	private Double percent;
	private String adjustmentMethodCode;
	private Double amount;
	private Double newPrice;

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
	 * @return the retailTransactionLineItemSequenceNumber
	 */
	public Integer getRetailTransactionLineItemSequenceNumber() {
		return retailTransactionLineItemSequenceNumber;
	}

	/**
	 * @param retailTransactionLineItemSequenceNumber
	 *            the retailTransactionLineItemSequenceNumber to set
	 */
	public void setRetailTransactionLineItemSequenceNumber(
			Integer retailTransactionLineItemSequenceNumber) {
		this.retailTransactionLineItemSequenceNumber = retailTransactionLineItemSequenceNumber;
	}

	/**
	 * @return the retailPriceModifierSequenceNumber
	 */
	public Integer getRetailPriceModifierSequenceNumber() {
		return retailPriceModifierSequenceNumber;
	}

	/**
	 * @param retailPriceModifierSequenceNumber
	 *            the retailPriceModifierSequenceNumber to set
	 */
	public void setRetailPriceModifierSequenceNumber(
			Integer retailPriceModifierSequenceNumber) {
		this.retailPriceModifierSequenceNumber = retailPriceModifierSequenceNumber;
	}

	/**
	 * @return the calculationMethodCode
	 */
	public String getCalculationMethodCode() {
		return calculationMethodCode;
	}

	/**
	 * @param calculationMethodCode
	 *            the calculationMethodCode to set
	 */
	public void setCalculationMethodCode(String calculationMethodCode) {
		this.calculationMethodCode = calculationMethodCode;
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
	 * @return the reasonCode
	 */
	public String getReasonCode() {
		return reasonCode;
	}

	/**
	 * @param reasonCode
	 *            the reasonCode to set
	 */
	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}

	/**
	 * @return the previousPrice
	 */
	public Double getPreviousPrice() {
		return previousPrice;
	}

	/**
	 * @param previousPrice
	 *            the previousPrice to set
	 */
	public void setPreviousPrice(Double previousPrice) {
		this.previousPrice = previousPrice;
	}

	/**
	 * @return the percent
	 */
	public Double getPercent() {
		return percent;
	}

	/**
	 * @param percent
	 *            the percent to set
	 */
	public void setPercent(Double percent) {
		this.percent = percent;
	}

	/**
	 * @return the adjustmentMethodCode
	 */
	public String getAdjustmentMethodCode() {
		return adjustmentMethodCode;
	}

	/**
	 * @param adjustmentMethodCode
	 *            the adjustmentMethodCode to set
	 */
	public void setAdjustmentMethodCode(String adjustmentMethodCode) {
		this.adjustmentMethodCode = adjustmentMethodCode;
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
	 * @return the newPrice
	 */
	public Double getNewPrice() {
		return newPrice;
	}

	/**
	 * @param newPrice
	 *            the newPrice to set
	 */
	public void setNewPrice(Double newPrice) {
		this.newPrice = newPrice;
	}
}
