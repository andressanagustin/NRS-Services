package com.allc.entities;

import java.io.Serializable;

public class VentaMayoreoItem implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	private Integer sequenceNumberMay;
	
	public Integer getTransactionID() {
		return transactionID;
	}
	public void setTransactionID(Integer transactionID) {
		this.transactionID = transactionID;
	}
	public Integer getSequenceNumber() {
		return sequenceNumber;
	}
	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	/**
	 * @return the sequenceNumberMay
	 */
	public Integer getSequenceNumberMay() {
		return sequenceNumberMay;
	}
	/**
	 * @param sequenceNumberMay the sequenceNumberMay to set
	 */
	public void setSequenceNumberMay(Integer sequenceNumberMay) {
		this.sequenceNumberMay = sequenceNumberMay;
	}
	

}
