package com.allc.entities;

import java.io.Serializable;

public class ReverseLineItemInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private String numNc;
	private Integer lineItem;
	private String monto;
	private String numAuto;
	
	
	public Integer getTransactionID() {
		return transactionID;
	}
	public void setTransactionID(Integer transactionID) {
		this.transactionID = transactionID;
	}
	public String getNumNc() {
		return numNc;
	}
	public void setNumNc(String numNc) {
		this.numNc = numNc;
	}
	public Integer getLineItem() {
		return lineItem;
	}
	public void setLineItem(Integer lineItem) {
		this.lineItem = lineItem;
	}
	public String getMonto() {
		return monto;
	}
	public void setMonto(String monto) {
		this.monto = monto;
	}
	public String getNumAuto() {
		return numAuto;
	}
	public void setNumAuto(String numAuto) {
		this.numAuto = numAuto;
	}
	
	
	
	
	

}
