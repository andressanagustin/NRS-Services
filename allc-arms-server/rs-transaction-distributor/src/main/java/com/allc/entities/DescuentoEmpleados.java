package com.allc.entities;

import java.io.Serializable;

public class DescuentoEmpleados implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	private String codArticulo;
	private Double montoDescuento = new Double(0);
	
	
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

	public String getCodArticulo() {
		return codArticulo;
	}
	public void setCodArticulo(String codArticulo) {
		this.codArticulo = codArticulo;
	}
	public Double getMontoDescuento() {
		return montoDescuento;
	}
	public void setMontoDescuento(Double montoDescuento) {
		this.montoDescuento = montoDescuento;
	}



}
