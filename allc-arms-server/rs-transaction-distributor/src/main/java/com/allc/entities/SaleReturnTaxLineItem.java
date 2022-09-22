/**
 * 
 */
package com.allc.entities;

/**
 * @author GUSTAVOK
 * 
 */
public class SaleReturnTaxLineItem {
	private Integer transactionID;
	private Integer retailTransactionLineItemSequenceNumber;
	private Integer saleReturnTaxSequenceNumber;
	private Double taxablePercent;
	private Double taxableAmount;
	private Double taxAmount;
	private Double taxPercent;
	private String taxType;
	private String typeCode;
	private String taxAuthority;
	private Boolean taxIncludedInPricesFlag;

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
	 * @return the saleReturnTaxSequenceNumber
	 */
	public Integer getSaleReturnTaxSequenceNumber() {
		return saleReturnTaxSequenceNumber;
	}

	/**
	 * @param saleReturnTaxSequenceNumber
	 *            the saleReturnTaxSequenceNumber to set
	 */
	public void setSaleReturnTaxSequenceNumber(
			Integer saleReturnTaxSequenceNumber) {
		this.saleReturnTaxSequenceNumber = saleReturnTaxSequenceNumber;
	}

	/**
	 * @return the taxablePercent
	 */
	public Double getTaxablePercent() {
		return taxablePercent;
	}

	/**
	 * @param taxablePercent
	 *            the taxablePercent to set
	 */
	public void setTaxablePercent(Double taxablePercent) {
		this.taxablePercent = taxablePercent;
	}

	/**
	 * @return the taxableAmount
	 */
	public Double getTaxableAmount() {
		return taxableAmount;
	}

	/**
	 * @param taxableAmount
	 *            the taxableAmount to set
	 */
	public void setTaxableAmount(Double taxableAmount) {
		this.taxableAmount = taxableAmount;
	}

	/**
	 * @return the taxAmount
	 */
	public Double getTaxAmount() {
		return taxAmount;
	}

	/**
	 * @param taxAmount
	 *            the taxAmount to set
	 */
	public void setTaxAmount(Double taxAmount) {
		this.taxAmount = taxAmount;
	}

	/**
	 * @return the taxPercent
	 */
	public Double getTaxPercent() {
		return taxPercent;
	}

	/**
	 * @param taxPercent
	 *            the taxPercent to set
	 */
	public void setTaxPercent(Double taxPercent) {
		this.taxPercent = taxPercent;
	}

	/**
	 * @return the taxType
	 */
	public String getTaxType() {
		return taxType;
	}

	/**
	 * @param taxType
	 *            the taxType to set
	 */
	public void setTaxType(String taxType) {
		this.taxType = taxType;
	}

	/**
	 * @return the typeCode
	 */
	public String getTypeCode() {
		return typeCode;
	}

	/**
	 * @param typeCode
	 *            the typeCode to set
	 */
	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	/**
	 * @return the taxAuthority
	 */
	public String getTaxAuthority() {
		return taxAuthority;
	}

	/**
	 * @param taxAuthority
	 *            the taxAuthority to set
	 */
	public void setTaxAuthority(String taxAuthority) {
		this.taxAuthority = taxAuthority;
	}

	/**
	 * @return the taxIncludedInPricesFlag
	 */
	public Boolean getTaxIncludedInPricesFlag() {
		return taxIncludedInPricesFlag;
	}

	/**
	 * @param taxIncludedInPricesFlag
	 *            the taxIncludedInPricesFlag to set
	 */
	public void setTaxIncludedInPricesFlag(Boolean taxIncludedInPricesFlag) {
		this.taxIncludedInPricesFlag = taxIncludedInPricesFlag;
	}
}
