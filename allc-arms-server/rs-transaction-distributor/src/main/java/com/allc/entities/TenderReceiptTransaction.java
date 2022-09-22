/**
 * 
 */
package com.allc.entities;


/**
 * @author GUSTAVOK
 * 
 */
public class TenderReceiptTransaction {
	private Integer transactionID;
	private Integer externalDepositoryID;
	private ExternalDepository externalDepository;
	private Integer safeID;
	private Safe safe;
	private String description;
	private Double amount = new Double(0);
	private Double foreignCurrencyAmount = new Double(0);

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
	 * @return the externalDepositoryID
	 */
	public Integer getExternalDepositoryID() {
		return externalDepositoryID;
	}

	/**
	 * @param externalDepositoryID
	 *            the externalDepositoryID to set
	 */
	public void setExternalDepositoryID(Integer externalDepositoryID) {
		this.externalDepositoryID = externalDepositoryID;
	}

	/**
	 * @return the externalDepository
	 */
	public ExternalDepository getExternalDepository() {
		return externalDepository;
	}

	/**
	 * @param externalDepository
	 *            the externalDepository to set
	 */
	public void setExternalDepository(ExternalDepository externalDepository) {
		this.externalDepository = externalDepository;
	}

	/**
	 * @return the safe
	 */
	public Safe getSafe() {
		return safe;
	}

	/**
	 * @param safe
	 *            the safe to set
	 */
	public void setSafe(Safe safe) {
		this.safe = safe;
	}

	/**
	 * @return the safeID
	 */
	public Integer getSafeID() {
		return safeID;
	}

	/**
	 * @param safeID
	 *            the safeID to set
	 */
	public void setSafeID(Integer safeID) {
		this.safeID = safeID;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
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

}
