/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class TenderLineItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	private String typeCode;
	private String tenderTypeCode;
	private Tender tender;
	private String tenderAccountNumber;
	private Double foreignCurrencyAmount = new Double(0);
	private Integer foreignCurrencyID;
	private Currency foreignCurrency;
	private Double amount = new Double(0);
	private Double feeAmount = new Double(0);
	private Integer status;
	private Double exchangeRate = new Double(0);
	private Double amountAppliedToTransaction = new Double(0);
	private Boolean isChangeFlag;
	//flag temporal solo para control, no se guarda en BD
	private Boolean isVoided;
	private Integer sequenceNumberVoided;
	private TenderReturnLineItem tenderReturnLineItem;


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
	 * @return the tenderAccountNumber
	 */
	public String getTenderAccountNumber() {
		return tenderAccountNumber;
	}

	/**
	 * @param tenderAccountNumber
	 *            the tenderAccountNumber to set
	 */
	public void setTenderAccountNumber(String tenderAccountNumber) {
		this.tenderAccountNumber = tenderAccountNumber;
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
	 * @return the foreignCurrencyID
	 */
	public Integer getForeignCurrencyID() {
		return foreignCurrencyID;
	}

	/**
	 * @param foreignCurrencyID
	 *            the foreignCurrencyID to set
	 */
	public void setForeignCurrencyID(Integer foreignCurrencyID) {
		this.foreignCurrencyID = foreignCurrencyID;
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

	/**
	 * @return the amountAppliedToTransaction
	 */
	public Double getAmountAppliedToTransaction() {
		return amountAppliedToTransaction;
	}

	/**
	 * @param amountAppliedToTransaction
	 *            the amountAppliedToTransaction to set
	 */
	public void setAmountAppliedToTransaction(Double amountAppliedToTransaction) {
		this.amountAppliedToTransaction = amountAppliedToTransaction;
	}

	/**
	 * @return the isChangeFlag
	 */
	public Boolean getIsChangeFlag() {
		if(isChangeFlag == null)
			isChangeFlag = Boolean.FALSE;
		return isChangeFlag;
	}

	/**
	 * @param isChangeFlag
	 *            the isChangeFlag to set
	 */
	public void setIsChangeFlag(Boolean isChangeFlag) {
		this.isChangeFlag = isChangeFlag;
	}

	/**
	 * @return the foreignCurrency
	 */
	public Currency getForeignCurrency() {
		return foreignCurrency;
	}

	/**
	 * @param foreignCurrency
	 *            the foreignCurrency to set
	 */
	public void setForeignCurrency(Currency foreignCurrency) {
		this.foreignCurrency = foreignCurrency;
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
	 * @return the feeAmount
	 */
	public Double getFeeAmount() {
		return feeAmount;
	}

	/**
	 * @param feeAmount
	 *            the feeAmount to set
	 */
	public void setFeeAmount(Double feeAmount) {
		this.feeAmount = feeAmount;
	}

	/**
	 * @return the status
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * @return the isVoided
	 */
	public Boolean getIsVoided() {
		if(isVoided == null)
			isVoided = Boolean.FALSE;
		return isVoided;
	}

	/**
	 * @param isVoided the isVoided to set
	 */
	public void setIsVoided(Boolean isVoided) {
		this.isVoided = isVoided;
	}

	/**
	 * @return the sequenceNumberVoided
	 */
	public Integer getSequenceNumberVoided() {
		if(sequenceNumberVoided == null)
			sequenceNumberVoided = 0;
		return sequenceNumberVoided;
	}

	/**
	 * @param sequenceNumberVoided the sequenceNumberVoided to set
	 */
	public void setSequenceNumberVoided(Integer sequenceNumberVoided) {
		this.sequenceNumberVoided = sequenceNumberVoided;
	}

	/**
	 * @return the tenderReturnLineItem
	 */
	public TenderReturnLineItem getTenderReturnLineItem() {
		if (tenderReturnLineItem != null)
			tenderReturnLineItem.setTransactionID(transactionID);
		return tenderReturnLineItem;
	}

	/**
	 * @param tenderReturnLineItem the tenderReturnLineItem to set
	 */
	public void setTenderReturnLineItem(TenderReturnLineItem tenderReturnLineItem) {
		this.tenderReturnLineItem = tenderReturnLineItem;
	}



}
