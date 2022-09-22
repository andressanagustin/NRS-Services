/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class TaxLineItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	private Double taxablePercent = new Double(0);
	private Double taxableAmount = new Double(0);
	private Double taxAmount = new Double(0);
	private Double taxPercent = new Double(0);
	private String taxType;
	private String taxSubType;

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
	 * @return the taxablePercent
	 */
	public Double getTaxablePercent() {
		if (taxablePercent == null)
			taxablePercent = new Double(0);
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
		if (taxableAmount == null)
			taxableAmount = new Double(0);
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
		if (taxAmount == null)
			taxAmount = new Double(0);
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
		if (taxPercent == null)
			taxPercent = new Double(0);
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
	 * @return the taxSubType
	 */
	public String getTaxSubType() {
		return taxSubType;
	}

	/**
	 * @param taxSubType
	 *            the taxSubType to set
	 */
	public void setTaxSubType(String taxSubType) {
		this.taxSubType = taxSubType;
	}
}
