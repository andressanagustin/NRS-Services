/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * Entidad asociada a una Giftcard Vendida.
 * 
 * @author gustavo
 *
 */
public class GiftcardData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	private String time;
	private String cardNumber;
	private Integer amount;
	private String referenceNbr;
	private String authorizationNbr;
	private String responseCode;
	private Integer status;

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
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * @return the cardNumber
	 */
	public String getCardNumber() {
		return cardNumber;
	}

	/**
	 * @param cardNumber the cardNumber to set
	 */
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	/**
	 * @return the amount
	 */
	public Integer getAmount() {
		return amount;
	}

	/**
	 * @param amount the amount to set
	 */
	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	/**
	 * @return the referenceNbr
	 */
	public String getReferenceNbr() {
		return referenceNbr;
	}

	/**
	 * @param referenceNbr the referenceNbr to set
	 */
	public void setReferenceNbr(String referenceNbr) {
		this.referenceNbr = referenceNbr;
	}

	/**
	 * @return the authorizationNbr
	 */
	public String getAuthorizationNbr() {
		return authorizationNbr;
	}

	/**
	 * @param authorizationNbr the authorizationNbr to set
	 */
	public void setAuthorizationNbr(String authorizationNbr) {
		this.authorizationNbr = authorizationNbr;
	}

	/**
	 * @return the responseCode
	 */
	public String getResponseCode() {
		return responseCode;
	}

	/**
	 * @param responseCode the responseCode to set
	 */
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * @return the status
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}


}
