package com.allc.entities;

import java.io.Serializable;

public class FerricardData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private String transactionNumber;
	private String almacenNumber;
	
	public Integer getTransactionID() {
		return transactionID;
	}
	public void setTransactionID(Integer transactionID) {
		this.transactionID = transactionID;
	}
	public String getTransactionNumber() {
		return transactionNumber;
	}
	public void setTransactionNumber(String transactionNumber) {
		this.transactionNumber = transactionNumber;
	}
	public String getAlmacenNumber() {
		return almacenNumber;
	}
	public void setAlmacenNumber(String almacenNumber) {
		this.almacenNumber = almacenNumber;
	}
	
	

}
