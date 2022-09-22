/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class TenderControlTransactionLineItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	private String tenderTypeCode;
	private Tender tender;
	private Integer currencyID;
	private Currency currency;
	private Integer denominationID;
	private Double exchangeRate = new Double(0);
	private Double amount = new Double(0);
	private Double foreignCurrencyAmount = new Double(0);
	private Double count = new Double(0);

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
	 * @return the currencyID
	 */
	public Integer getCurrencyID() {
		return currencyID;
	}

	/**
	 * @param currencyID
	 *            the currencyID to set
	 */
	public void setCurrencyID(Integer currencyID) {
		this.currencyID = currencyID;
	}

	/**
	 * @return the denominationID
	 */
	public Integer getDenominationID() {
		return denominationID;
	}

	/**
	 * @param denominationID
	 *            the denominationID to set
	 */
	public void setDenominationID(Integer denominationID) {
		this.denominationID = denominationID;
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
	 * @return the foreignCurrencyAmount
	 */
	public Double getForeignCurrencyAmount() {
		return foreignCurrencyAmount;
	}

	/**
	 * @param foreignCurrencyAmount
	 *            the foreignCurrencyAmount to set
	 */
	public void setForeignCurrencyAmount(Double foreignCurrencyAmount) {
		this.foreignCurrencyAmount = foreignCurrencyAmount;
	}

	/**
	 * @return the count
	 */
	public Double getCount() {
		if(count==null)
			count = new Double(0);
		return count;
	}

	/**
	 * @param count
	 *            the count to set
	 */
	public void setCount(Double count) {
		this.count = count;
	}

	/**
	 * @return the tender
	 */
	public Tender getTender() {
		return tender;
	}

	/**
	 * @param tender
	 *            the tender to set
	 */
	public void setTender(Tender tender) {
		this.tender = tender;
	}

	/**
	 * @return the currency
	 */
	public Currency getCurrency() {
		return currency;
	}

	/**
	 * @param currency
	 *            the currency to set
	 */
	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	/**
	 * @return the exchangeRate
	 */
	public Double getExchangeRate() {
		return exchangeRate;
	}

	/**
	 * @param exchangeRate
	 *            the exchangeRate to set
	 */
	public void setExchangeRate(Double exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

}
