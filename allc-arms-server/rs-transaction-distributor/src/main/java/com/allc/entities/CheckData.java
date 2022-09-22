package com.allc.entities;

import java.io.Serializable;

public class CheckData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer transactionID;
	private Integer sequenceNumber;
	private String bank;
	private String checkAccountNumber;
	private String checkNumber;
	private String checkDate;
	private String checkAmount;
	
	
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
	public String getBank() {
		return bank;
	}
	public void setBank(String bank) {
		this.bank = bank;
	}
	public String getCheckAccountNumber() {
		return checkAccountNumber;
	}
	public void setCheckAccountNumber(String checkAccountNumber) {
		this.checkAccountNumber = checkAccountNumber;
	}
	public String getCheckNumber() {
		return checkNumber;
	}
	public void setCheckNumber(String checkNumber) {
		this.checkNumber = checkNumber;
	}
	public String getCheckDate() {
		return checkDate;
	}
	public void setCheckDate(String checkDate) {
		this.checkDate = checkDate;
	}
	public String getCheckAmount() {
		return checkAmount;
	}
	public void setCheckAmount(String checkAmount) {
		this.checkAmount = checkAmount;
	}
	
	
	
	
	

}
