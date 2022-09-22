/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class RetailTransactionTotal implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private String transactionTotalTypeCode;
	private TransactionTotalType transactionTotalType;
	private Double amount;
	private Boolean cancelFlag = Boolean.FALSE;

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
	 * @return the transactionTotalTypeCode
	 */
	public String getTransactionTotalTypeCode() {
		return transactionTotalTypeCode;
	}

	/**
	 * @param transactionTotalTypeCode
	 *            the transactionTotalTypeCode to set
	 */
	public void setTransactionTotalTypeCode(String transactionTotalTypeCode) {
		this.transactionTotalTypeCode = transactionTotalTypeCode;
	}

	/**
	 * @return the transactionTotalType
	 */
	public TransactionTotalType getTransactionTotalType() {
		return transactionTotalType;
	}

	/**
	 * @param transactionTotalType
	 *            the transactionTotalType to set
	 */
	public void setTransactionTotalType(
			TransactionTotalType transactionTotalType) {
		this.transactionTotalType = transactionTotalType;
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

}
