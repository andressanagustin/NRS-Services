/**
 * 
 */
package com.allc.entities;

/**
 * @author GUSTAVOK
 * 
 */
public class TransactionType {
	private Integer transactionTypeCode;
	private String name;

	/**
	 * @return the transactionTypeCode
	 */
	public Integer getTransactionTypeCode() {
		return transactionTypeCode;
	}

	/**
	 * @param transactionTypeCode
	 *            the transactionTypeCode to set
	 */
	public void setTransactionTypeCode(Integer transactionTypeCode) {
		this.transactionTypeCode = transactionTypeCode;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
