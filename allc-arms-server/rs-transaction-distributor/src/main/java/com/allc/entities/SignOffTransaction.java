/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class SignOffTransaction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer quantityOfTransactions;

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
	 * @return the quantityOfTransactions
	 */
	public Integer getQuantityOfTransactions() {
		return quantityOfTransactions;
	}

	/**
	 * @param quantityOfTransactions
	 *            the quantityOfTransactions to set
	 */
	public void setQuantityOfTransactions(Integer quantityOfTransactions) {
		this.quantityOfTransactions = quantityOfTransactions;
	}

}
