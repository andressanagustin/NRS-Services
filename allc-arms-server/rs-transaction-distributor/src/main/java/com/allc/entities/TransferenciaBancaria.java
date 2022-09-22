package com.allc.entities;

import java.io.Serializable;

public class TransferenciaBancaria  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer transactionID;
	private Integer sequenceNumber;
	private String idBanco;
	private String numCuenta;
	private String monto;
	private String flagTender;
	
	
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
	
	public String getIdBanco() {
		return idBanco;
	}
	public void setIdBanco(String idBanco) {
		this.idBanco = idBanco;
	}
	
	public String getNumCuenta() {
		return numCuenta;
	}
	public void setNumCuenta(String numCuenta) {
		this.numCuenta = numCuenta;
	}
	public String getMonto() {
		return monto;
	}
	public void setMonto(String monto) {
		this.monto = monto;
	}
	public String getFlagTender() {
		return flagTender;
	}
	public void setFlagTender(String flagTender) {
		this.flagTender = flagTender;
	}
	
	
	
	

}
