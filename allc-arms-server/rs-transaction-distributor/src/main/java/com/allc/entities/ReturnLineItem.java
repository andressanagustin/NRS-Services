/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class ReturnLineItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	private Integer originalSequenceNumber;
	/**
	 * @return the transactionID
	 */
	public Integer getTransactionID() {
		return transactionID;
	}
	/**
	 * @param transactionID the transactionID to set
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
	 * @param sequenceNumber the sequenceNumber to set
	 */
	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	/**
	 * @return the originalSequenceNumber
	 */
	public Integer getOriginalSequenceNumber() {
		return originalSequenceNumber;
	}
	/**
	 * @param originalSequenceNumber the originalSequenceNumber to set
	 */
	public void setOriginalSequenceNumber(Integer originalSequenceNumber) {
		this.originalSequenceNumber = originalSequenceNumber;
	}
	
	

}
