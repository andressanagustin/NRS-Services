package com.allc.entities;

import java.io.Serializable;

public class BonoSolidario implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer transactionID;
	private Integer sequenceNumber;
	private String numTrx;
	private String idCliente;
	private String cupo;
	private String montoDescuento;
	
	
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
	public String getNumTrx() {
		return numTrx;
	}
	public void setNumTrx(String numTrx) {
		this.numTrx = numTrx;
	}
	public String getIdCliente() {
		return idCliente;
	}
	public void setIdCliente(String idCliente) {
		this.idCliente = idCliente;
	}
	public String getCupo() {
		return cupo;
	}
	public void setCupo(String cupo) {
		this.cupo = cupo;
	}
	public String getMontoDescuento() {
		return montoDescuento;
	}
	public void setMontoDescuento(String montoDescuento) {
		this.montoDescuento = montoDescuento;
	}
	
	
	
	
	

}
